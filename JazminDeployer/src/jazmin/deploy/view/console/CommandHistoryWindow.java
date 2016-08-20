package jazmin.deploy.view.console;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.server.Page;
import com.vaadin.server.WebBrowser;
import com.vaadin.ui.Window;

import jazmin.deploy.ui.BeanTable;

/**
 * 
 * @author yama
 * 13 Aug, 2016
 */
public class CommandHistoryWindow extends Window{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4723453634993653187L;
	//
	private static Set<String>historys=new TreeSet<String>();
	//
	public static class HistoryBean{
		public String cmd;
	}
	private String selectedCommand;
	//
	@SuppressWarnings("serial")
	public CommandHistoryWindow() {
		super();
		setCaption("Command History");
		setWidth("600");
		setHeight("400");
		BeanTable<HistoryBean> table = new BeanTable<HistoryBean>(null, HistoryBean.class);
		setContent(table);
		table.setSizeFull();
		center();
		//
		List<HistoryBean>beans=new ArrayList<CommandHistoryWindow.HistoryBean>();
		for(String s:historys){
			HistoryBean hb=new HistoryBean();
			hb.cmd=s;
			beans.add(hb);
		}
		table.setBeanData(beans);
		//
		table.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				WebBrowser wb=Page.getCurrent().getWebBrowser();
				if(wb.isIPhone()||wb.isAndroid()||event.isDoubleClick()){
					HistoryBean hb=table.getValueByItem(event.getItem());
					selectedCommand=hb.cmd;
					close();
				}
			}
		});
	}
	//
	public String getSelectedCommand(){
		return selectedCommand;
	}
	//
	public static void addHistory(String cmd){
		historys.add(cmd.trim());
	}
	
}
