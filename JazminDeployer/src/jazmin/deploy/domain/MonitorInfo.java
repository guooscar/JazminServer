package jazmin.deploy.domain;

/**
 * 
 * @author icecooly
 *
 */
public class MonitorInfo {
	//
	public static final String CATEGORY_TYPE_KV="KeyValue";
	public static final String CATEGORY_TYPE_VALUE="Value";
	public static final String CATEGORY_TYPE_COUNT="Count";
	//
	/**instance*/
	public String instance;
	/**type*/
	public String type;
	/**key*/
	public String name;
	/**value*/
	public String value;
	/**time*/
	public long time;
	//
	@Override
	public String toString(){
		StringBuilder sb=new StringBuilder();
		sb.append(time).append("\t").
		append(type).append("\t").
		append(name).append("\t").
		append(value);
		return sb.toString();
	}
}
