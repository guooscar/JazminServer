/**
 * 
 */
package jazmin.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.jcraft.jzlib.GZIPOutputStream;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

/**
 * @author yama 26 Dec, 2014
 */
public class IOUtil {
	private static Logger logger = LoggerFactory.get(IOUtil.class);
	private static final int CACHE_SIZE = 512;

	//
	//
	public static byte[] compress(byte[] inputs) {
		if (inputs.length == 0) {
			return inputs;
		}
		Deflater deflater = new Deflater(Deflater.BEST_SPEED);
		deflater.setInput(inputs);
		deflater.finish();
		byte outputs[] = new byte[0];
		try(ByteArrayOutputStream stream = new ByteArrayOutputStream(inputs.length);){
			byte[] bytes = new byte[CACHE_SIZE];
			int value;
			while (!deflater.finished()) {
				value = deflater.deflate(bytes);
				stream.write(bytes, 0, value);
			}
			outputs = stream.toByteArray();
			deflater.end();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return outputs;
	}
	//
	public static byte[] gzipCompress(byte[] inputs)throws Exception {
		if (inputs.length == 0) {
			return inputs;
		}
		try(ByteArrayOutputStream bos=new ByteArrayOutputStream();
				GZIPOutputStream is=new GZIPOutputStream(bos);){
			is.write(inputs);
			is.close();
			return bos.toByteArray();
		}
	}
	//
	public static byte[] gzipDecompress(byte[] inputs)throws Exception {
		if (inputs.length == 0) {
			return inputs;
		}
		try(ByteArrayInputStream bos=new ByteArrayInputStream(inputs);
				GZIPInputStream	in=new GZIPInputStream(bos);){
			ByteArrayOutputStream out=new ByteArrayOutputStream();
			IOUtil.copy(in, out);
			IOUtil.closeQuietly(in);
			IOUtil.closeQuietly(out);
			return out.toByteArray();
		}
	}
	//
	public static byte[] decompress(byte[] input) {
		if (input.length == 0) {
			return input;
		}
		Inflater decompressor = new Inflater();
		decompressor.setInput(input);
		ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);
		byte[] buf = new byte[CACHE_SIZE];
		try {
			while (!decompressor.finished()) {
				int count = decompressor.inflate(buf);
				if(count==0) {
					break;
				}
				bos.write(buf, 0, count);
			}
		} catch (DataFormatException e) {
			return input;
		} finally {
			try {
				decompressor.end();
				bos.close();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
		byte[] decompressedData = bos.toByteArray();
		return decompressedData;
	}

	//
	/**
	 */
	public static void closeQuietly(OutputStream output) {
		try {
			if (output != null) {
				output.flush();
				output.close();
			}
		} catch (IOException ioe) {
			logger.error(ioe.getMessage(), ioe);
		}
	}

	//
	/**
	 */
	public static void closeQuietly(InputStream input) {
		try {
			if (input != null) {
				input.close();
			}
		} catch (IOException ioe) {
			logger.error(ioe.getMessage(), ioe);
		}
	}

	//
	/**
	 * copy inputstream data to output stream
	 */
	public static long copy(InputStream input, OutputStream output)
			throws IOException {
		byte[] buffer = new byte[4096];
		long count = 0;
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}

	/**
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static String getContent(InputStream in) throws IOException {
		try (InputStreamReader isr = new InputStreamReader(in);
				BufferedReader br = new BufferedReader(isr);) {
			StringBuilder sb = new StringBuilder();
			String s = null;
			while ((s = br.readLine()) != null) {
				sb.append(s).append("\n");
			}
			return sb.toString();
		}
	}
	//
	public static List<String>getContentList(InputStream inStream)throws IOException{
		List<String>inputLines=new ArrayList<String>();
		BufferedReader br = new BufferedReader(new InputStreamReader(inStream));
		String line = null;
		while ((line = br.readLine()) != null) {
			inputLines.add(line);
		}
		return inputLines;
	}
	//
	public static void unzip(File file, File destFolder) throws IOException {
		final int BUFFER=2048;
		BufferedOutputStream dest = null;
		BufferedInputStream is = null;
		ZipEntry entry;
		ZipFile zipfile = new ZipFile(file);
		Enumeration<? extends ZipEntry> e = zipfile.entries();
		while (e.hasMoreElements()) {
			entry = (ZipEntry) e.nextElement();
			if (entry.isDirectory()) {
				File f = new File(destFolder, entry.getName());
				f.mkdirs();
			} else {
				InputStream iis=zipfile.getInputStream(entry);
				is = new BufferedInputStream(iis);
				int count;
				byte data[] = new byte[BUFFER];
				File theFile = new File(destFolder, entry.getName());
				FileOutputStream fos = new FileOutputStream(theFile);
				dest = new BufferedOutputStream(fos, BUFFER);
				while ((count = is.read(data, 0, BUFFER)) != -1) {
					dest.write(data, 0, count);
				}
				dest.flush();
				dest.close();
				is.close();
				iis.close();
			}
		}
		zipfile.close();
	}
	/**
	 * copy file from source file to dest file 
	 */
	public static void copyFile(File sourceFile,File destFile)throws Exception{
		copy(new FileInputStream(sourceFile),new FileOutputStream(destFile));
	}
	/**
	 * copy file from source url to destfile
	 */
	public static void copyFile(
			String sourceURL,
			String destFilePath,
			BiConsumer<Long,Long>progress)
			throws Exception {
		File destFile = new File(destFilePath);
		URL url = new URL(sourceURL);
		FileOutputStream fos = new FileOutputStream(destFile);
		URLConnection connection=url.openConnection();
		long contentLength=connection.getContentLength();
		long currentLength=0;
		InputStream is = connection.getInputStream();
		byte[] buffer = new byte[4096];
        int n = 0;
        while (-1 != (n = is.read(buffer))) {
        	currentLength+=n;
            fos.write(buffer, 0, n);
            if(progress!=null){
            	progress.accept(contentLength,currentLength);
            }
        }
		IOUtil.closeQuietly(is);
		IOUtil.closeQuietly(fos);
	}
	/**
	 *  if we use file.delete to delete a none empty directory,delete action will
	 *  fail,we need to delete all file under this directory first.
	 */
	public static boolean deleteDirectory(File directory) {
		if(!directory.exists()){
			return true;
		}
		Arrays.asList(directory.listFiles()).forEach(file->{
			if (file.isDirectory()) {
				deleteDirectory(file);
			} else {
				file.delete();
			}
		});	
		return (directory.delete());
	}

	/**
	 *convert input stream to byte array 
	 */
	public static byte[] toByteArray(InputStream input) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		for (int n = input.read(buf); n != -1; n = input.read(buf)) {
			os.write(buf, 0, n);
		}
		return os.toByteArray();
	}

	/**
	 * convert inputstream to string
	 */
	public static String toString(InputStream input) throws IOException {
		StringWriter sw = new StringWriter();
		copy(input, sw);
		return sw.toString();
	}
	/**
	 * copy input stream to writer
	 */
	public static void copy(InputStream input, Writer output)
			throws IOException {
		InputStreamReader in = new InputStreamReader(input); // NOSONAR
		copy(in, output);
	}
	/**
	 *copy input reader to output writer 
	 */
	public static int copy(Reader input, Writer output) throws IOException {
		long count = copyLarge(input, output);
		if (count > Integer.MAX_VALUE) {
			return -1;
		}
		return (int) count;
	}
	//
	public static long copyLarge(Reader input, Writer output)
			throws IOException {
		char[] buffer = new char[1024 * 4];
		long count = 0;
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}
}
