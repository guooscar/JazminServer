/**
 * 
 */
package jazmin.deploy.view.instance;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.vaadin.aceeditor.AceMode;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;

import jazmin.core.Jazmin;
import jazmin.deploy.DeploySystemUI;
import jazmin.deploy.domain.Application;
import jazmin.deploy.domain.Instance;
import jazmin.deploy.domain.Machine;
import jazmin.deploy.domain.monitor.MonitorInfo;
import jazmin.deploy.domain.optlog.OptLog;
import jazmin.deploy.domain.svn.WorkingCopy;
import jazmin.deploy.manager.DeployManager;
import jazmin.deploy.manager.MonitorManager;
import jazmin.deploy.manager.OptLogManager;
import jazmin.deploy.ui.BeanTable;
import jazmin.deploy.view.main.CodeEditorCallback;
import jazmin.deploy.view.main.CodeEditorWindow;
import jazmin.deploy.view.main.ConfirmWindow;
import jazmin.deploy.view.main.DeployBaseView;
import jazmin.deploy.view.main.InputWindow;
import jazmin.deploy.view.main.TaskProgressWindow;
import jazmin.deploy.view.main.WebSshWindow;

/**
 * @author yama 6 Jan, 2015
 */
@SuppressWarnings("serial")
public class InstanceInfoView extends DeployBaseView {
	//
	BeanTable<Instance> table;
	CheckBox optOnSelectCheckBox;
	List<Instance> instanceList;
	//
	public InstanceInfoView() {
		super();
		initUI();
		searchTxt.setValue("1=1 order by priority desc");
	}

	//
	protected void initBaseUI() {
		setSizeFull();
		//
		HorizontalLayout optLayout = new HorizontalLayout();
		optLayout.setSpacing(true);
		optLayout.addStyleName(ValoTheme.WINDOW_TOP_TOOLBAR);
		optLayout.setWidth(100.0f, Unit.PERCENTAGE);
		searchTxt = new TextField("Filter", "");
		searchTxt.setIcon(FontAwesome.SEARCH);
		searchTxt.setWidth(100.0f, Unit.PERCENTAGE);
		searchTxt.addStyleName(ValoTheme.TEXTFIELD_SMALL);
		searchTxt.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
		searchTxt.addShortcutListener(new ShortcutListener("Search", KeyCode.ENTER, null) {
			@Override
			public void handleAction(Object sender, Object target) {
				loadData();
			}
		});
		//
		optLayout.addComponent(searchTxt);
		optLayout.setExpandRatio(searchTxt, 1.0f);
		//
		optOnSelectCheckBox = new CheckBox("Only Selected");
		optOnSelectCheckBox.addStyleName(ValoTheme.COMBOBOX_SMALL);
		optOnSelectCheckBox.setValue(true);
		optLayout.addComponent(optOnSelectCheckBox);
		optLayout.setComponentAlignment(optOnSelectCheckBox, Alignment.BOTTOM_RIGHT);
		//
		Button ok = new Button("Query");
		ok.addStyleName(ValoTheme.BUTTON_SMALL);
		ok.addStyleName(ValoTheme.BUTTON_PRIMARY);
		optLayout.addComponent(ok);
		ok.addClickListener(e -> loadData());
		optLayout.setComponentAlignment(ok, Alignment.BOTTOM_RIGHT);

		//
		addComponent(optLayout);

		BeanTable<?> table = createTable();
		addComponent(table);
		table.setSizeFull();
		setExpandRatio(table, 1);
		tray = new HorizontalLayout();
		tray.setWidth(100.0f, Unit.PERCENTAGE);
		tray.addStyleName(ValoTheme.WINDOW_BOTTOM_TOOLBAR);
		tray.setSpacing(true);
		tray.setMargin(true);
		//
		Label emptyLabel = new Label("");
		tray.addComponent(emptyLabel);
		tray.setComponentAlignment(emptyLabel, Alignment.MIDDLE_RIGHT);
		tray.setExpandRatio(emptyLabel, 1.0f);
		//
		addComponent(tray);
	}

