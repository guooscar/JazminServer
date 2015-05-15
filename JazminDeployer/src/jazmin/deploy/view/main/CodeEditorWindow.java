/**
 * 
 */
package jazmin.deploy.view.main;

import org.vaadin.aceeditor.AceEditor;
import org.vaadin.aceeditor.AceMode;
import org.vaadin.aceeditor.AceTheme;

import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.Responsive;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

/**
 * @author yama
 *
 */
@SuppressWarnings("serial")
public class CodeEditorWindow extends Window{
	public static interface CodeEditorCallback{
		void onSave(String value);
	}
	//
	CodeEditorCallback callback;
	AceEditor editor;
	Button saveBtn;
	boolean isSaved;
	String title;
	//
	public CodeEditorWindow(CodeEditorCallback callback) {
		this.callback=callback;
        Responsive.makeResponsive(this);
        setCaption("Code Editor");
        setWidth("800px");
        center();
        setCloseShortcut(KeyCode.ESCAPE, null);
        setResizable(false);
        setClosable(true);
        setHeight(90.0f, Unit.PERCENTAGE);
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        setContent(content);
        editor= new AceEditor();
        editor.setThemePath("/ace");
        editor.setModePath("/ace");
        editor.setWorkerPath("/ace"); 
        editor.setUseWorker(true);
        editor.setTheme(AceTheme.eclipse);
        editor.setSizeFull();
        content.addComponent(editor);
        content.setExpandRatio(editor, 1f);
        //
        HorizontalLayout footer = new HorizontalLayout();
        footer.addStyleName(ValoTheme.WINDOW_BOTTOM_TOOLBAR);
        footer.setWidth(100.0f, Unit.PERCENTAGE);

        saveBtn = new Button("Save");
        saveBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        saveBtn.addClickListener(e->onSave());
        saveBtn.focus();
        footer.addComponent(saveBtn);
        footer.setComponentAlignment(saveBtn, Alignment.TOP_RIGHT);
        content.addComponent(footer);
        //
        editor.addTextChangeListener(new TextChangeListener() {
			@Override
			public void textChange(TextChangeEvent event) {
				isSaved=false;
				setCaption(title+" *");
				saveBtn.setEnabled(true);
			}
		});
        //
        isSaved=true;
	}
	//
	public void setValue(String title,String value,AceMode mode){
		this.title=title;
		editor.setMode(mode);
		editor.setValue(value);
		setCaption(title);
	}
	//
	@Override
	public void close(){
		//TODO confirm
		super.close();
	}
	//
	private void onSave(){
		if(callback!=null){
			callback.onSave(editor.getValue());
			isSaved=true;
			setCaption(title);
			saveBtn.setEnabled(false);
		}
	}
	//
	public String getValue(){
		return editor.getValue();
	}
	//
	public void setReadonly(boolean f){
		editor.setReadOnly(f);
		saveBtn.setVisible(!f);
	}	
}

