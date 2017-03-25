/**
 * 
 */
package jazmin.deploy.view.benchmark;

import javax.script.ScriptException;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import jazmin.deploy.DeploySystemUI;
import jazmin.deploy.manager.BenchmarkSession;
import jazmin.deploy.manager.BenchmarkSession.RobotFactory;
import jazmin.deploy.manager.DeployManager;
import jazmin.deploy.manager.JavascriptBenchmarkRobot;
import jazmin.deploy.view.benchmark.StartBenchmarkWindow.BenchmarkInfo;

/**
 * @author yama
 *
 */
@SuppressWarnings("serial")
public class BenchmarkView extends VerticalLayout{
	protected HorizontalLayout tray;
	protected Label titleLabel; 
	BenchmarkSession session;
	Button startButton;
	Button stopButton;
	//
	public BenchmarkView() {
		super();
		initBaseUI();
		//
		startButton=addOptButton("Start", ValoTheme.BUTTON_PRIMARY,this::startBenchmark);
		stopButton=addOptButton("Stop", ValoTheme.BUTTON_PRIMARY,this::stopBenchmark);
		//
		stopButton.setEnabled(false);
	}
	//
	private void startBenchmark(ClickEvent e){
		StartBenchmarkWindow w=new StartBenchmarkWindow(this::startBenchmark0);
		UI.getCurrent().addWindow(w);
		w.focus();
	}
	//
	private void startBenchmark0(BenchmarkInfo info){
		try{
			String file=DeployManager.getBenchmarkScriptContent(info.script);
			session=DeployManager.addBenchmarkSession();
			RobotFactory rf=()->{
				try {
					return new JavascriptBenchmarkRobot(
							session,
							"test",
							file);
				} catch (ScriptException e) {
					session.log(e.getMessage());
					return null;
				}
			};
			frame.setSource(new ExternalResource("/srv/benchmark/graph?id="+session.id));
			showConsole();
			session.addCompleteHandler(this::sessionComplete);
			session.start(info.script,rf, info.userCount, info.loopCount, info.rampUpPeriod);
			updateLabel();
			startButton.setEnabled(false);
			stopButton.setEnabled(true);
		}catch (Exception e) {
			DeploySystemUI.showInfo("Open Failed:"+e.getMessage());
		}	
	}
	//
	private void updateLabel(){
		if(session==null){
			titleLabel.setValue("No Benchmark");
		}else{
			String t=session.name+
					" UserCount:"+session.userCount+
					" LoopCount:"+session.loopCount+
					" RampUpPeriod:"+session.rampUpPeriod;
			titleLabel.setValue(
					(session.endTime==null?"[RUNNING]":"[COMPLETED]")+
					t);
		}
	}
	//
	private void sessionComplete(){
		stopButton.setEnabled(false);
		startButton.setEnabled(true);
		updateLabel();
	}
	//
	private void stopBenchmark(ClickEvent e){
		if(session!=null){
			session.stop();
		}
	}
	//
	private void showConsole(){
		BenchmarkLogWindow logWindow=new BenchmarkLogWindow();
		UI.getCurrent().addWindow(logWindow);
		logWindow.focus();
		if(session!=null){
			session.setLogHandler(logWindow);
		}
	}
	//
	protected void initBaseUI(){
		setSizeFull();
		HorizontalLayout optLayout=new HorizontalLayout();
		optLayout.setSpacing(true);
		optLayout.setWidth(100.0f, Unit.PERCENTAGE);
		optLayout.addStyleName(ValoTheme.WINDOW_TOP_TOOLBAR);
		titleLabel=new Label("No Benchmark");
		optLayout.addComponent(titleLabel);
		addComponent(optLayout);
		//
		Component table = createMainView();
		addComponent(table);
		table.setSizeFull();
        setExpandRatio(table, 1);
        tray = new HorizontalLayout();
		tray.setWidth(100.0f, Unit.PERCENTAGE);
		tray.addStyleName(ValoTheme.WINDOW_BOTTOM_TOOLBAR);
		tray.setSpacing(true);
		tray.setMargin(true);
		//
		Label emptyLabel=new Label("");
		tray.addComponent(emptyLabel);
		tray.setComponentAlignment(emptyLabel, Alignment.MIDDLE_RIGHT);
		tray.setExpandRatio(emptyLabel,1.0f);
		//
		addComponent(tray);
	}
	private BrowserFrame frame;
	//
	private Component createMainView(){
		frame = new BrowserFrame(null,
				new ExternalResource(""));
		frame.setImmediate(true);
		frame.setSizeFull();
		return frame;
	}
	//
	protected Button addOptButton(String name,String style,ClickListener cl){
		Button btn = new Button(name);
		if(style!=null){
			btn.addStyleName(style);
		}
		btn.addStyleName(ValoTheme.BUTTON_SMALL);
		tray.addComponent(btn,tray.getComponentCount()-1);
		tray.setComponentAlignment(btn, Alignment.MIDDLE_LEFT);
		btn.addClickListener(cl);
		return btn;
	}
}
