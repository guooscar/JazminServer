package jazmin.deploy.view.main;

import java.util.ArrayList;
import java.util.List;

import jazmin.deploy.DeploySystemUI;
import jazmin.deploy.domain.User;
import jazmin.deploy.manager.DeployManager;
import jazmin.deploy.util.OtpUtil;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

import org.vaadin.aceeditor.AceMode;

import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;

/**
 * A responsive menu component providing user information and the controls for
 * primary navigation between the views.
 */
public final class MainMenu extends CustomComponent {
	private static Logger logger=LoggerFactory.get(MainMenu.class);
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String ID = "dashboard-menu";
	private static final String REPORTS_BADGE_ID = "dashboard-menu-reports-badge";
	private static final String STYLE_VISIBLE = "valo-menu-visible";
    private MenuItem settingsItem;
    private MainView mainView;
    private CssLayout menuItemsLayout;
    private List<ValoMenuItemButton>allMenus=new ArrayList<MainMenu.ValoMenuItemButton>();
    
    //
    public MainMenu(MainView mainView) {
    	this.mainView=mainView;
    	addStyleName("valo-menu");
        setId(ID);
        setSizeUndefined();
        setCompositionRoot(buildContent());
    }
    //
    private Component buildContent() {
        final CssLayout menuContent = new CssLayout();
        menuContent.addStyleName("sidebar");
        menuContent.addStyleName(ValoTheme.MENU_PART);
        menuContent.addStyleName("no-vertical-drag-hints");
        menuContent.addStyleName("no-horizontal-drag-hints");
        menuContent.setWidth(null);
        menuContent.setHeight("100%");
        //
        menuContent.addComponent(buildTitle());
        menuContent.addComponent(buildUserMenu());
        menuContent.addComponent(buildToggleButton());
        menuContent.addComponent(buildMenuItems());
        //
        return menuContent;
    }

    private Component buildTitle() {
        Label logo = new Label("<strong>Jazmin</strong> Deployer",ContentMode.HTML);
        logo.setSizeUndefined();
        HorizontalLayout logoWrapper = new HorizontalLayout(logo);
        logoWrapper.setComponentAlignment(logo, Alignment.MIDDLE_CENTER);
        logoWrapper.addStyleName("valo-menu-title");
        return logoWrapper;
    }
    //
    private Component buildUserMenu() {
        final MenuBar settings = new MenuBar();
        settings.addStyleName("user-menu");
        settingsItem = settings.addItem("", new ThemeResource("img/profile-pic-300px.jpg"), null);
        updateUserName();
        if(DeploySystemUI.getUser().id.equals(User.ADMIN)){
        	settingsItem.addItem("Reload Config", (selectedItem)->{
            	DeployManager.reload();
            	DeploySystemUI.showNotificationInfo("Info","Config reload complete.");
            	if(!DeployManager.getErrorMessage().isEmpty()){
                  	ErrorMessageWindow bfw=new ErrorMessageWindow();
          			UI.getCurrent().addWindow(bfw);
          			bfw.focus();
                }
            });
        	settingsItem.addSeparator();
            //
            settingsItem.addItem("Machine Config", (selectedItem)->showConfig("machine.json"));
            settingsItem.addItem("Application Config", (selectedItem)->showConfig("application.json"));
            settingsItem.addItem("Instance Config", (selectedItem)->showConfig("instance.json"));
            settingsItem.addItem("Iptables Config", (selectedItem)->showConfig("iptables.rule"));
            settingsItem.addItem("Job Config", (selectedItem)->showConfig("job.json"));
            settingsItem.addItem("Webhook Config", (selectedItem)->showConfig("webhook.json"));
            settingsItem.addSeparator();
        }
        settingsItem.addItem("Robots", (selectedItem)->{
        	ScriptEditorWindow bfw = new ScriptEditorWindow("robot");
      		UI.getCurrent().addWindow(bfw);
      		bfw.focus();
        });
        //
        settingsItem.addItem("Benchmarks", (selectedItem)->{
        	ScriptEditorWindow bfw = new ScriptEditorWindow("benchmark");
      		UI.getCurrent().addWindow(bfw);
      		bfw.focus();
        });
        //
        settingsItem.addItem("Deployplans", (selectedItem)->{
        	ScriptEditorWindow bfw = new ScriptEditorWindow("deployplan");
      		UI.getCurrent().addWindow(bfw);
      		bfw.focus();
        });
        //
        settingsItem.addSeparator();
        settingsItem.addItem("DebugLog", (selectedItem)->{
        	DebugLogWindow dw=new DebugLogWindow();
      		UI.getCurrent().addWindow(dw);
      		dw.focus();
        });
        settingsItem.addSeparator();
        //
        settingsItem.addItem("Sign Out", (selectedItem)->{
        	DeploySystemUI.setUser(null);
        	DeploySystemUI.get().showLoginView();
        });
        return settings;
    }
    //
    private void showConfig(String file){
    	OtpUtil.call(()->{
    		CodeEditorCallback callback=new CodeEditorCallback() {
    			@Override
    			public String reload() {
    				return null;
    			}
    			
    			@Override
    			public void onSave(String value) {
    				DeployManager.saveConfigFile(file,value);
    			}
    		};
        	CodeEditorWindow cew=new CodeEditorWindow(callback);
    		cew.setValue(file,DeployManager.getConfigFile(file),AceMode.json);
    		UI.getCurrent().addWindow(cew);
    		cew.focus();
    	});
    }
    //
    private Component buildToggleButton() {
        Button valoMenuToggleButton = new Button("Menu",event->{
            if (getCompositionRoot().getStyleName().contains(STYLE_VISIBLE)) {
                getCompositionRoot().removeStyleName(STYLE_VISIBLE);
            } else {
                getCompositionRoot().addStyleName(STYLE_VISIBLE);
            }
        });
        valoMenuToggleButton.setIcon(FontAwesome.LIST);
        valoMenuToggleButton.addStyleName("valo-menu-toggle");
        valoMenuToggleButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        return valoMenuToggleButton;
    }

