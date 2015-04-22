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
	 * ffmpeg -f avfoundation -i "0:0" -vcodec libx264  -s 640x480 -tune zerolatency -b 500k  -f flv rtmp://localhost/live/test
	 *use ffmpeg to publish local file to live stream
	 *ffmpeg -re -i test.mov  -c copy -f flv rtmp://localhost/live/test
	 */
	 //--------------------------------------------------------------------------
    public static void main(String[] args) throws Exception{
    	//
    	LoggerFactory.setLevel("DEBUG");
    	RtmpServer rtmpServer=new RtmpServer();
    	//rtmpServer.setServerHome("/tmp/rtmp/");
    	
    	//
		Jazmin.addServer(rtmpServer);
		Jazmin.addServer(new ConsoleServer());
		Jazmin.start();
	}
}
