/**
 * 
 */
package jazmin.deploy.view.pkg;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import jazmin.core.Jazmin;
import jazmin.deploy.domain.PackageDownloadInfo;
import jazmin.deploy.manager.DeployManager;
import jazmin.deploy.ui.BeanTable;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.Responsive;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

/**
 * @author yama
 * 6 Jan, 2015
 */
@SuppressWarnings("serial")
public class PackageDownloadWindow extends Window{
	ScheduledFuture<?>scheduledFuture;
	BeanTable <PackageDownloadInfo>table;
	//
	public PackageDownloadWindow() {
        Responsive.makeResponsive(this);
        setCaption("Download Progress");
        setWidth(90.0f, Unit.PERCENTAGE);
        center();
        setCloseShortcut(KeyCode.ESCAPE, null);
        setResizable(false);
        setClosable(true);
        setHeight(90.0f, Unit.PERCENTAGE);
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        setContent(content);
        BeanTable<PackageDownloadInfo>table=createTable();
        table.setSizeFull();
        content.addComponent(table);
        content.setExpandRatio(table, 1f);
        //
        HorizontalLayout footer = new HorizontalLayout();
        footer.addStyleName(ValoTheme.WINDOW_BOTTOM_TOOLBAR);
        footer.setWidth(100.0f, Unit.PERCENTAGE);

        Button ok = new Button("Close");
        ok.addStyleName(ValoTheme.BUTTON_SMALL);
        ok.addStyleName(ValoTheme.BUTTON_PRIMARY);
        ok.addClickListener(e->close());
        ok.focus();
        footer.addComponent(ok);
        footer.setComponentAlignment(ok, Alignment.TOP_RIGHT);
        content.addComponent(footer);
        //
		scheduledFuture=Jazmin.scheduleAtFixedRate(this::refreshData, 1, 1,TimeUnit.SECONDS);
        addCloseListener(this::onClose);
        table.setBeanData(DeployManager.getPackageDownloadInfos());
    }
	//
	private void onClose(CloseEvent e){
		scheduledFuture.cancel(true);
	}
	//
	private void refreshData(){
		getUI().access(()->{
			table.setBeanData(DeployManager.getPackageDownloadInfos());
		});
	}
	//
	private BeanTable<PackageDownloadInfo>createTable(){
		table=new BeanTable<>(null, PackageDownloadInfo.class);
		return table;
	}
	//
}