	//
	@Override
	public BeanTable<?> createTable() {
		instanceList = new ArrayList<Instance>();
		table = new BeanTable<Instance>(null, Instance.class, "machine", "user", "password", "application",
				"properties");
		table.setCellStyleGenerator(new Table.CellStyleGenerator() {
			@Override
			public String getStyle(Table source, Object itemId, Object propertyId) {
				if (propertyId == null) {
					return null;
				}
				if (propertyId.equals("isAlive")) {
					Boolean isTrue = Boolean
							.valueOf(source.getItem(itemId).getItemProperty(propertyId).getValue() + "");
					return isTrue ? "green" : "red";
				} else {
					return null;
				}
			}
		});
		table.setMultiSelect(true);
		return table;
	}

	//
	private void initUI() {
		addOptButton("Console", ValoTheme.BUTTON_PRIMARY, (e) -> viewConsole());
		addOptButton("Monitor", ValoTheme.BUTTON_PRIMARY, (e) -> viewMonitor());
		addOptButton("TailLog", null, (e) -> viewTailLog());
		addOptButton("Detail", null, (e) -> viewDetail());
		addOptButton("BootFile", null, (e) -> viewBootFile());
		addOptButton("Test", null, (e) -> testInstance());
		//
		addOptButton("SetVer", ValoTheme.BUTTON_PRIMARY, (e) -> setPackageVersion());
		addOptButton("SetTag", ValoTheme.BUTTON_PRIMARY, (e) -> setScmTag());
		//
		addOptButton("Create", ValoTheme.BUTTON_DANGER, (e) -> createInstance());
		addOptButton("Start", ValoTheme.BUTTON_DANGER, (e) -> startInstance());
		addOptButton("Stop", ValoTheme.BUTTON_DANGER, (e) -> stopInstance());
		addOptButton("Restart", ValoTheme.BUTTON_DANGER, (e) -> restartInstance());
	}

	private void viewConsole() {
		Instance instance = table.getSelectValue();
		if (instance == null) {
			DeploySystemUI.showNotificationInfo("Info", "Please choose which instance to view.");
		} else {
			if (instance.application == null) {
				DeploySystemUI.showNotificationInfo("Info", "Can not find application on instance:" + instance.id);
				return;
			}
			//
			if (instance.application.type.equals(Application.TYPE_HAPROXY)) {
				InstanceHaproxyStatWindow window = new InstanceHaproxyStatWindow(instance);
				UI.getCurrent().addWindow(window);
				window.focus();
				return;
			}
			//
			if (instance.application.type.equals(Application.TYPE_MYSQL)) {
				InstanceMySQLWindow window = new InstanceMySQLWindow(instance);
				UI.getCurrent().addWindow(window);
				window.focus();
				return;
			}
			//
			if (instance.application.type.equals(Application.TYPE_MEMCACHED)) {
				InstanceMemcachedWindow window = new InstanceMemcachedWindow(instance);
				UI.getCurrent().addWindow(window);
				window.focus();
				return;
			}
			//
			if (instance.application.type.startsWith("jazmin")) {
				Machine machine=instance.machine;
				Machine fakeMachine=new Machine();
				Map<String,String>p=instance.properties;
				fakeMachine.publicHost=machine.publicHost;
				fakeMachine.sshUser=p.getOrDefault(Instance.P_JAZMIN_CONSOLE_USER, "jazmin");
				fakeMachine.sshPassword=p.getOrDefault(Instance.P_JAZMIN_CONSOLE_PWD, "jazmin");
				fakeMachine.sshPort=instance.port+10000;
				String token=DeployManager.createOneTimeSSHToken(fakeMachine,false,true,null);
				WebSshWindow bfw = new WebSshWindow(token);
				bfw.setCaption("jazmin@"+instance.id);
				UI.getCurrent().addWindow(bfw);
				bfw.focus();
				return;
			}
			//
			DeploySystemUI.showNotificationInfo("Info", "Not support application type:" + instance.application.type);

		}
	}

	//
	private void viewDetail() {
		Instance instance = table.getSelectValue();
		if (instance == null) {
			DeploySystemUI.showNotificationInfo("Info", "Please choose which instance to view.");
		} else {
			InstanceDetailWindow bfw = new InstanceDetailWindow(instance);
			UI.getCurrent().addWindow(bfw);
			bfw.focus();
		}
	}

