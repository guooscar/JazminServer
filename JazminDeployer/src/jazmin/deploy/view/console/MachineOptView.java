/**
 * 
 */
package jazmin.deploy.view.console;

import java.util.ArrayList;
import java.util.List;

import jazmin.deploy.DeploySystemUI;
import jazmin.deploy.domain.Machine;
import jazmin.deploy.manager.DeployManager;
import jazmin.deploy.ui.BeanTable;
import jazmin.deploy.view.machine.MachineWebSshWindow;
import jazmin.deploy.view.main.DeployBaseView;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Component;

/**
 * @author yama
 * 6 Jan, 2015
 */
@SuppressWarnings("serial")
public class MachineOptView extends DeployBaseView{
	BeanTable<Machine>table;
	private List<Machine>machines;
	//
	public MachineOptView() {
		super();
		setCaption("Machines");
		searchTxt.setValue("1=1");
		setSizeFull();
		loadData();
	}
	@Override
	public BeanTable<?> createTable() {
		machines=new ArrayList<Machine>();
		table= new BeanTable<Machine>(null, Machine.class,
				"sshPassword",
				"rootSshPassword",
				"jazminHome",
				"memcachedHome",
				"haproxyHome");
		table.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				if(event.isDoubleClick()){
					Machine machine=table.getItemValue(event.getItem());
					MachineWebSshWindow window=new MachineWebSshWindow(machine,false);
					Component c=window.getContent();
					c.setCaption(window.getCaption());
					DeploySystemUI.get().getMainView().getConsoleView().addOptView(c);
				}
			}
		});
		return table;
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
}
