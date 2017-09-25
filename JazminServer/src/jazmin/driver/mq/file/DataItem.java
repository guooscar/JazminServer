package jazmin.driver.mq.file;
/**
 * 
 * @author yama
 *
 */
public class DataItem {
	public static final byte MAGIC=(byte)0xff;
	//
	public static final byte PAYLOAD_TYPE_JSON=1;
	public static final byte PAYLOAD_TYPE_RAW=2;
	//
	public static final int HEAD_LENGTH=1+1+1+2;
	//
	public byte magic;
	public byte payloadType;
	public short payloadLength;
	public byte[] payload;
}
