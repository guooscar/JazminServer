/**
 * 
 */
package jazmin.driver.mq.file;

import java.nio.ByteBuffer;

/**
 * @author yama
 *
 */
public class IndexFileItem {
	public static byte FLAG_READY=0;
	public static byte FLAG_REJECTED=1;
	public static byte FLAG_ACCEPTED=2;
	public static byte FLAG_EXPRIED=3;
	//
	public static final int FILE_ITEM_SIZE=32+8+1+8;
	//
	public String uuid;//32 byte
	public long dataOffset;//8byte;
	public byte flag;//accept //reject //not send 
	public long lastDelieverTime;// 8 byte
	//
	public static IndexFileItem get(byte[]bytes){
		ByteBuffer buf=ByteBuffer.wrap(bytes);
		IndexFileItem item=new IndexFileItem();
		byte uuidBytes[]=new byte[32];
		buf.get(uuidBytes);
		item.uuid=new String(uuidBytes, 0, uuidBytes.length);
		item.dataOffset=buf.getLong();
		item.flag=buf.get();
		item.lastDelieverTime=buf.getLong();
		return item;
	}
	//
	public static byte [] get(IndexFileItem item){
		byte bb[]=new byte[FILE_ITEM_SIZE];
		ByteBuffer buf=ByteBuffer.wrap(bb);
		buf.put(item.uuid.getBytes());
		buf.putLong(item.dataOffset);
		buf.put(item.flag);
		buf.putLong(item.lastDelieverTime);
		return bb;
	}
}
