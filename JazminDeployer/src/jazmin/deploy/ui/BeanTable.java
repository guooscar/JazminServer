/**
 * 
 */
package jazmin.deploy.ui;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.ValoTheme;

/**
 * @author yama
 * 8 Jan, 2015
 */
public class BeanTable<T> extends Table{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public BeanTable(String caption,Class<T>beanClass,String ...excludeProperity) {
		super(caption);
		createTable(beanClass,excludeProperity);
	}
	//
	public void createTable(Class<T>beanClass,String ...excludeProperity){
		addStyleName(ValoTheme.TABLE_NO_HORIZONTAL_LINES);
        addStyleName(ValoTheme.TABLE_COMPACT);
        addStyleName(ValoTheme.TABLE_SMALL);
        setSizeFull();
     	setFooterVisible(false);
     	setImmediate(true);
     	setSelectable(true);
     	//
     	Set<String>excludeSet=new TreeSet<String>(Arrays.asList(excludeProperity));
		Field ff[]=beanClass.getFields();
		List<String>showColumns=new ArrayList<String>();
		for(Field f:ff){
			String fieldName=f.getName();
			if(Modifier.isPublic(f.getModifiers())
					&&!Modifier.isStatic(f.getModifiers())
					&&!excludeSet.contains(fieldName)){
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
				showColumns.add(fieldName);
				addContainerProperty(fieldName,fieldType, null);
			}			
		}
		//
		addContainerProperty("$object",beanClass, null);
		setVisibleColumns(showColumns.toArray());
	}
	//
	public void setData(List<T>list){
		removeAllItems();
		setData0(list);
	}
	//
	public void appendData(List<T>list){
		setData0(list);
	}
	//
	@SuppressWarnings("unchecked")
	private void setData0(List<T>list){
		for(Object o:list){
			Object newItemId = addItem();
			Item row = getItem(newItemId);
			Class<?>targetClass=o.getClass();
			row.getItemProperty("$object").setValue(o);
			for(Object id:getContainerPropertyIds()){
				try {
					Field ff=targetClass.getField(id.toString());
					Property<Object> p=row.getItemProperty(id);
					Object oo=ff.get(o);
					if(oo!=null){
						p.setValue(ff.get(o));
					}
				} catch (Exception e) {}	
			}
		}
	}
	//
	@SuppressWarnings("unchecked")
	public T getSelectValue(){
		Object itemId=getValue();
		if(itemId==null){
			return null;
		}
		Item item= getItem(itemId);
		return (T)item.getItemProperty("$object").getValue();
	}
}
