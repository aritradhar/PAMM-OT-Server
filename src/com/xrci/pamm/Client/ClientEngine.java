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


package com.xrci.pamm.Client;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Random;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.crypto.EvenGoldreichLempel;
import com.xrci.pamm.Util.*;

public class ClientEngine 
{
	/*
	 * client run from cmd
	 * 
		java -cp "C:\lib\Apache Commons\commons-codec-1.10.jar";"C:\Users\w4j3yyfd\git\JSON-java\bin";"C:\Users\w4j3yyfd\git\crypt\bin";C:\Users\w4j3yyfd\workspace\LZMA\bin;. com.xrci.pamm.Client.ClientEngine
	 
	 */
	
	private static String USER_AGENT = "Mozilla/5.0";
	//private static String SERVER_ADDRESS = "http://localhost:9080/AdResponse/MainServlet";
	private static String SERVER_ADDRESS = //"http://localhost:8080/PAMM_OT_Server/MainServlet";
			"http://localhost:8080/pammClient/MainServlet";
	public BigInteger N, E;
	public BigInteger[] X;
	public BigInteger K,V, Dec;
	
	public BigInteger[] Enc;
	public int choice;

	public String random_token;
	public PublicKey verifyKey;


	public ClientEngine(int choice)
	{
		this.choice = choice;
		
		if(ENV.USE_SESSION_TOKEN)
		{
			byte[] tokenBytes = new byte[16];
			SecureRandom rand = new SecureRandom();
			rand.nextBytes(tokenBytes);
			this.random_token = Base64.encodeBase64URLSafeString(tokenBytes);
		}
		else
			this.random_token = null;
	}

	/*
	 * Test main
	 */
	public static void main(String[] args) throws Exception 
	{
		long start = System.currentTimeMillis();
		
		int choice = new Random().nextInt(100);
		System.out.println("choice" + choice);
		ClientEngine CE = new ClientEngine(choice);
		BigInteger[] out = CE.sendHandShake();

		CE.N = out[0];
		CE.E = out[1];

		CE.X = CE.sendOTKeys();

		BigInteger[] KV =  EvenGoldreichLempel.generateQuery(CE.choice, CE.X, CE.N, CE.E);
		CE.K = KV[0];
		CE.V = KV[1];

		CE.Enc = CE.sendOTQuery();

		CE.Decrypt();
		System.out.println(Utils.bigIntegerToString(CE.Dec));
		
		CE.sessionEnd();
		
		long end = System.currentTimeMillis();
		
		System.out.println("Total time : " + (end - start) + " ms");
		

	}


	public void sessionEnd() throws IOException
	{
		String url = SERVER_ADDRESS;
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

		String urlParameters = (ENV.USE_SESSION_TOKEN) ? "flag=endSession&token="+ random_token : "flag=endSession";
		
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();
		
		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + urlParameters);
		System.out.println("Response Code : " + responseCode);
	}
	
	public BigInteger[] sendHandShake() throws IOException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException
	{
		String url = SERVER_ADDRESS;
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

		String urlParameters = (ENV.USE_SESSION_TOKEN) ? "flag=handshake&token="+ random_token :"flag=handshake";

		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();


		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + urlParameters);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(
				new InputStreamReader(con.getInputStream()));
		String inputLine;

		int counter = 0;
		BigInteger N = BigInteger.ZERO, E = BigInteger.ZERO;

		while ((inputLine = in.readLine()) != null) 
		{
			++counter;

			if(counter == 1)
				N = new BigInteger(inputLine);

			if(counter == 2)
				E = new BigInteger(inputLine);
			
			if(counter == 3)
			{
				byte[] pk_byte = Base64.decodeBase64(inputLine);
				KeyFactory kf = KeyFactory.getInstance("EC", "SunEC");
				this.verifyKey = kf.generatePublic(new X509EncodedKeySpec(pk_byte));			
			}
		}
		System.out.println("N : " + N);
		System.out.println("E : " + E);
		System.out.println("Verify key : " + this.verifyKey.toString());
		in.close();

		return new BigInteger[]{N, E};
	}

	//get enc data
	public BigInteger[] sendOTQuery() throws IOException
	{
		String url = SERVER_ADDRESS;
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

		String urlParameters = (ENV.USE_SESSION_TOKEN) ? "flag=otQuery&query=" + this.V + "&token=" + random_token : "flag=otQuery&query=" + this.V;

		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();


		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + urlParameters);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = null; 
		//BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		
		//long st = System.currentTimeMillis();
		
		if(!ENV.TRAFFIC_COMPRESSION)
		in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		
		else
		{
			byte[] data = Utils.LZMA_UNZIP( IOUtils.toByteArray(con.getInputStream()));
			in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(data)));
		}
		
		//long en = System.currentTimeMillis();
		//System.out.println("Decode time : " + (en - st));
		
		String inputLine;
		StringBuffer inputJSON = new StringBuffer("");

		while ((inputLine = in.readLine()) != null) 
		{
			//System.out.println(inputLine);
			inputJSON = inputJSON.append(inputLine);
		}

		JSONObject jObject = new JSONObject(inputJSON.toString());

		JSONArray jArray = (JSONArray) jObject.get("Enc");

		BigInteger[] Enc = new BigInteger[jArray.length()];

		JSONObject ob = null;
		Object obj1 = null;
		
		for(int i = 0; i< jArray.length(); i++)
		{
			ob = (JSONObject) jArray.get(i);
			obj1 = ob.get("Enc_i");
			Enc[i] = new BigInteger(obj1.toString());

			//System.out.println(Enc[i]);
		}

		return Enc;

	}

	public BigInteger[] sendOTKeys() throws IOException 
	{

		String url = SERVER_ADDRESS;
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

		String urlParameters = (ENV.USE_SESSION_TOKEN) ? "flag=otKeys&token="+ random_token : "flag=otKeys";

		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();

		int responseCode = con.getResponseCode();
		
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + urlParameters);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

		String inputLine;
		StringBuffer inputJSON = new StringBuffer("");

		while ((inputLine = in.readLine()) != null) 
		{
			//System.out.println(inputLine);
			inputJSON = inputJSON.append(inputLine);
		}

		JSONObject jObject = new JSONObject(inputJSON.toString());

		JSONArray jArray = (JSONArray) jObject.get("X");

		BigInteger[] X = new BigInteger[jArray.length()];

		JSONObject ob = null;
		Object obj1 = null;
		for(int i = 0; i< jArray.length(); i++)
		{
			ob = (JSONObject) jArray.get(i);
			obj1 = ob.get("X_i");
			X[i] = new BigInteger(obj1.toString());
			//System.out.println(X[i]);
		}

		//System.out.println("Here");
		return X;
	}

	public void Decrypt()
	{
		//System.out.println(choice);
		//System.out.println(Enc.length);
		//System.out.println(K);
		this.Dec = EvenGoldreichLempel.decrypt(choice, Enc, K);
	}
}
