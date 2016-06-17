package jazmin.deploy.view.instance;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Window;

public class MonitorWindow extends Window {
	private static final long serialVersionUID = 1L;

	private BrowserFrame frame;
	private String instance;
	private String keyvalues;
	private String charts;

	public MonitorWindow(String instance, String keyvalues, String charts) {
		this.instance = instance;
		this.keyvalues = keyvalues;
		this.charts = charts;
		initUI();
	}

	private void initUI() {
		center();
		setCloseShortcut(KeyCode.ESCAPE, null);
		setResizable(true);
		setClosable(true);
		setCaption("[" + instance + "]Monitor");
		setHeight("90%");
		setWidth("1010px");
		this.frame = new BrowserFrame(null,
				new ExternalResource("/srv/monitor/view?instance=" + instance + "&charts=" + charts + "&keyvalues=" + keyvalues));
		this.frame.setImmediate(true);
		this.frame.setSizeFull();
		setContent(frame);
	}
}
