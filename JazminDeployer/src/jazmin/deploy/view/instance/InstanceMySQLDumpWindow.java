/**
 * 
 */
package jazmin.deploy.view.instance;

import java.text.SimpleDateFormat;
import java.util.Date;

import jazmin.deploy.DeploySystemUI;
import jazmin.deploy.domain.Instance;
import jazmin.deploy.domain.Machine;
import jazmin.util.SshUtil;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Responsive;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

/**
 * @author yama
 * 6 Jan, 2015
 */
@SuppressWarnings("serial")
public class InstanceMySQLDumpWindow extends Window{
	TextField fromField;
	Instance instance;
	//
	public InstanceMySQLDumpWindow(Instance instance){
		this.instance=instance;
	    Responsive.makeResponsive(this);
        setCaption("Dump Sql - "+instance.id);
        setWidth("500px");
        center();
        setCloseShortcut(KeyCode.ESCAPE, null);
        setResizable(false);
        setClosable(true);
        setHeight("250px");
        //
        fromField=new TextField("Dump File");
        fromField.setWidth("100%");
        fromField.setIcon(FontAwesome.ANCHOR);
        fromField.setWidth(100.0f, Unit.PERCENTAGE);
        fromField.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
        SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmss");
        fromField.setValue("/home/appadmin/db_backup/"+sdf.format(new Date())+".sql");
        VerticalLayout inputLayout=new VerticalLayout(fromField);
        inputLayout.setSpacing(true);
        inputLayout.addStyleName(ValoTheme.WINDOW_TOP_TOOLBAR);
        inputLayout.setWidth(100.0f, Unit.PERCENTAGE);
        
        //
    	HorizontalLayout optLayout = new HorizontalLayout();
		optLayout.setSpacing(true);
		optLayout.addStyleName(ValoTheme.WINDOW_TOP_TOOLBAR);
		optLayout.setWidth(100.0f, Unit.PERCENTAGE);
		
		Button getBtn = new Button("OK");
		getBtn.addStyleName(ValoTheme.BUTTON_SMALL);
		optLayout.addComponent(getBtn);
	    getBtn.addClickListener(e->{
	    	String from=fromField.getValue();
	    	if(from==null||from.trim().isEmpty()){
	    		DeploySystemUI.showNotificationInfo("error","Dump file can not be null.");
	    		return;
	    	}
	    	dump(from);
	    	close();
	    });
	    //
	    VerticalLayout content=new VerticalLayout(inputLayout,optLayout);
	    content.setExpandRatio(inputLayout,1);
	    content.setSizeFull();
        setContent(content);
	}
	//
	private void dump(String file){
		try{
			Machine machine=instance.machine;
			String cmd="mysqldump --user='"+instance.user+"' --password='"
						+instance.password+"' "+instance.id+"  > "+file;
			SshUtil.execute(machine.publicHost,
					machine.sshPort,
					machine.sshUser, 
					machine.sshPassword, 
					cmd, 
					machine.getSshTimeout(),(s,e)->{
						DeploySystemUI.showNotificationInfo("INFO",s+"/"+e);
					});
		} catch (Exception e) {
			e.printStackTrace();
			DeploySystemUI.showNotificationInfo("ERROR",e.getMessage());
		}
	}
}
