/**
 * 
 */
package jazmin.deploy.view.instance;

import java.util.List;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.Responsive;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

/**
 * @author yama
 * 6 Jan, 2015
 */
@SuppressWarnings("serial")
public class InstanceMySQLRowWindow extends Window{
	//
	@SuppressWarnings("unchecked")
	public InstanceMySQLRowWindow(List<String>headers,List<String>values) {
        Responsive.makeResponsive(this);
        setCaption("Item Value");
        setWidth("600px");
        center();
        setCloseShortcut(KeyCode.ESCAPE, null);
        setResizable(true);
        setClosable(true);
        setHeight(90.0f, Unit.PERCENTAGE);
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        setContent(content);
        Table table=new Table();
        table.addStyleName(ValoTheme.TABLE_NO_HORIZONTAL_LINES);
        table.addStyleName(ValoTheme.TABLE_COMPACT);
        table.addStyleName(ValoTheme.TABLE_SMALL);
        table.setSizeFull();
        table.setFooterVisible(false);
        table.setImmediate(true);
        table.setSelectable(true);
        table.addContainerProperty("KEY",String.class,"");
        table.addContainerProperty("VALUE",String.class,"");
        //
        for(int i=0;i<headers.size();i++){
        	Object newItemId = table.addItem();
			Item row = table.getItem(newItemId);
			Property<Object> p1 = row.getItemProperty("KEY");
			p1.setValue(headers.get(i)+"");
			Property<Object> p2 = row.getItemProperty("VALUE");
			p2.setValue(values.get(i)+"");
        }
        //
        table.setSizeFull();
        content.addComponent(table);
        content.setExpandRatio(table, 1f);
    }

}
