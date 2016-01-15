package jazmin.deploy;

import jazmin.deploy.domain.User;
import jazmin.deploy.view.main.LoginView;
import jazmin.deploy.view.main.MainView;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.DefaultErrorHandler;
import com.vaadin.server.Page;
import com.vaadin.server.Responsive;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.Position;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;

/**
 * 
 * @author yama
 * 28 Dec, 2014
 */
@Theme("dashboard")
@Widgetset("jazmin.deploy.AppWidgetSet")
@Title("JazminDeployer")
@Push
public class DeploySystemUI extends UI {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//
	private MainView mainView;
	//
	@Override
    protected void init(VaadinRequest request) {
		Responsive.makeResponsive(this);
		addStyleName(ValoTheme.UI_WITH_MENU);
		if(getUser()==null){
			showLoginView();	
		}else{
			showMainView();		
		}
	}
	//
	public  void showMainView(){
		mainView=new MainView();
		get().setContent(mainView);
	}
	//
	public  void showLoginView(){
		get().setContent(new LoginView());
	}
	//
	public static User getUser(){
		VaadinSession vs=VaadinSession.getCurrent();
		if(vs==null){
			return null;
		}
		return VaadinSession.getCurrent().getAttribute(User.class);
	}
	//
	public static void setUser(User user){
		VaadinSession vs=VaadinSession.getCurrent();
		vs.setAttribute(User.class, user);
		vs.getSession().setAttribute("user",user);
	}
	//
	public static void showNotificationInfo(String caption,String description){
		showInfo(caption+":"+description);
	}
	
	/**
	 * @return the mainView
	 */
	public MainView getMainView() {
		return mainView;
	}
	//
	public static DeploySystemUI get(){
		return (DeploySystemUI) getCurrent();
	}
	//
	public static void showInfo(String content){
		Notification success = new Notification(content);
		success.setHtmlContentAllowed(true);
        success.setDelayMsec(2000);
        success.setStyleName("bar success small");
        success.setPosition(Position.TOP_CENTER);
        success.show(Page.getCurrent());
	}
	//
	@SuppressWarnings("serial")
	public static void setupErrorHandler(){
		//
		UI.getCurrent().setErrorHandler(new DefaultErrorHandler() {
		    @Override
		    public void error(com.vaadin.server.ErrorEvent event) {
		        String cause = "<b>ERROR:</b><br/>";
		        for (Throwable t = event.getThrowable(); t != null;
		             t = t.getCause()){
		            if (t.getCause() == null) {
		                cause += t.getClass().getName() + "<br/>";
				        showInfo(cause);
				        t.printStackTrace();	
				            	
		            }
		        }
		    } 
		});
	}	
}
