/**
 * 
 */
package jazmin.test.driver.mq;

import java.util.UUID;

import jazmin.driver.mq.file.IndexFile;
import jazmin.driver.mq.file.IndexFileItem;

/**
 * @author yama
 *
 */
public class IndexFileTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		IndexFile file=new IndexFile("/Users/yama/Desktop/test", 1000);
		file.open();
		//
		IndexFileItem item=new IndexFileItem();
		item.uuid=UUID.randomUUID().toString().replaceAll("-", "");
		item.dataOffset=1;
		file.addItem(item);
		file.close();
		//
	}

}
