/**
 * 
 */
package jazmin.deploy.view.machine;

import jazmin.deploy.DeploySystemUI;
import jazmin.deploy.domain.Machine;
import jazmin.util.SshUtil;

import org.vaadin.aceeditor.AceEditor;
import org.vaadin.aceeditor.AceMode;
import org.vaadin.aceeditor.AceTheme;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.Responsive;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

/**
 * @author yama
 * 6 Jan, 2015
 */
@SuppressWarnings("serial")
public class MachineIPTablesWindow extends Window{
	AceEditor editor;
	Label label;
	Machine machine;
	HorizontalLayout optLayout;
	//
	public MachineIPTablesWindow(Machine machine){
		this.machine=machine;
        Responsive.makeResponsive(this);
        setCaption(machine.id+" iptables");
        setWidth("600px");
        center();
        setCloseShortcut(KeyCode.ESCAPE, null);
        setResizable(true);
        setClosable(true);
        setHeight(90.0f, Unit.PERCENTAGE);
        //
        optLayout = new HorizontalLayout();
		optLayout.setSpacing(true);
		optLayout.addStyleName(ValoTheme.WINDOW_TOP_TOOLBAR);
		optLayout.setWidth(100.0f, Unit.PERCENTAGE);
		
		addBtn("View Rules",this::viewConfig);
		addBtn("Save Rules",this::saveConfig);
		addBtn("Status",this::status);
		addBtn("Start",this::start);
		addBtn("Stop",this::stop);
		addBtn("Restart",this::restart);
		//
	    label=new Label();
	    label.setWidth("100%");
	    optLayout.addComponent(label);
	    optLayout.setExpandRatio(label, 1);
	   
        //
        editor= new AceEditor();
        editor.setShowGutter(false);
        editor.setThemePath("/ace");
        editor.setModePath("/ace");
        editor.setWorkerPath("/ace"); 
        editor.setMode(AceMode.sh);
        editor.setShowPrintMargin(false);
        editor.setUseWorker(true);
        editor.setTheme(AceTheme.eclipse);
        editor.setSizeFull();
       
        VerticalLayout content = new VerticalLayout(optLayout,editor);
        content.setSizeFull();
        content.setExpandRatio(editor, 1);
        setContent(content);
        //
	}
	//
	private void addBtn(String name,Runnable r){
		Button getBtn = new Button(name);
		getBtn.addStyleName(ValoTheme.BUTTON_SMALL);
	    optLayout.addComponent(getBtn);
	    getBtn.addClickListener(e->{r.run();});
	}
	//
	private void viewConfig(){
		try{
			SshUtil.execute(machine.privateHost,
					machine.sshPort,
					"root", 
					machine.rootSshPassword, 
					"cat /etc/sysconfig/iptables", machine.getSshTimeout(),(s,e)->{
						editor.setValue(s);
					});
		} catch (Exception e) {
			e.printStackTrace();
			label.setValue("");
			DeploySystemUI.showNotificationInfo("ERROR",e.getMessage());
		}
	}
	//
	private void saveConfig(){
		try{
			String config=editor.getValue();
			SshUtil.execute(machine.privateHost,
					machine.sshPort,
					"root", 
					machine.rootSshPassword, 
					"echo '"+config+"'>/etc/sysconfig/iptables", machine.getSshTimeout(),(s,e)->{
						
					});
			DeploySystemUI.showNotificationInfo("INFO","iptables rule save completed");
		} catch (Exception e) {
			e.printStackTrace();
			label.setValue("");
			DeploySystemUI.showNotificationInfo("ERROR",e.getMessage());
		}
	}
	//
	private void restart(){
		runCmd("service iptables restart","iptables restart completed",false);
	}
	//
	private void start(){
		runCmd("service iptables start","iptables start completed",false);
	}
	//
	private void stop(){
		runCmd("service iptables stop","iptables stop completed",false);
	}
	//
	private void status(){
		runCmd("service iptables status","iptables status completed",true);
	}
	//
	private void runCmd(String cmd,String info,boolean showResult){
		try{
			SshUtil.execute(machine.privateHost,
					machine.sshPort,
					"root", 
					machine.rootSshPassword, 
					cmd, machine.getSshTimeout(),(s,e)->{
						if(showResult){
							editor.setValue(s);
						}
					});
			DeploySystemUI.showNotificationInfo("INFO",info);
		} catch (Exception e) {
			e.printStackTrace();
			label.setValue("");
			DeploySystemUI.showNotificationInfo("ERROR",e.getMessage());
		}
	}
	//
}
