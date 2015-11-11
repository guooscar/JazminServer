package jazmin.deploy.view.console;

import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
/**
 * 
 * @author yama
 * 30 Dec, 2014
 */
@SuppressWarnings("serial")
public class ConsoleView extends VerticalLayout {
	private TabSheet tabsheet;
    public ConsoleView() {
        setSizeFull();
        tabsheet=new TabSheet();
        tabsheet.setSizeFull();
        addComponent(tabsheet);
        setExpandRatio(tabsheet, 1);
        //
        tabsheet.addComponent(new MachineOptView());
        tabsheet.addComponent(new InstanceOptView());
      }
    //
    public void addOptView(Component component){
    	tabsheet.addComponent(component);
    	tabsheet.setSelectedTab(component);
    	tabsheet.getTab(component).setClosable(true);
    	
    }
}
