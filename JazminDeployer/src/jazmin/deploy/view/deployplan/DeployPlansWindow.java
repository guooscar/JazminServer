package jazmin.deploy.view.deployplan;

import java.util.List;

import jazmin.deploy.domain.JavaScriptSource;
import jazmin.deploy.manager.DeployManager;
import jazmin.deploy.ui.BeanTable;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.server.Page;
import com.vaadin.server.WebBrowser;
import com.vaadin.ui.Window;

/**
 * 
 * @author yama
 * 13 Aug, 2016
 */
public class DeployPlansWindow extends Window{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4723453634993653187L;

	private String selectedPlan;
	//
	@SuppressWarnings("serial")
	public DeployPlansWindow() {
		super();
		setCaption("Deploy Plans");
		setWidth("600");
		setHeight("400");
		BeanTable<JavaScriptSource> table = new BeanTable<JavaScriptSource>(null, JavaScriptSource.class,"lastModifiedTime");
		setContent(table);
		table.setSizeFull();
		center();
		//
		List<JavaScriptSource>beans=DeployManager.getScripts("deployplan");
		table.setBeanData(beans);
		//
		table.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				WebBrowser wb=Page.getCurrent().getWebBrowser();
				//support ios and android
				if(wb.isIPhone()||wb.isAndroid()||event.isDoubleClick()){
					JavaScriptSource hb=table.getValueByItem(event.getItem());
					selectedPlan=hb.name;
					close();
				}
			}
		});
	}
	//
	public String getSelectedPlan(){
		return selectedPlan;
	}
	
}
