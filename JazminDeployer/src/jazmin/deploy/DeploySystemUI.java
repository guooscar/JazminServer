package jazmin.deploy;

import jazmin.deploy.view.main.LoginView;
import jazmin.deploy.view.main.MainView;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.Position;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;

/**
 * 
 * @author yama
 * 28 Dec, 2014
 */
@Title("JazminDeployer")
@Theme("dashboard")
@Push
public class DeploySystemUI extends UI {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
    protected void init(VaadinRequest request) {
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
	public static UserInfo getUser(){
		return VaadinSession.getCurrent().getAttribute(UserInfo.class);
	}
	//
	public static void setUser(UserInfo user){
		VaadinSession.getCurrent().setAttribute(UserInfo.class, user);
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
