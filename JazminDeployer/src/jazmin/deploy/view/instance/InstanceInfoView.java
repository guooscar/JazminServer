/**
 * 
 */
package jazmin.deploy.view.instance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import jazmin.core.Jazmin;
import jazmin.deploy.DeploySystemUI;
import jazmin.deploy.domain.Application;
import jazmin.deploy.domain.DeployManager;
import jazmin.deploy.domain.Instance;
import jazmin.deploy.ui.BeanTable;
import jazmin.deploy.view.main.ActionReportWindow;
import jazmin.deploy.view.main.CodeEditorCallback;
import jazmin.deploy.view.main.CodeEditorWindow;
import jazmin.deploy.view.main.DeployBaseView;

import org.vaadin.aceeditor.AceMode;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;

/**
 * @author yama
 * 6 Jan, 2015
 */
@SuppressWarnings("serial")
public class InstanceInfoView extends DeployBaseView{
	BeanTable<Instance>table;
	CheckBox optOnSelectCheckBox;
	List<Instance>instanceList;
	//
	public InstanceInfoView() {
		super();
		initUI();
		searchTxt.setValue("1=1 order by priority desc");
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
		searchTxt.addStyleName(ValoTheme.TEXTFIELD_TINY);
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
        optLayout.setComponentAlignment(optOnSelectCheckBox, Alignment.BOTTOM_RIGHT);
		//
        Button ok = new Button("Query");
        ok.addStyleName(ValoTheme.BUTTON_PRIMARY);
        ok.addStyleName(ValoTheme.BUTTON_SMALL);
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
		instanceList=new ArrayList<Instance>();
		table= new BeanTable<Instance>(null, Instance.class,
				"machine","user","password","application","properties");
		table.setCellStyleGenerator(new Table.CellStyleGenerator() {
			@Override
			public String getStyle(Table source, Object itemId, Object propertyId) {
				if(propertyId==null){
					return null;
				}
				if(propertyId.equals("isAlive")){
					Boolean isTrue=Boolean.valueOf(
							source.getItem(itemId).getItemProperty(propertyId).getValue()+"");
					return isTrue?"green":"red";
				}else{
					return null;
				}
			}
		});
		table.setMultiSelect(true);
		return table;
	}
	//
	private void initUI(){
		addOptButton("Console",ValoTheme.BUTTON_PRIMARY, (e)->viewConsole());
		addOptButton("TailLog",null, (e)->viewTailLog());
		addOptButton("Detail",null, (e)->viewDetail());
		addOptButton("BootFile",null, (e)->viewBootFile());
		addOptButton("Report",null, (e)->viewActionReport());
		addOptButton("Test",null, (e)->testInstance());
		//
		addOptButton("SetVer",ValoTheme.BUTTON_PRIMARY, (e)->setPackageVersion());
		//
		addOptButton("Create",ValoTheme.BUTTON_DANGER, (e)->createInstance());
		addOptButton("Start",ValoTheme.BUTTON_DANGER, (e)->startInstance());
		addOptButton("Stop",ValoTheme.BUTTON_DANGER, (e)->stopInstance());
		addOptButton("Restart",ValoTheme.BUTTON_DANGER, (e)->restartInstance());
	}
	private void viewConsole(){
		Instance instance=table.getSelectValue();
		if(instance==null){
			DeploySystemUI.showNotificationInfo("Info",
					"Please choose which instance to view.");
		}else{
			if(instance.application==null){
				DeploySystemUI.showNotificationInfo("Info",
						"Can not find application on instance:"+instance.id);
				return;
			}
			//
			if(instance.application.type.equals(Application.TYPE_HAPROXY)){
				InstanceHaproxyStatWindow window=new InstanceHaproxyStatWindow(instance);
				UI.getCurrent().addWindow(window);
				window.focus();
				return;
			}
			//
			if(instance.application.type.equals(Application.TYPE_MYSQL)){
				InstanceMySQLWindow window=new InstanceMySQLWindow(instance);
				UI.getCurrent().addWindow(window);
				window.focus();
				return;
			}
			//
			if(instance.application.type.startsWith("jazmin")){
				InstanceWebSshWindow bfw=new InstanceWebSshWindow(instance);
				UI.getCurrent().addWindow(bfw);
				bfw.focus();
				return;
			}
			//
			DeploySystemUI.showNotificationInfo("Info",
					"Not support application type:"+instance.application.type);
			
		}
	}
	//
	private void viewDetail(){
		Instance instance=table.getSelectValue();
		if(instance==null){
			DeploySystemUI.showNotificationInfo("Info",
					"Please choose which instance to view.");
		}else{
			InstanceDetailWindow bfw=new InstanceDetailWindow(instance);
			UI.getCurrent().addWindow(bfw);
			bfw.focus();
		}
	}
	//
	private void viewBootFile(){
		Instance instance=table.getSelectValue();
		if(instance==null){
			DeploySystemUI.showNotificationInfo("Info",
					"Please choose which instance to view.");
		}else{
			String result=DeployManager.renderTemplate(instance.id);
			if(result==null){
				DeploySystemUI.showNotificationInfo("Info",
						"Can not found instance boot file");
				return;
			}
			CodeEditorWindow cew=new CodeEditorWindow(new CodeEditorCallback() {
				@Override
				public String reload() {
					String result=DeployManager.renderTemplate(instance.id);
					return result;
				}
				@Override
				public void onSave(String value) {
					
				}
			});
			cew.setValue("BootFile-"+instance.id, result,AceMode.javascript);
			cew.setReadonly(true);
			UI.getCurrent().addWindow(cew);
			cew.focus();
		}
	}
	//
	private void viewTailLog(){
		Instance instance=table.getSelectValue();
		if(instance==null){
			DeploySystemUI.showNotificationInfo("Info",
					"Please choose which instance to view.");
		}else{
			InstanceTailLogWindow bfw=new InstanceTailLogWindow(instance);
			UI.getCurrent().addWindow(bfw);
			bfw.focus();
		}
	}
	//
	//
	private void viewActionReport(){
		ActionReportWindow bfw=new ActionReportWindow();
		UI.getCurrent().addWindow(bfw);
		bfw.focus();
	}
	//
	@Override
	public void loadData(){
		String search=getSearchValue();
    	if(search==null){
    		return;
    	}
    	try {
			instanceList=DeployManager.getInstances(search);
			if(instanceList.isEmpty()){
				DeploySystemUI.showNotificationInfo("Result","No mactch result found.");		
			}
		 	table.setData(instanceList);
    	} catch (Throwable e1) {
    		DeploySystemUI.showNotificationInfo("Error",e1.getMessage());
		}
	}
	//
	public List<Instance>getOptInstances(){
		if(optOnSelectCheckBox.getValue()){
			return table.getSelectValues();
		}else{
			return instanceList;
		}
	}
	//
	private void testInstance(){
		OptProgressWindow optWindow=new OptProgressWindow(window->{
			Jazmin.execute(()->{
				testInstance0(window);
			});
		});
		optWindow.setCaption("Confirm");
		optWindow.setInfo("Confirm test total "+getOptInstances().size()+" instance(s) state?");
		UI.getCurrent().addWindow(optWindow);
	}
	//
	//
	private void testInstance0(OptProgressWindow window){
		AtomicInteger counter=new AtomicInteger();
		getOptInstances().forEach(instance->{
			window.getUI().access(()->{
				window.setInfo("test "+instance.id+" "+
						counter.incrementAndGet()+
						"/"+getOptInstances().size()+"...");	
			});
			DeployManager.testInstance(instance);
			window.getUI().access(()->{
				window.setInfo("test "+instance.id+" result:"+instance.isAlive);	
			});
		});
		window.getUI().access(()->{
			window.close();
			loadData();
		});
	}
	//
	//
	private void createInstance(){
		//createInstacne1(value);	
		final CodeEditorWindow cew=new CodeEditorWindow(new CodeEditorCallback() {
			@Override
			public String reload() {
				return "";
			}
			@Override
			public void onSave(String value) {
				createInstacne1(value);
			}
		});
		cew.setValue("Instance Boot File", "", AceMode.javascript);
		UI.getCurrent().addWindow(cew);
	}
	//
	private void createInstacne1(String jsFile){
		OptProgressWindow optWindow=new OptProgressWindow(window->{
			Jazmin.execute(()->{
				DeployManager.resetActionReport();
				createInstance0(window,jsFile);
			});
		});
		optWindow.setCaption("Confirm");
		optWindow.setInfo("Confirm create total "+getOptInstances().size()+" instance(s)?");
		UI.getCurrent().addWindow(optWindow);
	}
	//
	private void createInstance0(OptProgressWindow window,String jsFile){
		AtomicInteger counter=new AtomicInteger();
		getOptInstances().forEach(instance->{
			window.getUI().access(()->{
				window.setInfo("create "+instance.id+" "+
						counter.incrementAndGet()+
						"/"+getOptInstances().size()+"...");	
			});
			DeployManager.createInstance(instance,jsFile);
		});
		window.getUI().access(()->{
			window.close();
			DeploySystemUI.showNotificationInfo("Info", "create complete");
		});	
	}
	//
	private void startInstance(){
		OptProgressWindow optWindow=new OptProgressWindow(window->{
			Jazmin.execute(()->{
				DeployManager.resetActionReport();
				startInstance0(window);
			});
		});
		optWindow.setCaption("Confirm");
		optWindow.setInfo("Confirm start total "+getOptInstances().size()+" instance(s)?");
		UI.getCurrent().addWindow(optWindow);
	}
	//
	//
	private void startInstance0(OptProgressWindow window){
		AtomicInteger counter=new AtomicInteger();
		AtomicInteger waitCounter=new AtomicInteger();
		getOptInstances().forEach(instance->{
			window.getUI().access(()->{
				window.setInfo("start "+instance.id+" "+
						counter.incrementAndGet()+
						"/"+getOptInstances().size()+"...");	
			});
			waitCounter.set(0);
			DeployManager.startInstance(instance);
			while(waitCounter.get()<30){
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (Exception e) {}
				window.getUI().access(()->window.setInfo(
						"wait "+instance.id+" "+
								waitCounter.incrementAndGet()+" seconds"));
				DeployManager.testInstance(instance);
				if(instance.isAlive){
					break;
				}
			}
			//wait for 15 seconds still not response maybe error happened
			if(!instance.isAlive){
				window.getUI().access(()->DeploySystemUI.showNotificationInfo(
						"Error",instance.id+" not response after 30 seconds"));
			
			}
		});
		window.getUI().access(()->{
			window.close();
			DeploySystemUI.showNotificationInfo("Info", "start complete");
		});	
	}
	//
	//
	private void stopInstance(){
		OptProgressWindow optWindow=new OptProgressWindow(window->{
			Jazmin.execute(()->{
				DeployManager.resetActionReport();
				stopInstance0(window,true);
			});
		});
		optWindow.setCaption("Confirm");
		optWindow.setInfo("Confirm stop total "+getOptInstances().size()+" instance(s)?");
		UI.getCurrent().addWindow(optWindow);
	}
	//
	private void stopInstance0(OptProgressWindow window,boolean stopWindow){
		AtomicInteger counter=new AtomicInteger();
		getOptInstances().forEach(instance->{
			window.getUI().access(()->{
				window.setInfo("stop "+instance.id+" "+
						counter.incrementAndGet()+
						"/"+getOptInstances().size()+"...");	
			});
			DeployManager.stopInstance(instance);
		});
		window.getUI().access(()->{
			if(stopWindow){
				window.close();
			}
			DeploySystemUI.showNotificationInfo("Info", "stop complete");
		});
	}
	//
	private void restartInstance(){
		OptProgressWindow optWindow=new OptProgressWindow(window->{
			Jazmin.execute(()->{
				DeployManager.resetActionReport();
				stopInstance0(window,false);
				startInstance0(window);
			});
		});
		optWindow.setCaption("Confirm");
		optWindow.setInfo("Confirm restart total "+getOptInstances().size()+" instance(s)?");
		UI.getCurrent().addWindow(optWindow);
	}
	//
	private void setPackageVersion(){
		InputWindow sw=new InputWindow(window->{
			String version=window.getInputValue();
			try{
				DeployManager.setPackageVersion(getOptInstances(), version);
				DeployManager.saveInstanceConfig();
			}catch(Exception e){
				DeploySystemUI.showNotificationInfo("Error",e.getMessage());
			}
			window.close();
			DeploySystemUI.showNotificationInfo("Info","Package version set to "+version);
			loadData();
		});
		sw.setCaption("Change instance package version");
		sw.setInfo("Change "+getOptInstances().size()+" instance(s) package version");
		UI.getCurrent().addWindow(sw);
	}
}
