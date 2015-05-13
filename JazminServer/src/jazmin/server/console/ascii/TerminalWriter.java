/**
 * 
 */
package jazmin.server.console.ascii;

import java.io.PrintWriter;

/**
 * @author yama 27 Dec, 2014
 */
public class TerminalWriter {
	private PrintWriter out;
	public TerminalWriter(PrintWriter out) {
		this.out=out;
	}
	//
	public static final String CLS = "\033[H\033[J\033[3J";	
	public static final String GOTO1_1 = "\033[1;1H";
	public static final String BOLD = "\033[1m";
	public static final String BLINK = "\033[5m";
	public static final String REVERSE = "\033[30m";
	public static final String EXTRA = "\033[47m"; // black & white;
	public static final String REVERSE1 = "\033[7m"; // color;
	public static final String ITALICS = "\033[2m";
	public static final String RESET = "\033[0m";
	public static final String INVIS = "\033[8m";
	// #******************** ForGround Colors ***********************
	public static final String FBLACK = "\033[30m";
	public static final String FRED = "\033[31m";
	public static final String FGREEN = "\033[32m";
	public static final String FYELLOW = "\033[33m";
	public static final String FBLUE = "\033[34m";
	public static final String FMAGENTA = "\033[35m";
	public static final String FCYAN = "\033[36m";
	public static final String FWHITE = "\033[37m";
	// #******************** BackGround Colors ***********************
	public static final String BBLACK = "\033[40m";
	public static final String BRED = "\033[41m";
	public static final String BGREEN = "\033[42m";
	public static final String BYELLOW = "\033[43m";
	public static final String BBLUE = "\033[44m";
	public static final String BMAGENTA = "\033[45m";
	public static final String BCYAN = "\033[46m";
	public static final String BWHITE = "\033[47m";
	//
	// ////////////////////////////////////////////////////////////////////

	/******************************** cls() *******************************/
	public void cls() {
		//out.print(GOTO1_1);
		//out.print("\033[J");
		out.print("\033[H\033[J\033[3J");
	}

	/******************************** bold() *******************************/
	public void bold() {
		out.print(BOLD);
	}

	/******************************** blink() *******************************/
	public void blink() {
		out.print(BLINK);
	}

	/******************************** reverse() *******************************/
	public void reverse() {
		out.print(REVERSE);
		out.print(EXTRA);
	}

	/******************************** reverse1() *******************************/
	public void reverse1() {
		out.print(REVERSE1);
	}

	/******************************** italics() *******************************/
	public void italics() {
		out.print(ITALICS);
	}

	/******************************** reset() *******************************/
	public  void reset() {
		out.print(RESET);
	}

	/******************************** invis() *******************************/
	public  void invis() {
		out.print(INVIS);
	}

	/******************************** fblack() *******************************/
	public  void fblack() {
		out.print(FBLACK);
	}

	/******************************** fred() *******************************/
	public  void fred() {
		out.print(FRED);
	}

	/******************************** fgreen() *******************************/
	public  void fgreen() {
		out.print(FGREEN);
	}

	/******************************** fyellow() *******************************/
	public  void fyellow() {
		out.print(FYELLOW);
	}

	/******************************** fblue() *******************************/
	public  void fblue() {
		out.print(FBLUE);
	}

	/******************************** fmagenta() *******************************/
	public  void fmagenta() {
		out.print(FMAGENTA);
	}

	/******************************** fcyan() *******************************/
	public  void fcyan() {
		out.print(FCYAN);
	}

	/******************************** fwhite() *******************************/
	public  void fwhite() {
		out.print(FWHITE);
	}

	/******************************** bblack() *******************************/
	public  void bblack() {
		out.print(BBLACK);
	}

	/******************************** bred() *******************************/
	public  void bred() {
		out.print(BRED);
	}

	/******************************** bgreen() *******************************/
	public  void bgreen() {
		out.print(BGREEN);
	}

	/******************************** byellow() *******************************/
	public  void byellow() {
		out.print(BYELLOW);
	}

	/******************************** bblue() *******************************/
	public  void bblue() {
		out.print(BBLUE);
	}

	/******************************** bmagenta() *******************************/
	public  void bmagenta() {
		out.print(BMAGENTA);
	}

	/******************************** bcyan() *******************************/
	public  void bcyan() {
		out.print(BCYAN);
	}

	/******************************* bwhite() *******************************/
	public  void bwhite() {
		out.print(BWHITE);
	}

	/******************************** locate() *******************************/
	public  void locate(int row, int col) {
		out.print("\033[" + row + ";" + col + "H");
	}

	/******************************** box() *********************************/
	public  void box(int r1, int c1, int r2, int c2, char c) {
		int i;
		int z = c2 - c1 + 1;
		String line = "";
		for (i = 1; i <= z; i++) {
			line += c;
		}
		locate(r1, c1);
		out.print(line);
		locate(r2, c1);
		out.print(line);
		i = r1 + 1;
		while (i < r2) {
			locate(i, c1);
			out.print(c);
			locate(i, c2);
			out.print(c);
			i++;
		}
	}
}
