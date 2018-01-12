package jazmin.util;

/**
 * 
 * @author skydu
 *
 */
public class ArrayUtils {

	//
	public static Object[] convert(final int[] t) {
		if(t==null) {
			return null;
		}
		Object[] ret=new Object[t.length];
		for(int i=0;i<t.length;i++) {
			ret[i]=t[i];
		}
		return ret;
	}
	//
	public static  Object[] convert(final byte[] t) {
		if(t==null) {
			return null;
		}
		Object[] ret=new Object[t.length];
		for(int i=0;i<t.length;i++) {
			ret[i]=t[i];
		}
		return ret;
	}
	//
	public static  Object[] convert(final short[] t) {
		if(t==null) {
			return null;
		}
		Object[] ret=new Object[t.length];
		for(int i=0;i<t.length;i++) {
			ret[i]=t[i];
		}
		return ret;
	}
	//
	public static  <T> Object[] convert(final T[] t) {
		if(t==null) {
			return null;
		}
		Object[] ret=new Object[t.length];
		for(int i=0;i<t.length;i++) {
			ret[i]=t[i];
		}
		return ret;
	}
}
