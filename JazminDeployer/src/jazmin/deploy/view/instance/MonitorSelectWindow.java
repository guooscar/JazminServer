package jazmin.deploy.view.instance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import jazmin.deploy.domain.monitor.MonitorInfo;

/**
 *
 * @author ginko.wang
 * @date 2016年6月10日 下午7:28:50
 */
public class MonitorSelectWindow extends Window {
	private static final long serialVersionUID = 1L;

	private Map<String, List<MonitorInfo>> dataMap;
	private Set<String> instanceSet;
	private OptionGroupWithTitleCommponent<String> keyValueCommponent;
	private OptionGroupWithTitleCommponent<String> monitorCommponent;

	public MonitorSelectWindow(Map<String, List<MonitorInfo>> dataMap) {
		this.dataMap = dataMap;
		initUI();
	}

	private void initUI() {
		instanceSet = new TreeSet<>();
		StringBuffer buffer = new StringBuffer();
		Map<String, String> keyValueMap = new LinkedHashMap<>();
		Map<String, String> monitorMap = new LinkedHashMap<>();
		Set<Entry<String, List<MonitorInfo>>> entries = dataMap.entrySet();
		for (Entry<String, List<MonitorInfo>> entry : entries) {
			buffer.append(entry.getKey());
			buffer.append("$");
			instanceSet.add(entry.getKey());
			List<MonitorInfo> datas = entry.getValue();
			for (MonitorInfo data : datas) {
				if (MonitorInfo.CATEGORY_TYPE_KV.equals(data.type)) {
					keyValueMap.put(data.name, data.name);
				} else {
					monitorMap.put(data.name, data.name);
				}
			}
		}
		if (buffer.length() > 1) {
			buffer = buffer.deleteCharAt(buffer.length() - 1);
		}
		String instances = buffer.toString();
		center();
		setCloseShortcut(KeyCode.ESCAPE, null);
		setResizable(false);
		setClosable(true);
		setResponsive(true);
		setCaption("[" + instances + "] Monitor Options");
		setWidth("800px");
		setHeight("700px");
		VerticalLayout contentLayout = new VerticalLayout();
		contentLayout.setSizeFull();
		VerticalLayout monitorLayout = new VerticalLayout();
		monitorLayout.setHeightUndefined();
		this.keyValueCommponent = new OptionGroupWithTitleCommponent<>("Infos", true, keyValueMap);
		this.monitorCommponent = new OptionGroupWithTitleCommponent<>("Charts", true, monitorMap);
		this.keyValueCommponent.checkAll();
		this.monitorCommponent.checkAll();
		monitorLayout.addComponents(keyValueCommponent, monitorCommponent);
		HorizontalLayout footerLayout = new HorizontalLayout();
		footerLayout.addStyleName(ValoTheme.WINDOW_BOTTOM_TOOLBAR);
		footerLayout.setWidth("100%");
		HorizontalLayout optLayout = new HorizontalLayout();
		optLayout.setSpacing(true);
		optLayout.setWidthUndefined();
		Button confirmBtn = new Button("Confirm");
		confirmBtn.addStyleName(ValoTheme.BUTTON_SMALL);
		confirmBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
		confirmBtn.addClickListener(this::confirm);
		Button closeBtn = new Button("Close");
		closeBtn.addStyleName(ValoTheme.BUTTON_SMALL);
		closeBtn.addClickListener(e -> close());
		optLayout.addComponents(confirmBtn, closeBtn);
		footerLayout.addComponent(optLayout);
		footerLayout.setComponentAlignment(optLayout, Alignment.MIDDLE_RIGHT);
		contentLayout.addComponents(monitorLayout, footerLayout);
		contentLayout.setExpandRatio(monitorLayout, 1f);
		setContent(contentLayout);
	}

	private void confirm(Event event) {
		String keyvalues = this.keyValueCommponent.getTagString("$");
		String charts = this.monitorCommponent.getTagString("$");
		MonitorWindow window = new MonitorWindow(this.instanceSet, keyvalues, charts);
		UI.getCurrent().addWindow(window);
		this.close();
	}

	/**
	 * Option Group
	 *
	 * @author ginko.wang
	 * @date 2016-6-10 20:46:30
	 */
	public class OptionGroupWithTitleCommponent<T> extends VerticalLayout {

