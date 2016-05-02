/**
 * 
 */
package jazmin.deploy.view.instance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.vaadin.aceeditor.AceEditor;
import org.vaadin.aceeditor.AceMode;
import org.vaadin.aceeditor.AceTheme;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.Responsive;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import jazmin.deploy.DeploySystemUI;
import jazmin.deploy.domain.Instance;
import jazmin.deploy.ui.BeanTable;
import jazmin.util.JdbcUtil;
import jazmin.util.JdbcUtil.ColumnInfo;
import jazmin.util.JdbcUtil.DatabaseInfo;
import jazmin.util.JdbcUtil.TableInfo;

/**
 * @author yama
 * 6 Jan, 2015
 */
@SuppressWarnings("serial")
public class InstanceMySQLTableDomainWindow extends Window{
	private Instance instance;
	private BeanTable<JdbcUtil.TableInfo> tablesList;
	private Panel panel;
	private AceEditor editor;
	private AceEditor daoEditor;
	//
	public InstanceMySQLTableDomainWindow(Instance instance) {
		this.instance=instance;
        Responsive.makeResponsive(this);
        setCaption(instance.id+" tables");
        setWidth(90.0f, Unit.PERCENTAGE);
        setHeight(90.0f, Unit.PERCENTAGE);
        center();
        setCloseShortcut(KeyCode.ESCAPE, null);
        setResizable(true);
        setClosable(true);
        //
        panel=new Panel();
		panel.setStyleName(ValoTheme.PANEL_BORDERLESS);
		panel.setSizeFull();
		//
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        setContent(panel);
        tablesList = new BeanTable<JdbcUtil.TableInfo>("", JdbcUtil.TableInfo.class);
        tablesList.setWidth("100%");
        tablesList.setHeight("200px");
        content.addComponent(tablesList);
        //
        editor= new AceEditor();
        editor.setThemePath("/ace");
        editor.setModePath("/ace");
        editor.setWorkerPath("/ace"); 
        editor.setUseWorker(true);
        editor.setTheme(AceTheme.eclipse);
        editor.setMode(AceMode.java);
        editor.setWidth(100.0f, Unit.PERCENTAGE);
        //
        daoEditor=new AceEditor();
        daoEditor.setThemePath("/ace");
        daoEditor.setModePath("/ace");
        daoEditor.setWorkerPath("/ace"); 
        daoEditor.setUseWorker(true);
        daoEditor.setTheme(AceTheme.eclipse);
        daoEditor.setMode(AceMode.java);
        daoEditor.setWidth(100.0f, Unit.PERCENTAGE);
        HorizontalLayout hl=new HorizontalLayout(editor,daoEditor);
        hl.setExpandRatio(editor,0.5f);
        content.addComponent(hl);
        panel.setContent(content);
        //
        tablesList.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				TableInfo ti=tablesList.getItemValue(event.getItem());
				loadDomain(ti.name);
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
	private void loadDomain(String table){
		String jdbcUrl="jdbc:mysql://"+instance.getMachine().getPublicHost()
				+":"+instance.port+"/"
				+instance.id
				+"?useUnicode=true&characterEncoding=UTF-8";
		String user=instance.getUser();
		String pwd=instance.getPassword();
		try {
			List<ColumnInfo>clist=JdbcUtil.getColumns(jdbcUrl, user, pwd, table);
			StringBuilder sb=new StringBuilder();
			//
			StringBuffer propertiesString=new StringBuffer();
			Set<String> includeString=new TreeSet<String>();
			for(ColumnInfo ci:clist){
				String type=typeMap.get(ci.type);
				if(type==null){
					type="UNKNOW";
				}
				if((ci.type.equals("TINYINT")||ci.type.equals("TINYINT UNSIGNED"))
						&&ci.name.startsWith("is_")){
					type="boolean";
				}
				if(ci.remarks!=null&&!ci.remarks.isEmpty()){
					propertiesString.append("\t/**"+ci.remarks+"*/\n");
				}
				propertiesString.append("\tpublic "+type+" "+getDomain(ci.name,false)+";\n");
				String include=includeMap.get(type);
				if(include!=null){
					includeString.add(include);
				}
			}
			//
			includeString.forEach(s->{
					sb.append("import "+s+";\n");
				});
			sb.append("/**\n*\n*/\n");
			sb.append("public class ");
			sb.append(getDomain(table,true));
			sb.append("{\n");
			sb.append(propertiesString);
			sb.append("}");
			//
			editor.setValue(sb.toString());
			//DAO
			String className=getDomain(table,true);
			StringBuilder daoSB=new StringBuilder();
			daoSB.append("import jazmin.driver.jdbc.QueryTerms;\n");
			daoSB.append("import jazmin.driver.jdbc.SmartBeanDAO;\n\n");
			daoSB.append("public class "+className+"DAO extends SmartBeanDAO<"+className+">{\n");
			daoSB.append("\n");
			daoSB.append("\t").append("public int add").append(className).append("(").append(className).append(" bean){\n");
			daoSB.append("\t\t").append("bean.updateTime = new Date();\n");
			daoSB.append("\t\t").append("bean.createTime = new Date();\n");
			daoSB.append("\t\t").append("return insert(bean, true, \"id\");\n");
			daoSB.append("\t}\n\n");
			//
			daoSB.append("\t").append("public void update").append(className).append("(").append(className).append(" bean){\n");
			daoSB.append("\t\t").append("bean.updateTime = new Date();\n");
			daoSB.append("\t\t").append("update(bean, QueryTerms.create().where(\"id\",bean.id),");
			daoSB.append("\"createTime\", \"id\");\n");	
			daoSB.append("\t}\n\n");
			//
			daoSB.append("\n}");
			daoEditor.setValue(daoSB.toString());
		} catch (Exception e) {
			e.printStackTrace();
			DeploySystemUI.showNotificationInfo("ERROR",e.getMessage());
		}
	}
	
	//
	private static Map<String,String>includeMap=new HashMap<String, String>();
	static{
		includeMap.put("String","java.lang.String");
		includeMap.put("Date","java.util.Date");
		includeMap.put("BigDecimal","java.math.BigDecimal");
	}
	//
	private static Map<String,String>typeMap=new HashMap<String, String>();
	static{
		typeMap.put("VARCHAR","String");
		typeMap.put("CHAR","String");
		typeMap.put("TINYTEXT","String");
		typeMap.put("TEXT","String");
		typeMap.put("MEDIUMTEXT","String");
		typeMap.put("LONGTEXT","String");
		
		typeMap.put("BLOB","byte[]");
		typeMap.put("MEDIUMBLOB","byte[]");
		typeMap.put("LONGBLOB","byte[]");
		typeMap.put("BINARY","byte[]");
		
		typeMap.put("INT","int");
		typeMap.put("INTEGER","int");
		typeMap.put("MEDIUMINT","int");
		typeMap.put("SMALLINT","short");
		typeMap.put("TINYINT","short");
		typeMap.put("BIGINT","BigDecimal");
		typeMap.put("INT UNSIGNED","int");
		typeMap.put("INTEGER UNSIGNED","int");
		typeMap.put("MEDIUMINT UNSIGNED","int");
		typeMap.put("SMALLINT UNSIGNED","short");
		typeMap.put("TINYINT UNSIGNED","short");
		typeMap.put("BIGINT UNSIGNED","BigDecimal");
		typeMap.put("FLOAT","float");
		typeMap.put("DOUBLE","double");
		typeMap.put("FLOAT UNSIGNED","float");
		typeMap.put("DOUBLE UNSIGNED","double");
		typeMap.put("DOUBLE","double");
		
		typeMap.put("DATE","Date");
		typeMap.put("TIME","Date");
		typeMap.put("YEAR","Date");
		typeMap.put("DATETIME","Date");
		typeMap.put("TIMESTAMP","Date");
	}
	
	//
	private static String getDomain(String tableItem,boolean firstUpper){
		if(tableItem.startsWith("t_")){
			tableItem=tableItem.substring(2);
		}
		StringBuffer result=new StringBuffer();
		boolean nextUpper=false;
		for(int i=0;i<tableItem.length();i++){
			if(i==0&&firstUpper){
				result.append((tableItem.charAt(i)+"").toUpperCase());
				continue;
			}
			if(tableItem.charAt(i)=='_'){
				nextUpper=true;
				continue;
			}
			if(nextUpper){
				nextUpper=false;
				result.append((tableItem.charAt(i)+"").toUpperCase());
			}else{
				result.append(tableItem.charAt(i));
			}
		}
		return result.toString();
	}
}
