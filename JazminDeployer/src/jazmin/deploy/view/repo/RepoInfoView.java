/**
 * 
 */
package jazmin.deploy.view.repo;

import java.util.ArrayList;
import java.util.List;

import jazmin.deploy.DeploySystemUI;
import jazmin.deploy.domain.RepoItem;
import jazmin.deploy.manager.DeployManager;
import jazmin.deploy.ui.BeanTable;
import jazmin.deploy.view.main.DeployBaseView;

import com.vaadin.ui.UI;

/**
 * @author yama
 * 6 Jan, 2015
 */
@SuppressWarnings("serial")
public class RepoInfoView extends DeployBaseView{
	private List<RepoItem>repoItems;
	BeanTable<RepoItem>table;
	//
	public RepoInfoView() {
		super();
		initUI();
		searchTxt.setValue("1=1");
	}
	@Override
	public BeanTable<?> createTable() {
		repoItems=new ArrayList<RepoItem>();
		table= new BeanTable<RepoItem>(null, RepoItem.class);
		return table;
	}
	//
	private void initUI(){
		addOptButton("Download",null, (e)->download());
	}
	//
	private void download(){
		RepoItem pkg=table.getSelectValue();
		if(pkg==null){
			DeploySystemUI.showNotificationInfo("Info",
					"Please choose which package to download.");
		}else{
			UI.getCurrent().getPage().open("/srv/ivy/repo/"+pkg.id, pkg.id);
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
    		repoItems=DeployManager.getRepoItems(search);
			if(repoItems.isEmpty()){
				DeploySystemUI.showNotificationInfo("Result","No mactch result found.");		
			}
			table.setData(repoItems);
    	} catch (Throwable e1) {
    		DeploySystemUI.showNotificationInfo("Error",e1.getMessage());
		}
	}
}