	//
	private void viewBootFile() {
		Instance instance = table.getSelectValue();
		if (instance == null) {
			DeploySystemUI.showNotificationInfo("Info", "Please choose which instance to view.");
		} else {
			String result = DeployManager.renderTemplate(instance.id);
			if (result == null) {
				DeploySystemUI.showNotificationInfo("Info", "Can not found instance boot file");
				return;
			}
			CodeEditorWindow cew = new CodeEditorWindow(new CodeEditorCallback() {
				@Override
				public String reload() {
					String result = DeployManager.renderTemplate(instance.id);
					return result;
				}

				@Override
				public void onSave(String value) {

				}
			});
			cew.setValue("BootFile-" + instance.id, result, AceMode.javascript);
			cew.setReadonly(true);
			UI.getCurrent().addWindow(cew);
			cew.focus();
		}
	}

	//
	private void viewTailLog() {
		Instance instance = table.getSelectValue();
		if (instance == null) {
			DeploySystemUI.showNotificationInfo("Info", "Please choose which instance to view.");
		} else {
			String token=DeployManager.createOneTimeSSHToken(
					instance.machine,
					false,
					false,
					"tail -f "+
					instance.machine.jazminHome+"log/"+
					instance.id+".log");
			WebSshWindow bfw=new WebSshWindow(token);
			bfw.setCaption(instance.id+"-log");
			UI.getCurrent().addWindow(bfw);
			bfw.focus();
		}
	}

	//
	@Override
	public void loadData() {
		String search = getSearchValue();
		if (search == null) {
			return;
		}
		try {
			instanceList = DeployManager.getInstances(DeploySystemUI.getUser().id, search);
			if (instanceList.isEmpty()) {
				DeploySystemUI.showNotificationInfo("Result", "No mactch result found.");
			}
			table.setBeanData(instanceList);
		} catch (Throwable e1) {
			DeploySystemUI.showNotificationInfo("Error", e1.getMessage());
		}
	}

	//
	public List<Instance> getOptInstances() {
		if (optOnSelectCheckBox.getValue()) {
			return table.getSelectValues();
		} else {
			return instanceList;
		}
	}

	//
	private void testInstance() {
		TaskProgressWindow optWindow = new TaskProgressWindow(window -> {
			Jazmin.execute(() -> {
				testInstance0(window);
			});
		});
		optWindow.setCaption("Confirm");
		for (Instance i : getOptInstances()) {
			optWindow.addTask(i.id, "");
		}
		optWindow.setInfo("Confirm test total " + getOptInstances().size() + " instance(s) state?");
		UI.getCurrent().addWindow(optWindow);
	}

	//
	//
	private void testInstance0(TaskProgressWindow window) {
		AtomicInteger counter = new AtomicInteger();
		getOptInstances().forEach(instance -> {
			window.getUI().access(() -> {
				window.setInfo("test " + instance.id + " " + counter.incrementAndGet() + "/" + getOptInstances().size()
						+ "...");
				window.updateTask(instance.id, "testing...");
			});
			DeployManager.testInstance(instance);
			window.getUI().access(() -> {
				window.setInfo("test " + instance.id + " result:" + instance.isAlive);
				window.updateTask(instance.id, "alive:" + instance.isAlive);
			});
		});
		window.getUI().access(() -> {
			window.close();
			loadData();
		});
	}

	//
	//
	private void createInstance() {
		createInstacne1();
	}

	//
	private void createInstacne1() {
		TaskProgressWindow optWindow = new TaskProgressWindow(window -> {
			Jazmin.execute(() -> {
				createInstance0(window);
			});
		});
		optWindow.setCaption("Confirm");
		for (Instance i : getOptInstances()) {
			optWindow.addTask(i.id, "");
		}

		optWindow.setInfo("Confirm create total " + getOptInstances().size() + " instance(s)?");
		UI.getCurrent().addWindow(optWindow);
	}

	//
	private void createInstance0(TaskProgressWindow window) {
		AtomicInteger counter = new AtomicInteger();
		for (Instance instance : getOptInstances()) {
			if (window.isCancel()) {
				break;
			}
			window.getUI().access(() -> {
				window.setInfo("create " + instance.id + " " + counter.incrementAndGet() + "/"
						+ getOptInstances().size() + "...");
				window.updateTask(instance.id, "creating...");
			});
			final StringBuilder result = new StringBuilder("done");
			try {
				result.append(":" + DeployManager.createInstance(instance));
			} catch (Exception e) {
				result.append(":" + e.getMessage());
			}
			window.getUI().access(() -> {
				window.updateTask(instance.id, result.toString());
			});

		}
		;
		window.getUI().access(() -> {
			window.close();
			DeploySystemUI.showNotificationInfo("Info", "create complete");
		});
	}

