package jazmin.deploy.ui;

import java.io.File;

import jazmin.deploy.view.ApplicationInfoView;
import jazmin.deploy.view.DashboardView;
import jazmin.deploy.view.InstanceInfoView;
import jazmin.deploy.view.MachineInfoView;
import jazmin.deploy.view.PackageInfoView;
import jazmin.deploy.view.RepoInfoView;

import com.vaadin.server.FileResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
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
    }
    //
    public void setView(Component view){
    	content.removeAllComponents();
    	content.addComponent(view);
    }
    //
    private void initMenus(){
    	menuView.addMenuItem("Dashboard", FontAwesome.DASHBOARD,DashboardView.class);	
    	menuView.addMenuItem("Instances", FontAwesome.HOME,new InstanceInfoView());
    	menuView.addMenuItem("Machines", FontAwesome.GEAR,new MachineInfoView());	
    	menuView.addMenuItem("Applications", FontAwesome.ANDROID,new ApplicationInfoView());	
    	menuView.addMenuItem("Packages", FontAwesome.ANCHOR,new PackageInfoView());	
    	menuView.addMenuItem("Repos", FontAwesome.APPLE,new RepoInfoView());	
    }
}
