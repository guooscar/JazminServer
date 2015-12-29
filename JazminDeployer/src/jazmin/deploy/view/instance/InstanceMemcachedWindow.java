/**
 * 
 */
package jazmin.deploy.view.instance;

import jazmin.deploy.DeploySystemUI;
import jazmin.deploy.domain.Instance;
import jazmin.deploy.domain.MemcachedUtil;

import org.vaadin.aceeditor.AceEditor;
import org.vaadin.aceeditor.AceMode;
import org.vaadin.aceeditor.AceTheme;

import com.alibaba.fastjson.JSON;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Responsive;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

/**
 * @author yama
 * 6 Jan, 2015
 */
@SuppressWarnings("serial")
public class InstanceMemcachedWindow extends Window{
	AceEditor editor;
	Label label;
	Instance instance;
	TextField keyField;
	TextField valueField;
	
	//
	private static final int OPT_GET=1;
	private static final int OPT_ADD=2;
	private static final int OPT_DELETE=3;
	private static final int OPT_REPLACE=4;
	
	//
	public InstanceMemcachedWindow(Instance instance){
		this.instance=instance;
        Responsive.makeResponsive(this);
        setCaption(instance.id);
        setWidth(90.0f, Unit.PERCENTAGE);
        center();
        setCloseShortcut(KeyCode.ESCAPE, null);
        setResizable(true);
        setClosable(true);
        setHeight(90.0f, Unit.PERCENTAGE);
        //
        VerticalLayout topLayout=new VerticalLayout();
        keyField=new TextField("Key");
        keyField.setWidth("200px");
        keyField.setIcon(FontAwesome.ANCHOR);
        keyField.setWidth(100.0f, Unit.PERCENTAGE);
        keyField.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
        valueField=new TextField("Value");
        valueField.setWidth("100%");
        valueField.setIcon(FontAwesome.ADJUST);
        valueField.setWidth(100.0f, Unit.PERCENTAGE);
        valueField.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
        HorizontalLayout inputLayout=new HorizontalLayout(keyField,valueField);
        inputLayout.setSpacing(true);
        inputLayout.addStyleName(ValoTheme.WINDOW_TOP_TOOLBAR);
        inputLayout.setWidth(100.0f, Unit.PERCENTAGE);
        
        topLayout.addComponent(inputLayout);
        //
    	HorizontalLayout optLayout = new HorizontalLayout();
		optLayout.setSpacing(true);
		optLayout.addStyleName(ValoTheme.WINDOW_TOP_TOOLBAR);
		optLayout.setWidth(100.0f, Unit.PERCENTAGE);
		
		Button getBtn = new Button("Get");
	    optLayout.addComponent(getBtn);
	    getBtn.addClickListener(e->loadData(OPT_GET));
	    //
	    Button addBtn = new Button("Add");
	    optLayout.addComponent(addBtn);
	    addBtn.addClickListener(e->loadData(OPT_ADD));
	    //
	    Button deleteBtn = new Button("Delete");
	    optLayout.addComponent(deleteBtn);
	    deleteBtn.addClickListener(e->loadData(OPT_DELETE));
	    //
	    Button replaceBtn = new Button("Replace");
	    optLayout.addComponent(replaceBtn);
	    replaceBtn.addClickListener(e->loadData(OPT_REPLACE));
	    //
	    Button formatBtn = new Button("Format");
	    formatBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
	    optLayout.addComponent(formatBtn);
	    optLayout.setComponentAlignment(formatBtn, Alignment.MIDDLE_RIGHT);
	    formatBtn.addClickListener(e->format());
	    //
	    label=new Label();
	    label.setWidth("100%");
	    optLayout.addComponent(label);
	    optLayout.setExpandRatio(label, 1);
	    topLayout.addComponent(optLayout);
	    topLayout.setWidth("100%");
	    topLayout.setHeight("130px");
        //
        editor= new AceEditor();
        editor.setShowGutter(false);
        editor.setThemePath("/ace");
        editor.setModePath("/ace");
        editor.setWorkerPath("/ace"); 
        editor.setMode(AceMode.json);
        editor.setShowPrintMargin(false);
        editor.setUseWorker(true);
        editor.setTheme(AceTheme.eclipse);
        editor.setSizeFull();
       
        VerticalLayout content = new VerticalLayout(topLayout,editor);
        content.setSizeFull();
        content.setExpandRatio(editor, 1);
        setContent(content);
        //
	}
	//
	private void format(){
		String v=editor.getValue();
		if(v==null||v.trim().isEmpty()){
			return;
		}
		try{
			String formatedValue=JSON.toJSONString(JSON.parseObject(v),true);
			editor.setReadOnly(false);
			editor.setValue(formatedValue);
			editor.setReadOnly(true);
		}catch(Exception e){}
	}
	//
	private void loadData(int opt){
		editor.setReadOnly(false);
		editor.setValue("");
		editor.setReadOnly(true);
		String key=keyField.getValue();
		if(key==null||key.trim().isEmpty()){
			DeploySystemUI.showNotificationInfo("ERROR","Key can not be null.");
			return;
		}
		key=key.trim();
		try {
			if(opt==OPT_GET){
				String v=MemcachedUtil.get(instance.machine.publicHost,instance.port, key);
				if(v==null){
					DeploySystemUI.showNotificationInfo("ERROR","value not found.");
					return;
				}
				editor.setReadOnly(false);
				editor.setValue(v);
				editor.setReadOnly(true);
			}
			if(opt==OPT_DELETE){
				MemcachedUtil.delete(instance.machine.publicHost,instance.port, key);
				DeploySystemUI.showNotificationInfo("INFO","key :"+key+" deleted");
			}
			if(opt==OPT_ADD){
				String value=valueField.getValue();
				if(value==null||value.trim().isEmpty()){
					DeploySystemUI.showNotificationInfo("ERROR","Value can not be null.");
					return;
				}
				value=value.trim();
				MemcachedUtil.add(instance.machine.publicHost,instance.port, key,value);
				DeploySystemUI.showNotificationInfo("INFO","key :"+key+" added");
			}
			if(opt==OPT_REPLACE){
				String value=valueField.getValue();
				if(value==null||value.trim().isEmpty()){
					DeploySystemUI.showNotificationInfo("ERROR","Value can not be null.");
					return;
				}
				value=value.trim();
				MemcachedUtil.replace(instance.machine.publicHost,instance.port, key,value);
				DeploySystemUI.showNotificationInfo("INFO","key :"+key+" replaced");
			}
		} catch (Exception e) {
			e.printStackTrace();
			label.setValue("");
			DeploySystemUI.showNotificationInfo("ERROR",e.getMessage());
		}
	}
	
}
