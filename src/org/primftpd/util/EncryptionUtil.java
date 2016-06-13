package org.primftpd.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import org.apache.mina.util.Base64;

public class EncryptionUtil{
	static private Date now = new Date();
	static private int Stretch = (int)Math.random()*1000+1;
	static private final String SALT = "T!O@P#F$"+Math.random()*100+now;
	public static String encrypt(String str){
		try {
			MessageDigest cipher = MessageDigest.getInstance("SHA-512");
			byte[] encrypted = cipher.digest((str + SALT).getBytes("UTF-8"));
			for(int i=0;i<Stretch;i++)
				encrypted = 
				(i%5==0) ? Base64.encodeBase64(encrypted) : cipher.digest(encrypted);				
			return new String(encrypted, "UTF-8");
		}catch(NoSuchAlgorithmException e){
		}catch(UnsupportedEncodingException e){
		}
		return null;
	}
}
