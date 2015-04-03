/**
 * 
 */
package jazmin.deploy.ui;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.vaadin.ui.AbstractField;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;

/**
 * @author yama
 * 9 Jan, 2015
 */
@SuppressWarnings("serial")
public class InputBeanForm<T> extends HorizontalLayout {
	private Class<T> beanClass;
	private Map<String,AbstractField<?>>fieldMap;
	private List<FormLayout>formList;
	private int columnCount;
	private Set<String>excludeSet;
	public InputBeanForm(String captain,
			Class<T>beanClass,
			int columnCount,
			String ...excludeProperity) {
		this.beanClass=beanClass;
		this.columnCount=columnCount;
		excludeSet=new TreeSet<String>(Arrays.asList(excludeProperity));
		initUI();
	}
	//
	private void initUI(){
		fieldMap=new HashMap<String, AbstractField<?>>();
		formList=new ArrayList<FormLayout>();
		for(int i=0;i<columnCount;i++){
			FormLayout fl=new FormLayout();
			fl.addStyleName(ValoTheme.FORMLAYOUT_LIGHT);
			fl.setSizeFull();
			formList.add(fl);
			addComponent(fl);
		}
		//
		Field ff[]=beanClass.getDeclaredFields();
		int idx=0;
		for(Field f:ff){
			String fieldName=f.getName();
			if(Modifier.isPublic(f.getModifiers())
					&&!Modifier.isStatic(f.getModifiers())
					&&!excludeSet.contains(fieldName)){
				try{
					AbstractField<?> c=createComponent(f);
					if(c!=null){
						fieldMap.put(fieldName, c);
						c.setCaption(fieldName);
						formList.get(idx++).addComponent(c);
						if(idx>=formList.size()){
							idx=0;
						}	
					}
				}catch(Exception e){}
			}
		}
	}
	//
	private AbstractField<?> createComponent(Field f){
		Class<?>fieldType=f.getType();
		if(fieldType.equals(int.class)){
			fieldType=Integer.class;
		}
		if(fieldType.equals(float.class)){
			fieldType=Float.class;
		}
		if(fieldType.equals(double.class)){
			fieldType=Double.class;
		}
		if(fieldType.equals(long.class)){
			fieldType=Long.class;
		}
		if(fieldType.equals(char.class)){
			fieldType=Character.class;
		}
		if(fieldType.equals(byte.class)){
			fieldType=Byte.class;
		}
		if(fieldType.equals(boolean.class)){
			fieldType=Boolean.class;
		}
		//
		if(fieldType.equals(String.class)
				||fieldType.equals(Character.class)){
			return new TextField();
		}
		if(fieldType.equals(Date.class)){
			return new DateField();
		}
		if(fieldType.equals(Boolean.class)){
			return new CheckBox();
		}
		if(fieldType.equals(Integer.class)
				||fieldType.equals(Float.class)
				||fieldType.equals(Double.class)
				||fieldType.equals(Long.class)){
			TextField ff=new TextField("0");
			ff.setConverter(fieldType);
			return ff;
		}
		return null;
	}
	//
	public void validate(){
		for(AbstractField<?> c:fieldMap.values()){
			c.validate();
		}
	}
	//
	public boolean isValid(){
		for(AbstractField<?> c:fieldMap.values()){
			if(!c.isValid()){
				return false;
			}
		}
		return true;
	}
	//
	public T getBean(){
		try {
			T ret=beanClass.newInstance();
			fieldMap.forEach((k,c)->{
				try {
					Field f=beanClass.getField(k);
					if(c instanceof TextField){
						TextField tf=(TextField)c;
						f.set(ret, tf.getConvertedValue());
					}
					if(c instanceof DateField){
						DateField df=(DateField)c;
						f.set(ret, df.getValue());
					}
					if(c instanceof CheckBox){
						CheckBox df=(CheckBox)c;
						f.set(ret, df.getValue());
					}
				} catch (Exception e) {
				}
				
			});
			return ret;
		} catch (Exception e) {
		}
		return null;
	}

}
