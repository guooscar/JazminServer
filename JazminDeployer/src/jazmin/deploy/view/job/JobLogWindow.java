/**
 * 
 */
package jazmin.deploy.view.job;

import java.util.ArrayList;
import java.util.List;

import org.vaadin.aceeditor.AceEditor;
import org.vaadin.aceeditor.AceMode;
import org.vaadin.aceeditor.AceTheme;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.Responsive;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import jazmin.deploy.DeploySystemUI;
import jazmin.deploy.manager.DeployManager;
import jazmin.deploy.ui.BeanTable;

/**
 * @author yama
 * 6 Jan, 2015
 */
@SuppressWarnings("serial")
public class JobLogWindow extends Window{
	//
	public static class JobLogName{
		public JobLogName() {
		}
		public JobLogName(String name) {
			this.name=name;
		}
		public String name;
	}
	//
	BeanTable<JobLogName> table;
	AceEditor editor ;
	String jobId;
	//
	public JobLogWindow(String jobId) {
		this.jobId=jobId;
		Responsive.makeResponsive(this);
		setCaption("JobLog-"+jobId);
        setWidth(90.0f, Unit.PERCENTAGE);
        setHeight(90.0f, Unit.PERCENTAGE);
        center();
        setCloseShortcut(KeyCode.ESCAPE, null);
        setResizable(true);
        setClosable(true);
       
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        setContent(content);
        table = new BeanTable<JobLogName>(null,JobLogName.class);
		content.addComponent(table);
		table.setWidth("300px");
		table.setHeight("100%");
		//
        editor= new AceEditor();
		editor.setThemePath("/ace");
		editor.setModePath("/ace");
		editor.setWorkerPath("/ace");
		editor.setMode(AceMode.sh);
		editor.setShowPrintMargin(false);
		editor.setUseWorker(true);
		editor.setTheme(AceTheme.eclipse);
		editor.setWidth("100%");
		editor.setHeight("100%");
		HorizontalLayout topLayout=new HorizontalLayout(table,editor);
		topLayout.setExpandRatio(editor,1);
		topLayout.setSizeFull();
		content.addComponent(topLayout);
        content.setExpandRatio(topLayout, 1f);
        HorizontalLayout footer = new HorizontalLayout();
        footer.setSpacing(true);
        footer.addStyleName(ValoTheme.WINDOW_BOTTOM_TOOLBAR);
        footer.setWidth(100.0f, Unit.PERCENTAGE);
        //
        Label empty=new Label();
        footer.addComponent(empty);
        footer.setExpandRatio(empty, 1);
        //
        //
        Button deleteBtn = new Button("Delete"); 
        deleteBtn.addStyleName(ValoTheme.BUTTON_SMALL);
        deleteBtn.addClickListener(e->deleteScript());
        deleteBtn.addStyleName(ValoTheme.BUTTON_DANGER); 
        footer.addComponent(deleteBtn);
        footer.setComponentAlignment(deleteBtn, Alignment.TOP_LEFT);
        content.addComponent(footer);
        //
        table.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				JobLogName script=table.getItemValue(event.getItem());
				showLogContent(script);
			}
		});
        //
        loadData();
    }
	//
	private void showLogContent(JobLogName script){
		try {
			editor.setValue(DeployManager.getJobLog(jobId, script.name));
		} catch(Exception e) {
			DeploySystemUI.showNotificationInfo("ERROR", e.getMessage());
		}
	}
	//
	private void loadData(){
		List<JobLogName>names=new ArrayList<>();
		DeployManager.getJobLogNames(jobId).forEach(c->names.add(new JobLogName(c)));
		table.setBeanData(names);
		editor.setValue("");
	}
	//
	private void deleteScript(){
		JobLogName script=table.getSelectValue();
		if(script==null){
			DeploySystemUI.showNotificationInfo("INFO", "Choose log to delete");
			return;
		}
		DeployManager.deleteJobLog(jobId,script.name);
		loadData();
	}
	
}
