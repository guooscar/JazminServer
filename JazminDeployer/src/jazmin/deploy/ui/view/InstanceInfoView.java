/**
 * 
 */
package jazmin.deploy.ui.view;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import jazmin.core.Jazmin;
import jazmin.deploy.DeploySystemUI;
import jazmin.deploy.domain.DeployManager;
import jazmin.deploy.domain.Instance;
import jazmin.deploy.ui.BeanTable;

import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;

/**
 * @author yama
 * 6 Jan, 2015
 */
@SuppressWarnings("serial")
public class InstanceInfoView extends DeployBaseView{
	BeanTable<Instance>table;
	List<Instance>instances;
	//
	public InstanceInfoView() {
		super();
		initUI();
		searchTxt.setValue("1=1 order by priority");
	}
	@Override
	public BeanTable<?> createTable() {
		instances=new ArrayList<Instance>();
		table= new BeanTable<Instance>(null, Instance.class,
				"machine","user","password");
		table.setCellStyleGenerator(new Table.CellStyleGenerator() {
			@Override
			public String getStyle(Table source, Object itemId, Object propertyId) {
				if(propertyId==null){
					return null;
				}
				if(propertyId.equals("isAlive")){
					Boolean isTrue=(Boolean)
							source.getItem(itemId).getItemProperty(propertyId).getValue();
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
		addOptButton("View BootFile",null, (e)->viewBootFile());
		addOptButton("View TailLog",null, (e)->viewTailLog());
		addOptButton("View Report",null, (e)->viewActionReport());
		addOptButton("Test Instances",null, (e)->testInstance());
		//
		addOptButton("Set Version",ValoTheme.BUTTON_PRIMARY, (e)->setPackageVersion());
		//
		addOptButton("Start Instances",ValoTheme.BUTTON_DANGER, (e)->startInstance());
		addOptButton("Stop Instances",ValoTheme.BUTTON_DANGER, (e)->stopInstance());
		addOptButton("Restart Instances",ValoTheme.BUTTON_DANGER, (e)->restartInstance());
	}
	//
	private void viewBootFile(){
		Instance instance=table.getSelectValue();
		if(instance==null){
			DeploySystemUI.showNotificationInfo("Info",
					"Please choose which instance to view.");
		}else{
			BootFileWindow bfw=new BootFileWindow(instance);
			UI.getCurrent().addWindow(bfw);
			bfw.focus();
		}
	}
	//
	private void viewTailLog(){
		Instance instance=table.getSelectValue();
		if(instance==null){
			DeploySystemUI.showNotificationInfo("Info",
					"Please choose which instance to view.");
		}else{
			TailLogWindow bfw=new TailLogWindow(instance);
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
		String search=searchTxt.getValue();
    	if(search==null){
    		return;
    	}
    	try {
			instances=DeployManager.instances(search);
			if(instances.isEmpty()){
				DeploySystemUI.showNotificationInfo("Result","No mactch result found.");		
			}
		 	table.setData(instances);
    	} catch (Throwable e1) {
    		DeploySystemUI.showNotificationInfo("Error",e1.getMessage());
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
		optWindow.setInfo("Confirm test total "+instances.size()+" instance(s) state?");
		UI.getCurrent().addWindow(optWindow);
	}
	//
	//
	private void testInstance0(OptProgressWindow window){
		AtomicInteger counter=new AtomicInteger();
		instances.forEach(instance->{
			window.getUI().access(()->{
				window.setInfo("test "+instance.id+" "+
						counter.incrementAndGet()+
						"/"+instances.size()+"...");	
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
	private void startInstance(){
		OptProgressWindow optWindow=new OptProgressWindow(window->{
			Jazmin.execute(()->{
				DeployManager.resetActionReport();
				startInstance0(window);
			});
		});
		optWindow.setCaption("Confirm");
		optWindow.setInfo("Confirm start total "+instances.size()+" instance(s)?");
		UI.getCurrent().addWindow(optWindow);
	}
	//
	//
	private void startInstance0(OptProgressWindow window){
		AtomicInteger counter=new AtomicInteger();
		AtomicInteger waitCounter=new AtomicInteger();
		instances.forEach(instance->{
			window.getUI().access(()->{
				window.setInfo("start "+instance.id+" "+
						counter.incrementAndGet()+
						"/"+instances.size()+"...");	
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
		optWindow.setInfo("Confirm stop total "+instances.size()+" instance(s)?");
		UI.getCurrent().addWindow(optWindow);
	}
	//
	private void stopInstance0(OptProgressWindow window,boolean stopWindow){
		AtomicInteger counter=new AtomicInteger();
		instances.forEach(instance->{
			window.getUI().access(()->{
				window.setInfo("stop "+instance.id+" "+
						counter.incrementAndGet()+
						"/"+instances.size()+"...");	
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
		optWindow.setInfo("Confirm restart total "+instances.size()+" instance(s)?");
		UI.getCurrent().addWindow(optWindow);
	}
	//
	private void setPackageVersion(){
		InputWindow sw=new InputWindow(window->{
			String version=window.getInputValue();
			try{
				DeployManager.setPackageVersion(instances, version);
				DeployManager.saveInstanceConfig();
			}catch(Exception e){
				DeploySystemUI.showNotificationInfo("Error",e.getMessage());
			}
			window.close();
			DeploySystemUI.showNotificationInfo("Info","Package version set to "+version);
			loadData();
		});
		sw.setCaption("Change instance package version");
		sw.setInfo("Change "+instances.size()+" instance(s) package version");
		UI.getCurrent().addWindow(sw);
	}
}
