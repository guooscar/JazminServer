package jazmin.misc.encrypt;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import jazmin.util.Base64Util;

/**
 * openssl genrsa -out private_key.pem 1024
 * openssl rsa -in private_key.pem -pubout -out public_key.pem
 * openssl pkcs8 -topk8 -in private_key.pem -out pkcs8_private_key.pem -nocrypt
 * 
 * private key file need pkcs8 encode
 * @author yama
 *
 */
public class RsaEncryptor {
	PublicKey publicKey;
	PrivateKey privateKey;
	KeyFactory keyFactory;
	//
	
	/**
	 * @return the publicKey
	 */
	public PublicKey getPublicKey() {
		return publicKey;
	}
	/**
	 * @param publicKey the publicKey to set
	 */
	public void setPublicKey(PublicKey publicKey) {
		this.publicKey = publicKey;
	}
	/**
	 * @return the privateKey
	 */
	public PrivateKey getPrivateKey() {
		return privateKey;
	}
	/**
	 * @param privateKey the privateKey to set
	 */
	public void setPrivateKey(PrivateKey privateKey) {
		this.privateKey = privateKey;
	}
	/**
	 * @return the keyFactory
	 */
	public KeyFactory getKeyFactory() {
		return keyFactory;
	}
	/**
	 * @param keyFactory the keyFactory to set
	 */
	public void setKeyFactory(KeyFactory keyFactory) {
		this.keyFactory = keyFactory;
	}

	//
	public void loadKeys()throws Exception{
		keyFactory = KeyFactory.getInstance("RSA");
		InputStream publicKeyStream=RsaEncryptor.class.getResourceAsStream(
				"public_key.pem");
		byte publicKeyBytes[]=loadKeyFile(publicKeyStream);
		X509EncodedKeySpec spec1 = new X509EncodedKeySpec(publicKeyBytes);
		
		publicKey= keyFactory.generatePublic(spec1);
		//
		InputStream privateKeyStream=RsaEncryptor.class.getResourceAsStream(
				"pkcs8_private_key.pem");
		byte privateKeyBytes[]=loadKeyFile(privateKeyStream);
		PKCS8EncodedKeySpec spec2 =new PKCS8EncodedKeySpec(privateKeyBytes);
        privateKey=keyFactory.generatePrivate(spec2);
	}
	//
	private byte[] loadKeyFile(InputStream in)throws Exception{
		StringBuilder result=new StringBuilder();
		try(BufferedReader br=new BufferedReader(new InputStreamReader(in))){
			String s=null;
			while((s=br.readLine())!=null){
				if(!s.startsWith("-")){
					result.append(s);
				}
			}
		}
		byte bb[]=result.toString().trim().getBytes();
		return Base64Util.decode(bb);
	}
	//
	public byte[] encryptByPublicKey(byte[] data) throws Exception {
		Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		return cipher.doFinal(data);
	}
	public byte[] encryptByPrivateKey(byte[] data, String key) throws Exception {
		Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
		cipher.init(Cipher.ENCRYPT_MODE, privateKey);
		return cipher.doFinal(data);
	}
	//
	public byte[] decryptByPrivateKey(byte[] data) throws Exception {
		Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		return cipher.doFinal(data);
	}
	//
	public byte[] decryptByPublicKey(byte[] data) throws Exception {
		Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
		cipher.init(Cipher.DECRYPT_MODE, publicKey);
		return cipher.doFinal(data);
	}
	//
	public static void main(String[] args) throws Exception{
		RsaEncryptor encryptor=new RsaEncryptor();
		encryptor.loadKeys();
		//
		String data="12345";
		byte encode[]=encryptor.encryptByPublicKey(data.getBytes());
		byte decoded[]=encryptor.decryptByPrivateKey(encode);
		System.out.println(new String(Base64Util.encode(encode)));
		System.out.println(new String(decoded));
		
	}
}
