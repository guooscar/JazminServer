/**
 * 
 */
package jazmin.codec.rtp;

/**
 * http://www.iana.org/assignments/rtp-parameters/rtp-parameters.xhtml
 * 
 * @author yama 1 May, 2015
 */
public class RtpPayloadType {
	private static RtpPayloadType[] rtpPayloadTypes = new RtpPayloadType[128];
	static {
		c(0, "PCMU", "A", 8000);
		c(1, "Reserved", "", 0);
		c(2, "Reserved", "", 0);
		c(3, "GSM", "A", 8000);// 1 [RFC3551]
		c(4, "G723", "A", 8000);// 1 [Vineet_Kumar][RFC3551]
		c(5, "DVI4", "A", 8000);// 1 [RFC3551]
		c(6, "DVI4", "A", 16000);// 1 [RFC3551]
		c(7, "LPC", "A", 8000);// 1 [RFC3551]
		c(8, "PCMA", "A", 8000);// 1 [RFC3551]
		c(9, "G722", "A", 8000);// 1 [RFC3551]
		c(10, "L16", "A", 44100);// 2 [RFC3551]
		c(11, "L16", "A", 44100);// 1 [RFC3551]
		c(12, "QCELP", "A", 8000);// 1 [RFC3551]
		c(13, "CN", "A", 8000);// 1 [RFC3389]
		c(14, "MPA", "A", 90000);// [RFC3551][RFC2250]
		c(15, "G728", "A", 8000);// 1 [RFC3551]
		c(16, "DVI4", "A", 11025);// 1 [Joseph_Di_Pol]
		c(17, "DVI4", "A", 22050);// 1 [Joseph_Di_Pol]
		c(18, "G729", "A", 8000);// 1 [RFC3551]
		c(19, "Reserved", "A", 0);//
		c(20, "Unassigned", "A", 0);//
		c(21, "Unassigned", "A", 0);//
		c(22, "Unassigned", "A", 0);//
		c(23, "Unassigned", "A", 0);//
		c(24, "Unassigned", "V", 0);//
		c(25, "CelB", "V", 90000);// [RFC2029]
		c(26, "JPEG", "V", 90000);// [RFC2435]
		c(27, "Unassigned", "V", 0);//
		c(28, "nv", "V", 90000);// [RFC3551]
		c(29, "Unassigned", "V", 0);//
		c(30, "Unassigned", "V", 0);//
		c(31, "H261", "V", 90000);// [RFC4587]
		c(32, "MPV", "V", 90000);// [RFC2250]
		c(33, "MP2T", "AV", 90000);// [RFC2250]
		c(34, "H263", "V", 90000);// [Chunrong_Zhu]	
		for(int i=35;i<=71;i++){
			c(i,"Unassigned","?",0);
		}
		for(int i=72;i<=76;i++){
			c(i,"Reserved for RTCP conflict avoidance","",0);
		}
		for(int i=77;i<=95;i++){
			c(i,"Unassigned","?",0);
		}
		for(int i=96;i<=127;i++){
			c(i,"dynamic","?",0);
		}
	}
	//
	private static void c(int id, String name, String mediaType, int clockRate) {
		RtpPayloadType pt = new RtpPayloadType(id, name, mediaType, clockRate);
		rtpPayloadTypes[id] = pt;
	}
	//
	public static RtpPayloadType get(int id){
		if(id<0||id>=rtpPayloadTypes.length-1){
			throw new IllegalArgumentException("bad rtp type:"+id);
		}
		return rtpPayloadTypes[id];
	}
	//
	private int id;
	private String name;
	private String mediaType;
	private int clockRate;

	public RtpPayloadType(int id, String name, String mediaType, int clockRate) {
		super();
		this.id = id;
		this.name = name;
		this.mediaType = mediaType;
		this.clockRate = clockRate;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the mediaType
	 */
	public String getMediaType() {
		return mediaType;
	}

	/**
	 * @param mediaType
	 *            the mediaType to set
	 */
	public void setMediaType(String mediaType) {
		this.mediaType = mediaType;
	}

	/**
	 * @return the clockRate
	 */
	public int getClockRate() {
		return clockRate;
	}

	/**
	 * @param clockRate
	 *            the clockRate to set
	 */
	public void setClockRate(int clockRate) {
		this.clockRate = clockRate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RtpPayloadType [id=" + id + ", name=" + name + ", mediaType="
				+ mediaType + ", clockRate=" + clockRate + "]";
	}

}
