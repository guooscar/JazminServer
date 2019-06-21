package jazmin.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 
 * @author yama
 * 20 Jan, 2015
 */
public class MD5Util {
	//
	public static String encodeMD5(String input){
		MessageDigest md5;
		try {
			md5 = MessageDigest.getInstance( "MD5" );
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		try {
			md5.update(input.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		StringBuilder sb=new StringBuilder();
		for(byte b:md5.digest()){
			sb.append(String.format("%02X",b));
		}
		return sb.toString();
	}
	//
	public static String encodeSHA1(byte[]input){
		byte bb[]=encodeSHA1Bytes(input);
		StringBuilder sb=new StringBuilder();
		for(byte b:bb){
			sb.append(String.format("%02X",b));
		}
		return sb.toString();
	}
	//
	public static byte[] encodeSHA1Bytes(byte[]input){
		MessageDigest md5;
		try {
			md5 = MessageDigest.getInstance( "SHA1");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		md5.reset();
		md5.update(input);
		return md5.digest();
	}
}
