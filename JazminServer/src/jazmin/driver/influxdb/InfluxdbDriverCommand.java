package jazmin.driver.influxdb;
import jazmin.core.Jazmin;
import jazmin.server.console.builtin.ConsoleCommand;
/**
 * 
 * @author yama
 * 26 Dec, 2014
 */
public class InfluxdbDriverCommand extends ConsoleCommand {
    private InfluxdbDriver driver;
	public InfluxdbDriverCommand() {
    	super();
    	id="influxdb";
    	desc="influxdb driver ctrl command";
    	addOption("i",false,"show driver information.",this::showDriverInfo);
    	addOption("count",false,"show invoke count.",this::showInvokeCount);
    	addOption("stat",false,"show server stat.",this::showStats);
    	addOption("diagiosis",false,"show server diagiosis.",this::showDiagiosis);
    	addOption("use",true,"set current database.",this::useDatabase);
    	addOption("query",true,"set current database.",this::query);
    	addOption("write",true,"set current database.",this::write);
    	
    	//
    	driver=Jazmin.getDriver(InfluxdbDriver.class);
    }
	//
	@Override
	public void run() throws Exception{
		 if(driver==null){
			 out.println("can not find InfluxdbDriver.");
			 return;
		 }
		 super.run();
	}
	//
    private void showDiagiosis(String args){
    	out.println(InfluxdbResultFormatter.dump(driver.showDiagnostics()));
    }
	//
    private void showStats(String args){
    	out.println(InfluxdbResultFormatter.dump(driver.showStats()));
    }
    //
    private void showDriverInfo(String args)throws Exception{
    	out.println(driver.info());
    }
    //
    private void showInvokeCount(String args)throws Exception{
    	out.println("WriteCount:\t"+driver.getWriteCount());
    	out.println("QueryCount:\t"+driver.getQueryCount());
    	out.println("ErrorCount:\t"+driver.getErrorCount());
    	
    }
    //
    private void useDatabase(String args){
    	driver.setDatabase(args.trim());
    	out.println("current database set to:"+args);
    }
    //
    private void query(String args){
    	InfluxdbResponse rsp=driver.query(args);
    	out.println(InfluxdbResultFormatter.dump(rsp));
    }
    //
    private void write(String args){
    	driver.write(args);
    	out.println("write complete.");
    }
}
