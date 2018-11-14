package org.psoft.wishlist.util;

public class TokenGenerator {

	public static String createToken(int length){
		StringBuilder token = new StringBuilder(length);
		
		for (int i=0;i<length;i++){
			int r = (int)Math.floor(Math.random()*62);
			token.append(
					base62(r));
		}
		
		return token.toString();
	}
	
	public static char base62(int r) {
		if (r<10) {
			return (char)(r+48);
		} else if (r<36) {
			return (char)(r+55);
		} else {
			return (char)(r+61);
		}
	}
	
}
