/**
 * 
 */
package jazmin.deploy.view.main;

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
public class WebSshWindow extends Window{
	//
	public WebSshWindow(String token) {
        Responsive.makeResponsive(this);
        setWidth(700,Unit.PIXELS);
        center();
        setCloseShortcut(KeyCode.ESCAPE, null);
        setResizable(true);
        setClosable(true);
        setHeight(90.0f, Unit.PERCENTAGE);
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        setContent(content);
        content.setImmediate(false);
        String url="/srv/deploy/webssh?token="+token;
        BrowserFrame frame=new BrowserFrame(null,
        		new ExternalResource(url));
        //
        frame.setSizeFull();
        content.addComponent(frame);
        content.setExpandRatio(frame, 1f);
        
    }
}
