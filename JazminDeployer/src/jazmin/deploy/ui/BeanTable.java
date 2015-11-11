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
@SuppressWarnings("serial")
public class BeanTable<T> extends Table{
	private CellRender cellRender;
	//
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
				showColumns.add(fieldName);
				addContainerProperty(fieldName,String.class, null);
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
					String idStr=id.toString();
					if(idStr.equals("$object")){
						continue;
					}
					Field ff=targetClass.getField(idStr);
					Property<Object> p=row.getItemProperty(id);
					Object oo=ff.get(o);
					if(oo!=null){
						String result=oo+"";
						if(cellRender!=null){
							result=cellRender.renderCell(idStr,oo);
						}
						p.setValue(result);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}	
			}
		}
	}
	//
	@SuppressWarnings("unchecked")
	public T getItemValue(Item item){
		T obj=(T)item.getItemProperty("$object").getValue();
		return obj;
	}
	//
	@SuppressWarnings("unchecked")
	public List<T> getSelectValues(){
		List<T>result=new ArrayList<T>();
		if(isMultiSelect()){
			Set<Object> itemIds=(Set<Object>) getValue();
			for(Object o:itemIds){
				Item item= getItem(o);	
				T obj=(T)item.getItemProperty("$object").getValue();
				result.add(obj);
			}
		}else{
			Object itemId= getValue();
			if(itemId!=null){
				Item item= getItem(itemId);	
				T obj=(T)item.getItemProperty("$object").getValue();
				result.add(obj);
			}
		}
		return result;
	}
	//
	public T getSelectValue(){
		List<T> result=getSelectValues();
		return result.isEmpty()?null:result.get(0);
	}
	/**
	 * @return the cellRender
	 */
	public CellRender getCellRender() {
		return cellRender;
	}
	/**
	 * @param cellRender the cellRender to set
	 */
	public void setCellRender(CellRender cellRender) {
		this.cellRender = cellRender;
	}
	//
	@SuppressWarnings("unchecked")
	public T getValueByItem(Item item){
		T obj=(T)item.getItemProperty("$object").getValue();
		return obj;
	}
	//
}
