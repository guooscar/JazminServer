/**
 * 
 */
package jazmin.driver.mongodb;

import java.util.Collection;
import java.util.List;

import org.bson.Document;

import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.ReadPreference;
import com.mongodb.ReplicaSetStatus;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.mongodb.client.ListDatabasesIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;

import jazmin.core.Driver;
import jazmin.misc.InfoBuilder;

/**
 * @author yama
 *
 */
public class MongodbDriver extends Driver{
	
	private String mongoUri;
	private MongoClient mongoClient;
	private String database;
	//
	public MongodbDriver() {
	}
	//
	@Override
	public void start() throws Exception {
		 MongoClientURI connStr = new MongoClientURI(mongoUri);
	     mongoClient = new MongoClient(connStr);
	}
	//
	
	//
	@Override
	public void stop() throws Exception {
		if(mongoClient!=null){
			mongoClient.close();
		}
	}
	/**
	 * @param option
	 * @see com.mongodb.Mongo#addOption(int)
	 */
	public void addOption(int option) {
		mongoClient.addOption(option);
	}
	/**
	 * @param dbName
	 * @see com.mongodb.Mongo#dropDatabase(java.lang.String)
	 */
	public void dropDatabase(String dbName) {
		mongoClient.dropDatabase(dbName);
	}
	/**
	 * @param async
	 * @return
	 * @see com.mongodb.Mongo#fsync(boolean)
	 */
	public CommandResult fsync(boolean async) {
		return mongoClient.fsync(async);
	}
	/**
	 * @return
	 * @see com.mongodb.Mongo#fsyncAndLock()
	 */
	public CommandResult fsyncAndLock() {
		return mongoClient.fsyncAndLock();
	}
	/**
	 * @return
	 * @see com.mongodb.Mongo#getAddress()
	 */
	public ServerAddress getAddress() {
		return mongoClient.getAddress();
	}
	/**
	 * @return
	 * @see com.mongodb.Mongo#getAllAddress()
	 */
	public List<ServerAddress> getAllAddress() {
		return mongoClient.getAllAddress();
	}
	/**
	 * @return
	 * @see com.mongodb.Mongo#getConnectPoint()
	 */
	public String getConnectPoint() {
		return mongoClient.getConnectPoint();
	}
	/**
	 * @return
	 * @see com.mongodb.MongoClient#getCredentialsList()
	 */
	public List<MongoCredential> getCredentialsList() {
		return mongoClient.getCredentialsList();
	}

	/**
	 * @param databaseName
	 * @return
	 * @see com.mongodb.MongoClient#getDatabase(java.lang.String)
	 */
	public MongoDatabase getDatabase(String databaseName) {
		return mongoClient.getDatabase(databaseName);
	}
	
	/**
	 * @return
	 * @see com.mongodb.Mongo#getMaxBsonObjectSize()
	 */
	public int getMaxBsonObjectSize() {
		return mongoClient.getMaxBsonObjectSize();
	}
	/**
	 * @return
	 * @see com.mongodb.MongoClient#getMongoClientOptions()
	 */
	public MongoClientOptions getMongoClientOptions() {
		return mongoClient.getMongoClientOptions();
	}
	
	/**
	 * @return
	 * @see com.mongodb.Mongo#getOptions()
	 */
	public int getOptions() {
		return mongoClient.getOptions();
	}
	/**
	 * @return
	 * @see com.mongodb.Mongo#getReadPreference()
	 */
	public ReadPreference getReadPreference() {
		return mongoClient.getReadPreference();
	}
	/**
	 * @return
	 * @see com.mongodb.Mongo#getReplicaSetStatus()
	 */
	public ReplicaSetStatus getReplicaSetStatus() {
		return mongoClient.getReplicaSetStatus();
	}
	/**
	 * @return
	 * @see com.mongodb.Mongo#getServerAddressList()
	 */
	public List<ServerAddress> getServerAddressList() {
		return mongoClient.getServerAddressList();
	}
	/**
	 * @return
	 * @see com.mongodb.Mongo#getUsedDatabases()
	 */
	public Collection<DB> getUsedDatabases() {
		return mongoClient.getUsedDatabases();
	}
	/**
	 * @return
	 * @see com.mongodb.Mongo#getWriteConcern()
	 */
	public WriteConcern getWriteConcern() {
		return mongoClient.getWriteConcern();
	}
	/**
	 * @return
	 * @see com.mongodb.Mongo#isLocked()
	 */
	public boolean isLocked() {
		return mongoClient.isLocked();
	}
	/**
	 * @return
	 * @see com.mongodb.MongoClient#listDatabaseNames()
	 */
	public MongoIterable<String> listDatabaseNames() {
		return mongoClient.listDatabaseNames();
	}
	/**
	 * @return
	 * @see com.mongodb.MongoClient#listDatabases()
	 */
	public ListDatabasesIterable<Document> listDatabases() {
		return mongoClient.listDatabases();
	}
	/**
	 * @param clazz
	 * @return
	 * @see com.mongodb.MongoClient#listDatabases(java.lang.Class)
	 */
	public <T> ListDatabasesIterable<T> listDatabases(Class<T> clazz) {
		return mongoClient.listDatabases(clazz);
	}
	/**
	 * 
	 * @see com.mongodb.Mongo#resetOptions()
	 */
	public void resetOptions() {
		mongoClient.resetOptions();
	}
	/**
	 * @param options
	 * @see com.mongodb.Mongo#setOptions(int)
	 */
	public void setOptions(int options) {
		mongoClient.setOptions(options);
	}
	/**
	 * @param readPreference
	 * @see com.mongodb.Mongo#setReadPreference(com.mongodb.ReadPreference)
	 */
	public void setReadPreference(ReadPreference readPreference) {
		mongoClient.setReadPreference(readPreference);
	}
	/**
	 * @param writeConcern
	 * @see com.mongodb.Mongo#setWriteConcern(com.mongodb.WriteConcern)
	 */
	public void setWriteConcern(WriteConcern writeConcern) {
		mongoClient.setWriteConcern(writeConcern);
	}
	
	/**
	 * @return
	 * @see com.mongodb.Mongo#unlock()
	 */
	public DBObject unlock() {
		return mongoClient.unlock();
	}
	/**
	 * 
	 * @return
	 */
	public MongoClient getMongoClient() {
		return mongoClient;
	}
	/**
	 * 
	 * @param mongoClient
	 */
	public void setMongoClient(MongoClient mongoClient) {
		this.mongoClient = mongoClient;
	}
	/**
	 * 
	 * @return
	 */
	public String getMongoUri() {
		return mongoUri;
	}
	/**
	 * 
	 * @param mongoUri
	 */
	public void setMongoUri(String mongoUri) {
		this.mongoUri = mongoUri;
	}
	//
	/**
	 * @return the database
	 */
	public String getDatabase() {
		return database;
	}
	/**
	 * @param database the database to set
	 */
	public void setDatabase(String database) {
		this.database = database;
	}
	//
	@Override
	public String info() {
		InfoBuilder ib= InfoBuilder.create().format("%-30s:%-30s\n");
		int idx=1;
		ib.print("connectPoint",getConnectPoint());
		ib.print("maxBsonObjectSize",getMaxBsonObjectSize());
		ib.print("writeConcern",getWriteConcern());
		ib.print("mongoUri",mongoUri);
		//
		idx=1;
		for(String name:listDatabaseNames()){
			ib.print("DB-"+idx++,name);
		}
		return ib.toString();
	}
	
	
}
