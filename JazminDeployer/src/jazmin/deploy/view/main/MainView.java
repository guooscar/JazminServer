package jazmin.deploy.view.main;

import java.io.File;

import jazmin.deploy.DeploySystemUI;
import jazmin.deploy.domain.User;
import jazmin.deploy.manager.DeployManager;
import jazmin.deploy.view.app.ApplicationInfoView;
import jazmin.deploy.view.benchmark.BenchmarkView;
import jazmin.deploy.view.deployplan.DeployPlanView;
import jazmin.deploy.view.instance.InstanceInfoView;
import jazmin.deploy.view.job.JobInfoView;
import jazmin.deploy.view.machine.MachineInfoView;
import jazmin.deploy.view.pkg.PackageInfoView;
import jazmin.deploy.view.repo.RepoInfoView;
import jazmin.deploy.view.workflow.WorkflowView;

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
        //
    	setView(dashboardView);
    }
    //
    public void setView(Component view){
    	content.removeAllComponents();
    	content.addComponent(view);
    }
    DashboardView dashboardView;
    //
    private void initMenus(){
    	dashboardView=new DashboardView();
    	menuView.addMenuItem("Dashboard", FontAwesome.DASHBOARD,dashboardView);	
    	menuView.addMenuItem("Instance", FontAwesome.LIST,new InstanceInfoView());
    	menuView.addMenuItem("Machine", FontAwesome.DESKTOP,new MachineInfoView());	
    	menuView.addMenuItem("Application", FontAwesome.FILE,new ApplicationInfoView());	
        if(DeploySystemUI.getUser().admin){
    		menuView.addMenuItem("Package", FontAwesome.FOLDER_O,new PackageInfoView());	
        	menuView.addMenuItem("Repo", FontAwesome.CLOUD_UPLOAD,new RepoInfoView());
    	}
     	menuView.addMenuItem("DeployPlan", FontAwesome.CODE,new DeployPlanView());
     	menuView.addMenuItem("Job", FontAwesome.CUBES,new JobInfoView());
     	menuView.addMenuItem("Benchmark", FontAwesome.CAB,new BenchmarkView());
     	menuView.addMenuItem("Workflow", FontAwesome.GEARS,new WorkflowView());
    }
    
}
