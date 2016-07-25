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

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import org.apache.tomcat.util.codec.binary.Base64;

import com.crypto.EvenGoldreichLempel;
import com.xrci.pamm.Util.CryptoUtils;

public class MakeKey 
{
	public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException 
	{
		BigInteger[] out = EvenGoldreichLempel.generateServerKey(512, 512);
		BigInteger N = out[0];
		BigInteger D = out[1];
		BigInteger E = out[2];
		
		byte[][] keys = CryptoUtils.generateECkeys();
		byte[] publicKey = keys[0];
		byte[] privateKey = keys[1];
		
		FileWriter fwN = new FileWriter("C:\\KeyBase\\N.key");
		FileWriter fwD = new FileWriter("C:\\KeyBase\\D.key");
		FileWriter fwE = new FileWriter("C:\\KeyBase\\E.key");
		FileWriter fwp = new FileWriter("C:\\KeyBase\\EC_PK.key");
		FileWriter fws = new FileWriter("C:\\KeyBase\\EC_SK.key");
		
		fwN.append(N.toString());
		fwD.append(D.toString());
		fwE.append(E.toString());
		fwp.append(Base64.encodeBase64URLSafeString(publicKey));
		fws.append(Base64.encodeBase64URLSafeString(privateKey));
		
		fwN.close();
		fwD.close();
		fwE.close();
		fwp.close();
		fws.close();
	}
}
