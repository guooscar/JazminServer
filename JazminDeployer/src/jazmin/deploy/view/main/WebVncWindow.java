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
public class WebVncWindow extends Window{
	//
	public WebVncWindow(String token,String password) {
        Responsive.makeResponsive(this);
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
        String url="/srv/deploy/webvnc?token="+token+"&password="+password;
        BrowserFrame frame=new BrowserFrame(null,
        		new ExternalResource(url));
        //
        frame.setSizeFull();
        content.addComponent(frame);
        content.setExpandRatio(frame, 1f);
        
    }
}
