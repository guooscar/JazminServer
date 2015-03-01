/**
 * 
 */
package jazmin.server.console;

import java.util.Arrays;

/**
 * @author yama 1 Jan, 2015
 * Port of drawille(https://github.com/asciimoo/drawille) to Java.
 * about braille see also http://www.alanwood.net/unicode/braille_patterns.html.
 */
public class AsciiCanvas {
	private static final int [][]PIXEL_MAP = { 
			{0x1, 0x8},
			{0x2, 0x10},
			{0x4, 0x20},
			{0x40, 0x80}};
	//
	private int width;
	private int height;
	private int[] content;
		//
	public AsciiCanvas(int width, int height) {
		if (width % 2 != 0) {
			throw new IllegalArgumentException("Width must be multiple of 2!");
		}
		if (height % 4 != 0) {
			throw new IllegalArgumentException("Height must be multiple of 4!");
		}
		this.width = width;
		this.height = height;
		this.content = new int[width * height / 8];
		clear();
	}

	//
	public void clear() {
		Arrays.fill(content, (char) 0);
	}
	
	//
	private int[] getCoord(int x, int y) {
		if (!(x >= 0 && x < this.width && y >= 0 && y < this.height)) {
			return null;
		}
		int nx = (int)(x / 2);
		int ny = (int)(y / 4);
		int coord = nx + this.width / 2 * ny;
		int mask = PIXEL_MAP[y % 4][ x % 2];
		return new int[] { coord, mask };
	}
	//
	public void set(int x, int y) {
		int[] coord = getCoord(x, y);
		if (coord == null) {
			return;
		}
		this.content[coord[0]] |= coord[1];
	}

	//
	public void unset(int x, int y) {
		int[] coord = getCoord(x, y);
		if (coord == null) {
			return;
		}
		this.content[coord[0]] &= ~coord[1];
	}

	//
	public void toggle(int x, int y) {
		int[] coord = getCoord(x, y);
		if (coord == null) {
			return;
		}
		this.content[coord[0]] ^= coord[1];
	}

	//
	public void line(int x1, int y1, int x2, int y2) {
		int xdiff = Math.abs(x1 - x2);
		int ydiff = Math.abs(y2 - y1);
		float xdir, ydir;
		if (x1 <= x2) {
			xdir = 1;
		} else {
			xdir = -1;
		}
		if (y1 <= y2) {
			ydir = 1;
		} else {
			ydir = -1;
		}
		float r = Math.max(xdiff, ydiff);
		int x, y;
		for (int i = 0; i < Math.round(r) + 1; i++) {
			x = x1;
			y = y1;
			if (ydiff != 0) {
				y += ((float) ((i) * ydiff)) / (r * ydir);
			}
			if (xdiff != 0) {
				x += (((float) (i) * xdiff)) / (r * xdir);
			}
			set(x,y);
		}
	}

	//
	public void circle(int x0, int y0, int radius) {
		int x = radius;
		  int y = 0;
		  int radiusError = 1-x;
		 
		  while(x >= y){
		    set(x + x0, y + y0);
		    set(y + x0, x + y0);
		    set(-x + x0, y + y0);
		    set(-y + x0, x + y0);
		    set(-x + x0, -y + y0);
		    set(-y + x0, -x + y0);
		    set(x + x0, -y + y0);
		    set(y + x0, -x + y0);
		    y++;
		    if (radiusError<0) {
		      radiusError += 2 * y + 1;
		    }else{
		      x--;
		      radiusError += 2 * (y - x + 1);
		    }
		  }
	}
	public String frame() {
		return frame(true);
	}
	//
	public String frame(boolean showBorder) {
		String delimiter =showBorder?"\n|":"\n";
		StringBuilder result = new StringBuilder();
		for (int i = 0, j = 0; i < this.content.length; i++, j++) {
			if (j == this.width / 2) {
				result.append(delimiter);
				j = 0;
			}
			if (this.content[i] == 0) {
				result.append(' ');
			} else {
				char vv = (char) (0x2800 + this.content[i]);
				result.append(vv);
			}
		}
		result.append("\n");
		if(showBorder){
			result.append("|");
			for(int i=0;i<width/2;i++){
				result.append("_");
			}
		}
		return result.toString();
	}
	//
	public static void main(String[] args) {
		AsciiCanvas canvas=new AsciiCanvas(80, 80);
		for(int x=0;x<1800;x+=10){
			canvas.set(x / 10, (int)(10 + Math.sin(Math.toRadians(x)) * 10));	
		}
		canvas.line(0,0,200, 80);
		canvas.circle(50, 50, 20);
		System.out.println(canvas.frame());
	}
}
