/**
 * 
 */
package jazmin.server.console;

import java.util.LinkedList;

/**
 * @author yama
 * 31 Dec, 2014
 */
public class AsciiChart {
	private LinkedList<Integer>values;
	private AsciiCanvas canvas;
	private int width;
	private int height;
	public AsciiChart(int width,int height) {
		values=new LinkedList<Integer>();
		canvas=new AsciiCanvas(width,height);
		this.width=width;
		this.height=height;
		reset();
	}
	//
	public void reset(){
		canvas.clear();
	}
	//
	public void addValue(int value){
		values.add(value);
		if(values.size()>width){
			values.removeFirst();
		}
	}
	//
	public String draw(){
		//1.find max
		float max=0;
		for(int v:values){
			if(max<v){
				max=v;
			}
		}
		if(((int)max)==0){
			return "";
		}
		StringBuilder result=new StringBuilder();
		result.append("max:"+max+"\n");
		//
		int col=0;
		int lastX=-1;
		int lastY=0;
		for(int v:values){
			float vv=v;
			int p=(int)(vv/max*(height-1));
			if(lastX>=0){
				canvas.line(lastX, (height-1)-lastY, col, (height-1)-p);
			}
			lastX=col;
			lastY=p;
			col++;
		}
		result.append(canvas.frame());
		return result.toString();
	}
}
