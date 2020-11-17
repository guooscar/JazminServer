package jazmin.driver.mongodb;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import jazmin.core.app.AutoWired;
import jazmin.driver.mongodb.MongodbDriver;

/**
 * 
 * @author skydu
 *
 */
public class MongoDAO {
	//
	@AutoWired
	public MongodbDriver driver;
	//
	protected MongoDatabase database;
	//
	protected CodecRegistry pojoCodecRegistry;
	//
	protected MongoDatabase getDatabase() {
		if(database==null) {
			database=driver.getMongoClient().getDatabase(driver.getDatabase());
			pojoCodecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(),
	                fromProviders(PojoCodecProvider.builder().automatic(true).build()));
			database=database.withCodecRegistry(pojoCodecRegistry);
		}
		return database;
	}
	/**
	 * 
	 * @param databaseName
	 * @param collectionName
	 * @return
	 */
	protected MongoCollection<Document> getCollection(String databaseName,String collectionName) {
		MongoDatabase database = driver.getMongoClient().getDatabase(databaseName);
		CodecRegistry pojoCodecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build()));
		database=database.withCodecRegistry(pojoCodecRegistry);
		return database.getCollection(collectionName);
	}
	/**
	 * 
	 * @return
	 */
	protected MongoCollection<Document> getCollection(String collectionName) {
		return getDatabase().getCollection(collectionName);
	}
	//
	protected <T> MongoCollection<T> getCollection(Class<T> clazz,String collectionName) {
		return getDatabase().getCollection(collectionName,clazz);
	}
}
