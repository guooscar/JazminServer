/**
 * 
 */
package jazmin.driver.http;

import com.ning.http.client.ProxyServer;
import com.ning.http.client.ProxyServer.Protocol;

/**
 * @author yama
 * 11 Feb, 2015
 */
public class HttpProxyServer {
	ProxyServer proxyServer;
	public HttpProxyServer(String host,int port) {
		this.proxyServer=new ProxyServer(host, port);
	}
	//
	public HttpProxyServer(String proto,String host,int port) {
		this.proxyServer=new ProxyServer(getProtocol(proto),host, port);
	}
	//
	private Protocol getProtocol(String proto){
		Protocol protcol=Protocol.HTTP;
		if(proto.equalsIgnoreCase("https")){
			protcol=Protocol.HTTPS;
		}
		if(proto.equalsIgnoreCase("kebreros")){
			protcol=Protocol.KERBEROS;
		}
		if(proto.equalsIgnoreCase("ntlm")){
			protcol=Protocol.NTLM;
		}
		if(proto.equalsIgnoreCase("spnego")){
			protcol=Protocol.SPNEGO;
		}
		return protcol;
	}
	//
	public HttpProxyServer(String host,int port,String principal,String password) {
		this.proxyServer=new ProxyServer(host, port, principal, password);
	}
	//
	//
	public HttpProxyServer(String proto,String host,int port,String principal,String password) {
		this.proxyServer=new ProxyServer(getProtocol(proto),host, port, principal, password);
	}
}
