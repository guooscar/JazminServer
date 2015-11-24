package jazmin.server.console.repl;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import jazmin.core.Jazmin;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.server.console.ConsoleServer;
import jazmin.server.console.builtin.ConsoleCommand;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ReplThread implements Closeable {
	private static Logger logger=LoggerFactory.get(ReplThread.class);
    private final InputStream stdin;
    private final OutputStream stderr;
    private final Thread thread;

    public ReplThread(
    				ConsoleServer consoleServer,
    				final TerminalInputStream stdin, 
    				final OutputStream stdout, 
    				final OutputStream stderr,
                    final ReadLineEnvironment environment, 
                    final Map<String, ConsoleCommand> commands,
                    final Runnable onExit) {
        this.stdin = stdin;
        this.stderr = stderr;

        thread = new Thread(()->{
        try {
        	String prompt=
        			"\033[32;49;1m"+"JazminConsole"+"\033[39;49;0m"+
        			"\033[33;49;1m@"+environment.user+"\033[39;49;0m"+
                    "\033[35;49;1m#"+Jazmin.getServerName()+"\033[39;49;0m"+
        			">";
        	Repl.repl(consoleServer,stdin, stdout, stderr, environment, commands,prompt);
        } catch (Exception e) {
        	logger.catching(e);
        }finally {
            try {
                onExit.run();
            } catch (Exception e) {
            	logger.catching(e);
            }
        }
        });
        thread.setName("ReplThread");
        thread.setDaemon(true);
    }

    public void start() {
        thread.start();
    }

    public void close() throws IOException {
        Repl.closeSilently(stdin);
        Repl.closeSilently(stderr);
    }
}
