/**
 * 
 */
package jazmin.deploy.ui;


import jazmin.deploy.domain.Instance;
import jazmin.util.DumpUtil;

import com.vaadin.ui.Button;
import com.vaadin.ui.VerticalLayout;



/**
 * @author yama
 * 9 Jan, 2015
 */
@SuppressWarnings("serial")
public class TestView extends VerticalLayout{
	public TestView() {
		setSizeFull();
		Instance iii=new Instance();
		iii.id="xxxxxxx";
		addComponent(new StaticBeanForm<Instance>(iii,2));
		InputBeanForm<Instance> ib=new InputBeanForm<>("test",Instance.class,2);
		addComponent(ib);
		Button btn=new Button("test");
		btn.addClickListener((e)->{System.out.println(DumpUtil.dump(ib.getBean()));});
		addComponent(btn);
	}
}
