/**
 * 
 */
package jazmin.deploy.view.machine;

import java.util.ArrayList;
import java.util.List;

import jazmin.deploy.DeploySystemUI;
import jazmin.deploy.domain.Instance;
import jazmin.deploy.domain.Machine;
import jazmin.deploy.manager.DeployManager;
import jazmin.deploy.ui.BeanTable;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.Responsive;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * @author yama
 * 6 Jan, 2015
 */
@SuppressWarnings("serial")
public class MachineInstanceWindow extends Window{
	//
	public static class MachineInstance{
		public String id;
		public String appId;
		public String appType;
		public int port;
	}
	//
	Machine machine;
	BeanTable<MachineInstance> table;
	//
	public MachineInstanceWindow(Machine machine) {
		this.machine=machine;
        Responsive.makeResponsive(this);
        setCaption("instances on "+machine.id);
        setWidth("600px");
        center();
        setCloseShortcut(KeyCode.ESCAPE, null);
        setResizable(true);
        setClosable(true);
        setHeight(90.0f, Unit.PERCENTAGE);
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        setContent(content);
        table = createTable();
		content.addComponent(table);
		table.setSizeFull();
        content.setExpandRatio(table, 1);
        content.addComponent(table);
        content.setExpandRatio(table, 1f);
        loadData();
    }
	//
	public BeanTable<MachineInstance> createTable() {
		BeanTable<MachineInstance> table= new BeanTable<MachineInstance>(null, MachineInstance.class);
		return table;
	}
	//
	private void loadData(){
		try {
			List<Instance>instanceList=DeployManager.getInstances(
					DeploySystemUI.getUser().id,"machineId='"+machine.id+"'");
			List<MachineInstance>mis=new ArrayList<MachineInstanceWindow.MachineInstance>();
			for(Instance ii:instanceList){
				MachineInstance mi=new MachineInstance();
				mi.appId=ii.appId;
				mi.appType=ii.application.type;
				mi.id=ii.id;
				mi.port=ii.port;
				mis.add(mi);
			}
		 	table.setBeanData(mis);
    	} catch (Throwable e1) {
    		DeploySystemUI.showNotificationInfo("Error",e1.getMessage());
		}
	}
}
