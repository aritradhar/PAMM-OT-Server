//*************************************************************************************
//*********************************************************************************** *
//author Aritra Dhar 																* *
//Research Engineer																  	* *
//Xerox Research Center India													    * *
//Bangalore, India																    * *
//--------------------------------------------------------------------------------- * * 
///////////////////////////////////////////////// 									* *
//The program will do the following:::: // 											* *
///////////////////////////////////////////////// 									* *
//version 1.0 																		* *
//*********************************************************************************** *
//*************************************************************************************


package com.xrci.pamm.Util;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.whispersystems.curve25519.Curve25519;
import org.whispersystems.curve25519.Curve25519KeyPair;


public class CryptoUtils 
{
	/**
	 * 
	 * @param plainText
	 * @param key
	 * @return {@code cipherText} and {@code IV}
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public static byte[][] encAES(byte[] plainText, byte[] key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException
	{
		if(key.length != 32)
			throw new RuntimeException("Invalid key size, only AES 256 is allowed");
		
		SecureRandom rand = new SecureRandom();
		
		Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
		byte[] iv = new byte[16];
		rand.nextBytes(iv);
		
		IvParameterSpec ivSpec = new IvParameterSpec(iv);
		SecretKey sec = new SecretKeySpec(key, "AES");
		
		c.init(Cipher.ENCRYPT_MODE, sec, ivSpec);
	
		return new byte[][]{c.doFinal(plainText), iv};
	}
	
	/**
	 * AES encryption
	 * @param plainText
	 * @param key {@code byte array} type
	 * @return cipher text and IV
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public static byte[][] encAES(byte[] plainText, SecretKey key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException
	{
		if(key.getEncoded().length != 32)
			throw new RuntimeException("Invalid key size, only AES 256 is allowed");
		
		SecureRandom rand = new SecureRandom();
		
		Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
		byte[] iv = new byte[16];
		rand.nextBytes(iv);
		
		IvParameterSpec ivSpec = new IvParameterSpec(iv);
		
		c.init(Cipher.ENCRYPT_MODE, key, ivSpec);
	
		return new byte[][]{c.doFinal(plainText), iv};
	}
	
	/**
	 * 
	 * @param cipherText
	 * @param key
	 * @param iv
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public static byte[] decAES(byte[] cipherText, byte[] key, byte[] iv) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException
	{
		if(key.length != 32)
			throw new RuntimeException("Invalid key size, only AES 256 is allowed");
		
		Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
		IvParameterSpec ivSpec = new IvParameterSpec(iv);
		SecretKey sec = new SecretKeySpec(key, "AES");
		
		c.init(Cipher.DECRYPT_MODE, sec, ivSpec);
		
		return c.doFinal(cipherText);
	}
	
	/**
	 * AES decryption
	 * @param cipherText
	 * @param key {@code SecretKey} object
	 * @param iv
	 * @return Decrypted plain text
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public static byte[] decAES(byte[] cipherText,SecretKey key, byte[] iv) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException
	{
		if(key.getEncoded().length != 32)
			throw new RuntimeException("Invalid key size, only AES 256 is allowed");
		
		Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
		IvParameterSpec ivSpec = new IvParameterSpec(iv);	
		
		c.init(Cipher.DECRYPT_MODE, key, ivSpec);
		
		return c.doFinal(cipherText);
	}
	
	/**
	 * 
	 * @param otherPublicKey
	 * @return public key, private key and shared secret (directly to be used for AES 256) for each session
	 * @throws NoSuchAlgorithmException 
	 */
	public static byte[][] generateSharedSecret(byte[] otherPublicKey) throws NoSuchAlgorithmException
	{
		Curve25519 cipher = Curve25519.getInstance(Curve25519.BEST);
		
		byte[][] keys = generateECkeys();
		byte[] publicKey = keys[0];
		byte[] privateKey = keys[1];
		byte[] sharedSecret = cipher.calculateAgreement(otherPublicKey, privateKey);
		
		MessageDigest md = MessageDigest.getInstance("SHA-512");
		byte[] digest = md.digest(sharedSecret);
		byte[] sharedKey = new byte[32];
		System.arraycopy(digest, 0, sharedKey, 0, 32);
		
		return new byte[][]{publicKey, privateKey, sharedKey};		
	}
	
	/**
	 * 
	 * @param selfPrivateKey
	 * @param otherPublicKey
	 * @return shared secret (directly to be used in AES 256)
	 * @throws NoSuchAlgorithmException 
	 */
	public static byte[] generateSharedSecret(byte[] selfPrivateKey, byte[] otherPublicKey) throws NoSuchAlgorithmException
	{
		Curve25519 cipher = Curve25519.getInstance(Curve25519.BEST);
		byte[] sharedSecret = cipher.calculateAgreement(otherPublicKey, selfPrivateKey);
		
		MessageDigest md = MessageDigest.getInstance("SHA-512");
		byte[] digest = md.digest(sharedSecret);
		byte[] sharedKey = new byte[32];
		System.arraycopy(digest, 0, sharedKey, 0, 32);
		
		return sharedKey;
	}
	
	public static String generateSignature(String data, byte[] privateKey)
	{
		Curve25519 cipher = Curve25519.getInstance(Curve25519.BEST);
		return Base64.encodeBase64URLSafeString(cipher.calculateSignature(privateKey, data.getBytes()));
	}
	
	public static boolean verifySignature(String data, String signature, byte[] publicKey)
	{
		Curve25519 cipher = Curve25519.getInstance(Curve25519.BEST);
		return cipher.verifySignature(publicKey, data.getBytes(), Base64.decodeBase64(signature));
	}
	
	/**
	 *  @return new public key and private key
	 */
	public static byte[][] generateECkeys()
	{
		Curve25519 cipher = Curve25519.getInstance(Curve25519.BEST);
		Curve25519KeyPair kp = cipher.generateKeyPair();
		
		return new byte[][]{kp.getPublicKey(), kp.getPrivateKey()};
	}
	
	public static SecretKey makeSecretKey(byte[] key)
	{
		if(key.length != 32)
			throw new  IllegalArgumentException("Invalid key size; only AES 256 is applicable");
		
		return new SecretKeySpec(key, "AES");	
	}
}
;