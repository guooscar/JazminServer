package jazmin.util;

import java.util.Base64;

/**
 * 
 * @author yama
 *
 */
public class Base64Util {
	//
	public static byte[] encode(byte input[]){
		return Base64.getEncoder().encode(input);
	}
	//
	public static byte[] decode(byte input[]){
		return Base64.getDecoder().decode(input);
	}
}
