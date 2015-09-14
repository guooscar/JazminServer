package jazmin.misc.encrypt;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
/**
 * 
 * @author yama
 *
 */
public class AesEncryptor {       
    /** 
     * 密钥算法 
    */  
    private static final String KEY_ALGORITHM = "AES";  
    private static final String DEFAULT_CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";  
    //
    private Key defaultKey;
    public AesEncryptor(){
    	//key size = 16 bytes
    	defaultKey=toKey("jazm1n_server_aw".getBytes());
    }
    //
    public byte [] encrypt(byte[] input) throws Exception{
    	return encrypt(input,defaultKey);
    }
    //
    public byte [] decrypt(byte[] input) throws Exception{
    	return decrypt(input,defaultKey);
    }
    /** 
     * 初始化密钥 
     *  
     * @return byte[] 密钥  
     * @throws Exception 
     */  
    public static byte[] initSecretKey() {  
        KeyGenerator kg = null;  
        try {  
            kg = KeyGenerator.getInstance(KEY_ALGORITHM);  
        } catch (NoSuchAlgorithmException e) {  
            return new byte[0];  
        }  
        kg.init(128);  
        SecretKey  secretKey = kg.generateKey();  
        return secretKey.getEncoded();  
    }  
      
    /** 
     * 转换密钥 
     *  
     * @param key   二进制密钥 
     * @return 密钥 
     */  
    private static Key toKey(byte[] key){  
        return new SecretKeySpec(key, KEY_ALGORITHM);  
    }  
      
    /** 
     * 加密 
     *  
     * @param data  待加密数据 
     * @param key   密钥 
     * @return byte[]   加密数据 
     * @throws Exception 
     */  
    public static byte[] encrypt(byte[] data,Key key) throws Exception{  
        return encrypt(data, key,DEFAULT_CIPHER_ALGORITHM);  
    }  
      
    /** 
     * 加密 
     *  
     * @param data  待加密数据 
     * @param key   二进制密钥 
     * @return byte[]   加密数据 
     * @throws Exception 
     */  
    public static byte[] encrypt(byte[] data,byte[] key) throws Exception{  
        return encrypt(data, key,DEFAULT_CIPHER_ALGORITHM);  
    }  
      
      
    /** 
     * 加密 
     *  
     * @param data  待加密数据 
     * @param key   二进制密钥 
     * @param cipherAlgorithm   加密算法/工作模式/填充方式 
     * @return byte[]   加密数据 
     * @throws Exception 
     */  
    public static byte[] encrypt(byte[] data,byte[] key,String cipherAlgorithm) throws Exception{  
        Key k = toKey(key);  
        return encrypt(data, k, cipherAlgorithm);  
    }  
      
    /** 
     * 加密 
     *  
     * @param data  待加密数据 
     * @param key   密钥 
     * @param cipherAlgorithm   加密算法/工作模式/填充方式 
     * @return byte[]   加密数据 
     * @throws Exception 
     */  
    public static byte[] encrypt(byte[] data,Key key,String cipherAlgorithm) throws Exception{  
        Cipher cipher = Cipher.getInstance(cipherAlgorithm);  
        cipher.init(Cipher.ENCRYPT_MODE, key);  
        return cipher.doFinal(data);  
    }  
      
      
      
    /** 
     * 解密 
     *  
     * @param data  待解密数据 
     * @param key   二进制密钥 
     * @return byte[]   解密数据 
     * @throws Exception 
     */  
    public static byte[] decrypt(byte[] data,byte[] key) throws Exception{  
        return decrypt(data, key,DEFAULT_CIPHER_ALGORITHM);  
    }  
      
    /** 
     * 解密 
     *  
     * @param data  待解密数据 
     * @param key   密钥 
     * @return byte[]   解密数据 
     * @throws Exception 
     */  
    public static byte[] decrypt(byte[] data,Key key) throws Exception{  
        return decrypt(data, key,DEFAULT_CIPHER_ALGORITHM);  
    }  
      
    /** 
     * 解密 
     *  
     * @param data  待解密数据 
     * @param key   二进制密钥 
     * @param cipherAlgorithm   加密算法/工作模式/填充方式 
     * @return byte[]   解密数据 
     * @throws Exception 
     */  
    public static byte[] decrypt(byte[] data,byte[] key,String cipherAlgorithm) throws Exception{  
        Key k = toKey(key);  
        return decrypt(data, k, cipherAlgorithm);  
    }  
  
    /** 
     * 解密 
     *  
     * @param data  待解密数据 
     * @param key   密钥 
     * @param cipherAlgorithm   加密算法/工作模式/填充方式 
     * @return byte[]   解密数据 
     * @throws Exception 
     */  
    public static byte[] decrypt(byte[] data,Key key,String cipherAlgorithm) throws Exception{  
        Cipher cipher = Cipher.getInstance(cipherAlgorithm);  
        cipher.init(Cipher.DECRYPT_MODE, key);  
        return cipher.doFinal(data);  
    }  
  
    //
    public static void main(String[] args) throws Exception { 
    	//
    	AesEncryptor aesEncryptor=new AesEncryptor();
    	byte bbb[]=aesEncryptor.encrypt("12345".getBytes());
    	System.out.println(new String(aesEncryptor.decrypt(bbb)));
    }  
    
} 