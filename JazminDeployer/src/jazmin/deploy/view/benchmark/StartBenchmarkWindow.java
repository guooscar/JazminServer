/**
 * 
 */
package jazmin.deploy.view.benchmark;



import java.util.function.Consumer;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Responsive;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import jazmin.deploy.DeploySystemUI;
import jazmin.deploy.manager.DeployManager;

/**
 * @author yama
 * 6 Jan, 2015
 */
@SuppressWarnings("serial")
public class StartBenchmarkWindow extends Window{
	ComboBox scriptCombox;
	TextField userCountTf;
	TextField loopCountTf;
	TextField rampUpPeriodTf;
	CheckBox haltOnExceptionCb;
	CheckBox showConsoleCb;
	//
	public static class BenchmarkInfo{
		public String script;
		public int userCount;
		public int loopCount;
		public int rampUpPeriod;
		public boolean haltOnException;
		public boolean showConsole;
	}
	//
	public StartBenchmarkWindow(Consumer<BenchmarkInfo>callback){
		Responsive.makeResponsive(this);
        setCaption("Run Benchmark");
        setWidth("500px");
        setHeight("500px");
        center();
        setCloseShortcut(KeyCode.ESCAPE, null);
        setResizable(false);
        setClosable(true);
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setMargin(true);
        content.setSpacing(true);
        setContent(content);
        scriptCombox=new ComboBox("Benchmark");
        scriptCombox.setNewItemsAllowed(false);
        scriptCombox.setNullSelectionAllowed(false);
        scriptCombox.setInvalidAllowed(false);
        scriptCombox.setIcon(FontAwesome.CAB);
        scriptCombox.setWidth(100.0f, Unit.PERCENTAGE);
        //
        userCountTf=createTf("User Count","1");
        loopCountTf=createTf("Loop Count","1");
        rampUpPeriodTf=createTf("Ramp Up Period(seconds)","0");
        //
        haltOnExceptionCb=new CheckBox("Halt On Exception");
        showConsoleCb=new CheckBox("Show Console");
        //
        VerticalLayout formLayout=new VerticalLayout();
        formLayout.addComponent(scriptCombox);
        formLayout.addComponent(userCountTf);
        formLayout.addComponent(loopCountTf);
        formLayout.addComponent(rampUpPeriodTf);
        formLayout.addComponent(haltOnExceptionCb);
        formLayout.addComponent(showConsoleCb);
        formLayout.setSizeFull();
        //
        content.addComponent(formLayout);
        content.setExpandRatio(formLayout, 1);
        //
        HorizontalLayout footer = new HorizontalLayout();
        footer.addStyleName(ValoTheme.WINDOW_BOTTOM_TOOLBAR);
        footer.setWidth(100.0f, Unit.PERCENTAGE);

        Button ok = new Button("Run");
        ok.addStyleName(ValoTheme.BUTTON_SMALL);
        ok.addStyleName(ValoTheme.BUTTON_DANGER);
        ok.addClickListener(e->runRobot(callback));
        footer.addComponent(ok);
        footer.setComponentAlignment(ok, Alignment.TOP_RIGHT);
        content.addComponent(footer);
        //
        DeployManager.getScripts("benchmark").forEach(robot->{
        	Object itemId=scriptCombox.addItem(robot.name);
        	scriptCombox.setItemCaption(itemId, robot.name);
        });
        //
	}
	//
	private TextField createTf(String name,String defaultValue){
		TextField tf=new TextField(name);
		tf.addStyleName(ValoTheme.TEXTFIELD_SMALL);
		tf.setValue(defaultValue);
		return tf;
	}
	//
	private void runRobot(Consumer<BenchmarkInfo> callback){
		BenchmarkInfo info=new BenchmarkInfo();
		info.script=(String)scriptCombox.getValue();
		info.userCount=getIntValue(userCountTf);
		info.loopCount=getIntValue(loopCountTf);
		info.rampUpPeriod=getIntValue(rampUpPeriodTf);
		info.haltOnException=haltOnExceptionCb.getValue();
		info.showConsole=showConsoleCb.getValue();
		//
		if(info.userCount<1){
			DeploySystemUI.showInfo("setup user count");
			return;
		}
		if(info.loopCount<1){
			DeploySystemUI.showInfo("setup user loop count");
			return;
		}
		if(info.script==null){
			DeploySystemUI.showInfo("choose benchmark");
			return;
		}
		close();
		callback.accept(info);	
	}
	//
	private int getIntValue(TextField tf){
		int t=0;
		if(tf.getValue()!=null){
			try{
				return Integer.parseInt(tf.getValue());
			}catch (Exception e) {}
		}
		return t>0?t:0;
	}
}
