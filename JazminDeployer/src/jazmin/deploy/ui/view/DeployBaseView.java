/**
 * 
 */
package jazmin.deploy.ui.view;

import jazmin.deploy.ui.BeanTable;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

/**
 * @author yama
 * 6 Jan, 2015
 */
@SuppressWarnings("serial")
public abstract class DeployBaseView extends VerticalLayout{
	protected TextField searchTxt;
	private HorizontalLayout tray;
	//
	public DeployBaseView() {
		super();
		initUI();
	}
	//
	public abstract void loadData();
	public abstract BeanTable<?> createTable();
	//
	private void initUI(){
		HorizontalLayout optLayout = new HorizontalLayout();
		optLayout.setSpacing(true);
		optLayout.addStyleName(ValoTheme.WINDOW_TOP_TOOLBAR);
		optLayout.setWidth(100.0f, Unit.PERCENTAGE);
		searchTxt = new TextField("Filter", "");
		searchTxt.setIcon(FontAwesome.SEARCH);
		searchTxt.setWidth(100.0f, Unit.PERCENTAGE);
		searchTxt.addStyleName(ValoTheme.TEXTFIELD_TINY);
		searchTxt.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
		searchTxt.addShortcutListener(new ShortcutListener("Search",KeyCode.ENTER,null) {
			@Override
			public void handleAction(Object sender, Object target) {
				loadData();
			}
		});
		//
		optLayout.addComponent(searchTxt);
		optLayout.setExpandRatio(searchTxt,1.0f);
		//
        Button ok = new Button("Query");
        ok.addStyleName(ValoTheme.BUTTON_PRIMARY);
        ok.addStyleName(ValoTheme.BUTTON_SMALL);
        optLayout.addComponent(ok);
        ok.addClickListener(e->loadData());
        optLayout.setComponentAlignment(ok, Alignment.BOTTOM_RIGHT);
        addComponent(optLayout);
        
        BeanTable<?> table = createTable();
		addComponent(table);
        setExpandRatio(table, 1);
        tray = new HorizontalLayout();
		tray.setWidth(100.0f, Unit.PERCENTAGE);
		tray.addStyleName(ValoTheme.WINDOW_BOTTOM_TOOLBAR);
		tray.setSpacing(true);
		tray.setMargin(true);
		//
		Label emptyLabel=new Label("");
		tray.addComponent(emptyLabel);
		tray.setComponentAlignment(emptyLabel, Alignment.MIDDLE_RIGHT);
		tray.setExpandRatio(emptyLabel,1.0f);
		//
		addComponent(tray);
	}
	//
	protected void addOptButton(String name,String style,ClickListener cl){
		Button btn = new Button(name);
		btn.addStyleName(ValoTheme.BUTTON_SMALL);
		if(style!=null){
			btn.addStyleName(style);
		}
		tray.addComponent(btn,tray.getComponentCount()-1);
		tray.setComponentAlignment(btn, Alignment.MIDDLE_LEFT);
		btn.addClickListener(cl);
		
	}
}
