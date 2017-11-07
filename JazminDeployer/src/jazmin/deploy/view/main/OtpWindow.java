/**
 * 
 */
package jazmin.deploy.view.main;

import java.util.function.Consumer;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.Responsive;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

/**
 * @author yama
 * 6 Jan, 2015
 */
@SuppressWarnings("serial")
public class OtpWindow extends Window{
	private Label infoLabel;
	private TextField textField;
	//	
	public OtpWindow(Consumer<OtpWindow>consumer) {
        Responsive.makeResponsive(this);
        setWidth(300,Unit.PIXELS);
        setHeight(200,Unit.PIXELS);
        center();
        setCloseShortcut(KeyCode.ESCAPE, null);
        setResizable(false);
        setClosable(false);
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        setContent(content);
        //
        VerticalLayout infoContainer=new VerticalLayout();
        infoContainer.setSizeFull();
        infoContainer.setMargin(true);
        infoContainer.setSpacing(true);
        content.addComponent(infoContainer);
        content.setExpandRatio(infoContainer, 1);
        //
        //
        textField=new TextField("OTP");
        textField.setWidth(100,Unit.PERCENTAGE);
        textField.setRequired(true);
        textField.setRequiredError("Input OTP code");
        infoContainer.addComponent(textField);
        infoContainer.setComponentAlignment(textField, Alignment.MIDDLE_CENTER);
      
        //
        HorizontalLayout footer = new HorizontalLayout();
        footer.setSpacing(true);
        footer.addStyleName(ValoTheme.WINDOW_BOTTOM_TOOLBAR);
        footer.setWidth(100.0f, Unit.PERCENTAGE);
        //
        Label empty=new Label();
        footer.addComponent(empty);
        footer.setExpandRatio(empty, 1);
        //
        Button ok = new Button("OK");
        ok.addStyleName(ValoTheme.BUTTON_SMALL);
        ok.addStyleName(ValoTheme.BUTTON_PRIMARY); 
        ok.addClickListener(e->{
        	if(!textField.isValid()){
        		return;
        	}
        	consumer.accept(this);
        });
        ok.focus();
        footer.addComponent(ok);
        footer.setComponentAlignment(ok, Alignment.TOP_RIGHT);
        //
        Button cancel = new Button("Cancel");
        cancel.addStyleName(ValoTheme.BUTTON_SMALL);
        cancel.addClickListener(e->close());
        cancel.focus();
        footer.addComponent(cancel);
        footer.setComponentAlignment(cancel, Alignment.TOP_RIGHT);
        //
        content.addComponent(footer);
        setCaption("OTP");
    }
	public String getInputValue(){
		return textField.getValue();
	}
}
