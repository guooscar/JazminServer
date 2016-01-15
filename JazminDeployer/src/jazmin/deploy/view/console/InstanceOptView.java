/**
 * 
 */
package jazmin.deploy.view.console;

import java.util.ArrayList;
import java.util.List;

import jazmin.deploy.DeploySystemUI;
import jazmin.deploy.domain.Application;
import jazmin.deploy.domain.DeployManager;
import jazmin.deploy.domain.Instance;
import jazmin.deploy.ui.BeanTable;
import jazmin.deploy.view.instance.InstanceHaproxyStatWindow;
import jazmin.deploy.view.instance.InstanceMemcachedWindow;
import jazmin.deploy.view.instance.InstanceMySQLWindow;
import jazmin.deploy.view.instance.InstanceWebSshWindow;
import jazmin.deploy.view.main.DeployBaseView;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

/**
 * @author yama
 * 6 Jan, 2015
 */
@SuppressWarnings("serial")
public class InstanceOptView extends DeployBaseView{
	BeanTable<Instance>table;
	List<Instance>instanceList;
	//
	public InstanceOptView() {
		super();
		setCaption("Instances");
		searchTxt.setValue("1=1 order by priority desc");
		loadData();
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
		instanceList=new ArrayList<Instance>();
		table= new BeanTable<Instance>(null, Instance.class,
				"machine","user","password","application","properties");
		table.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				if(event.isDoubleClick()){
					Instance instance=table.getItemValue(event.getItem());
					openConsole(instance);
				}
			}
		});
		return table;
	}
	//
	private void openConsole(Instance instance){
		if(instance.application==null){
			DeploySystemUI.showNotificationInfo("Info",
					"Can not find application on instance:"+instance.id);
			return;
		}
		Window window=null;
		//
		if(instance.application.type.equals(Application.TYPE_HAPROXY)){
			window=new InstanceHaproxyStatWindow(instance);
		}
		//
		if(instance.application.type.equals(Application.TYPE_MYSQL)){
			window=new InstanceMySQLWindow(instance);
		}
		//
		if(instance.application.type.equals(Application.TYPE_MEMCACHED)){
			window=new InstanceMemcachedWindow(instance);
			
		}
		//
		if(instance.application.type.startsWith("jazmin")){
			window=new InstanceWebSshWindow(instance);
			
		}
		if(window!=null){
			Component c=window.getContent();
			c.setCaption(window.getCaption());
			DeploySystemUI.get().getMainView().getConsoleView().addOptView(c);
		}else{
			DeploySystemUI.showNotificationInfo("Info",
					"Not support application type:"+instance.application.type);
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
			instanceList=DeployManager.getInstances(DeploySystemUI.getUser().id,search);
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
		return instanceList;
	}
	
}
