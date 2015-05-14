package jazmin.core.app;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author yama
 *
 */
public class AutoWiredObject {
	public static class AutoWiredField {
		public String fieldName;
		public Class<?>fieldClass;
		public boolean shared;
		public boolean hasValue;
		AutoWiredField(String fieldName,Class<?>fieldClass,boolean shared) {
			this.fieldClass=fieldClass;
			this.fieldName=fieldName;
			this.shared=shared;
		}
	}
	//
	public Class<?>clazz;
	public Object instance;
	//
	public List<AutoWiredField>fields=new ArrayList<>();
}