	//
	private void startInstance() {
		TaskProgressWindow optWindow = new TaskProgressWindow(window -> {
			Jazmin.execute(() -> {
				startInstance0(window);
			});
		});
		//
		boolean hasMysql=false;
		//
		for (Instance i : getOptInstances()) {
			if(i.application.type.equals(Application.TYPE_MYSQL)){
				hasMysql=true;
			}
			optWindow.addTask(i.id, "");
		}
		optWindow.setCaption("Confirm");
		optWindow.setInfo("Confirm start total " + getOptInstances().size() + " instance(s)?");
		if(hasMysql){
			ConfirmWindow cw=new ConfirmWindow((c)->{
				UI.getCurrent().addWindow(optWindow);
			});
			cw.setCaption("Confirm");
			cw.setInfo("database instance in operate queue\nCONFIRM YOUR OPERATION!");
			UI.getCurrent().addWindow(cw);
		}else{
			UI.getCurrent().addWindow(optWindow);			
		}
	}

	//
	//
	private void startInstance0(TaskProgressWindow window) {
		AtomicInteger counter = new AtomicInteger();
		AtomicInteger waitCounter = new AtomicInteger();
		for (Instance instance : getOptInstances()) {
			if (window.isCancel()) {
				break;
			}
			//
			OptLogManager.addOptLog(OptLog.OPT_TYPE_START_INSTANCE,instance.id);
			//
			window.getUI().access(() -> {
				window.setInfo("start " + instance.id + " " + counter.incrementAndGet() + "/" + getOptInstances().size()
						+ "...");
				window.updateTask(instance.id, "starting...");
			});
			waitCounter.set(0);
			final StringBuilder result = new StringBuilder("done");
			boolean error = false;
			try {
				result.append(":" + DeployManager.startInstance(instance));
			} catch (Exception e1) {
				error = true;
				result.append(":" + e1.getMessage());
			}
			if (error) {
				window.getUI().access(() -> {
					window.updateTask(instance.id, result.toString());
				});
			}
			while (waitCounter.get() < 30 && !error) {
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (Exception e) {
				}
				window.getUI().access(() -> {
					window.setInfo("wait " + instance.id + " " + waitCounter.incrementAndGet() + " seconds");
					window.updateTask(instance.id, waitCounter.intValue() + " seconds");
				});
				DeployManager.testInstance(instance);
				if (instance.isAlive) {
					break;
				}
			}
			if (!error) {
				// wait for 15 seconds still not response maybe error happened
				if (!instance.isAlive) {
					window.getUI().access(() -> DeploySystemUI.showNotificationInfo("Error",
							instance.id + " not response after 30 seconds"));
					window.updateTask(instance.id, "not response");
				} else {
					window.getUI().access(() -> window.updateTask(instance.id, "done"));
				}
			}
		}
		window.getUI().access(() -> {
			window.close();
			DeploySystemUI.showNotificationInfo("Info", "start complete");
			DeploySystemUI.get().showWebNotification("JazminDeployer","start complete");
		});
	}

	//
	//
	private void stopInstance() {
		TaskProgressWindow optWindow = new TaskProgressWindow(window -> {
			Jazmin.execute(() -> {
				stopInstance0(window, true);
			});
		});
		for (Instance i : getOptInstances()) {
			optWindow.addTask(i.id, "");
		}
		optWindow.setCaption("Confirm");
		optWindow.setInfo("Confirm stop total " + getOptInstances().size() + " instance(s)?");
		UI.getCurrent().addWindow(optWindow);
	}

	//
	private void stopInstance0(TaskProgressWindow window, boolean stopWindow) {
		AtomicInteger counter = new AtomicInteger();
		for (Instance instance : getOptInstances()) {
			if (window.isCancel()) {
				break;
			}
			//
			OptLogManager.addOptLog(OptLog.OPT_TYPE_STOP_INSTANCE,instance.id);
			//
			window.getUI().access(() -> {
				window.setInfo("stop " + instance.id + " " + counter.incrementAndGet() + "/" + getOptInstances().size()
						+ "...");
				window.updateTask(instance.id, "stopping...");
			});
			final StringBuilder result = new StringBuilder("done");
			try {
				result.append(":" + DeployManager.stopInstance(instance));
			} catch (Exception e) {
				result.append(":" + e.getMessage());
			}
			window.getUI().access(() -> {
				window.updateTask(instance.id, result.toString());
			});
		}
		window.getUI().access(() -> {
			if (stopWindow) {
				window.close();
			}
			DeploySystemUI.showNotificationInfo("Info", "stop complete");
			DeploySystemUI.get().showWebNotification("Info","JazminDeployer complete");
		});
	}

