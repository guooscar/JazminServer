/**
 * 
 */
package jazmin.deploy.view.app;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import jazmin.core.Jazmin;
import jazmin.deploy.DeploySystemUI;
import jazmin.deploy.domain.Application;
import jazmin.deploy.manager.DeployManager;
import jazmin.deploy.ui.BeanTable;
import jazmin.deploy.view.main.CodeEditorCallback;
import jazmin.deploy.view.main.CodeEditorWindow;
import jazmin.deploy.view.main.DeployBaseView;
import jazmin.deploy.view.main.InputWindow;
import jazmin.deploy.view.main.TaskProgressWindow;

import org.vaadin.aceeditor.AceMode;

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

/**
 * @author yama
 * 6 Jan, 2015
 */
@SuppressWarnings("serial")
public class ApplicationInfoView extends DeployBaseView{
	BeanTable<Application>table;
	private List<Application>applications;
	CheckBox optOnSelectCheckBox;
	//
	public ApplicationInfoView() {
		super();
		initUI();
		searchTxt.setValue("1=1 order by priority desc;");
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
		applications=new ArrayList<Application>();
		table= new BeanTable<Application>(null, Application.class,
				"scmUser","scmPassword","properties");
		table.setMultiSelect(true);
		return table;
	}
	//
	private void initUI(){
		addOptButton("View Detail",null, (e)->viewDetail());
		addOptButton("View Default Template",null, (e)->viewDefaultTemplate());
		addOptButton("View Template",null, (e)->viewTemplate());
		addOptButton("System Graph",null, (e)->viewSystemGraph());
		addOptButton("Instance Graph",null, (e)->viewInstanceGraph());
		//
		addOptButton("Compile",ValoTheme.BUTTON_PRIMARY, (e)->compileApp());
	}
	private void viewTemplate0(String appId){
		String result=DeployManager.getTemplate(appId);
		if(result==null){
			DeploySystemUI.showNotificationInfo("Info",
					"Can not found app template file");
			return;
		}
		//
		CodeEditorWindow cew=new CodeEditorWindow(new CodeEditorCallback() {
			@Override
			public String reload() {
				String result=DeployManager.getTemplate(appId);
				if(result==null){
					DeploySystemUI.showNotificationInfo("Info",
							"Can not found app template file");
				}
				return result;
			}
			//
			@Override
			public void onSave(String value) {
				DeployManager.saveTemplate(appId,value);
			}
		});
		cew.setValue("Template-"+appId, result,AceMode.velocity);
		//cew.setReadonly(true);
		UI.getCurrent().addWindow(cew);
		cew.focus();
	}
	//
	private void viewTemplate(){
		Application app=table.getSelectValue();
		if(app==null){
			DeploySystemUI.showNotificationInfo("Info",
					"Please choose which app to view.");
		}else{
			viewTemplate0(app.id);
		}
	}
	private void viewDefaultTemplate(){
		viewTemplate0("Default");
	}
	//
	private void viewDetail(){
		Application app=table.getSelectValue();
		if(app==null){
			DeploySystemUI.showNotificationInfo("Info",
					"Please choose which app to view.");
		}else{
			ApplicationDetailWindow bfw=new ApplicationDetailWindow(app);
			UI.getCurrent().addWindow(bfw);
			bfw.focus();
		}
	}
	//
	private void viewSystemGraph(){
		Application app=table.getSelectValue();
		if(app==null){
			DeploySystemUI.showNotificationInfo("Info",
					"Please choose which application to view.");
		}else{
			DeployGraphWindow bfw=new DeployGraphWindow(DeployGraphWindow.TYPE_SYSTEM,app.system,null);
			UI.getCurrent().addWindow(bfw);
			bfw.focus();
		}
	}
	//
	private void viewInstanceGraph(){
		Application app=table.getSelectValue();
		if(app==null){
			DeploySystemUI.showNotificationInfo("Info",
					"Please choose which application to view.");
		}else{
			InputWindow iw=new InputWindow(new Consumer<InputWindow>() {
				@Override
				public void accept(InputWindow t) {
					String cluster=t.getInputValue();
					t.close();
					if(cluster!=null){
						DeployGraphWindow bfw=new DeployGraphWindow(DeployGraphWindow.TYPE_INSTANCE,app.system,cluster);
						UI.getCurrent().addWindow(bfw);
						bfw.focus();	
					}else{
						DeploySystemUI.showNotificationInfo("info",
								"Please input cluster name.");
					}
				}
			});
			iw.setWidth("400px");
			iw.setHeight("300px");
			iw.setCaption("Input cluster name");
			UI.getCurrent().addWindow(iw);
			iw.focus();
		}
	}
	//
	public void compileApp(){
		TaskProgressWindow optWindow=new TaskProgressWindow(window->{
			Jazmin.execute(()->{
				compileApp0(window);
			});
		});
		optWindow.setCaption("Confirm");
		for(Application i:getOptApps()){
			optWindow.addTask(i.id,"");
		}
		
		optWindow.setInfo("Confirm compile total "+getOptApps().size()+" app(s)?");
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
	private void compileApp0(TaskProgressWindow window){
		currentTaskWindow=window;
		AtomicInteger counter=new AtomicInteger();
		for(Application app:getOptApps()){
			if(window.isCancel()){
				break;
			}
			window.getUI().access(()->{
				window.setInfo("compile "+app.id+" "+
						counter.incrementAndGet()+
						"/"+getOptApps().size()+"...");
				window.updateTask(app.id, "compiling...");
			});
			final StringBuilder result=new StringBuilder();
			try {
				int ret=DeployManager.compileApp(app,
						ApplicationInfoView.this::appendOutput);
				if(ret==0){
					result.append("success");
				}else{
					result.append("fail");
				}
			} catch (Exception e) {
				result.append(":"+e.getMessage());
			}
			window.getUI().access(()->{
				window.updateTask(app.id, result.toString());
			});
			
		};
		window.getUI().access(()->{
			window.finish();
			DeploySystemUI.showNotificationInfo("Info", "compile complete");
			DeploySystemUI.get().showWebNotification("JazminDeployer", "compile complete");
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
    		applications=DeployManager.getApplications(DeploySystemUI.getUser().id,search);
			if(applications.isEmpty()){
				DeploySystemUI.showNotificationInfo("Result","No mactch result found.");		
			}
			table.setBeanData(applications);
    	} catch (Throwable e1) {
    		DeploySystemUI.showNotificationInfo("error",e1.getMessage());
		}
	}
	//
	public List<Application>getOptApps(){
		if(optOnSelectCheckBox.getValue()){
			return table.getSelectValues();
		}else{
			return applications;
		}
	}
	//
	
}
