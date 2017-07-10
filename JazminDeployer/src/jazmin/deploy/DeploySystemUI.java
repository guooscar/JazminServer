package jazmin.deploy;

import jazmin.deploy.domain.User;
import jazmin.deploy.view.main.LoginView;
import jazmin.deploy.view.main.MainView;
import jazmin.deploy.webnotifications.WebNotification;

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
	private static String clientIpAddress;
	private static String userId;
	//
	private WebNotification webNotification;
	//
	@Override
    protected void init(VaadinRequest request) {
		clientIpAddress=getAddress(request);
		webNotification=new WebNotification(this);
		Responsive.makeResponsive(this);
		addStyleName(ValoTheme.UI_WITH_MENU);
		if(getUser()==null){
			showLoginView();	
		}else{
			webNotification.requestPermission();
			showMainView();		
		}
	}
	//
	private static String getAddress(VaadinRequest request){
		String ret=null;
		ret=request.getHeader("X-Forwarded-For");
		if(ret==null||ret.trim().isEmpty()){
			ret=request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if(ret==null||ret.trim().isEmpty()){
			ret=request.getRemoteAddr();
		}
		return ret;
	}
	//
	public void showWebNotification(String title,String content){
		webNotification.show(title, content,"/image/jazmin-logo.png");
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
	public static String getClientIpAddress(){
		return clientIpAddress;
	}
	//
	public static String getUserId(){
		return userId;
	}
	//
	public static void setUser(User user){
		VaadinSession vs=VaadinSession.getCurrent();
		vs.setAttribute(User.class, user);
		vs.getSession().setAttribute("user",user);
		if(user!=null){
			userId=user.id;
		}else{
			userId="";
		}
	}
	//
	public static void showNotificationInfo(String caption,String description){
		showInfo(caption.toLowerCase()+":"+description);
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
        success.setStyleName("warning system");
        success.setPosition(Position.MIDDLE_CENTER);
        success.show(Page.getCurrent());
	}
	//
	@SuppressWarnings("serial")
	public static void setupErrorHandler(){
		//
		UI.getCurrent().setErrorHandler(new DefaultErrorHandler() {
		    @Override
		    public void error(com.vaadin.server.ErrorEvent event) {
		        String cause = "";
		        for (Throwable t = event.getThrowable(); t != null;
		             t = t.getCause()){
		            if (t.getCause() == null) {
		                cause += t.getMessage() + "<br/>";
				        showInfo(cause);
				        t.printStackTrace();	
				            	
		            }
		        }
		    } 
		});
	}	
}
