/**
 * 
 */
package jazmin.deploy.view.machine;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jazmin.deploy.DeploySystemUI;
import jazmin.deploy.domain.Machine;
import jazmin.deploy.manager.DeployManager;

import org.vaadin.aceeditor.AceEditor;
import org.vaadin.aceeditor.AceMode;
import org.vaadin.aceeditor.AceTheme;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Responsive;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

/**
 * @author yama
 * 6 Jan, 2015
 */
@SuppressWarnings("serial")
public class MachineRunCmdWindow extends Window{
	TextField cmdField;
	TextField scriptField;
	List<Machine>machines;
	CheckBox cmdRootCheckBox;
	CheckBox scriptRootCheckBox;
	Map<String,AceEditor>editorMap;
	TabSheet tabsheet;
	//
	public MachineRunCmdWindow(List<Machine>machines){
		this.machines=machines;
	    Responsive.makeResponsive(this);
        setCaption("Run cmd");
        setWidth(90.0f, Unit.PERCENTAGE);
        setHeight(90.0f, Unit.PERCENTAGE);
        center();
        setCloseShortcut(KeyCode.ESCAPE, null);
        setResizable(true);
        setClosable(true);
        //
        cmdField=new TextField("cmd");
        cmdField.setWidth("100%");
        cmdField.setIcon(FontAwesome.ANCHOR);
        cmdField.setWidth(100.0f, Unit.PERCENTAGE);
        cmdField.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
        scriptField=new TextField("Script");
        scriptField.setWidth("100%");
        scriptField.setIcon(FontAwesome.ADJUST);
        scriptField.setWidth(100.0f, Unit.PERCENTAGE);
        scriptField.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
        Button cmdBtn = new Button("Run");
        cmdBtn.addStyleName(ValoTheme.BUTTON_SMALL);
        cmdBtn.addStyleName(ValoTheme.BUTTON_DANGER);
        cmdRootCheckBox=new CheckBox("Root");
        HorizontalLayout cmdLayout=new HorizontalLayout(cmdField,cmdRootCheckBox,cmdBtn);
        cmdLayout.setExpandRatio(cmdField,1);
        cmdLayout.setSpacing(true);
        cmdLayout.setWidth("100%");
        cmdLayout.setComponentAlignment(cmdRootCheckBox, Alignment.BOTTOM_LEFT);
        cmdLayout.setComponentAlignment(cmdBtn, Alignment.BOTTOM_RIGHT);
        //
        cmdBtn.addClickListener((e)->runCmd());
        //
        Button scriptBtn = new Button("Run");
        scriptBtn.addStyleName(ValoTheme.BUTTON_SMALL);
        scriptBtn.addStyleName(ValoTheme.BUTTON_DANGER);
        scriptBtn.addClickListener((e)->runScript());
        scriptRootCheckBox=new CheckBox("Root");
        HorizontalLayout scriptLayout=new HorizontalLayout(scriptField,scriptRootCheckBox,scriptBtn);
        scriptLayout.setExpandRatio(scriptField,1);
        scriptLayout.setSpacing(true);
        scriptLayout.setWidth("100%");
        scriptLayout.setComponentAlignment(scriptRootCheckBox, Alignment.BOTTOM_LEFT);
        scriptLayout.setComponentAlignment(scriptBtn, Alignment.BOTTOM_RIGHT);
        
        VerticalLayout inputLayout=new VerticalLayout(cmdLayout,scriptLayout);
        inputLayout.setSpacing(true);
        inputLayout.addStyleName(ValoTheme.WINDOW_TOP_TOOLBAR);
        inputLayout.setWidth(100.0f, Unit.PERCENTAGE);
        tabsheet= new TabSheet();
		tabsheet.setSizeFull();
		editorMap=new HashMap<String, AceEditor>();
		for (Machine machine : machines) {
			AceEditor editor = new AceEditor();
			editor.setThemePath("/ace");
			editor.setModePath("/ace");
			editor.setWorkerPath("/ace");
			editor.setMode(AceMode.sh);
			editor.setShowPrintMargin(false);
			editor.setUseWorker(true);
			editor.setTheme(AceTheme.eclipse);
			editor.setSizeFull();
			editor.setReadOnly(true);
			editor.setShowGutter(false);
			editorMap.put(machine.id, editor);
			tabsheet.addTab(editor, machine.id);
		}
        //
        VerticalLayout content=new VerticalLayout(inputLayout,tabsheet);
	    content.setExpandRatio(tabsheet,1);
	    content.setSizeFull();
        setContent(content);
	}
	//
	private void runCmd(){
		String cmd=cmdField.getValue();
		if(cmd==null||cmd.trim().isEmpty()){
			DeploySystemUI.showNotificationInfo("INFO","cmd can not be null");
			return;
		}
		boolean isRoot=cmdRootCheckBox.getValue();
		for(Machine machine:machines){
			AceEditor editor=editorMap.get(machine.id);
			getUI().access(()->{
				tabsheet.setSelectedTab(editor);
			});
			String result=DeployManager.runCmdOnMachine(machine,isRoot,cmd);
			getUI().access(()->{
				editor.setReadOnly(false);
				editor.setValue(result);
				editor.setReadOnly(true);
			});
			
		}
	}
	//
	private void runScript(){
		String script=scriptField.getValue();
		if(script==null||script.trim().isEmpty()){
			DeploySystemUI.showNotificationInfo("INFO","script can not be null");
			return;
		}
		boolean isRoot=scriptRootCheckBox.getValue();
		if(!DeployManager.existsScript(script)){
			DeploySystemUI.showNotificationInfo("INFO","can not find script:"+script);
			return;
		}
		for(Machine machine:machines){
			AceEditor editor=editorMap.get(machine.id);
			getUI().access(()->{
				tabsheet.setSelectedTab(editor);
			});
			String cmd;
			try {
				cmd = DeployManager.getScript(script);
			} catch (IOException e) {
				DeploySystemUI.showNotificationInfo("ERROR",e.getMessage());
				continue;
			}
			String result=DeployManager.runScriptOnMachine(machine,isRoot,cmd);
			getUI().access(()->{
				editor.setReadOnly(false);
				editor.setValue(result);
				editor.setReadOnly(true);
			});
			
		}
	}
}
