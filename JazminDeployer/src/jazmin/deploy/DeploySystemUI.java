package jazmin.deploy;

import jazmin.deploy.domain.User;
import jazmin.deploy.view.main.LoginView;
import jazmin.deploy.view.main.MainView;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.Widgetset;
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
	public static void showMainView(){
		get().setContent(new MainView());
	}
	//
	public static void showLoginView(){
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
		Notification n=new Notification(caption, description);
		n.setPosition(Position.TOP_CENTER);
		n.setHtmlContentAllowed(true);
		n.setStyleName("dark");
		n.show(Page.getCurrent());
	}
	//
	private  static DeploySystemUI get(){
		return (DeploySystemUI) getCurrent();
	}
}
