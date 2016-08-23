package jazmin.deploy.webnotifications;

import com.vaadin.annotations.JavaScript;
import com.vaadin.server.AbstractJavaScriptExtension;
import com.vaadin.ui.UI;

@SuppressWarnings("serial")
@JavaScript({"notify.js", "webnotifications-connector.js"})
public class WebNotification extends AbstractJavaScriptExtension {

	public WebNotification(UI ui) {
		extend(ui);
	}
	
	public void requestPermission() {
		callFunction("requestPermission");
	}
	
	public void show(String title, String body,String icon) {
		callFunction("show", title, body,icon);
	}

}
