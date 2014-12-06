/**
 * 
 */
package com.dexetra.securekeyslib.util;

import android.util.Log;

import com.dexetra.securekeyslib.util.Constants.SecureKeysConstants;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author awesomeeby
 * 
 */
public class Securekeys {

	public String getPassPhrase(String phrase, int pos, Boolean isCap,
			Boolean isSp, Boolean isNum) {
        phrase = phrase.replaceAll(" ","");
		String passPhrase = "";
		for (int i = 0; i < pos; i++)
			passPhrase += getChar(phrase.toLowerCase(), i, isCap, isSp, isNum);
		passPhrase = rotateStr(passPhrase, ((int) passPhrase.charAt(0))
				% passPhrase.length());
		return passPhrase;
	}

	public char getChar(String phrase, int pos, Boolean isCap, Boolean isSp,
			Boolean isNum) {
		char c;

		String tmpStr = rotateStr(phrase + pos, pos);
		String strmCode = getMd5Hash(tmpStr);
		strmCode = strmCode.substring(24, 32);
		long mCode = Long.parseLong(strmCode, 16);
		/*
		 * Every 3rd position is number Every 4th position is Capital letter
		 * Every 5th position is symbol All other positions are small letters.
		 */
		// We need to count from [1 to n] not [0 to n]
		int nPos = pos + 1;

		if (isNum && nPos % 3 == 0)
			c = SecureKeysConstants.NUMBERS
					.charAt((int) (Math.abs(mCode) % SecureKeysConstants.NUMBERS
							.length()));
		else if (isCap && nPos % 4 == 0)
			c = SecureKeysConstants.CAPITAL_ALPHABETS.charAt((int) (Math
					.abs(mCode) % SecureKeysConstants.CAPITAL_ALPHABETS
					.length()));
		else if (isSp && nPos % 5 == 0)
			c = SecureKeysConstants.SYMBOLS
					.charAt((int) (Math.abs(mCode) % SecureKeysConstants.SYMBOLS
							.length()));
		else
			c = SecureKeysConstants.SMALL_ALPHABETS
					.charAt((int) (Math.abs(mCode) % SecureKeysConstants.SMALL_ALPHABETS
							.length()));

		return c;
	}

	private String rotateStr(String phrase, int pos) {
		String tStr = "";
		if (pos > phrase.length() - 1) {
			while (phrase.length() < pos)
				phrase += phrase;
		}
		for (int i = pos; i < phrase.length(); i++)
			tStr = tStr + phrase.charAt(i);
		for (int i = 0; i < pos; i++)
			tStr = tStr + phrase.charAt(i);
        Log.i("HashString:", "value:" + tStr);
		return tStr;
	}

	private String getMd5Hash(String input) {

		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] messageDigest = md.digest(input.getBytes());
			BigInteger number = new BigInteger(1, messageDigest);
			String md5 = number.toString(16);
			while (md5.length() < 32)
				md5 = "0" + md5;
			return md5;

		} catch (NoSuchAlgorithmException e) {
			Log.e("MD5", e.getMessage());
			return null;
		}

	}

}
