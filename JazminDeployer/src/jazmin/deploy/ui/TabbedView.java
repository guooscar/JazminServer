/**
 * 
 */
package jazmin.deploy.ui;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.themes.ValoTheme;

/**
 * @author yama
 * 30 Dec, 2014
 */
public class TabbedView extends CssLayout{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private TabSheet tabs;
	public TabbedView() {
		super();
		setSizeFull();
		initUI();
	}
	//
	private void initUI(){
		tabs=new TabSheet();
		tabs.setSizeFull();
        tabs.addStyleName(ValoTheme.TABSHEET_PADDED_TABBAR);
        addComponent(tabs);
	}
	//
	protected void addPage(Component c){
	    tabs.addTab(c);	
	}
}
