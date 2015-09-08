/**
 * 
 */
package jazmin.deploy.view.main;

import jazmin.deploy.domain.DeployManager;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.Responsive;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * @author yama
 * 6 Jan, 2015
 */
@SuppressWarnings("serial")
public class ErrorMessageWindow extends Window{
	//
	public ErrorMessageWindow() {
        Responsive.makeResponsive(this);
        setCaption("ERROR");
        setWidth("600px");
        center();
        setCloseShortcut(KeyCode.ESCAPE, null);
        setResizable(true);
        setClosable(true);
        setHeight(90.0f, Unit.PERCENTAGE);
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        Label label=new Label();
        label.setContentMode(ContentMode.PREFORMATTED);
        label.setValue(DeployManager.getErrorMessage());
        content.addComponent(label);
        //
        setContent(content);
    }

}
