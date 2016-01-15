/**
 * 
 */
package jazmin.deploy.view.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.vaadin.aceeditor.AceEditor;
import org.vaadin.aceeditor.AceMode;
import org.vaadin.aceeditor.AceTheme;

import jazmin.deploy.ui.BeanTable;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.Responsive;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

/**
 * @author yama
 * 6 Jan, 2015
 */
@SuppressWarnings("serial")
public class TaskProgressWindow extends Window{
	//
	public static class Task{
		public int id;
		public String name;
		public String progress;
	}
	StringBuilder logs;
	BeanTable<Task> table;
	List<Task>tasks;
	Label label;
	Button cancel;
	Button ok;
	Map<String,Task>taskMap;
	boolean isCancel;
	AceEditor editor;
	//
	public TaskProgressWindow(Consumer<TaskProgressWindow>consumer) {
		Responsive.makeResponsive(this);
		logs=new StringBuilder();
		isCancel=false;
		tasks=new ArrayList<TaskProgressWindow.Task>();
		taskMap=new HashMap<String, TaskProgressWindow.Task>();
        setCaption("Task");
        setWidth("800px");
        center();
        setCloseShortcut(KeyCode.ESCAPE, null);
        setResizable(true);
        setClosable(true);
        setHeight(90.0f, Unit.PERCENTAGE);
        
        TabSheet  tabsheet= new TabSheet();
      	tabsheet.setSizeFull();
      	setContent(tabsheet);
      	
        
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        table = new BeanTable<TaskProgressWindow.Task>("",Task.class);
		content.addComponent(table);
		table.setSizeFull();
        //
        label=new Label();
        label.setContentMode(ContentMode.HTML);
        label.setWidth("100%");
        content.addComponent(label);
        //
        content.setExpandRatio(table, 1f);
     
        HorizontalLayout footer = new HorizontalLayout();
        footer.setSpacing(true);
        footer.addStyleName(ValoTheme.WINDOW_BOTTOM_TOOLBAR);
        footer.setWidth(100.0f, Unit.PERCENTAGE);
        //
        Label empty=new Label();
        footer.addComponent(empty);
        footer.setExpandRatio(empty, 1);
        //
        ok = new Button("OK");
        ok.addStyleName(ValoTheme.BUTTON_SMALL);
        ok.addStyleName(ValoTheme.BUTTON_PRIMARY); 
        ok.addClickListener(e->{
         	ok.setEnabled(false);
        	consumer.accept(this);
        });
        ok.focus();
        footer.addComponent(ok);
        footer.setComponentAlignment(ok, Alignment.TOP_RIGHT);
        cancel = new Button("Cancel");
        cancel.addStyleName(ValoTheme.BUTTON_SMALL);
        cancel.addClickListener(e->{
        	if(ok.isEnabled()){
        		close();
        		return;
        	}
        	cancel.setEnabled(false);
        	setInfo("Try to cancel task...");
        	isCancel=true;
        });
        cancel.focus();
        footer.addComponent(cancel);
        footer.setComponentAlignment(cancel, Alignment.TOP_RIGHT);
        //
        content.addComponent(footer);
        //
      
        //
        editor= new AceEditor();
        editor.setThemePath("/ace");
        editor.setModePath("/ace");
        editor.setWorkerPath("/ace"); 
        editor.setMode(AceMode.sql);
        editor.setShowPrintMargin(false);
        editor.setUseWorker(true);
        editor.setTheme(AceTheme.eclipse);
        editor.setMode(AceMode.sh);
        editor.setFontSize("10px");
        editor.setSizeFull();
        //
        tabsheet.addTab(content, "Tasks");
        tabsheet.addTab(editor, "Log"); 
	}
	//
	public void finish(){
		ok.setEnabled(true);
		cancel.setEnabled(true);
	}
	//
	public void appendLog(String log){
		logs.append(log);
		editor.setValue(logs.toString());
		editor.scrollToPosition(editor.getValue().length()-1);
	}
	//
	public void setInfo(String info){
		label.setValue("<h3 style='text-align:center'>"+info+"</h3>");
	}
	//
	public void updateTask(String name,String progress){
		taskMap.get(name).progress=progress;
		appendLog(">>>"+name+"<<<"+progress+"\n");
		reload();
	}
	//
	public Task addTask(String name,String progress){
		Task t=new Task();
		t.id=tasks.size()+1;
		t.name=name;
		t.progress=progress+"";
		tasks.add(t);
		taskMap.put(t.name,t);
		reload();
		return t;
	}
	//
	public void reload(){
		table.setData(tasks);
	}
	//
	/**
	 * @return the isCancel
	 */
	public boolean isCancel() {
		return isCancel;
	}
	
}