		private static final long serialVersionUID = 1L;

		private OptionGroup group;
		private CheckBox checkBox;
		private Panel showBtn;
		private String name;
		private Map<T, String> map;
		private List<T> values;
		private boolean defaultUnFold;

		public OptionGroupWithTitleCommponent(String name, Map<T, String> map) {
			super();
			this.name = name;
			this.map = map;
			this.values = new ArrayList<>();
			initUI();
			initData();
		}

		public OptionGroupWithTitleCommponent(String name, boolean defaultUnFold, Map<T, String> map) {
			super();
			this.defaultUnFold = defaultUnFold;
			this.name = name;
			this.map = map;
			this.values = new ArrayList<>();
			initUI();
			initData();
		}

		public void setValues(List<T> values) {
			this.values.clear();
			if (values == null || values.isEmpty()) {
				return;
			}
			this.values.addAll(values);
			for (T value : values) {
				this.group.select(value);
			}
		}

		private void initUI() {
			setSpacing(true);
			setMargin(true);
			Label label = new Label();
			label.addStyleName("section-label");
			label.addStyleName(ValoTheme.LABEL_SMALL);
			label.setCaption(name);
			HorizontalLayout optLayout = new HorizontalLayout();
			optLayout.setWidthUndefined();
			optLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
			checkBox = new CheckBox("Check All");
			checkBox.addStyleName("check-all");
			checkBox.addStyleName(ValoTheme.CHECKBOX_SMALL);
			showBtn = new Panel();
			showBtn.setIcon(FontAwesome.CARET_DOWN);
			showBtn.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
			showBtn.addStyleName(ValoTheme.BUTTON_SMALL);
			showBtn.addStyleName("toggle-btn");
			optLayout.addComponents(checkBox, showBtn);
			group = new OptionGroup();
			group.setWidth("100%");
			group.setMultiSelect(true);
			group.addStyleName("horizontal-optiongroup");
			group.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
			group.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
			if (defaultUnFold) {
				show();
			} else {
				hide();
			}
			addComponents(label, optLayout, group);
			checkBox.addValueChangeListener(new ValueChangeListener() {
				private static final long serialVersionUID = 1L;

				@Override
				public void valueChange(ValueChangeEvent event) {
					boolean checked = (boolean) event.getProperty().getValue();
					Collection<?> itemIds = group.getItemIds();
					itemIds.forEach(itemId -> {
						if (checked) {
							group.select(itemId);
						} else {
							group.unselect(itemId);
						}
					});
				}
			});
			showBtn.addClickListener(callback -> {
				boolean showing = (boolean) showBtn.getData();
				if (showing) {
					hide();
				} else {
					show();
				}
			});
		}

		public void checkAll() {
			this.checkBox.setValue(true);
		}

		public void show() {
			showBtn.setData(true);
			group.setVisible(true);
			showBtn.removeStyleName("rotate-180");
		}

		public void hide() {
			showBtn.setData(false);
			group.setVisible(false);
			showBtn.addStyleName("rotate-180");
		}

		private void initData() {
			map.forEach((key, val) -> {
				group.addItem(key);
				group.setItemCaption(key, val);
			});
		}

		@SuppressWarnings("unchecked")
		/**
		 * 获取选择的值
		 * 
		 * @return
		 */
		public List<T> getValues() {
			Set<T> values = (Set<T>) group.getValue();
			return new ArrayList<>(values);
		}

		/**
		 * 获取标签列表
		 * 
		 * @return
		 */
		public List<String> getTags() {
			List<T> values = getValues();
			List<String> tags = new ArrayList<>();
			for (T t : values) {
				tags.add(map.get(t));
			}
			return tags;
		}

		/**
		 * 获取标签字符串
		 * 
		 * @return
		 */
		public String getTagString(String separator) {
			List<String> tags = getTags();
			if (tags == null || tags.isEmpty()) {
				return "";
			}
			StringBuffer buffer = new StringBuffer();
			for (String value : tags) {
				buffer.append(value);
				buffer.append(separator);
			}
			if (buffer.length() > 0) {
				buffer.deleteCharAt(buffer.length() - 1);
			}
			return buffer.toString();
		}

		@Override
		public void addStyleName(String style) {
			super.addStyleName(style);
			this.group.addStyleName(style);
		}
	}
}
