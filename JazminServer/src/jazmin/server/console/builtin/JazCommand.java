package jazmin.server.console.builtin;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import jazmin.core.Jazmin;
import jazmin.server.console.ascii.AsciiCanvas;
import jazmin.server.console.ascii.TerminalWriter;

/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
public class JazCommand extends ConsoleCommand {
   public JazCommand() {
    	super(false);
    	id="jaz";
    	desc="just for fun";
    }
    //
   	@Override
    public void run()throws Exception{
   		URL url=Jazmin.class.getResource("jazmin-logo.png");
		BufferedImage image=ImageIO.read(url);
		float i=0.05f;
		TerminalWriter tw=new TerminalWriter(out);
   		while(i<0.2){
   			tw.cls();
   			out.println(getImage(image,i));
   			out.flush();
   			i+=0.002;
   			TimeUnit.MILLISECONDS.sleep(10);
   		}
    }
   	//
   	private static String getImage(BufferedImage image,float scale){
   		int cWidth=200;
   		int cHeight=160;
   		BufferedImage tag=new BufferedImage(cWidth,cHeight,BufferedImage.TYPE_INT_ARGB);
   		Graphics2D g=tag.createGraphics();
   		g.translate((cWidth-scale*image.getWidth())/2,(cHeight-scale*image.getHeight())/2);
   		g.scale(scale, scale);
   		g.drawImage(image, 0, 0, null);
        g.dispose();
     	AsciiCanvas canvas=new AsciiCanvas(cWidth,cHeight);
		for(int i=0;i<tag.getWidth();i++){
			for(int j=0;j<tag.getHeight();j++){
				int z=tag.getRGB(i,j);
				if(z!=0){
					canvas.set(i, j);
				}			
			}
		}
		return canvas.frame(false);
   	}
}
