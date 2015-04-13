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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONObject;

import com.crypto.EvenGoldreichLempel;
import com.xrci.pamm.Util.*;


/**
 * Servlet implementation class MainServlet
 */
@WebServlet("/MainServlet")
public class OTServer extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
	public static BigInteger N,D,E;	
	int n_msg;
	public static String keyJson;
	//string received
	public static String[] base64Keys;
	//strings converted to biginteger number
	public static BigInteger[] bigIntegerKeys;
	public static byte[] privateKey, publicKey;
	public static int rowNum;
	
	public static boolean loaded = false;

	public OTServer() throws IOException 
	{
		super();

		BufferedReader br = new BufferedReader(new FileReader(ENV.STRING_DB));
		String s = "";
		StringBuffer sb = new StringBuffer("");

		while((s = br.readLine()) != null)
		{
			sb = sb.append(s).append("\n");
		}

		keyJson = sb.toString();
		br.close();
		
		// parse json from the key file

		JSONObject jObject = new JSONObject(keyJson);
		JSONArray jArray = jObject.getJSONArray("StringDB");
		this.n_msg = jArray.length();
		base64Keys = new String[this.n_msg];
		bigIntegerKeys = new BigInteger[this.n_msg];
			
		for(int i = 0; i < n_msg; i++)
		{
			JSONObject ob = (JSONObject) jArray.get(i);
			//System.out.println(ob.get("imgFile").toString());

			base64Keys[i] = (String) ob.get("string");
			bigIntegerKeys[i] = Utils.stringToBigInteger(base64Keys[i]);

		}

		//System.out.println(bigIntegerKeys[10]);
		System.out.println("Started");
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse res) throws ServletException, IOException 
	{
		// TODO Auto-generated method stub
		String response = "GET request not supported";
		res.getOutputStream().write(response.getBytes());
		res.getOutputStream().flush();
		res.getOutputStream().close();
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse res) throws ServletException, IOException 
	{
		res.setContentType("text/plain");

		String flag = request.getParameter("flag");

		String ip = request.getRemoteAddr();
		int port = request.getRemotePort();
		System.out.println("Request from");
		System.out.println("IP : " + ip);
		System.out.println("PORT : " + port);
		System.out.println("FLAG : " + flag);
		
		String random_token = ENV.USE_SESSION_TOKEN ? request.getParameter("token") : null;
		
		if(ENV.USE_SESSION_TOKEN)
			System.out.println("TOKEN : " + random_token);
		
		try 
		{
			System.out.println("Loaded key file " + loaded);

			if(!loaded)
			{
				BufferedReader brN = new BufferedReader(new FileReader(ENV.KEYBASE_N));
				BufferedReader brD = new BufferedReader(new FileReader(ENV.KEYBASE_D));
				BufferedReader brE = new BufferedReader(new FileReader(ENV.KEYBASE_E));

				BufferedReader brSignKey = new BufferedReader(new FileReader(ENV.KEYBASE_EC_SK));
				BufferedReader brVerifyKey = new BufferedReader(new FileReader(ENV.KEYBASE_EC_PK));
				
				N = new BigInteger(brN.readLine());
				D = new BigInteger(brD.readLine());
				E = new BigInteger(brE.readLine());
				
				privateKey = Base64.decodeBase64(brSignKey.readLine());
				publicKey = Base64.decodeBase64(brVerifyKey.readLine());

				brN.close();
				brD.close();
				brE.close();
				brSignKey.close();
				brVerifyKey.close();

				loaded = true;
			}
		} 

		catch (Exception e) 
		{
			System.out.println("Exception in key genration");

		}

		//System.out.println("N " + N);
		//System.out.println("D " + D);
		//System.out.println("E " + E);

		if(flag.equalsIgnoreCase("rowNum"))
		{
			res.getOutputStream().write(this.n_msg);
			res.getOutputStream().flush();
			res.getOutputStream().close();
			return;
		}
		
		//destroy all states
		if(flag.equalsIgnoreCase("endSession"))
		{
			boolean end = false;
			
			end = ENV.USE_SESSION_TOKEN ? UserList.endSession(random_token) : UserList.endSession(ip, port);
			
			if(!end)
			{
				System.err.println("Invalid request for client " + random_token);
				String response = "invalid_rerquest";
				res.getOutputStream().write(response.getBytes());
				res.getOutputStream().flush();
				res.getOutputStream().close();
				return;
			}
			else
			{
				System.err.println("Session end request for client " + random_token);
				String response = "session_end";
				res.getOutputStream().write(response.getBytes());
				res.getOutputStream().flush();
				res.getOutputStream().close();
				return;

			}
		}

		//handshake -> state 1
		//send RSA n and e and Elliptic curve public key for signature
		else if(flag.equalsIgnoreCase("handshake"))
		{
			if(!ENV.USE_SESSION_TOKEN)
			{
				if(UserList.getState(ip, port) > 0)
				{
					System.err.println("Wrong state. Client rejected " + random_token);
					String response = "wrong_state";
					res.getOutputStream().write(response.getBytes());
					res.getOutputStream().flush();
					res.getOutputStream().close();
					return;
				}
			}
			else
			{
				if(UserList.getState(random_token) >0 )
				{
					System.err.println("Wrong state. Client rejected " + random_token);
					String response = "wrong_state";
					res.getOutputStream().write(response.getBytes());
					res.getOutputStream().flush();
					res.getOutputStream().close();
					return;
				}
			}
			String otherPublicKey = request.getParameter("publicKey");
			String signature = request.getParameter("signature");
			
			if(!CryptoUtils.verifySignature(otherPublicKey, signature, Base64.decodeBase64(otherPublicKey)))
			{
				System.err.println("Signature mismatch. Client rejected " + random_token);
				res.getOutputStream().write("SignatureMismatch".getBytes());
				res.getOutputStream().flush();
				res.getOutputStream().close();
				return;
			}
			else
				System.err.println("Signature verified " + signature);
			
			if(!ENV.USE_SESSION_TOKEN)
			{
				UserList.setState(request.getRemoteAddr(), request.getRemotePort(), 1);
				UserList.setPublicKey(ip, port, Base64.decodeBase64(otherPublicKey));
				try 
				{
					byte[] sharedSecret = CryptoUtils.generateSharedSecret(privateKey, Base64.decodeBase64(otherPublicKey));
					UserList.setSharedSecret(ip, port, sharedSecret);
				} 
				
				catch (NoSuchAlgorithmException e) 
				{
					e.printStackTrace();
				}
			}
			else
			{
				UserList.setState(random_token, 1);
				
				UserList.setPublicKey(random_token, Base64.decodeBase64(otherPublicKey));
				try 
				{
					byte[] sharedSecret = CryptoUtils.generateSharedSecret(privateKey, Base64.decodeBase64(otherPublicKey));
					UserList.setSharedSecret(random_token, sharedSecret);
					
					System.out.println("Shared secret "+Base64.encodeBase64URLSafeString(UserList.getSharedSecret(random_token).getEncoded()));
				} 
				
				catch (NoSuchAlgorithmException e) 
				{
					e.printStackTrace();
				}
			}		
			
			String response = N.toString().concat("\n").concat(E.toString()).concat("\n").concat(Base64.encodeBase64URLSafeString(publicKey));
			res.getOutputStream().write(response.getBytes());
			res.getOutputStream().flush();
			res.getOutputStream().close();
		}

		
		else if(flag.equalsIgnoreCase("otKeys"))
		{
			BigInteger[] X =  EvenGoldreichLempel.generateRandomMsg(n_msg, 4);
			boolean status = false;
			
			status = ENV.USE_SESSION_TOKEN ? UserList.putX(random_token, X) : UserList.putX(ip, port, X);

			if(!status)
			{
				String response = "invalid_request";
				res.getOutputStream().write(response.getBytes());
				res.getOutputStream().flush();
				res.getOutputStream().close();
			}

			JSONObject X_Json = Utils.putInJSON(X);
			byte[] json_bytes = X_Json.toString(2).getBytes();
			SecretKey sec = UserList.getSharedSecret(random_token);
			
			if(sec == null)
			{
				res.getOutputStream().write("Missing shared secret".getBytes());
				res.getOutputStream().flush();
				res.getOutputStream().close();
				return;
			}
			
			try 
			{
				byte[][] enc = CryptoUtils.encAES(json_bytes, sec);
				byte[] iv = enc[1];
				byte[] cipherText = enc[0];
			
				//System.out.println(iv.length + cipherText.length);
				//iv | cipher text
				byte[] out = new byte[iv.length + cipherText.length];
				System.arraycopy(iv, 0, out, 0, 16);
				System.arraycopy(cipherText, 0, out, 16, cipherText.length);
				
				res.getOutputStream().write(out);
				res.getOutputStream().flush();
				res.getOutputStream().close();
				
			} 
			catch (InvalidKeyException | NoSuchAlgorithmException
					| NoSuchPaddingException
					| InvalidAlgorithmParameterException
					| IllegalBlockSizeException | BadPaddingException e) 
			
			{
				e.printStackTrace();
				
				res.getOutputStream().write("Problem with shared key".getBytes());
				res.getOutputStream().flush();
				res.getOutputStream().close();
			}
			
//			res.getOutputStream().write(X_Json.toString(2).getBytes());
//			res.getOutputStream().flush();
//			res.getOutputStream().close();
		}

		else if(flag.equalsIgnoreCase("otQuery"))
		{
			BigInteger query = new BigInteger(request.getParameter("query"));

			//for(int i = 0; i < base64Keys.length; i++)
			//{
			BigInteger V = query;
			//System.out.println("V " + V);
			
			BigInteger[] X = null;
			
			if(!ENV.USE_SESSION_TOKEN)
			{
				UserList.putQuery(ip, port, query);
				X = UserList.getX(ip, port);
			}
			else
			{
				UserList.putQuery(random_token, query);
				X = UserList.getX(random_token);
			}
			
			//BigInteger[] X = UserList.getX(ip, port);

			if(X == null)
			{
				String response = "invalid_state";
				res.getOutputStream().write(response.getBytes());
				res.getOutputStream().flush();
				res.getOutputStream().close();
			}
			//System.out.println("X " + X[0]);

			BigInteger[] enc = EvenGoldreichLempel.sendObliviousEnc(V, n_msg, X, D, N, bigIntegerKeys);

			JSONObject enc_Json = Utils.putInJSONEnc(enc);
			
			//logging
			FileWriter wr = new FileWriter("C:\\logs\\" + Utils.makeAddress(ip, port) + "_log.txt", true);
			wr.append(enc_Json.toString(2));
			wr.close();
			
			//Utils.LZMA_ZIP("C:\\logs\\" + Utils.makeAddress(ip, port) + "_log.txt", "C:\\logs\\" + Utils.makeAddress(ip, port) + "_lzma.xz");
			//enc_Json can be compressed
			
			//res.getOutputStream().write(enc_Json.toString(2).getBytes());
			//long st = System.currentTimeMillis();
			
			byte[] bytes = null;

			if(!ENV.TRAFFIC_COMPRESSION)
				bytes = enc_Json.toString(2).getBytes();		
			else
				bytes =Utils.LZMA_ZIP(enc_Json.toString(2).getBytes());
			
			/*
			 * long en = System.currentTimeMillis();
			 * System.out.println("Encode time : " + (en - st));
			 * */
			
			SecretKey sec = UserList.getSharedSecret(random_token);
			
			if(sec == null)
			{
				res.getOutputStream().write("Missing shared secret".getBytes());
				res.getOutputStream().flush();
				res.getOutputStream().close();
				return;
			}
			
			try 
			{
				byte[][] aes_enc = CryptoUtils.encAES(bytes, sec);
				byte[] iv = aes_enc[1];
				byte[] cipherText = aes_enc[0];
			
				//System.out.println(iv.length + cipherText.length);
				//iv | cipher text
				byte[] out = new byte[iv.length + cipherText.length];
				System.arraycopy(iv, 0, out, 0, 16);
				System.arraycopy(cipherText, 0, out, 16, cipherText.length);
				
				res.getOutputStream().write(out);
				res.getOutputStream().flush();
				res.getOutputStream().close();
				
			} 
			catch (InvalidKeyException | NoSuchAlgorithmException
					| NoSuchPaddingException
					| InvalidAlgorithmParameterException
					| IllegalBlockSizeException | BadPaddingException e) 
			
			{
				e.printStackTrace();
				
				res.getOutputStream().write("Problem with shared key".getBytes());
				res.getOutputStream().flush();
				res.getOutputStream().close();
				
				return;
			}
			
			/*
			res.getOutputStream().write(bytes);
			res.getOutputStream().flush();
			res.getOutputStream().close();
			*/
			//System.out.println("here");
			//}

		}

		else if(flag.equalsIgnoreCase("getkeys"))
		{
			res.getOutputStream().write(keyJson.getBytes());
			res.getOutputStream().flush();
			res.getOutputStream().close();
		}

		else
		{
			System.out.println("Wrong flag type : " + flag);
			
			String response = "wrong_flag_type";
			res.getOutputStream().write(response.getBytes());
			res.getOutputStream().flush();
			res.getOutputStream().close();

		}

	}
}