    private Component buildMenuItems() {
    	menuItemsLayout=new CssLayout();
        menuItemsLayout.addStyleName("valo-menuitems");
        menuItemsLayout.setHeight(100.0f, Unit.PERCENTAGE);
        return menuItemsLayout;
    }
    //
    private Component buildBadgeWrapper(
    		final Component menuItemButton,
            final Component badgeLabel) {
        CssLayout dashboardWrapper = new CssLayout(menuItemButton);
        dashboardWrapper.addStyleName("badgewrapper");
        dashboardWrapper.addStyleName(ValoTheme.MENU_ITEM);
        dashboardWrapper.setWidth(100.0f, Unit.PERCENTAGE);
        badgeLabel.addStyleName(ValoTheme.MENU_BADGE);
        badgeLabel.setWidthUndefined();
        badgeLabel.setVisible(false);
        dashboardWrapper.addComponent(badgeLabel);
        return dashboardWrapper;
    }

    //
    public void updateUserName() {
        User user = DeploySystemUI.getUser();
        settingsItem.setText(user.getId()+"");
    }
    //
    private final class ValoMenuItemButton extends Button {
    	private static final long serialVersionUID = 1L;
	    public ValoMenuItemButton(
	    		String name,
	    		Resource icon,
	    		Class<? extends Component>  view) {
            setPrimaryStyleName("valo-menu-item");
            setIcon(icon);
            setCaption(name);
            addClickListener(event->{
            	try{
            		Object viewObject=view.newInstance();
            		selectMenu(this);
                	
            		mainView.setView((Component)viewObject);
            	}catch(Exception e){
            		logger.catching(e);
            	}
            });
        }
	    public ValoMenuItemButton(
	    		String name,
	    		Resource icon,
	    		Component  view) {
            setPrimaryStyleName("valo-menu-item");
            setIcon(icon);
            setCaption(name);
            addClickListener(event->{
            	try{
            		selectMenu(this);
                	
            		mainView.setView((Component)view);
            	}catch(Exception e){
            		logger.catching(e);
            	}
            });
        }
    }
    //
    private void selectMenu(ValoMenuItemButton btn){
    	allMenus.forEach(m->{m.removeStyleName("selected");});
    	btn.addStyleName("selected");
    }
    //
    public void addMenuItem(String name,Resource icon, Component view){
    	ValoMenuItemButton menuItemComponent = new ValoMenuItemButton(name,icon,view);
    	allMenus.add(menuItemComponent);
    	Label reportsBadge = new Label();
        reportsBadge.setId(REPORTS_BADGE_ID);
        Component t = buildBadgeWrapper(menuItemComponent,
                  reportsBadge);
        menuItemsLayout.addComponent(t);
    }
    //
    public void addMenuItem(String name,Resource icon,Class<? extends Component>viewClass){
    	ValoMenuItemButton menuItemComponent = new ValoMenuItemButton(name,icon,viewClass);
    	allMenus.add(menuItemComponent);
    	Label reportsBadge = new Label();
        reportsBadge.setId(REPORTS_BADGE_ID);
        Component t  = buildBadgeWrapper(menuItemComponent,
                  reportsBadge);
        menuItemsLayout.addComponent(t);
    }
}
