/**
 * 
 */
package jazmin.deploy.view.instance;

import java.util.List;

import jazmin.deploy.DeploySystemUI;
import jazmin.deploy.domain.Instance;
import jazmin.deploy.ui.BeanTable;
import jazmin.util.JdbcUtil;
import jazmin.util.JdbcUtil.ColumnInfo;
import jazmin.util.JdbcUtil.DatabaseInfo;
import jazmin.util.JdbcUtil.IndexInfo;
import jazmin.util.JdbcUtil.PrimaryKeyInfo;
import jazmin.util.JdbcUtil.TableInfo;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.Responsive;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * @author yama
 * 6 Jan, 2015
 */
@SuppressWarnings("serial")
public class InstanceMySQLTableInfoWindow extends Window{
	private Instance instance;
	private BeanTable<JdbcUtil.TableInfo> tablesList;
	private BeanTable<JdbcUtil.ColumnInfo> columnList;
	private BeanTable<JdbcUtil.IndexInfo> indexList;
	private BeanTable<JdbcUtil.PrimaryKeyInfo> primaryKeyList;
	//
	public InstanceMySQLTableInfoWindow(Instance instance) {
		this.instance=instance;
        Responsive.makeResponsive(this);
        setCaption(instance.id+" tables");
        setWidth(90.0f, Unit.PERCENTAGE);
        setHeight(90.0f, Unit.PERCENTAGE);
        center();
        setCloseShortcut(KeyCode.ESCAPE, null);
        setResizable(true);
        setClosable(true);
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        setContent(content);
        tablesList = new BeanTable<JdbcUtil.TableInfo>("", JdbcUtil.TableInfo.class);
        tablesList.setWidth("100%");
        tablesList.setHeight("200px");
        content.addComponent(tablesList);
        columnList = new BeanTable<JdbcUtil.ColumnInfo>("", JdbcUtil.ColumnInfo.class);
        columnList.setSizeFull();
        indexList = new BeanTable<JdbcUtil.IndexInfo>("", JdbcUtil.IndexInfo.class);
        indexList.setSizeFull();
        primaryKeyList = new BeanTable<JdbcUtil.PrimaryKeyInfo>("", JdbcUtil.PrimaryKeyInfo.class);
        primaryKeyList.setSizeFull();
        //
        TabSheet tabsheet= new TabSheet();
		tabsheet.setSizeFull();
		tabsheet.addTab(columnList,"COLUMN");
		tabsheet.addTab(primaryKeyList,"PRIMARY KEY");
		tabsheet.addTab(indexList,"INDEX");
		content.addComponent(tabsheet);
        //
        content.setExpandRatio(tabsheet, 1f);
        //
        tablesList.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				TableInfo ti=tablesList.getItemValue(event.getItem());
				loadOthers(ti.name);
			}
		});
        //
        loadTable();
    }
	//
	private void loadTable(){
		String jdbcUrl="jdbc:mysql://"+instance.getMachine().getPublicHost()
				+":"+instance.port+"/"
				+instance.id
				+"?useUnicode=true&characterEncoding=UTF-8";
		String user=instance.getUser();
		String pwd=instance.getPassword();
		try {
			DatabaseInfo dbInfo= JdbcUtil.getDatabaseInfo(jdbcUrl, user, pwd);
			setCaption(instance.id+" ("+dbInfo.databaseProductName+"-"
										+dbInfo.databaseProductVersion+")");
			List<TableInfo>list=JdbcUtil.getTables(jdbcUrl, user, pwd);
			tablesList.setData(list);
		} catch (Exception e) {
			e.printStackTrace();
			DeploySystemUI.showNotificationInfo("ERROR",e.getMessage());
		}
		
	}
	//
	private void loadOthers(String table){
		String jdbcUrl="jdbc:mysql://"+instance.getMachine().getPublicHost()
				+":"+instance.port+"/"
				+instance.id
				+"?useUnicode=true&characterEncoding=UTF-8";
		String user=instance.getUser();
		String pwd=instance.getPassword();
		try {
			List<ColumnInfo>clist=JdbcUtil.getColumns(jdbcUrl, user, pwd, table);
			columnList.setData(clist);
			//
			List<PrimaryKeyInfo>plist=JdbcUtil.getPrimaryKeys(jdbcUrl, user, pwd, table);
			primaryKeyList.setData(plist);
			//
			List<IndexInfo>ilist=JdbcUtil.getIndexs(jdbcUrl, user, pwd, table);
			indexList.setData(ilist);
		} catch (Exception e) {
			e.printStackTrace();
			DeploySystemUI.showNotificationInfo("ERROR",e.getMessage());
		}
	}	
}
