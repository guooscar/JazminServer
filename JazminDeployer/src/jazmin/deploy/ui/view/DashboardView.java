/**
 * 
 */
package jazmin.deploy.ui.view;

import jazmin.deploy.domain.DeployManager;
import jazmin.deploy.domain.Instance;
import jazmin.deploy.domain.Machine;
import jazmin.deploy.ui.StaticBeanForm;

import com.vaadin.server.Responsive;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

/**
 * @author yama
 * 16 Jan, 2015
 */
@SuppressWarnings("serial")
public class DashboardView extends Panel{
	private VerticalLayout root;
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
        header.addComponent(titleLabel);
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
	private Component buildMachines(){
		ActiveStateBean bean=new ActiveStateBean();
		for(Machine m:DeployManager.machines()){
			bean.total++;
			if(m.isAlive){
				bean.active++;
			}else{
				bean.deActive++;
			}
		}
		StaticBeanForm<ActiveStateBean>form=
				new StaticBeanForm<DashboardView.ActiveStateBean>(bean,1);
		form.setSizeFull();
		form.setCaption("Machine Status");
		return createContentWrapper(form);
	}
	//
	private Component buildInstances(){
		ActiveStateBean bean=new ActiveStateBean();
		for(Instance m:DeployManager.instances()){
			bean.total++;
			if(m.isAlive){
				bean.active++;
			}else{
				bean.deActive++;
			}
		}
		StaticBeanForm<ActiveStateBean>form=
				new StaticBeanForm<DashboardView.ActiveStateBean>(bean,1);
		form.setSizeFull();
		form.setCaption("Instance Status");
		return createContentWrapper(form);
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
