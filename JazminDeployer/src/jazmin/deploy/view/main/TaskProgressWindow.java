/**
 * 
 */
package jazmin.deploy.view.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import jazmin.deploy.ui.BeanTable;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.Responsive;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
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
	BeanTable<Task> table;
	List<Task>tasks;
	Label label;
	Button cancel;
	Button ok;
	Map<String,Task>taskMap;
	boolean isCancel;
	//
	public TaskProgressWindow(Consumer<TaskProgressWindow>consumer) {
		Responsive.makeResponsive(this);
		isCancel=false;
		tasks=new ArrayList<TaskProgressWindow.Task>();
		taskMap=new HashMap<String, TaskProgressWindow.Task>();
        setCaption("Task");
        setWidth("600px");
        center();
        setCloseShortcut(KeyCode.ESCAPE, null);
        setResizable(true);
        setClosable(true);
        setHeight(90.0f, Unit.PERCENTAGE);
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        setContent(content);
        table = new BeanTable<TaskProgressWindow.Task>("",Task.class);
		content.addComponent(table);
		table.setSizeFull();
        content.addComponent(table);
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
        ok.addStyleName(ValoTheme.BUTTON_PRIMARY); 
        ok.addClickListener(e->{
         	ok.setEnabled(false);
        	consumer.accept(this);
        });
        ok.focus();
        footer.addComponent(ok);
        footer.setComponentAlignment(ok, Alignment.TOP_RIGHT);
        cancel = new Button("Cancel");
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
    }
	//
	public void setInfo(String info){
		label.setValue("<h3 style='text-align:center'>"+info+"</h3>");
	}
	//
	public void updateTask(String name,String progress){
		taskMap.get(name).progress=progress;
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
