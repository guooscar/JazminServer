/**
 * 
 */
package jazmin.deploy.view.machine;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import jazmin.core.Jazmin;
import jazmin.deploy.domain.Machine;
import jazmin.deploy.domain.MachineStat;
import jazmin.deploy.domain.MachineStat.FSInfo;
import jazmin.deploy.domain.MachineStat.NetInfInfo;
import jazmin.deploy.ui.BeanTable;
import jazmin.deploy.ui.CellRender;
import jazmin.deploy.ui.StaticBeanForm;
import jazmin.util.DumpUtil;

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
public class MachineStatWindow extends Window implements CellRender{
	//
	private MachineStat machineStat;
	private Machine machine;
	private StaticBeanForm<MachineStat>beanForm;
	private BeanTable<FSInfo>fsinfoTable;
	private BeanTable<NetInfInfo>networkInfoTable;
	//
	public MachineStatWindow(Machine machine) {
		this.machine=machine;
        Responsive.makeResponsive(this);
        setCaption(machine.id+" Loading...");
        setWidth("800px");
        center();
        setCloseShortcut(KeyCode.ESCAPE, null);
        setResizable(true);
        setClosable(true);
        setHeight(90.0f, Unit.PERCENTAGE);
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        setContent(content);
        machineStat=new MachineStat();
        beanForm=new StaticBeanForm<MachineStat>(
        		machineStat,2,"fsinfos","netInfInfos");
        beanForm.setSizeFull();
        beanForm.setCellRender(this);
        content.addComponent(beanForm);
        content.setExpandRatio(beanForm, 1.0f);
        //
        fsinfoTable=new BeanTable<MachineStat.FSInfo>("FSInfo", 
        		FSInfo.class);
        fsinfoTable.setWidth(100.f,Unit.PERCENTAGE);
        fsinfoTable.setHeight(200,Unit.PIXELS);
        fsinfoTable.setCellRender(this);
        content.addComponent(fsinfoTable);
        //
        networkInfoTable=new BeanTable<MachineStat.NetInfInfo>("NetInfInfo", 
        		MachineStat.NetInfInfo.class);
        networkInfoTable.setWidth(100.f,Unit.PERCENTAGE);
        networkInfoTable.setHeight(200,Unit.PIXELS);
        networkInfoTable.setCellRender(this);
        content.addComponent(networkInfoTable);
        //
        HorizontalLayout footer = new HorizontalLayout();
        footer.addStyleName(ValoTheme.WINDOW_BOTTOM_TOOLBAR);
        footer.setWidth(100.0f, Unit.PERCENTAGE);

        Button ok = new Button("Close");
        ok.addStyleName(ValoTheme.BUTTON_PRIMARY);
        ok.addClickListener(e->close());
        ok.focus();
        footer.addComponent(ok);
        footer.setComponentAlignment(ok, Alignment.TOP_RIGHT);
        content.addComponent(footer);
        //
        loadData();
    }
	//
	private void loadData(){
		Jazmin.execute(()->{
			try {
				loadData0();
			} catch (Exception e) {
				e.printStackTrace();
			}	
		});
	}
	//
	private void loadData0()throws Exception{
		machineStat.getMachineInfo(machine.publicHost
				,machine.sshPort,
				machine.sshUser,
				machine.sshPassword);
		getUI().access(()->{
			setCaption(machine.id);
			beanForm.setBean(machineStat);
			fsinfoTable.setData(machineStat.fsinfos);
			networkInfoTable.setData(new ArrayList<MachineStat.NetInfInfo>(machineStat.netInfInfos.values()));
		});
	}
	@Override
	public String renderCell(String propertyName, Object value) {
		String result=value+"";
		switch (propertyName) {
		case "upTime":
			Long upTime=(Long) value;
			Duration d=Duration.of(upTime,ChronoUnit.SECONDS);
			result= d.toString();
			break;
		case "memTotal":
		case "memFree":
		case "memCached":
		case "memBuffers":
		case "memSwapFree":
		case "memSwapTotal":
		//
		case "used":
		case "free":
		case "rx":
		case "tx":
			Long memoryBytes=(Long) value;
			result=DumpUtil.byteCountToString(memoryBytes);
			break;
		default:
			break;
		}
		return result;
	}
}
