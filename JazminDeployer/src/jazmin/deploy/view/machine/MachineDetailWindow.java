/**
 * 
 */
package jazmin.deploy.view.machine;

import jazmin.deploy.domain.Machine;
import jazmin.deploy.ui.StaticBeanForm;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.Responsive;
import com.vaadin.ui.Alignment;
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
public class MachineDetailWindow extends Window{
	//
	public MachineDetailWindow(Machine machine) {
        Responsive.makeResponsive(this);
        setCaption(machine.id);
        setWidth("600px");
        center();
        setCloseShortcut(KeyCode.ESCAPE, null);
        setResizable(true);
        setClosable(true);
        setHeight(90.0f, Unit.PERCENTAGE);
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        setContent(content);
        StaticBeanForm<Machine>beanForm=new StaticBeanForm<Machine>(
        		 machine, 1,"sshPassword");
        beanForm.setSizeFull();
        content.addComponent(beanForm);
        content.setExpandRatio(beanForm, 1f);
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
