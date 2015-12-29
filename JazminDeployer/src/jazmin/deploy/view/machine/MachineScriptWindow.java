/**
 * 
 */
package jazmin.deploy.view.machine;

import java.io.IOException;

import jazmin.deploy.DeploySystemUI;
import jazmin.deploy.domain.DeployManager;
import jazmin.deploy.domain.Script;
import jazmin.deploy.ui.BeanTable;
import jazmin.deploy.view.main.InputWindow;

import org.vaadin.aceeditor.AceEditor;
import org.vaadin.aceeditor.AceMode;
import org.vaadin.aceeditor.AceTheme;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.Responsive;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

/**
 * @author yama
 * 6 Jan, 2015
 */
@SuppressWarnings("serial")
public class MachineScriptWindow extends Window{

	BeanTable<Script> table;
	AceEditor editor ;
	Script currentScript;
	//
	public MachineScriptWindow() {
		Responsive.makeResponsive(this);
		setCaption("Scripts");
        setWidth(90.0f, Unit.PERCENTAGE);
        setHeight(90.0f, Unit.PERCENTAGE);
        center();
        setCloseShortcut(KeyCode.ESCAPE, null);
        setResizable(true);
        setClosable(true);
       
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        setContent(content);
        table = new BeanTable<Script>("",Script.class);
		content.addComponent(table);
		table.setWidth("100%");
		table.setHeight("200px");
        content.addComponent(table);
        //
        editor= new AceEditor();
		editor.setThemePath("/ace");
		editor.setModePath("/ace");
		editor.setWorkerPath("/ace");
		editor.setMode(AceMode.sh);
		editor.setShowPrintMargin(false);
		editor.setUseWorker(true);
		editor.setTheme(AceTheme.eclipse);
		editor.setSizeFull();
		content.addComponent(editor);
        //
        content.setExpandRatio(editor, 1f);
     
        HorizontalLayout footer = new HorizontalLayout();
        footer.setSpacing(true);
        footer.addStyleName(ValoTheme.WINDOW_BOTTOM_TOOLBAR);
        footer.setWidth(100.0f, Unit.PERCENTAGE);
        //
        Label empty=new Label();
        footer.addComponent(empty);
        footer.setExpandRatio(empty, 1);
        //
        Button newBtn = new Button("New"); 
        newBtn.addClickListener(e->newScript());
        footer.addComponent(newBtn);
        footer.setComponentAlignment(newBtn, Alignment.TOP_LEFT);
        //
        //
        Button deleteBtn = new Button("Delete"); 
        deleteBtn.addClickListener(e->deleteScript());
        deleteBtn.addStyleName(ValoTheme.BUTTON_DANGER); 
        footer.addComponent(deleteBtn);
        footer.setComponentAlignment(deleteBtn, Alignment.TOP_LEFT);
        //
        Button save = new Button("Save");
        save.setClickShortcut(KeyCode.S,ShortcutAction.ModifierKey.META);
        save.addStyleName(ValoTheme.BUTTON_PRIMARY); 
        save.addClickListener(e->save());
        footer.addComponent(save);
        footer.setComponentAlignment(save, Alignment.TOP_RIGHT);
        //
        content.addComponent(footer);
        //
        table.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				Script script=table.getItemValue(event.getItem());
				showScriptContent(script);
			}
		});
        //
        loadData();
    }
	//
	private void showScriptContent(Script script){
		try {
			currentScript=script;
			editor.setValue(DeployManager.getScript(script.name));
		} catch(Exception e) {
			DeploySystemUI.showNotificationInfo("ERROR", e.getMessage());
		}
	}
	//
	private void loadData(){
		table.setData(DeployManager.getScripts());
	}
	//
	private void deleteScript(){
		Script script=table.getSelectValue();
		if(script==null){
			DeploySystemUI.showNotificationInfo("INFO", "Choose script to delete");
			return;
		}
		DeployManager.deleteScript(script.name);
		loadData();
	}
	//
	private void newScript(){
		final InputWindow sw=new InputWindow(window->{
			String name=window.getInputValue();
			try {
				DeployManager.saveScript(name,"");
				DeploySystemUI.showNotificationInfo("INFO", "Create complete");
				loadData();
				window.close();
			} catch (Exception e) {
				DeploySystemUI.showNotificationInfo("ERROR", e.getMessage());
			}
		});
		sw.setCaption("Input script name");
		sw.setInfo("Input script name");
		UI.getCurrent().addWindow(sw);
	}
	//
	private void save(){
		if(currentScript==null){
			return;
		}
		try {
			DeployManager.saveScript(currentScript.name,editor.getValue());
			DeploySystemUI.showNotificationInfo("INFO", "Save complete");
		} catch (IOException e) {
			DeploySystemUI.showNotificationInfo("ERROR", e.getMessage());
		}
	}
}
