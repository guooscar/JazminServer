package jazmin.deploy.view.main;

import java.io.File;

import jazmin.deploy.DeploySystemUI;
import jazmin.deploy.domain.User;
import jazmin.deploy.manager.DeployManager;
import jazmin.deploy.view.app.ApplicationInfoView;
import jazmin.deploy.view.console.ConsoleView;
import jazmin.deploy.view.instance.InstanceInfoView;
import jazmin.deploy.view.job.JobInfoView;
import jazmin.deploy.view.machine.MachineInfoView;
import jazmin.deploy.view.pkg.PackageInfoView;
import jazmin.deploy.view.repo.RepoInfoView;

import com.vaadin.server.FileResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
/**
 * 
 * @author yama
 * 30 Dec, 2014
 */
@SuppressWarnings("serial")
public class MainView extends HorizontalLayout {
	private VerticalLayout content ;
	private MainMenu menuView;
	private ConsoleView consoleView;
     
    public MainView() {
        setSizeFull();
        addStyleName("mainview");
        menuView=new MainMenu(this);
        addComponent(menuView);
        initMenus();
        content = new VerticalLayout();
        content.addStyleName("view-content");
        content.setSizeFull();
        //
        String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();
        //Image as a file resource
        FileResource resource = new FileResource(
        		new File(basepath +"/image/jazmin-logo.png"));
        //Show the image in the application
        Image image = new Image("", resource);
        content.addComponent(image);
        content.setComponentAlignment(image, Alignment.BOTTOM_RIGHT);
        //
        addComponent(content);
        setExpandRatio(content, 1.0f);
        //
        if(!DeployManager.getErrorMessage().isEmpty()){
        	ErrorMessageWindow bfw=new ErrorMessageWindow();
			UI.getCurrent().addWindow(bfw);
			bfw.focus();
        }
        DeploySystemUI.setupErrorHandler();
    }
    //
    public void setView(Component view){
    	content.removeAllComponents();
    	content.addComponent(view);
    }
    //
    private void initMenus(){
    	menuView.addMenuItem("Dashboard", FontAwesome.DASHBOARD,DashboardView.class);	
    	menuView.addMenuItem("Instances", FontAwesome.LIST,new InstanceInfoView());
    	menuView.addMenuItem("Machines", FontAwesome.DESKTOP,new MachineInfoView());	
    	menuView.addMenuItem("Applications", FontAwesome.FILE,new ApplicationInfoView());	
        if(DeploySystemUI.getUser().id.equals(User.ADMIN)){
    		menuView.addMenuItem("Packages", FontAwesome.FOLDER_O,new PackageInfoView());	
        	menuView.addMenuItem("Repos", FontAwesome.CLOUD_UPLOAD,new RepoInfoView());
        	consoleView=new ConsoleView();
    	}
     	menuView.addMenuItem("Consoles", FontAwesome.CODE,consoleView);
    	menuView.addMenuItem("Jobs", FontAwesome.CUBES,new JobInfoView());	
        
    }
    //
    public ConsoleView getConsoleView(){
    	return consoleView;
    }
}
