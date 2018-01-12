package jazmin.util;

/**
 * 
 * @author icecooly
 *
 */
public class StringUtil {

	public static boolean isEmpty(String input){
		if(input==null||input.length()==0){
			return true;
		}
		return false;
	}
	
	public static Object[] convert(String[] values) {
		if(values==null) {
			return null;
		}
		Object[] oValues=new Object[values.length];
		for(int i=0;i<values.length;i++) {
			oValues[i]=values[i];
		}
		return oValues;
	}
}
