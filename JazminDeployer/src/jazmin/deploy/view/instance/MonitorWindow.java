package jazmin.deploy.view.instance;

import java.util.Set;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Window;

public class MonitorWindow extends Window {
	private static final long serialVersionUID = 1L;

	private BrowserFrame frame;
	private Set<String> instanceSet;
	private String keyvalues;
	private String charts;

	public MonitorWindow(Set<String> instanceSet, String keyvalues, String charts) {
		this.instanceSet = instanceSet;
		this.keyvalues = keyvalues;
		this.charts = charts;
		initUI();
	}

	private void initUI() {
		StringBuffer buffer = new StringBuffer();
		for (String instance : instanceSet) {
			buffer.append(instance);
			buffer.append("$");
		}
		if (buffer.length() > 1) {
			buffer = buffer.deleteCharAt(buffer.length() - 1);
		}
		String instances = buffer.toString();
		center();
		setCloseShortcut(KeyCode.ESCAPE, null);
		setResizable(true);
		setClosable(true);
		setCaption("[" + instances + "]Monitor");
		setHeight("90%");
		setWidth("1010px");
		this.frame = new BrowserFrame(null,
				new ExternalResource(
						"/srv/monitor/view?instances=" + instances + "&charts=" + charts + "&keyvalues=" + keyvalues));
		this.frame.setImmediate(true);
		this.frame.setSizeFull();
		setContent(frame);
	}
}
