/**
 * 
 */
package jazmin.deploy.view.machine;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import jazmin.core.Jazmin;
import jazmin.deploy.DeploySystemUI;
import jazmin.deploy.domain.DeployManager;
import jazmin.deploy.domain.Machine;
import jazmin.deploy.ui.BeanTable;
import jazmin.deploy.view.main.DeployBaseView;
import jazmin.deploy.view.main.TaskProgressWindow;
import jazmin.util.DumpUtil;
import jazmin.util.SshUtil;

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
public class MachineInfoView extends DeployBaseView{
	BeanTable<Machine>table;
	private List<Machine>machines;
	CheckBox optOnSelectCheckBox;
	
	//
	public MachineInfoView() {
		super();
		initUI();
		searchTxt.setValue("1=1");
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
        optOnSelectCheckBox.addStyleName(ValoTheme.COMBOBOX_SMALL);
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
		machines=new ArrayList<Machine>();
		table= new BeanTable<Machine>(null, Machine.class,
				"sshPassword",
				"rootSshPassword",
				"jazminHome",
				"memcachedHome",
				"haproxyHome");
		table.setMultiSelect(true);
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
		return table;
	}
	//
	private void initUI(){
		addOptButton("View Detail",null, (e)->viewDetail());
		addOptButton("View Stat",null, (e)->viewStat());
		addOptButton("View Instances",null, (e)->viewInstances());
		addOptButton("View Scripts",null, (e)->showScripts());
		addOptButton("Test Machine",null, (e)->checkMachine());
		addOptButton("Copy Files",ValoTheme.BUTTON_PRIMARY, (e)->copyFiles());
		addOptButton("Root SSH",ValoTheme.BUTTON_DANGER, (e)->rootSshLogin());
		addOptButton("SSH",ValoTheme.BUTTON_PRIMARY, (e)->sshLogin());
		addOptButton("Iptables",ValoTheme.BUTTON_DANGER, (e)->viewIptables());
		
		addOptButton("Run Command",ValoTheme.BUTTON_DANGER, (e)->runCmd());
	}
	//
	private void viewDetail(){
		Machine machine=table.getSelectValue();
		if(machine==null){
			DeploySystemUI.showNotificationInfo("Info",
					"Please choose which machine to view.");
		}else{
			MachineDetailWindow bfw=new MachineDetailWindow(machine);
			UI.getCurrent().addWindow(bfw);
			bfw.focus();
		}
	}
	//
	//
	private void showScripts() {
		MachineScriptWindow bfw = new MachineScriptWindow();
		UI.getCurrent().addWindow(bfw);
		bfw.focus();
	}
	//
	private void viewStat(){
		Machine machine=table.getSelectValue();
		if(machine==null){
			DeploySystemUI.showNotificationInfo("Info",
					"Please choose which machine to view.");
		}else{
			MachineStatWindow bfw=new MachineStatWindow(machine);
			UI.getCurrent().addWindow(bfw);
			bfw.focus();
		}
	}
	//
	private void viewInstances(){
		Machine machine=table.getSelectValue();
		if(machine==null){
			DeploySystemUI.showNotificationInfo("Info",
					"Please choose which machine to view.");
		}else{
			MachineInstanceWindow bfw=new MachineInstanceWindow(machine);
			UI.getCurrent().addWindow(bfw);
			bfw.focus();
		}
	}
	//
	private void sshLogin(){
		Machine machine=table.getSelectValue();
		if(machine==null){
			DeploySystemUI.showNotificationInfo("Info",
					"Please choose which machine to login.");
		}else{
			MachineWebSshWindow bfw=new MachineWebSshWindow(machine,false);
			UI.getCurrent().addWindow(bfw);
			bfw.focus();
		}
	}
	//
	private void rootSshLogin(){
		Machine machine=table.getSelectValue();
		if(machine==null){
			DeploySystemUI.showNotificationInfo("Info",
					"Please choose which machine to login.");
		}else{
			MachineWebSshWindow bfw=new MachineWebSshWindow(machine,true);
			UI.getCurrent().addWindow(bfw);
			bfw.focus();
		}
	}
	//
	private void copyFiles(){
		MachineCopyWindow bfw=new MachineCopyWindow(this::copyFiles);
		UI.getCurrent().addWindow(bfw);
		bfw.focus();
	}
	//
	private void copyFiles(String from,String to){
		TaskProgressWindow optWindow=new TaskProgressWindow(window->{
			Jazmin.execute(()->{
				copyFiles0(window,from,to);
			});
		});
		optWindow.setCaption("Confirm");
		for(Machine m:getOptMachines()){
			optWindow.addTask(m.id,"");
		}
		optWindow.setInfo("Confirm copy local file:"+from
				+" to "+getOptMachines().size()+" machine(s)?");
		UI.getCurrent().addWindow(optWindow);
	}
	//
	private void copyFiles0(TaskProgressWindow window,String from,String to){
		AtomicInteger counter=new AtomicInteger();
		for(Machine machine:getOptMachines()){
			if(window.isCancel()){
				break;
			}
			window.getUI().access(()->{
				window.setInfo("copy "+machine.id+" "+
						counter.incrementAndGet()+
						"/"+getOptMachines().size()+"...");	
				window.updateTask(machine.id, "copy...");
			});
			StringBuilder result=new StringBuilder("done");
			try {
				SshUtil.scp(machine.publicHost,
						machine.sshPort, 
						machine.sshUser,
						machine.sshPassword,
						from, to, machine.sshTimeout,(total,curr)->{
							window.getUI().access(()->{
								float a=total;
								float b=curr;
								window.updateTask(machine.id, String.format("%.2f",b/a*100)+"%");
								window.setInfo(from+" -> "+machine.id+" "+
								DumpUtil.byteCountToString(curr)+"/"
								+DumpUtil.byteCountToString(total));
							});
						});
			} catch (Exception e) {
				result.append(":"+e.getMessage());
			}
			window.getUI().access(()->{
				window.setInfo("copy to "+machine.id+" done");	
				window.updateTask(machine.id, result.toString());
			});
		}
		window.getUI().access(()->{
			window.close();
			loadData();
		});
	}
	//
	private void viewIptables(){
		Machine machine=table.getSelectValue();
		if(machine==null){
			DeploySystemUI.showNotificationInfo("Info",
					"Please choose which machine to view.");
		}else{
			MachineIptablesWindow bfw=new MachineIptablesWindow(machine);
			UI.getCurrent().addWindow(bfw);
			bfw.focus();
		}
	}
	//
	private void checkMachine(){
		TaskProgressWindow optWindow=new TaskProgressWindow(window->{
			Jazmin.execute(()->{
				checkMachine0(window);
			});
		});
		optWindow.setCaption("Confirm");
		for(Machine m:getOptMachines()){
			optWindow.addTask(m.id,"");
		}
		optWindow.setInfo("Confirm test total "+getOptMachines().size()+" machine(s) state?");
		UI.getCurrent().addWindow(optWindow);
	}
	//
	private void checkMachine0(TaskProgressWindow window){
		AtomicInteger counter=new AtomicInteger();
		for(Machine machine:getOptMachines()){
			if(window.isCancel()){
				break;
			}
			window.getUI().access(()->{
				window.setInfo("test "+machine.id+" "+
						counter.incrementAndGet()+
						"/"+getOptMachines().size()+"...");	
				window.updateTask(machine.id, "testing...");
			});
			DeployManager.testMachine(machine);
			window.getUI().access(()->{
				window.setInfo("test "+machine.id+" result:"+machine.isAlive);	
				window.updateTask(machine.id, "alive:"+machine.isAlive);
			});
		}
		window.getUI().access(()->{
			window.close();
			loadData();
		});
	}
	//
	private void runCmd(){
		if(machines.isEmpty()){
			DeploySystemUI.showNotificationInfo("INFO","Choose machine first.");	
			return;
		}
		MachineRunCmdWindow bfw=new MachineRunCmdWindow(machines);
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
    		machines=DeployManager.getMachines(DeploySystemUI.getUser().id,search);
			if(machines.isEmpty()){
				DeploySystemUI.showNotificationInfo("Result","No match result found.");		
			}
			table.setData(machines);
    	} catch (Throwable e1) {
    		DeploySystemUI.showNotificationInfo("Error",e1.getMessage());
		}
	}
	//
	public List<Machine>getOptMachines(){
		if(optOnSelectCheckBox.getValue()){
			return table.getSelectValues();
		}else{
			return machines;
		}
	}
}
