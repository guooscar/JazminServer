package jazmin.test.server.message;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;  
  
public class UDPClient {  
    private static final int TIMEOUT = 5000; 
    public static void main(String args[])throws IOException{  
        String strSend = "Hello UDPserver";  
        byte[] buf = new byte[1024];  
        DatagramSocket ds = new DatagramSocket(9000);  
        InetAddress loc = InetAddress.getLocalHost();  
        DatagramPacket dpSend= new DatagramPacket(strSend.getBytes(),strSend.length(),loc,5555);  
        DatagramPacket dpReceive = new DatagramPacket(buf, 1024);  
        ds.setSoTimeout(TIMEOUT);             
        boolean receivedResponse = false;     
        while(!receivedResponse){  
            ds.send(dpSend);  
            try{  
                ds.receive(dpReceive);  
                if(!dpReceive.getAddress().equals(loc)){  
                    throw new IOException("Received packet from an umknown source");  
                }  
                receivedResponse = true;  
            }catch(InterruptedIOException e){  
               e.printStackTrace(); 
            }  
        }  
        if(receivedResponse){  
            System.out.println("client received data from server：");  
            String strReceive = new String(dpReceive.getData(),0,dpReceive.getLength()) +   
                    " from " + dpReceive.getAddress().getHostAddress() + ":" + dpReceive.getPort();  
            System.out.println(strReceive);  
            dpReceive.setLength(1024);     
        }else{  
            System.out.println("No response -- give up.");  
        }  
        ds.close();  
    }    
}   