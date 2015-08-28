/**
 * 
 */
package jazmin.deploy.view.app;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Responsive;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

/**
 * @author yama
 * 6 Jan, 2015
 */
@SuppressWarnings("serial")
public class DeployGraphWindow extends Window{
	public static final String TYPE_SYSTEM="sysgraph";
	public static final String TYPE_INSTANCE="insgraph";
	//
	public DeployGraphWindow(String type,String systemId,String clusterName) {
        Responsive.makeResponsive(this);
        setCaption(systemId+" deploy graph");
        setWidth(90.0f, Unit.PERCENTAGE);
        center();
        setCloseShortcut(KeyCode.ESCAPE, null);
        setResizable(true);
        setClosable(true);
        setHeight(90.0f, Unit.PERCENTAGE);
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        setContent(content);
        BrowserFrame frame=null;
        if(clusterName!=null){
        	frame=new BrowserFrame(null,
            		new ExternalResource("/srv/deploy/"+type+"/"+systemId+"/"+clusterName));  
            	
        }else{
        	frame=new BrowserFrame(null,
            		new ExternalResource("/srv/deploy/"+type+"/"+systemId));  
            
        }
        frame.setSizeFull();
        content.addComponent(frame);
        content.setExpandRatio(frame, 1f);
        //
        HorizontalLayout footer = new HorizontalLayout();
        footer.addStyleName(ValoTheme.WINDOW_BOTTOM_TOOLBAR);
        footer.setWidth(100.0f, Unit.PERCENTAGE);

        Button ok = new Button("Close");
        ok.addStyleName(ValoTheme.BUTTON_PRIMARY);
        ok.addClickListener(e->close());
        ok.focus();
        footer.addComponent(ok);
        footer.setComponentAlignment(ok, Alignment.TOP_RIGHT);
        content.addComponent(footer);
    }

}
