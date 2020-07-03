/**
 * 
 */
package jazmin.test.core.app;

import java.sql.SQLException;

import jazmin.core.Jazmin;
import jazmin.core.app.Application;
import jazmin.core.app.AutoWired;
import jazmin.driver.http.HttpClientDriver;
import jazmin.driver.jdbc.DruidConnectionDriver;
import jazmin.driver.rpc.JazminRpcDriver;
import jazmin.log.LoggerFactory;
import jazmin.server.rpc.RpcServer;
import jazmin.test.core.app.TestAction.TestActionImpl;

/**
 * @author yama
 * 31 Mar, 2015
 */
public class TestApplication extends Application {
	@AutoWired
	TestActionImpl testAction;
	//
	@Override
	public void init() throws Exception {
		
	}
	//
	public void start() {
		System.err.println(testAction.testService);
		System.err.println(testAction.testService.testDAO);
		System.err.println(testAction.testService.testDAO.connectionDriver);
	}
	//
	public static void runAccount() throws Exception{
		LoggerFactory.setLevel("ALL");
		LoggerFactory.setFile("/tmp/" + TestApplication.class.getSimpleName() + ".log", true);
		//
		Jazmin.setServerName("TableItAccountSystem_uat");
		String dbUser = "root";
		String dbPassword = "ITITitit666!@#UAT";
		String dbHost = "gz-cdb-0al816nb.sql.tencentcdb.com:61141/db_tableit_account_uat";
		DruidConnectionDriver driver = new DruidConnectionDriver();
		driver.setDriverClass("com.mysql.jdbc.Driver");
		driver.setUrl("jdbc:mysql://" + dbHost
				+ "?useSSL=false&serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=UTF-8&rewriteBatchedStatements=true");
		driver.setUser(dbUser);
		driver.setPassword(dbPassword);
		Jazmin.addDriver(driver);
		//
		HttpClientDriver httpClientDriver = new HttpClientDriver();
		Jazmin.addDriver(httpClientDriver);
		//
		//
		RpcServer rpcServer = new RpcServer();
		rpcServer.setPort(9887);
		Jazmin.addServer(rpcServer);
		//
		Jazmin.loadApplication("/Users/skydu/eclipse-workspace11/TableItAccountSystem/release/TableItAccountSystem.jaz");
		Jazmin.start();
	}
	//
	public static void runBiz() throws Exception{
		LoggerFactory.setLevel("ALL");
		LoggerFactory.setFile("/tmp/" + TestApplication.class.getSimpleName() + ".log", true);
		//
		Jazmin.setServerName("TableItBizSystem_uat_0");
		String dbUser = "root";
		String dbPassword = "ITITitit666!@#UAT";
		String dbHost = "gz-cdb-0al816nb.sql.tencentcdb.com:61141/db_tableit_biz_uat";
		DruidConnectionDriver driver = new DruidConnectionDriver();
		driver.setDriverClass("com.mysql.jdbc.Driver");
		driver.setUrl("jdbc:mysql://" + dbHost
				+ "?useSSL=false&serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=UTF-8&rewriteBatchedStatements=true");
		driver.setUser(dbUser);
		driver.setPassword(dbPassword);
		Jazmin.addDriver(driver);
		//
		HttpClientDriver httpClientDriver = new HttpClientDriver();
		Jazmin.addDriver(httpClientDriver);
		//
		JazminRpcDriver rpcDriver = new JazminRpcDriver();
		rpcDriver.addRemoteServer("TableItAccountSystem", "app", "127.0.0.1", 9996);
//        rpcDriver.addRemoteServer(TableItAccountSystem.class.getSimpleName(), "app", "uat2.itit.io", 9996);
		Jazmin.addDriver(rpcDriver);
		//
		//
		RpcServer rpcServer = new RpcServer();
		rpcServer.setPort(9887);
		Jazmin.addServer(rpcServer);
		//
		Jazmin.loadApplication("/Users/skydu/eclipse-workspace11/TableItBizSystem/release/TableItBizSystem.jaz");
		Jazmin.start();
	}
	//
	public static void main(String[] args)throws Exception{
		runAccount();
	}
}
