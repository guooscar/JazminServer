/**
 * 
 */
package jazmin.deploy.view.machine;

import java.util.function.BiConsumer;

import jazmin.deploy.DeploySystemUI;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Responsive;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

/**
 * @author yama
 * 6 Jan, 2015
 */
@SuppressWarnings("serial")
public class MachineCopyWindow extends Window{
	TextField fromField;
	TextField toField;
	//
	public MachineCopyWindow(BiConsumer<String, String>callback){
	    Responsive.makeResponsive(this);
        setCaption("Copy File");
        setWidth("500px");
        center();
        setCloseShortcut(KeyCode.ESCAPE, null);
        setResizable(false);
        setClosable(true);
        setHeight("250px");
        //
        fromField=new TextField("Local File");
        fromField.setWidth("100%");
        fromField.setIcon(FontAwesome.ANCHOR);
        fromField.setWidth(100.0f, Unit.PERCENTAGE);
        fromField.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
        toField=new TextField("Remote File");
        toField.setWidth("100%");
        toField.setIcon(FontAwesome.ADJUST);
        toField.setWidth(100.0f, Unit.PERCENTAGE);
        toField.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
        VerticalLayout inputLayout=new VerticalLayout(fromField,toField);
        inputLayout.setSpacing(true);
        inputLayout.addStyleName(ValoTheme.WINDOW_TOP_TOOLBAR);
        inputLayout.setWidth(100.0f, Unit.PERCENTAGE);
        
        //
    	HorizontalLayout optLayout = new HorizontalLayout();
		optLayout.setSpacing(true);
		optLayout.addStyleName(ValoTheme.WINDOW_TOP_TOOLBAR);
		optLayout.setWidth(100.0f, Unit.PERCENTAGE);
		
		Button getBtn = new Button("OK");
	    optLayout.addComponent(getBtn);
	    getBtn.addClickListener(e->{
	    	String from=fromField.getValue();
	    	if(from==null||from.trim().isEmpty()){
	    		DeploySystemUI.showNotificationInfo("ERROR","Local file can not be null.");
	    		return;
	    	}
	    	String to=toField.getValue();
	    	if(to==null||to.trim().isEmpty()){
	    		DeploySystemUI.showNotificationInfo("ERROR","Remote file can not be null.");
	    		return;
	    	}
	    	callback.accept(from,to);
	    	close();
	    });
	    //
	    VerticalLayout content=new VerticalLayout(inputLayout,optLayout);
	    content.setExpandRatio(inputLayout,1);
	    content.setSizeFull();
        setContent(content);
	}
}
