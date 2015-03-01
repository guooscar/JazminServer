/**
 * 
 */
package jazmin.util;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
/**
 * 
 * @author yama
 * 23 Dec, 2014
 */
public class DumpUtil {
	/*dump invoke parameter*/
	public static String dumpInvokeArgs(String callToken,Object []args){
		return dumpInvokeArgs(callToken, args,0);
	}
	//
	public static String dumpInvokeArgs(String callToken,Object []args,int startIndex){
		StringBuilder sb=new StringBuilder();
		sb.append("\n--------------------------------------------------------\n");
		sb.append(callToken+"\n");
		if(args!=null){
			for(int i=startIndex;i<args.length;i++){
				sb.append("args[")
					.append(i)
					.append("]:")
					.append(JSON.toJSONString(args[i],SerializerFeature.PrettyFormat))
					.append("\n");
			}
		}
		sb.append("--------------------------------------------------------\n");
		return sb.toString();
	} 
	//
	public static String dumpInvokeObject(String callToken,Object o){
		StringBuilder sb=new StringBuilder();
		sb.append("\n--------------------------------------------------------\n")
		.append(callToken)
		.append("\n");
		if(o==null){
			sb.append("<null>\n");
		}else{
			sb.append(JSON.toJSONString(o,SerializerFeature.PrettyFormat)).append("\n");
		}
		sb.append("--------------------------------------------------------\n");
		return sb.toString();
	}
	//
	public static String dump(Object o){
		if(o==null){
			return "<null>";
		}
		return JSON.toJSONString(o,SerializerFeature.PrettyFormat);
	}
	//
	//
	public static String formatJSON(String input) {
		char array[] = input.toCharArray();
		StringBuilder result = new StringBuilder();
		int indent = 0;
		for (int i = 0; i < array.length; i++) {
			char c = array[i];
			if (c == '{' || c == '[') {
				result.append(c);
				result.append('\n');
				indent++;
				for (int j = 0; j < indent; j++) {
					result.append('\t');
				}
				continue;
			}
			if (c == '}' || c == ']') {
				indent--;
				result.append('\n');
				for (int j = 0; j < indent; j++) {
					result.append('\t');
				}
				result.append(c);
				continue;
			}
			if (c == ',') {
				result.append(c);
				result.append('\n');
				for (int j = 0; j < indent; j++) {
					result.append('\t');
				}
				continue;
			}
			result.append(c);
		}
		return result.toString();
	}
}
