/**
 * 
 */
package jazmin.deploy.ui.view;

import java.util.ArrayList;
import java.util.List;

import jazmin.deploy.DeploySystemUI;
import jazmin.deploy.domain.DeployManager;
import jazmin.deploy.domain.Package;
import jazmin.deploy.ui.BeanTable;

import com.vaadin.ui.UI;

/**
 * @author yama
 * 6 Jan, 2015
 */
@SuppressWarnings("serial")
public class PackageInfoView extends DeployBaseView{
	private List<Package>packages;
	BeanTable<Package>table;
	//
	public PackageInfoView() {
		super();
		initUI();
	}
	@Override
	public BeanTable<?> createTable() {
		packages=new ArrayList<Package>();
		table= new BeanTable<Package>(null, Package.class);
		return table;
	}
	//
	private void initUI(){
		addOptButton("Download",null, (e)->download());
	}
	//
	private void download(){
		Package pkg=table.getSelectValue();
		if(pkg==null){
			DeploySystemUI.showNotificationInfo("Info",
					"Please choose which package to download.");
		}else{
			UI.getCurrent().getPage().open("/srv/deploy/download/"+pkg.id, pkg.id);
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
    		packages=DeployManager.packages(search);
			if(packages.isEmpty()){
				DeploySystemUI.showNotificationInfo("Result","No mactch result found.");		
			}
			table.setData(packages);
    	} catch (Throwable e1) {
    		DeploySystemUI.showNotificationInfo("Error",e1.getMessage());
		}
	}
}
