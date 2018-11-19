package org.psoft.wishlist.service;

import java.io.OutputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EmailerService {
	
	@Value("${send.notifications:true}")
	boolean sendEmail;
	
	public void sendMail(String to, String from, String subject, String body) throws Exception{
		if (!sendEmail) {
			System.out.println("To: " + to);
			System.out.println("From: " + from);
			System.out.println("Subject: " + from);
			System.out.println("");
			System.out.println(body);
			return;
		}
		Process process = Runtime.getRuntime().exec(new String[] {"/usr/bin/mail",
				"-a", from,
				"-s", subject,
				to});
		
		
		OutputStream outputStream = process.getOutputStream();
		outputStream.write(body.getBytes());
		outputStream.close();
		
		process.waitFor();
		
	}

}
