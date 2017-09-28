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
	public static byte FLAG_READY=1;
	public static byte FLAG_REJECTED=2;
	public static byte FLAG_ACCEPTED=3;
	public static byte FLAG_EXPRIED=4;
	//
	public static final int FILE_ITEM_SIZE=1+8+8+2+1+8+2;
	//
	public static final byte MAGIC=(byte) 0xff;
	//
	public byte magic;//
	public long uuid;//32 byte
	public long dataOffset;//8byte;
	public short subscriber;//2byte
	public byte flag;//accept //reject //not send 
	public long lastDelieverTime;// 8 byte
	public short delieverTimes;
	//
	public static IndexFileItem get(byte[]bytes){
		ByteBuffer buf=ByteBuffer.wrap(bytes);
		IndexFileItem item=new IndexFileItem();
		item.magic=buf.get();
		item.uuid=buf.getLong();
		item.dataOffset=buf.getLong();
		item.subscriber=buf.getShort();
		item.flag=buf.get();
		item.lastDelieverTime=buf.getLong();
		item.delieverTimes=buf.getShort();
		return item;
	}
	//
	public static byte [] get(IndexFileItem item){
		byte bb[]=new byte[FILE_ITEM_SIZE];
		ByteBuffer buf=ByteBuffer.wrap(bb);
		buf.put(MAGIC);
		buf.putLong(item.uuid);
		buf.putLong(item.dataOffset);
		buf.putShort(item.subscriber);
		buf.put(item.flag);
		buf.putLong(item.lastDelieverTime);
		buf.putShort(item.delieverTimes);
		return bb;
	}
}
