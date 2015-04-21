package jazmin.test.server.rtmp;

import jazmin.core.Jazmin;
import jazmin.log.LoggerFactory;
import jazmin.server.console.ConsoleServer;
import jazmin.server.rtmp.RtmpServer;

/**
 * 
 * @author yama
 *
 */
public class RtmpServerTest {
	/*
	 *use ffmpeg to publish local file to live stream
	 *ffmpeg -re -i test.mov  -c copy -f flv rtmp://localhost/live/test
	 */
	 //--------------------------------------------------------------------------
    public static void main(String[] args) throws Exception{
    	//
    	LoggerFactory.setLevel("DEBUG");
    	RtmpServer rtmpServer=new RtmpServer();
    	rtmpServer.setServerHome("/tmp/rtmp/");
    	
    	//
		Jazmin.addServer(rtmpServer);
		Jazmin.addServer(new ConsoleServer());
		Jazmin.start();
	}
}
