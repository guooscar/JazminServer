package jazmin.deploy.view.instance;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Window;

public class MonitorWindow extends Window {
	private static final long serialVersionUID = 1L;

	private BrowserFrame frame;
	private String instance;

	public MonitorWindow(String instance) {
		this.instance = instance;
		initUI();
	}

	private void initUI() {
		center();
		setCloseShortcut(KeyCode.ESCAPE, null);
		setResizable(true);
		setClosable(true);
		setCaption("[" + instance + "]监控");
		setHeight("90%");
		setWidth("1000px");
		this.frame = new BrowserFrame(null, new ExternalResource("/srv/monitor/view?instance=" + instance));
		this.frame.setImmediate(true);
		this.frame.setSizeFull();
		setContent(frame);
	}
}
