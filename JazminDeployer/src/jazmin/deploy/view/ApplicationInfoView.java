/**
 * 
 */
package jazmin.deploy.view;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import jazmin.deploy.DeploySystemUI;
import jazmin.deploy.domain.Application;
import jazmin.deploy.domain.DeployManager;
import jazmin.deploy.ui.BeanTable;

import com.vaadin.ui.UI;

/**
 * @author yama
 * 6 Jan, 2015
 */
@SuppressWarnings("serial")
public class ApplicationInfoView extends DeployBaseView{
	BeanTable<Application>table;
	private List<Application>applications;
	//
	public ApplicationInfoView() {
		super();
		initUI();
		searchTxt.setValue("1=1");
	}
	@Override
	public BeanTable<?> createTable() {
		applications=new ArrayList<Application>();
		table= new BeanTable<Application>(null, Application.class);
		return table;
	}
	//
	private void initUI(){
		addOptButton("System Graph",null, (e)->viewSystemGraph());
		addOptButton("Instance Graph",null, (e)->viewInstanceGraph());
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
					if(cluster!=null){
						DeployGraphWindow bfw=new DeployGraphWindow(DeployGraphWindow.TYPE_INSTANCE,app.system,cluster);
						UI.getCurrent().addWindow(bfw);
						bfw.focus();	
					}else{
						DeploySystemUI.showNotificationInfo("Info",
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
	@Override
	public void loadData(){
		String search=searchTxt.getValue();
    	if(search==null){
    		return;
    	}
    	try {
    		applications=DeployManager.getApplications(search);
			if(applications.isEmpty()){
				DeploySystemUI.showNotificationInfo("Result","No mactch result found.");		
			}
			table.setData(applications);
    	} catch (Throwable e1) {
    		DeploySystemUI.showNotificationInfo("Error",e1.getMessage());
		}
	}
}
