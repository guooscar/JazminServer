package jazmin.deploy.view.console;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import jazmin.core.Jazmin;
import jazmin.deploy.DeploySystemUI;
import jazmin.deploy.domain.Application;
import jazmin.deploy.domain.Instance;
import jazmin.deploy.manager.DeployManager;

import org.vaadin.aceeditor.AceEditor;
import org.vaadin.aceeditor.AceMode;
import org.vaadin.aceeditor.AceTheme;

import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public  class CommandView extends VerticalLayout{
	protected TextField inputTxt;
	AceEditor editor;
	//
	public CommandView() {
		super();
		initBaseUI();
		setCaption("Command");
	}
	//
	protected void initBaseUI(){
		setSizeFull();
		//
		HorizontalLayout optLayout = new HorizontalLayout();
		optLayout.setSpacing(true);
		optLayout.addStyleName(ValoTheme.WINDOW_TOP_TOOLBAR);
		optLayout.setWidth(100.0f, Unit.PERCENTAGE);
		inputTxt = new TextField();
		inputTxt.setIcon(FontAwesome.CODE);
		inputTxt.setWidth(100.0f, Unit.PERCENTAGE);
		inputTxt.addStyleName(ValoTheme.TEXTFIELD_SMALL);
		inputTxt.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
		inputTxt.addShortcutListener(new ShortcutListener("Shortcut Name", ShortcutAction.KeyCode.ENTER, null) {
			@Override
			public void handleAction(Object sender, Object target) {
				loadData();
			}
		});
		//
		optLayout.addComponent(inputTxt);
		optLayout.setExpandRatio(inputTxt,1.0f);
		//
        Button ok = new Button("Execute");
        ok.addStyleName(ValoTheme.BUTTON_SMALL);
        ok.addStyleName(ValoTheme.BUTTON_PRIMARY);
        optLayout.addComponent(ok);
        ok.addClickListener(e->loadData());
        optLayout.setComponentAlignment(ok, Alignment.BOTTOM_RIGHT);
        //
        Button historyBtn = new Button("History");
        historyBtn.addStyleName(ValoTheme.BUTTON_SMALL);
        historyBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        optLayout.addComponent(historyBtn);
        historyBtn.addClickListener(e->showHistory());
        optLayout.setComponentAlignment(historyBtn, Alignment.BOTTOM_RIGHT);
        //
        editor= new AceEditor();
        editor.setThemePath("/ace");
        editor.setModePath("/ace");
        editor.setWorkerPath("/ace"); 
        editor.setMode(AceMode.sql);
        editor.setShowPrintMargin(false);
        editor.setUseWorker(true);
        editor.setShowGutter(false);
        editor.setTheme(AceTheme.eclipse);
        editor.setMode(AceMode.sh);
        editor.setFontSize("12px");
        editor.setSizeFull();
        addComponent(editor);
	    setExpandRatio(editor, 1);
        addComponent(optLayout);
        
	}
	//
	private void showHistory(){
		CommandHistoryWindow hw=new CommandHistoryWindow();
		UI.getCurrent().addWindow(hw);
		hw.focus();
		hw.addCloseListener(new CloseListener() {
			@Override
			public void windowClose(CloseEvent e) {
				if(hw.getSelectedCommand()!=null){
					inputTxt.setValue(hw.getSelectedCommand());
				}
			}
		});
	}
	//
	protected void loadData(){
		String text=inputTxt.getValue();
		if(text==null||text.trim().isEmpty()){
			return;
		}
		CommandHistoryWindow.addHistory(text);
		inputTxt.setValue("");
		String[] commands=text.split(";");
		Jazmin.execute(()->{
			for(String command:commands){
				executeCommand(command);
			}
		});
	}
	//
	private void executeCommand(String cmd){
		String cmds[]=cmd.split("\\s+");
		List<String>ss=new ArrayList<String>();
		for(String c:cmds){
			ss.add(c.trim());
		}
		cmds=ss.toArray(new String[ss.size()]);
		if(cmds.length<2){
			showResult("missing arguments");
			return;
		}
		if(cmds[0].equals("compile")){
			compile(cmds);
			return;
		}
		if(cmds[0].equals("start")){
			start(cmds);
			return;
		}
		if(cmds[0].equals("stop")){
			stop(cmds);
			return;
		}
		if(cmds[0].equals("restart")){
			restart(cmds);
			return;
		}
		showResult("bad command:"+cmds[0]);
	}
	//
	private void restart(String args[]){
		stop(args);
		start(args);
	}
	//
	private void stop(String args[]){
		List<Instance>result=getInstance(args);
		Collections.sort(result,(a,b)->{
			return b.priority-a.priority;
		});
		if(result.isEmpty()){
			getUI().access(()->{
				showResult("can not find target instance");
			});
			return;
		}
		getUI().access(()->{
			showResult("find "+result.size()+" instances");
			for(Instance ap:result){
				showResult(ap.id);
			}
		});
		AtomicInteger counter = new AtomicInteger();
		for (Instance instance : result) {
			getUI().access(() -> {
				showResult("stop " + instance.id + " " + counter.incrementAndGet() + "/" + result.size()
						+ "...");
			});
			final StringBuilder ss = new StringBuilder("done");
			try {
				ss.append(":" + DeployManager.stopInstance(instance));
			} catch (Exception e) {
				ss.append(":" + e.getMessage());
			}
			getUI().access(() -> {
				showResult(instance.id+" "+ss.toString());
			});
		}
		getUI().access(() -> {
			showResult("stop complete");
		});
		//
		getUI().access(() -> {
			DeploySystemUI.showNotificationInfo("Info", "stop complete");
			DeploySystemUI.get().showWebNotification("JazminDeployer","stop complete");
		});
	}
	//
	private void start(String args[]){
		List<Instance>result=getInstance(args);
		Collections.sort(result,(a,b)->{
			return b.priority-a.priority;
		});
		
		if(result.isEmpty()){
			getUI().access(()->{
				showResult("can not find target instance");
			});
			return;
		}
		getUI().access(()->{
			showResult("find "+result.size()+" instances");
			for(Instance ap:result){
				showResult(ap.id);
			}
		});
		AtomicInteger counter = new AtomicInteger();
		AtomicInteger waitCounter = new AtomicInteger();
		for (Instance instance : result) {
			getUI().access(() -> {
				showResult("==========================================================");
				showResult("start " + instance.id + " " + counter.incrementAndGet() + "/" + result.size()
						+ "...");
			});
			waitCounter.set(0);
			final StringBuilder ss = new StringBuilder("done");
			boolean error = false;
			try {
				ss.append(":" + DeployManager.startInstance(instance));
			} catch (Exception e1) {
				error = true;
				ss.append(":" + e1.getMessage());
			}
			if (error) {
				getUI().access(() -> {
					showResult(instance.id+" "+result.toString());
				});
			}
			while (waitCounter.get() < 30 && !error) {
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (Exception e) {
				}
				getUI().access(() -> {
					showResult("wait " + instance.id + " " + waitCounter.incrementAndGet() + " seconds");
				});
				DeployManager.testInstance(instance);
				if (instance.isAlive) {
					break;
				}
			}
			if (!error) {
				// wait for 15 seconds still not response maybe error happened
				if (!instance.isAlive) {
					getUI().access(() -> DeploySystemUI.showNotificationInfo("Error",
							instance.id + " not response after 30 seconds"));
				} else {
					getUI().access(() -> {
						showResult(instance.id+" done");
					});
				}
			}

		}
		getUI().access(() -> {
			DeploySystemUI.showNotificationInfo("Info", "start complete");
			DeploySystemUI.get().showWebNotification("JazminDeployer","start complete");
		});
	}
	//
	private void compile(String args[]){
		List<Application>result=getApplications(args);
		Collections.sort(result,(a,b)->{
			return b.priority-a.priority;
		});
		if(result.isEmpty()){
			getUI().access(()->{
				showResult("can not find target application");
			});
			return;
		}
		getUI().access(()->{
			showResult("find "+result.size()+" applications");
			for(Application ap:result){
				showResult(ap.id);
			}
		});
		
		//
		AtomicInteger counter=new AtomicInteger();
		for(Application app:result){
			getUI().access(()->{
				showResult("==========================================================");
				showResult("compile "+app.id+" "+
						counter.incrementAndGet()+
						"/"+result.size()+"...");
			});
			final StringBuilder ss=new StringBuilder();
			try {
				int ret=DeployManager.compileApp(app,
						CommandView.this::appendOutput);
				if(ret==0){
					ss.append("success **************************************");
				}else{
					ss.append("fail *****************************************");
				}
			} catch (Exception e) {
				ss.append(":"+e.getMessage());
			}
			getUI().access(()->{
				showResult(ss.toString());
			});
			
		};
		getUI().access(()->{
			DeploySystemUI.showNotificationInfo("Info", "compile complete");
			DeploySystemUI.get().showWebNotification("JazminDeployer","compile complete");
		});	
	}
	//
	private void appendOutput(String s){
		getUI().access(()->{
			editor.setValue(editor.getValue()+s);
			editor.scrollToPosition(editor.getValue().length()-1);
		});
		
	}
	//
	private List<Instance>getInstance(String args[]){
		List<Instance>result=new ArrayList<Instance>();
		for(int i=1;i<args.length;i++){
			String t=args[i];
			if(t.endsWith("*")&&t.length()>1){
				String ss=t.substring(0,t.length()-1);
				result.addAll(DeployManager.getInstanceByPrefix(ss));
			}else{
				Instance ap=DeployManager.getInstanceById(t);
				if(ap!=null){
					result.add(ap);
				}
			}
		}
		return result;
	}
	//
	private List<Application>getApplications(String args[]){
		List<Application>result=new ArrayList<Application>();
		for(int i=1;i<args.length;i++){
			String t=args[i];
			if(t.endsWith("*")&&t.length()>1){
				String ss=t.substring(0,t.length()-1);
				result.addAll(DeployManager.getApplicationByPrefix(ss));
			}else{
				Application ap=DeployManager.getApplicationById(t);
				if(ap!=null){
					result.add(ap);
				}
			}
		}
		return result;
	}
	//
	private void showResult(String s){
		String t=editor.getValue();
		if(t==null||t.isEmpty()){
			editor.setValue(s);
		}else{
			editor.setValue(editor.getValue()+"\n"+s);
		}
		editor.scrollToPosition(editor.getValue().length()-1);
	}
	//
	public String getSearchValue(){
		return (String) inputTxt.getValue();
	}
}