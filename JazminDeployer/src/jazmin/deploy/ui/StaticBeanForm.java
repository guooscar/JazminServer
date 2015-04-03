/**
 * 
 */
package jazmin.deploy.ui;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.ValoTheme;

/**
 * @author yama
 * 9 Jan, 2015
 */
@SuppressWarnings("serial")
public class StaticBeanForm<T> extends HorizontalLayout {
	private T bean;
	private Map<String,Label>fieldMap;
	private List<FormLayout>formList;
	private int columnCount;
	private Set<String>excludeSet;
	public StaticBeanForm(
			T bean,
			int columnCount,
			String ...excludeProperity) {
		this.bean=bean;
		this.columnCount=columnCount;
		excludeSet=new TreeSet<String>(Arrays.asList(excludeProperity));
		initUI();
	}
	//
	public void setBean(T bean){
		Class<?>beanClass=bean.getClass();
		Field ff[]=beanClass.getFields();
		for(Field f:ff){
			String fieldName=f.getName();
			Label label=fieldMap.get(fieldName);
			if(label!=null){
				Object value;
				try {
					value = f.get(bean);
					if(value!=null){
						label.setValue(value+"");
					}else{
						label.setValue("");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}
	}
	//
	private void initUI(){
		fieldMap=new HashMap<String, Label>();
		formList=new ArrayList<FormLayout>();
		for(int i=0;i<columnCount;i++){
			FormLayout fl=new FormLayout();
			fl.addStyleName(ValoTheme.FORMLAYOUT_LIGHT);
			fl.setSizeFull();
			formList.add(fl);
			addComponent(fl);
		}
		//
		Class<?>beanClass=bean.getClass();
		Field ff[]=beanClass.getFields();
		int idx=0;
		for(Field f:ff){
			String fieldName=f.getName();
			if(Modifier.isPublic(f.getModifiers())
					&&!Modifier.isStatic(f.getModifiers())
					&&!excludeSet.contains(fieldName)){
				try{
					Object value=f.get(bean);
					Label label=new Label();
					label.setCaption(fieldName);
					if(value!=null){
						label.setValue(value+"");		
					}
					fieldMap.put(fieldName, label);
					formList.get(idx++).addComponent(label);
					if(idx>=formList.size()){
						idx=0;
					}
				}catch(Exception e){}
			}
		}
	}
}
