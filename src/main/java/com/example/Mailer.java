package com.example;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Mailer {
//	public void sendEmail () {
//		String fromEmail = "trees.zendesk@gmail.com";
//		String fromPassword = "W3lcome123";
//		// Recipient's email ID needs to be mentioned.
//        String to = "diastowo@gmail.com";
//        String from = fromEmail;
//        String host = "smtp.gmail.com";
//
//        // Get system properties
//        Properties properties = System.getProperties();
//
//        // Setup mail server
//        properties.put("mail.smtp.host", host);
//        properties.put("mail.smtp.port", "465");
//        properties.put("mail.smtp.ssl.enable", "true");
//        properties.put("mail.smtp.auth", "true");
//
//        Session session = Session.getInstance(properties, new Authenticator() {
//
//            protected PasswordAuthentication getPasswordAuthentication() {
//                return new PasswordAuthentication(fromEmail, fromPassword);
//            }
//
//        });
//
//        // Used to debug SMTP issues
//        session.setDebug(true);
//
//        try {
//            MimeMessage message = new MimeMessage(session);
//            message.setFrom(new InternetAddress(from));
//            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
//            message.addRecipient(Message.RecipientType.BCC, new InternetAddress("yulius.agung@treessolutions.com"));
//            message.setSubject("IG Comment Connector Reminder");
//            message.setText("Hi,\n\nYour Instagram Comment Connector token is about to expired, please re-generate the token again.\n\nThanks.\n\nthis is an auto generate email. do not reply.");
//
//            System.out.println("sending...");
//            Transport.send(message);
//            System.out.println("Sent message successfully....");
//        } catch (MessagingException mex) {
//            mex.printStackTrace();
//        }
//	}
}
