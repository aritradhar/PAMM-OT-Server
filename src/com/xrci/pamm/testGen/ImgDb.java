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


package com.xrci.pamm.testGen;

import java.security.SecureRandom;
import java.util.HashMap;

import org.apache.commons.codec.binary.Base64;

public class ImgDb 
{
	public static HashMap<String, String> filePath = new HashMap<>();
	
	public static void main(String[] args) 
	{
		int hit = 0;
		for(int i = 0; i < 1000000; i++)
		{
			while(true)
			{
				SecureRandom rand = new SecureRandom();
				byte[] b = new byte[8];
				rand.nextBytes(b);
				String key = Base64.encodeBase64URLSafeString(b);
				if(filePath.containsKey(key))
				{
					hit++;
					continue;
				}
				else
				{
					filePath.put(key, "");
					break;
				}
			}
		}
		System.out.println(hit);
		System.out.println("done...");
	}
}
