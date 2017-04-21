package com.document.archive.processor;

import java.util.Properties;

import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;

import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;

public class FileProcessor {

	private static final String HEADER_FILE_NAME = "file_name";
	private static final String MSG_SUBJECT = "A file has been Created";
	private static final String MSG_BODY = "The following file has been uploaded to the system ";
	@Value("${gmail.userName}")
	private String mailUser;
	@Value("${gmail.password}")
	private String mailPassword;

	public void process(Message<String> msg) {
		String fileName = (String) msg.getHeaders().get(HEADER_FILE_NAME);
		String content = msg.getPayload();

		sendMail(content);
	}

	/**
	 * Send Email to Gmail account
	 * 
	 * @param fileName
	 * @param content
	 */
	private void sendMail(String content) {

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");

		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(mailUser, mailPassword);
			}
		});

		try {

			javax.mail.Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(mailUser));
			message.setRecipients(javax.mail.Message.RecipientType.TO, InternetAddress.parse(mailUser));
			message.setSubject(MSG_SUBJECT);
			message.setText(MSG_BODY + content);

			Transport.send(message);

		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}

	}

}
