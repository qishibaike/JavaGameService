package org.tont.util;

import java.util.Random;

public class TokenHelper {
	
	private static final String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

	public static String makeToken() {
	     Random random=new Random();
	     StringBuffer sb=new StringBuffer();
	     for(int i=0;i<32;i++){
	       int number=random.nextInt(62);
	       sb.append(str.charAt(number));
	     }
	     return sb.toString();
	}
	
}
