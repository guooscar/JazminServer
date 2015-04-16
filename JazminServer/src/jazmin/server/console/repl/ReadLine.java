package jazmin.server.console.repl;

import static java.lang.Character.isDigit;
import static java.lang.Character.isLetter;
import static java.lang.Character.valueOf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import jazmin.server.console.repl.Repl.CommandCompleter;
/**
 * 
 * @author yama
 * 15 Jan, 2015
 */
@SuppressWarnings({})
public class ReadLine {
	public static final byte ETX = 003; // Ctrl-c
	public static final byte BS = 010;
	public static final byte TAB = 011;
	public static final byte FF = 014;
	public static final byte ESC = 033;
	public static final byte SPACE = 040;
	public static final byte BEL = 0007;

	public static final byte[] CLEAR_EOL = { ESC, '[', 'K' };
	public static final byte[] CURSOR_HOME = { ESC, '[', 'H' };
	public static final byte[] CURSOR_UP = { ESC, '[', 'A' };
	public static final byte[] CURSOR_DOWN = { ESC, '[', 'B' };
	public static final byte[] CURSOR_RIGHT = { ESC, '[', 'C' };
	public static final byte[] CURSOR_LEFT = { ESC, '[', 'D' };
	public static final byte[] CLEAR_SCREEN_END2 = { ESC, '[', '2', 'J' };

	private final InputStream inputStream;
	private final OutputStream outputStream;

	List<Character> chars = new ArrayList<Character>();
	int position = 0;
	private byte erase;

	public ReadLine(InputStream inputStream, OutputStream outputStream,
			ReadLineEnvironment environment) {
		this.inputStream = inputStream;
		this.outputStream = outputStream;

		Integer erase = environment.erase;
		this.erase = erase == null ? 0x7f : erase.byteValue();
	}

	public int getPosition() {
		return position;
	}

	public String readLine(String prompt, CommandCompleter completer) {
		try {
			return doRead(prompt, completer);
		} catch (IOException e) {
			return null;
		}
	}

	//
	public static void addHistoryCommand(String cmd) {
		if (cmd == null || cmd.trim().length() == 0) {
			return;
		}
		commandHistory.add(cmd);
		commandIndex = commandHistory.size() - 1;
	}

	//
	public static String getNextHistoryCommand() {
		if (commandHistory.isEmpty()) {
			return null;
		}
		if (commandIndex < 0) {
			commandIndex = commandHistory.size() - 1;
		}
		if (commandIndex > commandHistory.size() - 1) {
			commandIndex = 0;
		}
		return commandHistory.get(commandIndex--);
	}

	//
	//
	public static String getPriorHistoryCommand() {
		if (commandHistory.isEmpty()) {
			return null;
		}
		if (commandIndex < 0) {
			commandIndex = commandHistory.size() - 1;
		}
		if (commandIndex > commandHistory.size() - 1) {
			commandIndex = 0;
		}
		return commandHistory.get(commandIndex++);
	}

	//

	//
	private static List<String> commandHistory = new ArrayList<String>();
	private static int commandIndex;

	//
	private void writeCmd(String cmd) throws IOException {
		if (cmd != null) {
			while (position > 0) {
				position--;
				chars.remove(position);
				outputStream.write(BS);
			}
			outputStream.write(CLEAR_EOL);
			for (char c : cmd.toCharArray()) {
				chars.add(c);
			}
			outputStream.write(cmd.getBytes());
			position = positionAtToEndOfLine();
		}else{
			position=0;
			chars.clear();
		}
	}

