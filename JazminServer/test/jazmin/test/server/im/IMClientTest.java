/**
 * 
 */
package jazmin.test.server.im;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.ArrayList;
import java.util.List;

import jazmin.log.LoggerFactory;
import jazmin.misc.netest.NetTestClient;

/**
 * @author yama
 * 9 May, 2015
 */
public class IMClientTest extends NetTestClient {
	//
	public void login(){
		short serviceId=0x01;
		short messageLength=50;
		ByteBuf bytes=Unpooled.buffer(messageLength);
		bytes.writeShort(messageLength);
		bytes.writeShort(serviceId);
		for(int i=0;i<messageLength-4;i++){
			bytes.writeByte((byte)'c');
		}
		send(bytes);
	}
	//--------------------------------------------------------------------------
	public static void main(String[] args)throws Exception {
		LoggerFactory.setLevel("WARN");
		List<IMClientTest>clients=new ArrayList<IMClientTest>();
		for(int i=0;i<1000;i++){
			IMClientTest t1=new IMClientTest();
			t1.connect("localhost", 5001);
			clients.add(t1);
		}
		//
		while(true){
			Thread.sleep(10);
			for(IMClientTest t:clients){
				t.login();
			}
		}
	}

}
