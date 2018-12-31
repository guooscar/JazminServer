package jazmin.server.mysqlproxy.mysql.protocol.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * <pre><b>server capabilities.</b></pre>
 * @author 
 * <pre>seaboat</pre>
 * <pre><b>email: </b>849586227@qq.com</pre>
 * <pre><b>blog: </b>http://blog.csdn.net/wangyangzhizhou</pre>
 * @version 1.0
 */
public class CharsetUtil {
	private static final Map<String, Integer> CHARSET_TO_INDEX = new HashMap<String, Integer>();
	static {
		CHARSET_TO_INDEX.put("big5_chinese_ci", 1);
		CHARSET_TO_INDEX.put("latin2_czech_cs", 2);
		CHARSET_TO_INDEX.put("dec8_swedish_ci", 3);
		CHARSET_TO_INDEX.put("cp850_general_ci", 4);
		CHARSET_TO_INDEX.put("latin1_german1_ci", 5);
		CHARSET_TO_INDEX.put("hp8_english_ci", 6);
		CHARSET_TO_INDEX.put("koi8r_general_ci", 7);
		CHARSET_TO_INDEX.put("latin1_swedish_ci", 8);
		CHARSET_TO_INDEX.put("latin2_general_ci", 8);
		CHARSET_TO_INDEX.put("swe7_swedish_ci", 8);
		CHARSET_TO_INDEX.put("iso-8859-1", 14);
		CHARSET_TO_INDEX.put("iso_8859_1", 14);
		CHARSET_TO_INDEX.put("utf-8", 33);
		CHARSET_TO_INDEX.put("utf8", 33);
	}

	public static final int getIndex(String charset) {
		if (charset == null || charset.length() == 0) {
			return 0;
		} else {
			Integer i = CHARSET_TO_INDEX.get(charset.toLowerCase());
			return (i == null) ? 0 : i.intValue();
		}
	}

}
