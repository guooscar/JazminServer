/**
 * 
 */
package jazmin.deploy.view.pkg;

import java.util.ArrayList;
import java.util.List;

import jazmin.deploy.DeploySystemUI;
import jazmin.deploy.domain.DeployManager;
import jazmin.deploy.domain.AppPackage;
import jazmin.deploy.ui.BeanTable;
import jazmin.deploy.view.main.DeployBaseView;

import com.vaadin.ui.UI;

/**
 * @author yama
 * 6 Jan, 2015
 */
@SuppressWarnings("serial")
public class PackageInfoView extends DeployBaseView{
	private List<AppPackage>packages;
	BeanTable<AppPackage>table;
	//
	public PackageInfoView() {
		super();
		initUI();
		searchTxt.setValue("1=1");
	}
	@Override
	public BeanTable<?> createTable() {
		packages=new ArrayList<AppPackage>();
		table= new BeanTable<AppPackage>(null, AppPackage.class);
		return table;
	}
	//
	private void initUI(){
		addOptButton("Download",null, (e)->download());
		addOptButton("Progress",null, (e)->progress());
	}
	//
	private void download(){
		AppPackage pkg=table.getSelectValue();
		if(pkg==null){
			DeploySystemUI.showNotificationInfo("Info",
					"Please choose which package to download.");
		}else{
			UI.getCurrent().getPage().open("/srv/deploy/download/"+pkg.id, pkg.id);
		}
	}
	//
	private void progress(){
		PackageDownloadWindow w=new PackageDownloadWindow();
		UI.getCurrent().addWindow(w);
		w.focus();
	}
	//
	@Override
	public void loadData(){
		String search=searchTxt.getValue();
    	if(search==null){
    		return;
    	}
    	try {
    		packages=DeployManager.getPackages(search);
			if(packages.isEmpty()){
				DeploySystemUI.showNotificationInfo("Result","No mactch result found.");		
			}
			table.setData(packages);
    	} catch (Throwable e1) {
    		DeploySystemUI.showNotificationInfo("Error",e1.getMessage());
		}
	}
}