	//
	private String doRead(String prompt, CommandCompleter completer)
			throws IOException {
		int b;
		int tabCount = 0;
		print(prompt);
		flush();
		do {
			b = tryNext();
			if (b == -1) {
				break;
			}
			if (0x20 <= b && b < 0x7f) {
				outputStream.write(b);
				flush();

				if (isEndOfLine()) {
					chars.add((char) b);
					position++;
				} else {
					chars.add(position++, (char) (0xff & b));
					for(int i=position;i<chars.size();i++){
						outputStream.write(chars.get(i));
					}
					for(int i=position;i<chars.size();i++){
						outputStream.write(CURSOR_LEFT);
					}
				}
			}
			
			if (b == '\r') {
				String ccc = charsToString();
				addHistoryCommand(ccc);
				// This seems to be correct, at least if ONLCR=1
				outputStream.write('\r');
				outputStream.write('\n');
				flush();
				break;
			}

			if (b == erase||b==BS) {
				if (position > 0) {
					position--;
					chars.remove(position);
					outputStream.write(BS);
					outputStream.write(CLEAR_EOL);
					//
					for(int i=position;i<chars.size();i++){
						outputStream.write(chars.get(i));
					}
					for(int i=position;i<chars.size();i++){
						outputStream.write(CURSOR_LEFT);
					}
				}
			} else if (b == FF) {
				outputStream.write(CURSOR_HOME);
				outputStream.write(CLEAR_SCREEN_END2);
				print(prompt);
				for (Character c : chars) {
					outputStream.write((byte) c.charValue());
				}
			} else if (b == ESC) {
				b = requireNext();

				if (b == '[') {
					b = requireNext();
					if (b == 'A') { // Up || down
						writeCmd(getNextHistoryCommand());

					} else if (b == 'B') {
						writeCmd(getPriorHistoryCommand());

					} else if (b == 'C') { // Right
						if (!isEndOfLine()) {
							position++;
							outputStream.write(CURSOR_RIGHT);
						}
					} else if (b == 'D') { // Left
						if (position > 0) {
							position--;
							outputStream.write(CURSOR_LEFT);
						}
					}
				} else if (b == 'b') { // backward-word
					int i =0;
					int count = position - i;
					for (int x = 0; x < count; x++) {
						outputStream.write(CURSOR_LEFT);
					}
					position = position - i;
				} else if (b == 'f') { // forward-word
					int i = findEndOfWord(chars, position);

					int count = position - i;
					for (int x = 0; x < count; x++) {
						outputStream.write(CURSOR_LEFT);
					}
					position = position - i;
				}
			} else if (b == TAB) {
				// -----------------------------------------------------------------------
				// Tab completion
				// -----------------------------------------------------------------------

				// -----------------------------------------------------------------------
				// completion is
				// shown. However, it should only require a single tab to
				// complete any
				// matches as far as possible.
				// -----------------------------------------------------------------------

				String currentLine = charsToString();

				// This probably assumes too much about how a program
				// wants to completeStrings something.
				// It's probably best to pass all the parameters to the
				// completer and have it return a bigger object.
				// In: current line, cursor position, out: options

				int wordStart = currentLine.lastIndexOf(' ', position);
				if (wordStart == -1) {
					wordStart = 0;
				} else {
					wordStart += 1;
				}
				List<String> options = completer.complete(currentLine, position);
				String s = findLongestMatch(0, options);
				/*
				 * foo bar ^ word start ^ position
				 * completion="barbara"
				 */
				int wordPosition = position - wordStart;
				for (int i = wordPosition; i < s.length(); i++) {
					char c = s.charAt(i);
					chars.add(c);
					outputStream.write(c);
				}

				if (options.size() == 0) {
					// Do nothing
				} else if (options.size() == 1) {
					tabCount = 0;
					chars.add(' ');
					outputStream.write(' ');
				} else {
					if (chars.size() == position) {
						if (tabCount == 1) {
							outputStream.write('\r');
							outputStream.write('\n');
							TreeSet<String> sortedOptions = new TreeSet<String>(
									options);
							for (String option : sortedOptions) {
								println(option);
							}
							print(prompt + charsToString());
							tabCount = 0;
						} else {
							tabCount++;
						}
					}
				}
				position = positionAtToEndOfLine();
			} else if (b == ETX) {
				tabCount = 0;
				position = 0;
				chars = new ArrayList<Character>();
				println("");
				print(prompt);
			}
			flush();
		} while (true);
		return charsToString();
	}

	/**
	 *
	 * @param start
	 *            The character to start checking at
	 * @param options
	 *            The list of strings to search in
	 */
	public static String findLongestMatch(int start, List<String> options) {
		if (options.isEmpty()) {
			return "";
		}
		String s = options.get(0);
		for (int i = start; i < s.length(); i++) {
			char c = s.charAt(i);
			for (int j = 1, optionsSize = options.size(); j < optionsSize; j++) {
				String t = options.get(j);
				if (t.length() == i) {
					return t;
				}
				if (c != t.charAt(i)) {
					return t.substring(0, i);
				}
			}
		}
		return s;
	}
	//
	private int positionAtToEndOfLine() {
		return chars.size();
	}
	//
	private String charsToString() {
		StringBuffer buffer = new StringBuffer(chars.size());
		for (Character c : chars) {
			buffer.append(valueOf(c));
		}
		return buffer.toString();
	}
	//
	private boolean isEndOfLine() {
		return position == chars.size();
	}
	//
	private int tryNext() throws IOException {
		int b = inputStream.read();
		if (b == -1) {
			return -1;
		}
		return b;
	}
	//
	private int requireNext() throws IOException {
		int b = inputStream.read();
		if (b == -1) {
			throw new IOException("Unexpected EOF.");
		}
		return b;
	}
	//
	public static int findEndOfWord(List<Character> chars, int position) {
		// Find first word
		int i =0;
		int end = chars.size();
		for (; i < end; i++) {
			Character c = chars.get(i);
			if (!isLetter(c) && !isDigit(c)) {
				return i;
			}
		}
		return i;
	}

	// -----------------------------------------------------------------------
	// Output
	// -----------------------------------------------------------------------

	private byte[] toBytes(String s) {
		return s.getBytes();
	}

	public void print(String s) throws IOException {
		outputStream.write(toBytes(s));
	}

	public void println(String s) throws IOException {
		outputStream.write(toBytes(s));
		outputStream.write('\r');
		outputStream.write('\n');
	}

	/**
	 * Sets the prompt of the terminal.
	 */
	public void sendPrompt(String s) throws IOException {
		outputStream.write(ESC);
		outputStream.write(toBytes("]0;")); 
		outputStream.write(toBytes(s));
		outputStream.write(BEL);
		flush();
	}

	public void flush() throws IOException {
		outputStream.flush();
	}
}
