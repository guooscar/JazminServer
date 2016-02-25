package jazmin.server.web.mvc;

import java.io.InputStream;

/**
 * 
 * @author yama
 * 25 Feb, 2016
 */
public class Stream {
	private InputStream inputStream;
	private String fileName;
	private String mimeType;
	private String contentDisposition;

	public Stream(String filename,InputStream is) {
		this.fileName=filename;
		this.inputStream=is;
		this.mimeType="application/octet-stream";
		contentDisposition="attachment";
	}
	//
	/**
	 * @return the inputStream
	 */
	public InputStream getInputStream() {
		return inputStream;
	}
	/**
	 * @param inputStream the inputStream to set
	 */
	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}
	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}
	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	/**
	 * @return the mimeType
	 */
	public String getMimeType() {
		return mimeType;
	}
	/**
	 * @param mimeType the mimeType to set
	 */
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	/**
	 * @return the contentDisposition
	 */
	public String getContentDisposition() {
		return contentDisposition;
	}
	/**
	 * @param contentDisposition the contentDisposition to set
	 */
	public void setContentDisposition(String contentDisposition) {
		this.contentDisposition = contentDisposition;
	}
	
	
}
