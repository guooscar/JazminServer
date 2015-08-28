/**
 * 
 */
package jazmin.deploy.view.instance;

import jazmin.deploy.domain.Instance;

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
public class InstanceTailLogWindow extends Window{
	//
	public InstanceTailLogWindow(Instance instance) {
        Responsive.makeResponsive(this);
        setCaption(instance.id+" tail log");
        setWidth(90.0f, Unit.PERCENTAGE);
        center();
        setCloseShortcut(KeyCode.ESCAPE, null);
        setResizable(true);
        setClosable(true);
        setHeight(90.0f, Unit.PERCENTAGE);
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        setContent(content);
        BrowserFrame frame=new BrowserFrame(null,
        		new ExternalResource("/srv/deploy/instance_taillog/"+instance.id));  
        frame.setSizeFull();
        content.addComponent(frame);
        content.setExpandRatio(frame, 1f); 
    }
}
