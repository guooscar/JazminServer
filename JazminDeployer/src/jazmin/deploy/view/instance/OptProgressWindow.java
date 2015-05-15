/**
 * 
 */
package jazmin.deploy.view.instance;

import java.util.function.Consumer;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.Responsive;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

/**
 * @author yama
 * 6 Jan, 2015
 */
@SuppressWarnings("serial")
public class OptProgressWindow extends Window{
	private Label infoLabel;
	private Button cancel;
	//
	public OptProgressWindow(Consumer<OptProgressWindow>consumer) {
        Responsive.makeResponsive(this);
        setWidth("600px");
        center();
        setCloseShortcut(KeyCode.ESCAPE, null);
        setResizable(false);
        setClosable(false);
        setHeight(60.0f, Unit.PERCENTAGE);
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        setContent(content);
        infoLabel=new Label();
        infoLabel.setContentMode(ContentMode.PREFORMATTED);
        infoLabel.addStyleName(ValoTheme.LABEL_H3);
        infoLabel.setSizeUndefined();
        content.addComponent(infoLabel);
        content.setComponentAlignment(infoLabel, Alignment.MIDDLE_CENTER);
        content.setExpandRatio(infoLabel, 1f);
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
        ok.addStyleName(ValoTheme.BUTTON_PRIMARY); 
        ok.addClickListener(e->{
         	ok.setEnabled(false);
         	cancel.setEnabled(false);
        	consumer.accept(this);
        });
        ok.focus();
        footer.addComponent(ok);
        footer.setComponentAlignment(ok, Alignment.TOP_RIGHT);
        cancel = new Button("Cancel");
        cancel.addClickListener(e->close());
        cancel.focus();
        footer.addComponent(cancel);
        footer.setComponentAlignment(cancel, Alignment.TOP_RIGHT);
        //
        content.addComponent(footer);
    }
	//
	public void setInfo(String info){
		infoLabel.setValue(info);
	}
}
