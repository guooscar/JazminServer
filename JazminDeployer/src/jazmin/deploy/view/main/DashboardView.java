/**
 * 
 */
package jazmin.deploy.view.main;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.vaadin.server.Responsive;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import jazmin.deploy.domain.Instance;
import jazmin.deploy.domain.Machine;
import jazmin.deploy.manager.DeployManager;
import jazmin.deploy.ui.BeanTable;
import jazmin.deploy.ui.StaticBeanForm;

/**
 * @author yama
 * 16 Jan, 2015
 */
@SuppressWarnings("serial")
public class DashboardView extends Panel{
	private VerticalLayout root;
	private Label healthCheckLabel;
	public DashboardView() {
		super();
		addStyleName(ValoTheme.PANEL_BORDERLESS);
        setSizeFull();

        root = new VerticalLayout();
        root.setSizeFull();
        root.setMargin(true);
        root.addStyleName("dashboard-view");
        setContent(root);
        Responsive.makeResponsive(root);
        root.addComponent(buildHeader());
        Component content=buildContent();
        root.addComponent(content);
        root.setExpandRatio(content,1);
        //
        reloadDataMachine();
        reloadDataInstance();
        //
        TimerTask t=new TimerTask() {
			@Override
			public void run() {
				getUI().access(()->{
					reloadDataMachine();
			        reloadDataInstance();
			        if(DeployManager.lastHealthCheckTime!=null){
			        	healthCheckLabel.setValue("Last Check:"+DeployManager.lastHealthCheckTime+"");
			        }
						
				});
			}
		};
		Timer tt = new Timer(true);
		tt.scheduleAtFixedRate(t, 5000, 10*1000);
    }
	//
	private Component buildHeader(){
		HorizontalLayout header = new HorizontalLayout();
        header.addStyleName("viewheader");
        header.setSpacing(true);
        Label titleLabel = new Label("Dashboard");
        titleLabel.setSizeUndefined();
        titleLabel.addStyleName(ValoTheme.LABEL_H1);
        titleLabel.addStyleName(ValoTheme.LABEL_NO_MARGIN);
        //
        healthCheckLabel=new Label("");
        healthCheckLabel.setSizeUndefined();
        healthCheckLabel.addStyleName(ValoTheme.LABEL_H4);
        healthCheckLabel.addStyleName(ValoTheme.LABEL_NO_MARGIN);
        
        header.addComponent(titleLabel);
        header.addComponent(healthCheckLabel);
        //
       
        return header;
	}
	//
	private Component buildContent() {
		CssLayout dashboardPanels = new CssLayout();
		dashboardPanels.setSizeFull();
        dashboardPanels.addStyleName("dashboard-panels");
        Responsive.makeResponsive(dashboardPanels);
        dashboardPanels.addComponent(buildMachines());
        dashboardPanels.addComponent(buildInstances());
        return dashboardPanels;
    }
	//
	public static class ActiveStateBean{
		public int active;
		public int deActive;
		public int total;
	}
	//
	public static class DeActiveItemBean{
		public String id;
		public String info;
	}
	//
	private void reloadDataMachine(){
		ActiveStateBean bean=new ActiveStateBean();
		List<DeActiveItemBean>items=new ArrayList<>();
		for(Machine m:DeployManager.getMachines()){
			bean.total++;
			if(m.isAlive){
				bean.active++;
			}else{
				bean.deActive++;
				DeActiveItemBean b=new DeActiveItemBean();
				b.id=m.id;
				b.info=m.publicHost;
				items.add(b);
			}
		}
		//
		machineForm.setBean(bean);
		machineTable.setBeanData(items);
	}
	//
	private void reloadDataInstance(){
		ActiveStateBean bean=new ActiveStateBean();
		List<DeActiveItemBean>items=new ArrayList<>();
		for(Instance m:DeployManager.getInstances()){
			bean.total++;
			if(m.isAlive){
				bean.active++;
			}else{
				bean.deActive++;
				DeActiveItemBean b=new DeActiveItemBean();
				b.id=m.id;
				b.info="["+m.cluster+"]"+m.appId;
				items.add(b);
			}
		}
		//
		instanceForm.setBean(bean);
		instanceTable.setBeanData(items);
	}
	//
	BeanTable<DeActiveItemBean>machineTable;
	StaticBeanForm<ActiveStateBean>machineForm;
	//
	BeanTable<DeActiveItemBean>instanceTable;
	StaticBeanForm<ActiveStateBean>instanceForm;
	//
	private Component buildMachines(){
		ActiveStateBean bean=new ActiveStateBean();
		machineForm=
				new StaticBeanForm<DashboardView.ActiveStateBean>(bean,1);
		machineForm.setSizeFull();
		machineForm.setCaption("Machine Status");
		//
		machineTable=new BeanTable<>("DeActive Machines", DeActiveItemBean.class);
		//
		VerticalLayout vl=new VerticalLayout(machineForm,machineTable);
		return createContentWrapper(vl);
	}
	//
	private Component buildInstances(){
		ActiveStateBean bean=new ActiveStateBean();
		instanceForm=
				new StaticBeanForm<DashboardView.ActiveStateBean>(bean,1);
		instanceForm.setSizeFull();
		instanceForm.setCaption("Instance Status");
		//
		instanceTable=new BeanTable<>("DeActive Instances", DeActiveItemBean.class);
		//
		VerticalLayout vl=new VerticalLayout(instanceForm,instanceTable);
		return createContentWrapper(vl);
	}
	//
	private Component createContentWrapper(final Component content) {
        final CssLayout slot = new CssLayout();
        slot.setWidth("100%");
        slot.addStyleName("dashboard-panel-slot");
        CssLayout card = new CssLayout();
        card.setWidth("100%");
        card.addStyleName(ValoTheme.LAYOUT_CARD);
        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.addStyleName("dashboard-panel-toolbar");
        toolbar.setWidth("100%");
        Label caption = new Label(content.getCaption());
        caption.addStyleName(ValoTheme.LABEL_H4);
        caption.addStyleName(ValoTheme.LABEL_COLORED);
        caption.addStyleName(ValoTheme.LABEL_NO_MARGIN);
        content.setCaption(null);
        toolbar.addComponents(caption);
        toolbar.setExpandRatio(caption, 1);
        toolbar.setComponentAlignment(caption, Alignment.MIDDLE_LEFT);
        card.addComponents(toolbar, content);
        slot.addComponent(card);
        return slot;
    }

}
