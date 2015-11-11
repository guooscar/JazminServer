/**
 * 
 */
package jazmin.deploy.view.machine;

import jazmin.deploy.domain.Machine;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Responsive;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * @author yama
 * 6 Jan, 2015
 */
@SuppressWarnings("serial")
public class MachineWebSshWindow extends Window{
	//
	public MachineWebSshWindow(Machine machine) {
        Responsive.makeResponsive(this);
        setCaption(machine.sshUser+"@"+machine.id);
        setWidth(90.0f, Unit.PERCENTAGE);
        center();
        setCloseShortcut(KeyCode.ESCAPE, null);
        setResizable(true);
        setClosable(true);
        setHeight(90.0f, Unit.PERCENTAGE);
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        setContent(content);
        content.setImmediate(false);
        BrowserFrame frame=new BrowserFrame(null,
        		new ExternalResource("/srv/deploy/webssh/"+machine.id));
        frame.setSizeFull();
        content.addComponent(frame);
        content.setExpandRatio(frame, 1f);
        
    }
}
