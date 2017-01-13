/**
 * 
 */
package jazmin.driver.influxdb;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author yama
 *
 */
public class InfluxdbResultFormatter {
	public static String dump(InfluxdbResponse response){
		StringWriter sw=new StringWriter();
		PrintWriter pw=new PrintWriter(sw);
		if(response==null){
			return "";
		}
		for(InfluxdbResult result:response.results){
			pw.println("result.series count:"+result.series.length);
			if(result.error!=null){
				pw.println("error:"+result.error);
			}
			int idx=0;
			for(InfluxdbSeries ser:result.series){
				pw.println();
				pw.println("series#"+(++idx)+" name:"+ser.name);
				String fmt="%-10s ";
				for(int i=0;i<ser.columns.length;i++){
					fmt+="%-25s ";
				}
				if(!fmt.isEmpty()){
					Object t[]=new Object[ser.columns.length+1];
					System.arraycopy(ser.columns,0, t, 1, ser.columns.length);
					t[0]="#";
					pw.format(fmt+"\n",t);
				}
				StringBuilder splitLine=new StringBuilder();
				for(int i=0;i<(10+ser.columns.length*25);i++){
					splitLine.append("-");
				}
				pw.println(splitLine.toString());
				//
				int valueIdx=1;
				for(String ss[]:ser.values){
					Object t[]=new Object[ss.length+1];
					System.arraycopy(ss,0, t, 1, ss.length);
					t[0]=""+(valueIdx++);
					pw.format(fmt+"\n",t);
				}
			}
		}
		return sw.toString();
	}
}
