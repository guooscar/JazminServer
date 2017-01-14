/**
 * 
 */
package jazmin.server.websockify;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.handler.timeout.IdleStateHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jazmin.core.Jazmin;
import jazmin.core.Server;
import jazmin.misc.InfoBuilder;
import jazmin.misc.io.IOWorker;
import jazmin.server.console.ConsoleServer;

/**
 * WebsockifyServer is a simple proxy server allow no vnc client connect
 * see also https://github.com/yudai/gotty
 * @author yama
 * 11 01, 2017
 */
public class WebsockifyServer extends Server{
	private int port;
	private int wssPort;
	private boolean enableWss;
	private  EventLoopGroup bossGroup;
	private  EventLoopGroup workerGroup;
	private  Map<String, WebsockifyChannel>channels;
	private String certificateFile;
	private String privateKeyFile;
	private String privateKeyPhrase;
	private SslContext sslContext;
	private HostInfoProvider hostInfoProvider;
	//
	public WebsockifyServer() {
		channels=new ConcurrentHashMap<String, WebsockifyChannel>();
		enableWss=false;
		port=9801;
		wssPort=9802;
		certificateFile="";
		privateKeyFile="";
		privateKeyPhrase="";
	}
	
	public HostInfoProvider getHostInfoProvider() {
		return hostInfoProvider;
	}

	public void setHostInfoProvider(HostInfoProvider hostInfoProvider) {
		this.hostInfoProvider = hostInfoProvider;
	}

	/**
	 * @return the certificateFile
	 */
	public String getCertificateFile() {
		return certificateFile;
	}

	/**
	 * @param certificateFile an X.509 certificate chain file in PEM format
  	 */
	public void setCertificateFile(String certificateFile) {
		this.certificateFile = certificateFile;
	}

	/**
	 * @return the privateKeyFile
	 */
	public String getPrivateKeyFile() {
		return privateKeyFile;
	}

	/**
	 * @param privateKeyFile a PKCS#8 private key file in PEM format
	 */
	public void setPrivateKeyFile(String privateKeyFile) {
		this.privateKeyFile = privateKeyFile;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @return the wssPort
	 */
	public int getWssPort() {
		return wssPort;
	}

	/**
	 * @param wssPort the wssPort to set
	 */
	public void setWssPort(int wssPort) {
		this.wssPort = wssPort;
	}

	/**
	 * @return the enableWss
	 */
	public boolean isEnableWss() {
		return enableWss;
	}


	/**
	 * @param enableWss the enableWss to set
	 */
	public void setEnableWss(boolean enableWss) {
		this.enableWss = enableWss;
	}

	//
    private SslContext createSslContext()throws Exception{
		if(sslContext!=null){
			return sslContext;
		}
		File certiFile = new File(certificateFile);
		File keyFile = new File(privateKeyFile);
		if (certiFile.exists() && keyFile.exists()) {
			sslContext=SslContextBuilder.forServer(new File(certificateFile),
					new File(privateKeyFile), privateKeyPhrase).build();
		} else {
			SelfSignedCertificate ssc = new SelfSignedCertificate();
			sslContext=SslContextBuilder.forServer(ssc.certificate(),ssc.privateKey()).build();
		}
		return sslContext;
    }
	//
	void addChannel(WebsockifyChannel c){
		channels.put(c.id, c);
	}
	//
	void removeChannel(String id){
		channels.remove(id);
	}
	//
	public List<WebsockifyChannel>getChannels(){
		return new ArrayList<WebsockifyChannel>(channels.values());
	}
	//
	private void createWSListeningPoint(boolean wss)throws Exception{
	    final ServerBootstrap b = new ServerBootstrap();
        b.group(this.bossGroup, this.workerGroup)
        .channel(NioServerSocketChannel.class)
        .childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(final SocketChannel ch) throws Exception {
                final ChannelPipeline pipeline = ch.pipeline();
                if(wss){
                	SslContext sslContext=createSslContext();
                	pipeline.addLast(sslContext.newHandler(ch.alloc()));	
                }
                pipeline.addLast("idleStateHandler",new IdleStateHandler(3600,3600,0));
                pipeline.addLast(new HttpServerCodec());
                pipeline.addLast(new HttpObjectAggregator(65536));
                pipeline.addLast(new WebSocketServerCompressionHandler());
                pipeline.addLast(new WebsockifyHandler(WebsockifyServer.this,wss));
            }
        })
        .option(ChannelOption.SO_BACKLOG, 128)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
        .childOption(ChannelOption.SO_KEEPALIVE, true)
        .childOption(ChannelOption.TCP_NODELAY, true);
        //
        if(wss){
        	b.bind(wssPort).sync();    	
        }else{
            b.bind(port).sync();    	
        }
    }
	//--------------------------------------------------------------------------
	@Override
	public void init() throws Exception {
		ConsoleServer cs = Jazmin.getServer(ConsoleServer.class);
		if (cs != null) {
			cs.registerCommand(WebsockifyCommand.class);
		}
	}
	//
	@Override
	public void start() throws Exception {
		IOWorker ioWorker=new IOWorker("WebSshServerWorker",
    			Runtime.getRuntime().availableProcessors()*2+1);
		bossGroup=new NioEventLoopGroup(1,ioWorker);
    	workerGroup=new NioEventLoopGroup(0,ioWorker);
		createWSListeningPoint(false);
		if(enableWss){
			createWSListeningPoint(true);	
		}
	}
	@Override
	public void stop() throws Exception {
		if(bossGroup!=null){
			bossGroup.shutdownGracefully();
		}
		if(workerGroup!=null){
			workerGroup.shutdownGracefully();
		}
	}
	//
	@Override
	public String info() {
		InfoBuilder ib=InfoBuilder.create();
		ib.section("info")
		.format("%-30s:%-30s\n")
		.print("port",getPort())
		.print("wssPort",getWssPort())
		.print("enableWss",isEnableWss())
		.print("privateKeyFile",getPrivateKeyFile())
		.print("certificateFile",getCertificateFile());
		return ib.toString();
	}
}
