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
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

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
			//"http://localhost:8080/pammClient/MainServlet";
			"http://13.218.151.91:9080/PAMM_OT_Server/MainServlet";
	
	public BigInteger N, E;
	public BigInteger[] X;
	public BigInteger K,V, Dec;
	
	public BigInteger[] Enc;
	public int choice;

	public String random_token;
	public byte[] serverPublicKey;
	
	private byte[] publicKey, privateKey, sharedKey;
	private SecretKey sharedSecretKey;

	public ClientEngine(int choice) throws NoSuchAlgorithmException
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
		
		this.setKeys();
		
	}

	private void setKeys() throws NoSuchAlgorithmException
	{
		byte[][] keys = CryptoUtils.generateECkeys();
		this.publicKey = keys[0];
		this.privateKey = keys[1];
	}
	
	private void setSesson(byte[] serverPublickey) throws NoSuchAlgorithmException
	{
		this.sharedKey = CryptoUtils.generateSharedSecret(this.privateKey, serverPublickey);
		this.sharedSecretKey = CryptoUtils.makeSecretKey(this.sharedKey);
	}

	public static int queryRowNumber() throws IOException
	{
		String url = SERVER_ADDRESS;
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

		String urlParameters = "flag=rowNum";
		
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();
		
		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + urlParameters);
		System.out.println("Response Code : " + responseCode);
		
		byte[] received = IOUtils.toByteArray(con.getInputStream());
		ByteBuffer bf = ByteBuffer.wrap(received);
		
		/*BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		
		while ((inputLine = in.readLine()) != null) 
		{
			rowNum = Integer.parseInt(inputLine);
		}*/
		
		return bf.getInt();
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
	
	/**
	 * Initialize a new connection
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 * @throws InvalidKeySpecException
	 */
	public BigInteger[] sendHandShake() throws IOException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException
	{
		String url = SERVER_ADDRESS;
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

		String sentKey = Base64.encodeBase64URLSafeString(this.publicKey);
		String sig = CryptoUtils.generateSignature(sentKey, privateKey);
		
		String urlParameters = (ENV.USE_SESSION_TOKEN) ? "flag=handshake&token="+ random_token :"flag=handshake";
		urlParameters += "&publicKey=" + sentKey + "&signature=" + sig;
		
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

		int counter = 0;
		BigInteger N = BigInteger.ZERO, E = BigInteger.ZERO;		
		
		while ((inputLine = in.readLine()) != null) 
		{
			++counter;

			if(counter == 1)
			{
				if(inputLine.equalsIgnoreCase("SignatureMismatch"))
				{
					System.err.println("Signature mismatch in server");
					System.exit(1);
				}
				N = new BigInteger(inputLine);
			}
			if(counter == 2)
				E = new BigInteger(inputLine);
			
			if(counter == 3)
			{
				this.serverPublicKey = Base64.decodeBase64(inputLine);	
				this.setSesson(serverPublicKey);
			}
		}
		System.out.println("N : " + N);
		System.out.println("E : " + E);
		System.out.println("Server Public key : " + Base64.encodeBase64URLSafeString(this.serverPublicKey));
		System.out.println("Shared secret key : " + Base64.encodeBase64URLSafeString(this.sharedKey));
		
		in.close();

		return new BigInteger[]{N, E};
	}

	//get enc data
	/**
	 * 3rd and final step. get enc data
	 * @return
	 * @throws IOException
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws InvalidAlgorithmParameterException 
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 */
	public BigInteger[] sendOTQuery() throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException
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
		
		//AES decryption then unzip
		byte[] bytes =  IOUtils.toByteArray(con.getInputStream());
		byte[] iv = new byte[16];
		System.arraycopy(bytes, 0, iv, 0, 16);
		byte[] cipherText = Arrays.copyOfRange(bytes, 16, bytes.length);
		byte[] pt = CryptoUtils.decAES(cipherText, this.sharedSecretKey, iv);
		
		
		if(!ENV.TRAFFIC_COMPRESSION)
			in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(pt)));
		
		else
		{
			byte[] data = Utils.LZMA_UNZIP(pt);
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

	/**
	 * 2nd step
	 * @return
	 * @throws IOException
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws InvalidAlgorithmParameterException 
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 */
	public BigInteger[] sendOTKeys() throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException 
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

		
		byte[] bytes =  IOUtils.toByteArray(con.getInputStream());
		byte[] iv = new byte[16];
		System.arraycopy(bytes, 0, iv, 0, 16);
		byte[] cipherText = Arrays.copyOfRange(bytes, 16, bytes.length);
		byte[] pt = CryptoUtils.decAES(cipherText, this.sharedSecretKey, iv);
		
		BufferedReader in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(pt)));
		
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
	
	
	/*
	 * Test main
	 */
	public static void main(String[] args) throws Exception 
	{
		long start = System.currentTimeMillis();
		
		int n = ClientEngine.queryRowNumber();
		System.out.println("Total database row : " + n);
		int choice = new Random().nextInt(n);
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
}
