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

import com.util.Utils;

public class UserList 
{
	// keep address (IP |$| PORT) and the state
	public static ConcurrentHashMap<String, Integer> userStateMap = new ConcurrentHashMap<>();
	
	// keep address (IP |$| PORT) and randomly generated X
	public static ConcurrentHashMap<String, BigInteger[]> userXval = new ConcurrentHashMap<String, BigInteger[]>();
	
	//keep address (IP |$| PORT) and user query
	public static ConcurrentHashMap<String, BigInteger> userQueryMap = new ConcurrentHashMap<String, BigInteger>();
	
	
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
	
	public static int getState(String ip, int port)
	{
		String address = Utils.makeAddress(ip, port);
		
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
	
	public static BigInteger[] getX(String ip, int port)
	{
		String address = Utils.makeAddress(ip, port);
		
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
	
	public static BigInteger getQuery(String ip, int port)
	{
		String address = Utils.makeAddress(ip, port);
		
		if(!userQueryMap.containsKey(address))
			return null;
		
		return userQueryMap.get(address);
	}
}
