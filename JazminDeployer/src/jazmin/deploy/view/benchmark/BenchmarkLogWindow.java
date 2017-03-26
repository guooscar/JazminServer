/**
 * 
 */
package jazmin.deploy.view.benchmark;

import org.vaadin.aceeditor.AceEditor;
import org.vaadin.aceeditor.AceMode;
import org.vaadin.aceeditor.AceTheme;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.Responsive;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import jazmin.deploy.manager.BenchmarkSession.LogHandler;

/**
 * @author yama
 * 6 Jan, 2015
 */
@SuppressWarnings("serial")
public class BenchmarkLogWindow extends Window implements LogHandler{
	
	StringBuilder logs;
	Label label;
	AceEditor editor;
	//
	public BenchmarkLogWindow() {
		Responsive.makeResponsive(this);
		logs=new StringBuilder();
	    setCaption("Benchmark Logs");
        setWidth("600px");
        center();
        setCloseShortcut(KeyCode.ESCAPE, null);
        setResizable(true);
        setClosable(true);
        setHeight(90.0f, Unit.PERCENTAGE);
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
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
        content.addComponent(editor);
        //
        setContent(content);
	}
	//
	public void appendLog(String log){
		logs.append(log);
		editor.setValue(logs.toString());
		editor.scrollToPosition(editor.getValue().length()-1);
	}
	//
	@Override
	public void log(String log) {
		getUI().access(()->{
			appendLog(log+"\n");			
		});
	}
}
