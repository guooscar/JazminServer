/**						JAZMIN SERVER SOURCE FILE
--------------------------------------------------------------------------------
	     	  ___  _______  _______  __   __  ___   __    _ 		
		     |   ||   _   ||       ||  |_|  ||   | |  |  | |		
		     |   ||  |_|  ||____   ||       ||   | |   |_| |		
		     |   ||       | ____|  ||       ||   | |       |		
		  ___|   ||       || ______||       ||   | |  _    |		
		 |       ||   _   || |_____ | ||_|| ||   | | | |   |		
		 |__yama_||__| |__||_______||_|   |_||___| |_|  |__|	 
		 
--------------------------------------------------------------------------------
 ********************************************************************************
 							Copyright (c) 2015 yama.
 This is not a free software,all rights reserved by yama(guooscar@gmail.com).
 ANY use of this software MUST be subject to the consent of yama.

 ********************************************************************************
 */
package jazmin.driver.mail;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.LongAdder;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import jazmin.core.Driver;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;

/**
 * @author yama
 *
 */
public class MailDriver extends Driver {
	private static Logger logger=LoggerFactory.get(MailDriver.class);
	//
	private String host;
	private int port;
	private String user;
	private String password;
	//
	private LongAdder totalSentCount;
	private LongAdder errorCount;
	//
	public MailDriver() {
	
	}
	//
	@Override
	public void init() throws Exception {
		if(host==null){
			throw new IllegalArgumentException("host can not be null");
		}
		if(port==0){
			throw new IllegalArgumentException("port can not be 0");
		}
		if(user==null){
			throw new IllegalArgumentException("user can not be null");
		}
		if(password==null){
			throw new IllegalArgumentException("password can not be null");
		}
	}
	//
	private static ArrayList<String>EMPTY_LIST=new ArrayList<String>();
	//
	public void send(
			String from, 
			String to,
			String subject, 
			String content) 
			throws AddressException, MessagingException {
		List<String>tos=new ArrayList<String>();
		tos.add(to);
		send(from, tos,EMPTY_LIST , EMPTY_LIST, subject, content);
	}
	/**
	 * 
	 */
	public void send(
			String from, 
			List<String> tos,
			List<String>ccs,
			List<String>bccs,
			String subject, 
			String content) 
			throws AddressException, MessagingException {
		try{
			Properties props = new Properties();
			props.put("mail.smtp.host", host);
			props.put("mail.smtp.auth", "true");
			Session session = Session.getDefaultInstance(props);
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			for(String to:tos){
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			}
			for(String cc:ccs){
				message.addRecipient(Message.RecipientType.CC, new InternetAddress(cc));
			}
			for(String bcc:bccs){
				message.addRecipient(Message.RecipientType.BCC, new InternetAddress(bcc));
			}
			message.setSubject(subject);
			Multipart multipart = new MimeMultipart();
			BodyPart contentPart = new MimeBodyPart();
			contentPart.setText(content);
			multipart.addBodyPart(contentPart);
			message.setContent(multipart);
			message.saveChanges();
			Transport transport = session.getTransport("smtp");
			transport.connect(host, user, password);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
		}catch(Exception e){
			errorCount.increment();
			logger.catching(e);
			throw e;
		}finally{
			totalSentCount.increment();
		}
	}
}
