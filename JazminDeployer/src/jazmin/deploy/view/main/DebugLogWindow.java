/**
 * 
 */
package jazmin.deploy.view.main;

import java.util.Timer;
import java.util.TimerTask;

import org.vaadin.aceeditor.AceEditor;
import org.vaadin.aceeditor.AceTheme;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.Responsive;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import jazmin.deploy.manager.DeployManager;

/**
 * @author yama
 *
 */
@SuppressWarnings("serial")
public class DebugLogWindow extends Window{
	CodeEditorCallback callback;
	AceEditor editor;
	TimerTask refreshTask;
	Timer timer;
	//
	public DebugLogWindow() {
	    Responsive.makeResponsive(this);
        setCaption("DebugLog");
        setWidth("600px");
        center();
        setCloseShortcut(KeyCode.ESCAPE, null);
        setResizable(true);
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
        editor.setFontSize("10px");
        editor.setSizeFull();
        content.addComponent(editor);
        content.setExpandRatio(editor, 1f);
        //
        HorizontalLayout footer = new HorizontalLayout();
        footer.addStyleName(ValoTheme.WINDOW_BOTTOM_TOOLBAR);
        footer.setWidth(100.0f, Unit.PERCENTAGE);
        footer.setSpacing(true);
        //
        content.addComponent(footer);
        refreshTask=new TimerTask() {
			@Override
			public void run() {
				if(getUI()==null){
					return;
				}
				getUI().access(()->{
					reload();
				});
			}
		};
		timer = new Timer(false);
		timer.scheduleAtFixedRate(refreshTask, 5000, 5*1000);
		//
		reload();
	}
	//
	private void reload(){
		StringBuilder sb=new StringBuilder();
		for(String log:DeployManager.getDebugLogs()){
			sb.append(log+"\n");
		}
		editor.setValue(sb.toString());
		editor.scrollToPosition(editor.getValue().length()-1);
	}
	//
	@Override
	public void close() {
		super.close();
		refreshTask.cancel();
		timer.cancel();
	}
}

