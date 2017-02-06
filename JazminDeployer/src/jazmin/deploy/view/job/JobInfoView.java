/**
 * 
 */
package jazmin.deploy.view.job;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;

import jazmin.core.Jazmin;
import jazmin.deploy.DeploySystemUI;
import jazmin.deploy.domain.MachineJob;
import jazmin.deploy.manager.DeployManager;
import jazmin.deploy.ui.BeanTable;
import jazmin.deploy.view.main.DeployBaseView;
import jazmin.deploy.view.main.TaskProgressWindow;

/**
 * @author yama
 * 6 Jan, 2015
 */
@SuppressWarnings("serial")
public class JobInfoView extends DeployBaseView{
	BeanTable<MachineJob>table;
	private List<MachineJob>jobs;
	CheckBox optOnSelectCheckBox;
	//
	public JobInfoView() {
		super();
		initUI();
		searchTxt.setValue("1=1 ");
	}
	//
	protected void initBaseUI(){
		setSizeFull();
		//
		HorizontalLayout optLayout = new HorizontalLayout();
		optLayout.setSpacing(true);
		optLayout.addStyleName(ValoTheme.WINDOW_TOP_TOOLBAR);
		optLayout.setWidth(100.0f, Unit.PERCENTAGE);
		searchTxt = new TextField("Filter", "");
		searchTxt.setIcon(FontAwesome.SEARCH);
		searchTxt.setWidth(100.0f, Unit.PERCENTAGE);
		searchTxt.addStyleName(ValoTheme.TEXTFIELD_SMALL);
		searchTxt.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
		searchTxt.addShortcutListener(new ShortcutListener("Search",KeyCode.ENTER,null) {
			@Override
			public void handleAction(Object sender, Object target) {
				loadData();
			}
		});
		//
		optLayout.addComponent(searchTxt);
		optLayout.setExpandRatio(searchTxt,1.0f);
		 //
        optOnSelectCheckBox=new CheckBox("Only Selected");
        optLayout.addComponent(optOnSelectCheckBox);
        optOnSelectCheckBox.addStyleName(ValoTheme.CHECKBOX_SMALL);
        optLayout.setComponentAlignment(optOnSelectCheckBox, Alignment.BOTTOM_RIGHT);
		//
        Button ok = new Button("Query");
        ok.addStyleName(ValoTheme.BUTTON_SMALL);
        ok.addStyleName(ValoTheme.BUTTON_PRIMARY);
        optLayout.addComponent(ok);
        ok.addClickListener(e->loadData());
        optLayout.setComponentAlignment(ok, Alignment.BOTTOM_RIGHT);
       
        //
        addComponent(optLayout);
        
        BeanTable<?> table = createTable();
		addComponent(table);
		table.setSizeFull();
        setExpandRatio(table, 1);
        tray = new HorizontalLayout();
		tray.setWidth(100.0f, Unit.PERCENTAGE);
		tray.addStyleName(ValoTheme.WINDOW_BOTTOM_TOOLBAR);
		tray.setSpacing(true);
		tray.setMargin(true);
		//
		Label emptyLabel=new Label("");
		tray.addComponent(emptyLabel);
		tray.setComponentAlignment(emptyLabel, Alignment.MIDDLE_RIGHT);
		tray.setExpandRatio(emptyLabel,1.0f);
		//
		addComponent(tray);
	}
	//
	@Override
	public BeanTable<?> createTable() {
		jobs=new ArrayList<MachineJob>();
		table= new BeanTable<MachineJob>(null, MachineJob.class);
		table.setMultiSelect(true);
		return table;
	}
	//
	private void initUI(){
		addOptButton("View Detail",null, (e)->viewDetail());
		addOptButton("View Logs",null, (e)->viewLogs());
		//
		addOptButton("Run",ValoTheme.BUTTON_PRIMARY, (e)->runJob());
	}
	
	//
	private void viewDetail(){
		MachineJob job=table.getSelectValue();
		if(job==null){
			DeploySystemUI.showNotificationInfo("Info",
					"Please choose which job to view.");
		}else{
			JobDetailWindow bfw=new JobDetailWindow(job);
			UI.getCurrent().addWindow(bfw);
			bfw.focus();
		}
	}
	
	//
	private void viewLogs(){
		MachineJob job=table.getSelectValue();
		if(job==null){
			DeploySystemUI.showNotificationInfo("Info",
					"Please choose which job to view.");
		}else{
			JobLogWindow bfw=new JobLogWindow(job.id);
			UI.getCurrent().addWindow(bfw);
			bfw.focus();
		}
	}
	//
	public void runJob(){
		TaskProgressWindow optWindow=new TaskProgressWindow(window->{
			Jazmin.execute(()->{
				runJob0(window);
			});
		});
		optWindow.setCaption("Confirm");
		for(MachineJob i:getOptJobs()){
			optWindow.addTask(i.id,"");
		}
		
		optWindow.setInfo("Confirm run total "+getOptJobs().size()+" job(s)?");
		UI.getCurrent().addWindow(optWindow);
	}
	//
	private void appendOutput(String s){
		if(currentTaskWindow!=null){
			getUI().access(()->{
				currentTaskWindow.appendLog(s);
			});
		}
	}
	private TaskProgressWindow currentTaskWindow;
	//
	private void runJob0(TaskProgressWindow window){
		currentTaskWindow=window;
		AtomicInteger counter=new AtomicInteger();
		for(MachineJob app:getOptJobs()){
			if(window.isCancel()){
				break;
			}
			window.getUI().access(()->{
				window.setInfo("run "+app.id+" "+
						counter.incrementAndGet()+
						"/"+getOptJobs().size()+"...");
				window.updateTask(app.id, "running...");
			});
			final StringBuilder result=new StringBuilder();
			try {
				DeployManager.runJob(app,
						JobInfoView.this::appendOutput);
			} catch (Exception e) {
				result.append(e.getMessage());
			}
			window.getUI().access(()->{
				window.updateTask(app.id, "complete");
				window.updateTask(app.id, result.toString());
			});
			
		};
		window.getUI().access(()->{
			window.finish();
			DeploySystemUI.showNotificationInfo("Info", "run complete");
			DeploySystemUI.get().showWebNotification("JazminDeployer", "run complete");
		});	
	}
	//
	@Override
	public void loadData(){
		String search=getSearchValue();
    	if(search==null){
    		return;
    	}
    	try {
    		jobs=DeployManager.getJobs(search);
			if(jobs.isEmpty()){
				DeploySystemUI.showNotificationInfo("Result","No mactch result found.");		
			}
			table.setBeanData(jobs);
    	} catch (Throwable e1) {
    		DeploySystemUI.showNotificationInfo("error",e1.getMessage());
		}
	}
	//
	public List<MachineJob>getOptJobs(){
		if(optOnSelectCheckBox.getValue()){
			return table.getSelectValues();
		}else{
			return jobs;
		}
	}
	//
	
}
