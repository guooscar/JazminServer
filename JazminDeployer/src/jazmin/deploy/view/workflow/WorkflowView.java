/**
 * 
 */
package jazmin.deploy.view.workflow;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

/**
 * @author yama
 *
 */
@SuppressWarnings("serial")
public class WorkflowView extends VerticalLayout{
	public WorkflowView() {
		super();
		initBaseUI();
	}
	//
	protected void initBaseUI(){
		setSizeFull();
		HorizontalLayout optLayout=new HorizontalLayout();
		optLayout.setSpacing(true);
		optLayout.setWidth(100.0f, Unit.PERCENTAGE);
		optLayout.addStyleName(ValoTheme.WINDOW_TOP_TOOLBAR);
		Label titleLabel=new Label("Workflow Editor");
		optLayout.addComponent(titleLabel);
		addComponent(optLayout);
		//
		Component table = createMainView();
		addComponent(table);
		table.setSizeFull();
        setExpandRatio(table, 1);
       
	}
	private BrowserFrame frame;
	//
	private Component createMainView(){
		frame = new BrowserFrame(null,
				new ExternalResource("/srv/workflow/editor"));
		frame.setImmediate(true);
		frame.setSizeFull();
		return frame;
	}
}