	//
	private void restartInstance() {
		TaskProgressWindow optWindow = new TaskProgressWindow(window -> {
			Jazmin.execute(() -> {
				stopInstance0(window, false);
				startInstance0(window);
			});
		});
		boolean hasMySql=false;
		for (Instance i : getOptInstances()) {
			optWindow.addTask(i.id, "");
			if(i.application.type.equals(Application.TYPE_MYSQL)){
				hasMySql=true;
			}
		}
		optWindow.setCaption("Confirm");
		optWindow.setInfo("Confirm restart total " + getOptInstances().size() + " instance(s)?");
		if(hasMySql){
			ConfirmWindow cw=new ConfirmWindow((c)->{
				UI.getCurrent().addWindow(optWindow);
			});
			cw.setCaption("Confirm");
			cw.setInfo("database instance in operate queue\nCONFIRM YOUR OPERATION!");
			UI.getCurrent().addWindow(cw);
		}else{
			UI.getCurrent().addWindow(optWindow);			
		}
	}

	//
	private void setPackageVersion() {
		InputWindow sw = new InputWindow(window -> {
			String version = window.getInputValue();
			try {
				DeployManager.setPackageVersion(getOptInstances(), version);
				DeployManager.saveInstanceConfig();
				DeploySystemUI.showNotificationInfo("info", "Package version set to " + version);
				loadData();
			} catch (Exception e) {
				DeploySystemUI.showNotificationInfo("error", e.getMessage());
			}
			window.close();
		});
		sw.setCaption("Change instance package version");
		sw.setInfo("Change " + getOptInstances().size() + " instance(s) package version");
		UI.getCurrent().addWindow(sw);
	}
	
	private void setScmTag() {
		List<Instance> instances=getOptInstances();
		if(instances.size()==0){
			DeploySystemUI.showInfo("Please select instances");
			return;
		}
		for (Instance instance : instances) {
			Application app=DeployManager.getApplicationById(instance.appId);
			if(app.scmPath==null||app.scmPath.isEmpty()){
				DeploySystemUI.showInfo(app.id+" scmPath cannot be null");
				return;
			}
			if(app.scmPath.lastIndexOf("/")==-1){
				DeploySystemUI.showInfo(app.id+" scmPath error."+app.scmPath);
				return;
			}
		}
		//
		InputWindow sw = new InputWindow(window -> {
			String tag = window.getInputValue();
			if(tag==null||tag.trim().length()==0){
				DeploySystemUI.showInfo("tag cannot be null");
				return;
			}
			try {
				for (Instance instance : instances) {
					Application app=DeployManager.getApplicationById(instance.appId);
					String targetUrl=app.scmPath.substring(0,app.scmPath.lastIndexOf("/"))+"/JazminTags/"+app.id+"/"+tag;
					WorkingCopy wc=new WorkingCopy(
							app.scmUser, 
							app.scmPassword,
							app.scmPath,null);
					wc.copy(targetUrl,"setScmTag "+tag);
				}
				DeploySystemUI.showNotificationInfo("info", "set tag success." + tag);
				
			} catch (Exception e) {
				DeploySystemUI.showNotificationInfo("error", e.getMessage());
			}
			window.close();
		});
		sw.setCaption("SetScmTag");
		sw.setInfo("Set " + getOptInstances().size() + " instance(s) scmTag");
		UI.getCurrent().addWindow(sw);
	}

	private void viewMonitor() {
		List<Instance> instanceList = this.table.getSelectValues();
		if (instanceList == null) {
			return;
		}
		Map<String, List<MonitorInfo>> dataMap = new LinkedHashMap<>();
		for (Instance instance : instanceList) {
			List<MonitorInfo> datas = MonitorManager.get().getMonitorInfos(instance.id);
			if (datas.isEmpty()) {
				continue;
			}
			datas.sort((a, b) -> {
				return a.name.compareTo(b.name);
			});
			dataMap.put(instance.id, datas);
		}
		if (dataMap.isEmpty()) {
			DeploySystemUI.showInfo("No Monitor Data");
			return;
		}
		MonitorSelectWindow window = new MonitorSelectWindow(dataMap);
		UI.getCurrent().addWindow(window);
	}

}
