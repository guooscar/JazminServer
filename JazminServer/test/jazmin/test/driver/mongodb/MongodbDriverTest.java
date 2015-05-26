/**
 * 
 */
package jazmin.test.driver.mongodb;

import jazmin.core.Jazmin;
import jazmin.driver.mongodb.MongodbDriver;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

/**
 * @author yama
 *
 */
public class MongodbDriverTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MongodbDriver driver=new MongodbDriver();
		driver.addConnection("localhost",27017,null,null,null);
		Jazmin.addDriver(driver);
		Jazmin.start();
		//
		MongoDatabase db= driver.getDatabase("test");
		MongoCollection<Document> collection = db.getCollection("test");
		Document doc = new Document("name", "MongoDB")
        .append("type", "database")
        .append("count", 1)
        .append("info", new Document("x", 203).append("y", 102));
		collection.insertOne(doc);
		//
		Document myDoc = collection.find().first();
		System.out.println(myDoc.toJson());
	}

}
