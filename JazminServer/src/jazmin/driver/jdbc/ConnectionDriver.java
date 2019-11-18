/**
 * 
 */
package jazmin.driver.jdbc;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

import jazmin.core.Driver;
import jazmin.core.Jazmin;
import jazmin.core.thread.DispatcherCallbackAdapter;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.io.InvokeStat;

/**
 * @author yama
 * 27 Dec, 2014
 */
public abstract class ConnectionDriver extends Driver{
	private static Logger logger=LoggerFactory.get(ConnectionDriver.class);
	//
	private Map<String,InvokeStat> sqlStatMap;
	private boolean isStatSql;
	private AtomicLong totalInvokeCounter;
	//
	static class ConnectionStatus {
		public ConnectionWrapper connection;
		public boolean needTranscation;
	}
	//
	private ThreadLocal<ConnectionStatus>globalStatusHolder;
	//
	public ConnectionDriver() {
		isStatSql=true;
		sqlStatMap=new ConcurrentHashMap<String, InvokeStat>();
		globalStatusHolder=new ThreadLocal<ConnectionDriver.ConnectionStatus>();
		totalInvokeCounter=new AtomicLong();
	}
	//
	public long getTotalInvokeCount(){
		return totalInvokeCounter.longValue();
	}
	//
	public void statSql(String sql,int time,boolean error){
		totalInvokeCounter.incrementAndGet();
		if(!isStatSql){
			return;
		}
		InvokeStat stat=sqlStatMap.get(sql);
		if(stat==null){
			if(sqlStatMap.size()>500){
				//max stat 500 sql query
				return;
			}
			stat=new InvokeStat();
			stat.name=sql;
			sqlStatMap.put(stat.name, stat);
		}
		stat.invoke(error, time,time);
	}
	//
	public List<InvokeStat>getInvokeStats(){
		return new ArrayList<InvokeStat>(sqlStatMap.values());
	}
	/**
	 * @return the isStatSql
	 */
	public boolean isStatSql() {
		return isStatSql;
	}

	/**
	 * @param isStatSql the isStatSql to set
	 */
	public void setStatSql(boolean isStatSql) {
		this.isStatSql = isStatSql;
	}

	//
	public abstract Connection getWorkConnection()throws SQLException;
	//
	public void executeQuery(String sql,
			BiConsumer<ResultSetMetaData,ResultSet>callback,
			Object ...args) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = getWorkConnection();
			stmt = conn.prepareStatement(sql);
			ConnectionUtil.set(stmt, args);
			stmt.setMaxRows(10000);
			ResultSet rs = stmt.executeQuery();
			ResultSetMetaData metaData=rs.getMetaData();
			callback.accept(metaData, rs);
			rs.close();
			stmt.close();
			conn.close();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
				logger.catching(se2);
			}
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				logger.catching(se);
			}
		}
	}
	/**
	 */
	public Connection getConnection(){
		ConnectionStatus cs=globalStatusHolder.get();
		if(cs==null){
			throw new IllegalStateException("call begin transcation first");
		}
		if(cs.connection==null){
			try {
				Connection realConnection =getWorkConnection();
				realConnection.setAutoCommit(!cs.needTranscation);
				cs.connection=new ConnectionWrapper(this,realConnection);
			} catch (SQLException e) {
				throw new ConnectionException(e);
			}
		}
		return cs.connection;
	}
	//
	/**
	 * commit current transaction.
	 */
	public  void commit() {
		ConnectionStatus cs=globalStatusHolder.get();
		if(cs==null){
			//TODO 嵌套事务
			return;
		}
		try {
			if(cs.needTranscation&&cs.connection!=null){
				cs.connection.commit();
			}
		} catch (Exception e) {
			logger.catching(e);
		}
	}
	/**
	 * end current transaction
	 */
	public void endTransaction() {
		ConnectionStatus cs=globalStatusHolder.get();
		globalStatusHolder.set(null);
		if(cs==null){
			//TODO 嵌套事务
			return;
		}
		try {
			if(cs.connection!=null){
				cs.connection.realConnection.close();
			}
		} catch (Exception e) {
			logger.catching(e);
		}
	}

	/**
	 * rollback current transaction.
	 */
	public void rollback() {
		ConnectionStatus cs=globalStatusHolder.get();
		if(cs==null){
			//TODO 嵌套事务
			return;
		}
		try {
			if(cs.needTranscation&&cs.connection!=null){
				cs.connection.rollback();
			}
		} catch (Exception e) {
			logger.catching(e);
		}
	}

	/**
	 * start new transaction.
	 * @param needTranscation boolean
	 */
	public void startTransaction(boolean needTranscation) {
		ConnectionStatus cs=globalStatusHolder.get();
		if(cs!=null&&cs.connection!=null) {
			logger.error("connection not release.connection:"+cs.connection);
		}
		//
		ConnectionStatus t=new ConnectionStatus();
		t.needTranscation=(needTranscation);
		globalStatusHolder.set(t);
	}
	//-------------------------------------------------------------------------
	static class AutoTranscationCallback extends DispatcherCallbackAdapter{
		ConnectionDriver connectionDriver;
		public AutoTranscationCallback(ConnectionDriver cd) {
			this.connectionDriver=cd;
		}
		@Override
		public void before(Object instance, Method method, Object[] args)
				throws Exception {
			
			boolean needTranscation=method.isAnnotationPresent(Transaction.class);
			if(logger.isDebugEnabled()){
				logger.debug("Transcation on method:{}={}",
						method.getDeclaringClass().getSimpleName()+"."+method.getName(),
						needTranscation);
			}
			connectionDriver.startTransaction(needTranscation);
		}
		//
		@Override
		public void end(Object instance, Method method, Object[] args,
				Object ret, Throwable e) {
			if(e==null){
				connectionDriver.commit();		
			}else{
				connectionDriver.rollback();		
			}
			connectionDriver.endTransaction();
		}
	}
	//
	@Override
	public void init() throws Exception {
		AutoTranscationCallback ac=new AutoTranscationCallback(this);
		Jazmin.dispatcher.addGlobalDispatcherCallback(ac);
	
	}
}
