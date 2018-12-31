/**
 * 
 */
package jazmin.server.mysqlproxy;

import java.util.Date;

import jazmin.util.MD5Util;

import org.bouncycastle.util.Arrays;

/**
 * @author yama
 *
 */
public class ProxySession {
	public String id;
	public String remoteHost;
	public int remotePort;
	public int localPort;
	public String user;
	public byte[] challenge;
	//
	public String dbUser;
	public long packetCount;
	public Date createTime;
	public Date lastAccTime;
	//
	public byte[]clientPassword;
	//
	public boolean comparePassword(byte[] shaPassword){
		byte[] newPassword=scramble411sha(shaPassword,challenge);
		return Arrays.areEqual(newPassword, clientPassword);
	}
	//
	static byte[] scramble411(byte[] password,byte[] seed){
    	byte[] shaPassword=MD5Util.encodeSHA1Bytes(password);			
		return scramble411sha(shaPassword,seed);
    }
    /**
     * XOR( SHA1(password), SHA1(challenge + SHA1(SHA1(password)))
     */
	static byte[] scramble411sha(byte[] shaPassword,byte[] seed){		
		byte[] shashaPassword=MD5Util.encodeSHA1Bytes(shaPassword);
		byte[] challengeByte=seed;
		byte[] challengeResult=MD5Util.encodeSHA1Bytes(Arrays.concatenate(challengeByte, shashaPassword));
		//xor
		byte[]result=new byte[shaPassword.length];
		for(int i=0;i<result.length;i++){
			result[i]=(byte) (shaPassword[i] ^ challengeResult[i]);
		}
		return result;
    }
}
