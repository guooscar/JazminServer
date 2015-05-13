/**
 * 
 */
package jazmin.deploy.view;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import jazmin.core.Jazmin;
import jazmin.deploy.DeploySystemUI;
import jazmin.deploy.domain.DeployManager;
import jazmin.deploy.domain.Machine;
import jazmin.deploy.ui.BeanTable;

import com.vaadin.ui.Table;
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
	//
	public MachineInfoView() {
		super();
		initUI();
		searchTxt.setValue("1=1");
	}
	@Override
	public BeanTable<?> createTable() {
		machines=new ArrayList<Machine>();
		table= new BeanTable<Machine>(null, Machine.class,
				"sshPassword",
				"jazminHome",
				"memcachedHome",
				"haproxyHome");
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
		addOptButton("Test Machine",null, (e)->checkMachine());
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
	private void checkMachine(){
		OptProgressWindow optWindow=new OptProgressWindow(window->{
			Jazmin.execute(()->{
				checkMachine0(window);
			});
		});
		optWindow.setCaption("Confirm");
		optWindow.setInfo("Confirm test total "+machines.size()+" machine(s) state?");
		UI.getCurrent().addWindow(optWindow);
	}
	//
	private void checkMachine0(OptProgressWindow window){
		AtomicInteger counter=new AtomicInteger();
		machines.forEach(machine->{
			window.getUI().access(()->{
				window.setInfo("test "+machine.id+" "+
						counter.incrementAndGet()+
						"/"+machines.size()+"...");	
			});
			DeployManager.testMachine(machine);
			window.getUI().access(()->{
				window.setInfo("test "+machine.id+" result:"+machine.isAlive);	
			});
		});
		window.getUI().access(()->{
			window.close();
			loadData();
		});
	}
	//
	private void runCmd(){
		InputWindow optWindow=new InputWindow(window->{
			Jazmin.execute(()->{
				runCmd0(window);
			});
		});
		optWindow.setCaption("Run command");
		optWindow.setInfo("Confirm run command on total "+machines.size()+" machine(s)?");
		UI.getCurrent().addWindow(optWindow);
	}
	//
	private void runCmd0(InputWindow window){
		AtomicInteger counter=new AtomicInteger();
		DeployManager.resetActionReport();
		machines.forEach(machine->{
			window.getUI().access(()->{
				window.setInfo("run on "+machine.id+" "+
						counter.incrementAndGet()+
						"/"+machines.size()+"...");	
			});
			String cmd=window.getInputValue();
			DeployManager.appendActionReport(DeployManager.runOnMachine(machine,cmd));
		});
		window.getUI().access(()->{
			ActionReportWindow rpw=new ActionReportWindow();
			window.getUI().addWindow(rpw);
			DeploySystemUI.showNotificationInfo("Info", "run command complete");
			window.close();
		});
	}
	//
	@Override
	public void loadData(){
		String search=searchTxt.getValue();
    	if(search==null){
    		return;
    	}
    	try {
    		machines=DeployManager.getMachines(search);
			if(machines.isEmpty()){
				DeploySystemUI.showNotificationInfo("Result","No mactch result found.");		
			}
			table.setData(machines);
    	} catch (Throwable e1) {
    		DeploySystemUI.showNotificationInfo("Error",e1.getMessage());
		}
	}
}
