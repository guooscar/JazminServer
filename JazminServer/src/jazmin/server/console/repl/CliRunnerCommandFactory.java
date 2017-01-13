package jazmin.server.console.repl;

import static java.lang.Integer.valueOf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.apache.sshd.common.Factory;
import org.apache.sshd.common.channel.PtyMode;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;

import jazmin.server.console.ConsoleServer;
import jazmin.server.console.builtin.ConsoleCommand;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class CliRunnerCommandFactory implements Factory<Command> {
    private final Map<String, ConsoleCommand> commands;
    private ConsoleServer consoleServer;
    public CliRunnerCommandFactory(
    		ConsoleServer consoleServer,
    		Map<String, ConsoleCommand> commands) {
    	this.consoleServer=consoleServer;
    	this.commands = commands;
    }
    //--------------------------------------------------------------------------
    public class JazminSshCommand implements Command{
        private TerminalInputStream stdin;
        private OutputStream stdout;
        private OutputStream stderr;
        private ExitCallback exitCallback;
        private ReplThread repl;
        //
        public void setInputStream(InputStream stdin) {
            this.stdin = new TerminalInputStream(stdin);
        }

        public void setOutputStream(OutputStream stdout) {
            this.stdout = stdout;
        }

        public void setErrorStream(OutputStream stderr) {
            this.stderr = stderr;
        }
        //
        public void setExitCallback(ExitCallback exitCallback) {
            this.exitCallback = exitCallback;
        }
        //
        public void start(Environment e) throws IOException {
        	repl = new ReplThread(
        			consoleServer,
            		stdin,
            		stdout, 
            		stderr, 
            		toReadLineEnvironment(e), 
            		commands, 
            		()->exitCallback.onExit(0));
            repl.start();
        }
        //
        public void destroy() {
        	Repl.closeSilently(repl);
        }
    }
    public Command create() {
        return new  JazminSshCommand();
    } 
    //--------------------------------------------------------------------------
    public static boolean getBoolean(Map<PtyMode, Integer> map, PtyMode mode) {
        Integer i = map.get(mode);

        return i != null && i == 1;
    }

    private static Integer getInteger(Map<String, String> env, String key) {
        String s = env.get(key);
        if (s == null) {
            return null;
        }
        try {
            return valueOf(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    //--------------------------------------------------------------------------
    public static ReadLineEnvironment toReadLineEnvironment(Environment environment) {
        final Map<String, String> env = environment.getEnv();
        Map<PtyMode, Integer> ptyModes = environment.getPtyModes();
        String encoding = null;
        Integer erase = ptyModes.get(PtyMode.VERASE);
        String user=env.get(Environment.ENV_USER);
        final ReadLineEnvironment readLineEnvironment = 
        		new ReadLineEnvironment(
        				user,
        				encoding, 
        				erase,
        				getBoolean(ptyModes, PtyMode.ICRNL),
        				getBoolean(ptyModes, PtyMode.OCRNL),
        				getInteger(env, Environment.ENV_COLUMNS),
        				getInteger(env, Environment.ENV_LINES));
        environment.addSignalListener((signal)->{
        	    readLineEnvironment.onWindowChange(
                    getInteger(env, Environment.ENV_COLUMNS),
                    getInteger(env, Environment.ENV_LINES));
            });

        return readLineEnvironment;
    }
}
