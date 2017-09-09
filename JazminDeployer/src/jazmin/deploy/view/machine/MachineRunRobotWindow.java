/**
 * 
 */
package jazmin.deploy.view.machine;

import java.util.List;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Responsive;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import jazmin.deploy.DeploySystemUI;
import jazmin.deploy.domain.Machine;
import jazmin.deploy.manager.DeployManager;
import jazmin.deploy.view.main.WebSshWindow;

/**
 * @author yama
 * 6 Jan, 2015
 */
@SuppressWarnings("serial")
public class MachineRunRobotWindow extends Window{
	ComboBox robotCombox;
	List<Machine>machines;
	CheckBox robotRootCheckBox;
	Label infoLabel;
	//
	public MachineRunRobotWindow(List<Machine>machines){
		this.machines=machines;
	    Responsive.makeResponsive(this);
        setCaption("Run robot");
        setWidth("500px");
        setHeight("500px");
        center();
        setCloseShortcut(KeyCode.ESCAPE, null);
        setResizable(false);
        setClosable(true);
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setSpacing(true);
        setContent(content);
        infoLabel=new Label("Confirm Label");
        //
        robotCombox=new ComboBox("Robot");
        robotCombox.setWidth("100%");
        robotCombox.setNewItemsAllowed(false);
        robotCombox.setNullSelectionAllowed(false);
        robotCombox.setInvalidAllowed(false);
        robotCombox.setIcon(FontAwesome.ANDROID);
        robotCombox.setWidth(100.0f, Unit.PERCENTAGE);
        
        Button cmdBtn = new Button("Run");
        cmdBtn.addStyleName(ValoTheme.BUTTON_SMALL);
        cmdBtn.addStyleName(ValoTheme.BUTTON_DANGER);
        robotRootCheckBox=new CheckBox("Run as root");
        VerticalLayout cmdLayout=new VerticalLayout(infoLabel,robotCombox,robotRootCheckBox);
        cmdLayout.setSizeFull();
        cmdLayout.setSpacing(true);
        cmdLayout.setMargin(true);
        content.addComponent(cmdLayout);
        HorizontalLayout footer = new HorizontalLayout();
        footer.addStyleName(ValoTheme.WINDOW_BOTTOM_TOOLBAR);
        footer.setWidth(100.0f, Unit.PERCENTAGE);

        Button ok = new Button("Run");
        ok.addStyleName(ValoTheme.BUTTON_SMALL);
        ok.addStyleName(ValoTheme.BUTTON_DANGER);
        ok.addClickListener(e->runRobot());
        footer.addComponent(ok);
        footer.setComponentAlignment(ok, Alignment.TOP_RIGHT);
        content.addComponent(footer);
        content.setExpandRatio(cmdLayout, 1);
        //
        DeployManager.getScripts("robot").forEach(robot->{
        	Object itemId=robotCombox.addItem(robot.name);
        	robotCombox.setItemCaption(itemId, robot.name);
        });
        //
        StringBuilder machineList=new StringBuilder();
        machines.forEach(m->machineList.append(m.id+" "));
        //
        infoLabel.setValue("<div style='font-size:16px;font-weight:700;color:red'>confirm run robot in <br>"+machineList+"</div>");
        infoLabel.setContentMode(ContentMode.HTML);
	}
	//
	private void runRobot(){
		String robot=(String) robotCombox.getValue();
		if(robot==null){
			DeploySystemUI.showInfo("choose robot first");
			return;
		}
		boolean isRunAsRoot=robotRootCheckBox.getValue();
		//
		int idx=0;
		for(Machine machine:machines){
			try {
				String token=DeployManager.createOneTimeRobotToken(machine, isRunAsRoot, robot);
				WebSshWindow sw=new WebSshWindow(token);
				sw.setWidth("650px");
				sw.setHeight("500px");
				sw.setCaption(robot+"#"+(isRunAsRoot?"root":machine.sshUser)+"@"+machine.id);
				sw.center();
				sw.setPositionX(sw.getPositionX()+(idx++)*100);
				UI.getCurrent().addWindow(sw);
				sw.focus();
			
			} catch (Exception e) {
				DeploySystemUI.showNotificationInfo("ERROR", e.getMessage());
			}
		}
	}
}
