/**
 * *****************************************************************************
 * 							Copyright (c) 2014 yama.
 * This is not a free software,all rights reserved by yama(guooscar@gmail.com).
 * ANY use of this software MUST be subject to the consent of yama.
 *
 * *****************************************************************************
 */
package jazmin.server.console.repl;


/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ReadLineEnvironment {
    public final String encoding;
    public final Integer erase;
    public final boolean icrnl;
    public final boolean ocrnl;
    public String user;
    private int columns;
    private int lines;

  
    public ReadLineEnvironment(
    		String user,
    		String encoding, 
    		Integer erase, 
    		boolean icrnl, 
    		boolean ocrnl, 
    		Integer columns, 
    		Integer lines) {
    	this.user=user;
        this.encoding = encoding;
        this.erase = erase;
        this.icrnl = icrnl;
        this.ocrnl = ocrnl;
        onWindowChange(columns, lines);
    }

    public int getColumns() {
        return columns;
    }

    public int getLines() {
        return lines;
    }

    public void onWindowChange(Integer newColumns, Integer newLines) {
        this.columns = newColumns != null ? newColumns : 80;
        this.lines = newLines != null ? newLines : 25;
    }
}
