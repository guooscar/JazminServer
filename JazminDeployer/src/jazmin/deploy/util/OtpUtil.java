/**
 * 
 */
package jazmin.deploy.util;

import com.vaadin.ui.UI;

import jazmin.deploy.DeploySystemUI;
import jazmin.deploy.manager.DeployManager;
import jazmin.deploy.view.main.OtpWindow;

/**
 * @author yama
 *
 */
public class OtpUtil {
	public static void call(Runnable r){
		OtpWindow ow=new OtpWindow((c)->{
			String v=c.getInputValue();
			String token=DeployManager.getOTPToken()+"";
			if(token.equals(v)){
				r.run();
			}else{
				DeploySystemUI.showNotificationInfo("OTP error", "Wrong OTP code");
			}
		});
		UI.getCurrent().addWindow(ow);
		ow.focus();
	}
}
