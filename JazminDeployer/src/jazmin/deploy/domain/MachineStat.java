/**
 * 
 */
package jazmin.deploy.domain;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jazmin.util.SshUtil;

/**
 * @author yama
 *
 */
public class MachineStat {
	public static class FSInfo{
		public String mountPoint;
		public long used;
		public long free;
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "FSInfo [mountPoint=" + mountPoint + ", used=" + used
					+ ", free=" + free + "]";
		}
		
	}
	//
	public static class NetInfInfo{
		public String name;
		public boolean ipv4;
		public boolean ipv6;
		public long rx;
		public long tx;
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "NetInfInfo [name=" + name + ", ipv4=" + ipv4 + ", ipv6="
					+ ipv6 + ", rx=" + rx + ", tx=" + tx + "]";
		}
		
	}
	public long upTime;
	public String ulimit;
	public String hostName;
	public String load1;
	public String load5;
	public String load10;
	public long memTotal;
	public long memFree;
	public long memBuffers;
	public long memCached;
	public long memSwapTotal;
	public long memSwapFree;
	public List<FSInfo>fsinfos;
	public Map<String,NetInfInfo>netInfInfos;
	//
	//
	private String host;
	private int port;
	private String user;
	private String pwd;
	private int timeout;
	//
	public MachineStat() {
		fsinfos=new ArrayList<MachineStat.FSInfo>();
		netInfInfos=new HashMap<>();
	}
	//
	public void getMachineInfo(String host,int port,String user,String pwd,int timeout)
	throws Exception{
		this.host=host;
		this.port=port;
		this.user=user;
		this.pwd=pwd;
		this.timeout=timeout;
		//
		getUpTime();
		getLoad();
		getUlimit();
		getHostName();
		getMemory();
		getFS();
		getInterfaces();
		getInterfaceInfo();
	}
	//
	private void getLoad()throws Exception{
		SshUtil.execute(host, port, user, pwd, 
				"/bin/cat /proc/loadavg",timeout,(out,err)->{
			String ss[]=out.split("\\s+");
			if(ss.length==5){
				load1=ss[0];
				load5=ss[1];
				load10=ss[2];
			}
		});
	}
	//
	private void getUpTime()throws Exception{
		SshUtil.execute(host, port, user, pwd, 
				"/bin/cat /proc/uptime",timeout,(out,err)->{
			String ss[]=out.split("\\s+");
			if(ss.length==2){
				upTime=Double.valueOf(ss[0]).longValue();
			}
		});
	}
	//
	private void getUlimit()throws Exception{
		SshUtil.execute(host, port, user, pwd, 
				"/bin/cat /etc/security/limits.conf",timeout,(out,err)->{
			BufferedReader br=new BufferedReader(new StringReader(out));
			String line=null;
			try {
				StringBuilder sb=new StringBuilder();
				while((line=br.readLine())!=null){
					line=line.trim();
					if(line.startsWith("#")||line.isEmpty()){
						continue;
					}
					sb.append(line+"\n");
				}
				ulimit=sb.toString();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
	//
	private void getHostName()throws Exception{
		SshUtil.execute(host, port, user, pwd, 
				"/bin/cat /etc/hosts",timeout,(out,err)->{
			hostName=out;
		});
	}
	//
	private void getMemory()throws Exception{
		SshUtil.execute(host, port, user, pwd, 
				"/bin/cat /proc/meminfo",timeout,(out,err)->{
			BufferedReader br=new BufferedReader(new StringReader(out));
			String line=null;
			try {
				while((line=br.readLine())!=null){
					String ss[]=line.split("\\s+");
					if(ss.length==3){
						long val=Long.valueOf(ss[1]);
						val*=1024;
						switch( ss[0] ){
						case "MemTotal:":
							memTotal = val;
						case "MemFree:":
							memFree = val;
						case "Buffers:":
							memBuffers = val;
						case "Cached:":
							memCached = val;
						case "SwapTotal:":
							memSwapTotal = val;
						case "SwapFree:":
							memSwapFree = val;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
	private void getFS()throws Exception{
		SshUtil.execute(host, port, user, pwd, 
				"/bin/df -B1",timeout,(out,err)->{
			BufferedReader br=new BufferedReader(new StringReader(out));
			String line=null;
			try {
				while((line=br.readLine())!=null){
					String ss[]=line.split("\\s+");
					if(ss.length==6 &&ss[0].indexOf("/dev/")==0){
						FSInfo fs=new FSInfo();
						fs.used=Long.valueOf(ss[2]);
						fs.free=Long.valueOf(ss[3]);
						fs.mountPoint=ss[5];
						fsinfos.add(fs);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
	//
	private void getInterfaces()throws Exception{
		SshUtil.execute(host, port, user, pwd, 
				"/sbin/ip -o addr",timeout,(out,err)->{
			BufferedReader br=new BufferedReader(new StringReader(out));
			String line=null;
			try {
				while((line=br.readLine())!=null){
					String ss[]=line.split("\\s+");
					if(ss.length>=4 &&(ss[2].equals("inet")||ss[2].equals("inet6"))){
						NetInfInfo net=new NetInfInfo();
						net.ipv4=ss[2].equals("inet");
						net.ipv6=ss[2].equals("inet6");
						net.name=ss[1];
						netInfInfos.put(net.name,net);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
	//
	private void getInterfaceInfo()throws Exception{
		SshUtil.execute(host, port, user, pwd, 
				"/bin/cat /proc/net/dev",timeout,(out,err)->{
			BufferedReader br=new BufferedReader(new StringReader(out));
			String line=null;
			try {
				while((line=br.readLine())!=null){
					String ss[]=line.split("\\s+");
					if(ss.length==17){
						String intf=ss[1];
						if(intf.indexOf(':')==-1){
							continue;
						}
						String firstStr[]=intf.split(":");
						intf=firstStr[0];
						NetInfInfo net=netInfInfos.get(intf);
						if(net==null){
							continue;
						}
						//
						net.rx=Long.parseLong(firstStr[1]);
						net.tx=Long.parseLong(ss[10]);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
}
