package com.github.ideahut.qms.shared.core.util;

import java.security.MessageDigest;

public abstract class DigestUtil {

	/*
	 * DIGEST
	 */
	public static String digest(String algorithm, String text) {
		if (text == null) {
			return "";
		}
		try {
			MessageDigest md = MessageDigest.getInstance(algorithm);
			byte[] b = md.digest(text.getBytes("UTF-8"));
			String result = "";
			for (int i = 0; i < b.length; i++) {
				result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
			}
			return result;			
		} catch (Exception e) {
			return "";
		}
	}
}
