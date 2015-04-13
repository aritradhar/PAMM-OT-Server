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


package com.xrci.pamm.Server;

import java.math.BigInteger;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import com.xrci.pamm.Util.*;;

public class UserList 
{
	// keep address (IP |$| PORT) and the state
	private static ConcurrentHashMap<String, Integer> userStateMap = new ConcurrentHashMap<>();
	
	// keep address (IP |$| PORT) and randomly generated X
	private static ConcurrentHashMap<String, BigInteger[]> userXval = new ConcurrentHashMap<String, BigInteger[]>();
	
	//keep address (IP |$| PORT) and user query
	private static ConcurrentHashMap<String, BigInteger> userQueryMap = new ConcurrentHashMap<String, BigInteger>();
	
	//keep address (IP |$| PORT) and shared secret with the user
	private static ConcurrentHashMap<String, SecretKey> userSharedSecretMap = new ConcurrentHashMap<String, SecretKey>();
	
	//keep address (IP |$| PORT) and the user public key
	private static ConcurrentHashMap<String, byte[]> userPublicKeyMap = new ConcurrentHashMap<String, byte[]>();
		
	
	/*
	 * address at the argument is the random token to maintain state
	 */
	
	public static void setPublicKey(String ip, int port, byte[] publicKey)
	{
		String address = Utils.makeAddress(ip, port);
		
		if(!userPublicKeyMap.containsKey(address))
		{
			userPublicKeyMap.put(address, publicKey);
		}
		else
		{
			userPublicKeyMap.put(address, publicKey);
		}
	}
	
	public static void setPublicKey(String address, byte[] publicKey)
	{		
		if(!userPublicKeyMap.containsKey(address))
		{
			userPublicKeyMap.put(address, publicKey);
		}
		else
		{
			userPublicKeyMap.put(address, publicKey);
		}
	}
	
	public static void setSharedSecret(String ip, int port, byte[] sharedSecret)
	{
		String address = Utils.makeAddress(ip, port);
		SecretKey sec = new SecretKeySpec(sharedSecret, "AES");
		
		if(!userSharedSecretMap.containsKey(address))
		{
			userSharedSecretMap.put(address, sec);
		}
		else
		{
			userSharedSecretMap.put(address, sec);
		}
	}
	
	public static void setSharedSecret(String address, byte[] sharedSecret)
	{
		SecretKey sec = new SecretKeySpec(sharedSecret, "AES");
		
		if(!userSharedSecretMap.containsKey(address))
		{
			userSharedSecretMap.put(address, sec);
		}
		else
		{
			userSharedSecretMap.put(address, sec);
		}
	}
	
	public static SecretKey getSharedSecret(String ip, int port)
	{
		String address = Utils.makeAddress(ip, port);
		
		return userSharedSecretMap.get(address);
	}
	
	public static SecretKey getSharedSecret(String address)
	{
		return userSharedSecretMap.get(address);
	}
	
	
	public static void setState(String ip, int port, int state)
	{
		String address = Utils.makeAddress(ip, port);
		
		if(!userStateMap.containsKey(address))
		{
			userStateMap.put(address, state);
		}
		
		else
		{
			userStateMap.put(address, state);
		}
	}
	
	public static void setState(String address, int state)
	{
		
		if(!userStateMap.containsKey(address))
		{
			userStateMap.put(address, state);
		}
		
		else
		{
			userStateMap.put(address, state);
		}
	}
	
	public static int getState(String ip, int port)
	{
		String address = Utils.makeAddress(ip, port);
		
		return userStateMap.containsKey(address) ? userStateMap.get(address) : -1;
	}
	
	public static int getState(String address)
	{		
		return userStateMap.containsKey(address) ? userStateMap.get(address) : -1;
	}
	
	public static boolean endSession(String ip, int port)
	{
		String address = Utils.makeAddress(ip, port);
		
		if(!userStateMap.containsKey(address))
			return false;
		
		userStateMap.remove(address);
		userXval.remove(address);
		userQueryMap.remove(address);
		userSharedSecretMap.remove(address);
		
		return true;
	}
	
	public static boolean endSession(String address)
	{
		if(!userStateMap.containsKey(address))
			return false;
		
		userStateMap.remove(address);
		userXval.remove(address);
		userQueryMap.remove(address);
		userSharedSecretMap.remove(address);
		
		return true;
	}
	
	public static boolean putX(String ip, int port, BigInteger[] X)
	{
		String address = Utils.makeAddress(ip, port);
		
		if(!userXval.containsKey(address) && !userStateMap.containsKey(address))
			return false;
		
		userStateMap.put(address, 2);
		userXval.put(address, X);
		return true;
	}
	
	public static boolean putX(String address, BigInteger[] X)
	{		
		if(!userXval.containsKey(address) && !userStateMap.containsKey(address))
			return false;
		
		userStateMap.put(address, 2);
		userXval.put(address, X);
		return true;
	}
	
	public static BigInteger[] getX(String ip, int port)
	{
		String address = Utils.makeAddress(ip, port);
		
		if(!userXval.containsKey(address) && userStateMap.get(address) != 2)
			return null;
		
		return userXval.get(address);
	}
	
	public static BigInteger[] getX(String address)
	{
		if(!userXval.containsKey(address) && userStateMap.get(address) != 2)
			return null;
		
		return userXval.get(address);
	}
	
	public static boolean putQuery(String ip, int port, BigInteger query)
	{
		String address = Utils.makeAddress(ip, port);
		
		if(!userQueryMap.containsKey(address))
			return false;
		
		userStateMap.put(address, 3);
		userQueryMap.put(address, query);
		return false;
	}
	
	public static boolean putQuery(String address, BigInteger query)
	{		
		if(!userQueryMap.containsKey(address))
			return false;
		
		userStateMap.put(address, 3);
		userQueryMap.put(address, query);
		return false;
	}
	
	public static BigInteger getQuery(String ip, int port)
	{
		String address = Utils.makeAddress(ip, port);
		
		if(!userQueryMap.containsKey(address))
			return null;
		
		return userQueryMap.get(address);
	}
	
	public static BigInteger getQuery(String address)
	{
		if(!userQueryMap.containsKey(address))
			return null;
		
		return userQueryMap.get(address);
	}
}
