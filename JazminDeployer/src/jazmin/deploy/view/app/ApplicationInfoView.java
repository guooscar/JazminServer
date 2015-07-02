/**
 * 
 */
package jazmin.deploy.view.app;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.vaadin.aceeditor.AceMode;

import jazmin.deploy.DeploySystemUI;
import jazmin.deploy.domain.Application;
import jazmin.deploy.domain.DeployManager;
import jazmin.deploy.ui.BeanTable;
import jazmin.deploy.view.instance.InputWindow;
import jazmin.deploy.view.main.CodeEditorCallback;
import jazmin.deploy.view.main.CodeEditorWindow;
import jazmin.deploy.view.main.DeployBaseView;

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
		addOptButton("View Detail",null, (e)->viewDetail());
		addOptButton("View Default Template",null, (e)->viewDefaultTemplate());
		addOptButton("View Template",null, (e)->viewTemplate());
		addOptButton("System Graph",null, (e)->viewSystemGraph());
		addOptButton("Instance Graph",null, (e)->viewInstanceGraph());
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
		String search=getSearchValue();
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
