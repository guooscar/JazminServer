package jazmin.server.webssh;

/**
 * 
 * @author yama
 *
 */
public interface ScriptChannelContext{
	/**
	 * except callback
	 */
	public static interface ExpectCallback{
		void invoke(String input);
	}
	/**
	 * except callback
	 */
	public static interface HookInputCallback{
		void invoke(String input);
	}
	/**
	 * ticket callback
	 */
	public static interface TicketCallback{
		void invoke(long ticket);
	}
	/**
	 * action callback
	 */
	public static interface ActionCallback{
		void invoke();
	}
	/**
	 * http callback
	 */
	public static interface HttpCallback{
		void invoke(String response,String error);
	}
	/**
	 * @return get ssh server address
	 */
	String host();
	/**
	 * @return get ssh server port
	 */
	int port();
	/**
	 * @return ssh server user
	 */
	String user();
	/**
	 * @return ssh server password
	 */
	String password();
	/**
	 * @return current ticket
	 */
	long ticket();
	/**
	 * send message to ssh server
	 * @param msg the message will be sent
	 */
	void sends(String msg);
	/**
	 * send message to websocket client
	 * @param msg the message will be sent
	 */
	void sendc(String msg);
	/**
	 * close ssh session
	 */
	void close();
	/**
	 * ticket callback will invoke every 1 second
	 * @param callback the callback
	 */
	void ticket(TicketCallback callback);
	
	long setTimeout(int ticket,ActionCallback callback);
	void clearTimeout(long ticket);
	/**
	 * check every line of server response if reponse string match regex the callback will be invoked
	 * @param regex the regex pattern
	 * @param callback the callback
	 */
	void expect(String regex,ExpectCallback callback);
	void expectClear();
	void hookin(HookInputCallback callback);
	/**
	 * the callback will be invoked after connection opend
	 * @param callback
	 */
	void open(ActionCallback callback);
	/**
	 * the callback will be invoked after connection closed
	 * @param callback
	 */
	void close(ActionCallback callback);
	/**
	 * preform a http request 
	 * @param url the http url
	 * @param callback response callback
	 */
	void http(String url,HttpCallback callback);
	/**
	 * record log
	 * @param msg the log message
	 */
	void log(String msg);
	/**
	 * enable user input
	 * @param enable
	 */
	void enableInput(boolean enable);
	/**
	 * is user input enabled
	 * @return
	 */
	boolean enableInput();
}