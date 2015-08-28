/**
 * 
 */
package jazmin.deploy.view.instance;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

import jazmin.deploy.DeploySystemUI;
import jazmin.deploy.domain.Instance;
import jazmin.deploy.domain.JdbcUtil;

import org.vaadin.aceeditor.AceEditor;
import org.vaadin.aceeditor.AceEditor.SelectionChangeEvent;
import org.vaadin.aceeditor.AceEditor.SelectionChangeListener;
import org.vaadin.aceeditor.AceMode;
import org.vaadin.aceeditor.AceTheme;
import org.vaadin.aceeditor.TextRange;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.Responsive;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

/**
 * @author yama
 * 6 Jan, 2015
 */
@SuppressWarnings("serial")
public class InstanceMySQLWindow extends Window{
	Table table;
	TextArea textArea;
	AceEditor editor;
	TextRange selectRange;
	Label label;
	TabSheet tabsheet;
	Instance instance;
	List<String>currentHeaders;
	//
	public InstanceMySQLWindow(Instance instance){
		currentHeaders=new ArrayList<String>();
		this.instance=instance;
        Responsive.makeResponsive(this);
        setCaption(instance.id+" mysql console");
        setWidth(90.0f, Unit.PERCENTAGE);
        center();
        setCloseShortcut(KeyCode.ESCAPE, null);
        setResizable(true);
        setClosable(true);
        setHeight(90.0f, Unit.PERCENTAGE);
        //
    	HorizontalLayout optLayout = new HorizontalLayout();
		optLayout.setSpacing(true);
		optLayout.addStyleName(ValoTheme.WINDOW_TOP_TOOLBAR);
		optLayout.setWidth(100.0f, Unit.PERCENTAGE);
		Button runBtn = new Button("Run(Alt-R)");
		runBtn.addStyleName(ValoTheme.BUTTON_SMALL);
	    optLayout.addComponent(runBtn);
	    runBtn.addClickListener(e->loadData());
	    runBtn.setClickShortcut(KeyCode.R,ShortcutAction.ModifierKey.ALT);
	    //
	    label=new Label();
	    label.setWidth("100%");
	    optLayout.addComponent(label);
	    optLayout.setExpandRatio(label, 1);
        //
        editor= new AceEditor();
        editor.setThemePath("/ace");
        editor.setModePath("/ace");
        editor.setWorkerPath("/ace"); 
        editor.setMode(AceMode.sql);
        editor.setShowPrintMargin(false);
        editor.setUseWorker(true);
        editor.setTheme(AceTheme.eclipse);
        editor.setMode(AceMode.sql);
        editor.setSizeFull();
        editor.addSelectionChangeListener(new SelectionChangeListener() {
			@Override
			public void selectionChanged(SelectionChangeEvent e) {
				selectRange=e.getSelection();
			}
		});
        //
        table=new Table();
        table.addStyleName(ValoTheme.TABLE_NO_HORIZONTAL_LINES);
        table.addStyleName(ValoTheme.TABLE_COMPACT);
        table.addStyleName(ValoTheme.TABLE_SMALL);
        table.setSizeFull();
        table.setFooterVisible(false);
        table.setImmediate(true);
        table.setSelectable(true);
        table.addContainerProperty("",String.class,"");
        table.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            private static final long serialVersionUID = 2068314108919135281L;
            public void itemClick(ItemClickEvent event) {
                if (event.isDoubleClick()) {
                	Item item=event.getItem();
                	List<String> values=new ArrayList<String>();
                	for(String s:currentHeaders){
                		values.add(item.getItemProperty(s).getValue()+"");		
                	}
                	//
                	InstanceMySQLRowWindow bfw=new InstanceMySQLRowWindow(currentHeaders,values);
        			UI.getCurrent().addWindow(bfw);
        			bfw.focus();
                }
            }
        });
        //
        textArea=new TextArea();
        textArea.setSizeFull();
        //
        tabsheet= new TabSheet();
		tabsheet.setSizeFull();
		tabsheet.addTab(table,"TABLE");
		tabsheet.addTab(textArea,"TXT");
	    //
        VerticalLayout vl=new VerticalLayout(optLayout,editor);
        vl.setSizeFull();
        vl.setExpandRatio(editor, 1.0f);
        VerticalSplitPanel content = new VerticalSplitPanel(vl,tabsheet);
        content.setLocked(true);
        content.setSplitPosition(25, Unit.PERCENTAGE);
        content.setSizeFull();
        setContent(content);
        //
	}
	//
	private void loadData(){
		String sql="";
		if(selectRange==null){
			sql=editor.getValue();
		}else{
			if(selectRange.isZeroLength()){
				sql=editor.getValue();
			}else{
				int v1=selectRange.getStart();
				int v2=selectRange.getEnd();
				sql=editor.getValue().substring(Math.min(v1,v2), Math.max(v1,v2));
			}
		}
		if(sql.isEmpty()){
			return;
		}
		label.setValue("running...");
		try {
			String jdbcUrl="jdbc:mysql://"+instance.getMachine().getPublicHost()
					+":"+instance.port+"/"
					+instance.id
					+"?useUnicode=true&characterEncoding=UTF-8";
			String user=instance.getUser();
			String pwd=instance.getPassword();
			if(sql.startsWith("select")||sql.startsWith("show")){
				JdbcUtil.executeQuery(jdbcUrl,user,pwd,sql,this::setupData);		
			}else if(sql.startsWith("update")||sql.startsWith("delete")){
				int r=JdbcUtil.executeUpdate(jdbcUrl,user,pwd,sql);		
				label.setValue("Execute complete.Total effect "+r+" records");
				DeploySystemUI.showNotificationInfo("Info","Execute complete.Total effect "+r+" records");
			}else{
				boolean r=JdbcUtil.execute(jdbcUrl,user,pwd,sql);	
				label.setValue("Execute complete.Return "+r);
				DeploySystemUI.showNotificationInfo("Info","Execute complete.Return "+r);
			}
		} catch (Exception e) {
			e.printStackTrace();
			label.setValue("");
			DeploySystemUI.showNotificationInfo("ERROR",e.getMessage());
		}
	}
	//

	public int exportTxt(ResultSetMetaData metaData,ResultSet rs)
			throws Exception{
		StringWriter sw=new StringWriter();
		PrintWriter pw=new PrintWriter(sw);
		StringBuilder format=new StringBuilder();
		Object columnNames[]=new Object[metaData.getColumnCount()];
		for(int i=1;i<=metaData.getColumnCount();i++){
			int size=metaData.getColumnDisplaySize(i);
			format.append("%"+size+"s ");
			columnNames[i-1]=metaData.getColumnLabel(i);
		}
		format.append("\n");
		pw.format(format.toString(), columnNames);
		int count=0;
		while (rs.next()) {
			count++;
			Object columnValues[]=new Object[metaData.getColumnCount()];
			for(int i=1;i<=metaData.getColumnCount();i++){
				columnValues[i-1]=rs.getString(i);
			}
			pw.format(format.toString(), columnValues);
		}
		textArea.setValue(sw.toString());
		return count;
	}
	public int  exportTable(ResultSetMetaData metaData,ResultSet rs) throws Exception{
		table.removeAllItems();
		List<Object> ids = new ArrayList<Object>(table.getContainerPropertyIds());
		for (Object id : ids) {
			table.removeContainerProperty(id);
		}
		currentHeaders.clear();
		for (int i = 1; i <= metaData.getColumnCount(); i++) {
			String s = metaData.getColumnLabel(i);
			table.addContainerProperty(s, String.class, "");
			currentHeaders.add(s);
		}
		//
		int count = 0;
		while (rs.next()) {
			count++;
			Object newItemId = table.addItem();
			Item row = table.getItem(newItemId);
			for (int i = 1; i <= metaData.getColumnCount(); i++) {
				String s = metaData.getColumnLabel(i);
				@SuppressWarnings("unchecked")
				Property<Object> p = row.getItemProperty(s);
				String v = rs.getString(s);
				p.setValue(v + "");
			}
		}
		return count;

	}
	//
	private void setupData(ResultSetMetaData metaData,ResultSet rs){
		try{
			int count=0;
			if(tabsheet.getSelectedTab()==table){
				count=exportTable(metaData, rs);
			}else{
				count=exportTxt(metaData, rs);
			}
			label.setValue("Execute Complete.Total "+count+" records");
			DeploySystemUI.showNotificationInfo("Info","Execute complete.Total "+count+" records");
		}catch(Exception e){
			e.printStackTrace();
			DeploySystemUI.showNotificationInfo("ERROR",e.getMessage());
		}
	}
	
}
