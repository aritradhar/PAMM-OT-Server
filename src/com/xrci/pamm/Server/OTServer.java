package com.xrci.pamm.Server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.crypto.EvenGoldreichLempel;
import com.util.Utils;
import com.xeci.pamm.Util.ENV;


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
	public static String signKey, verifyKey;

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
		base64Keys = new String[jArray.length()];
		bigIntegerKeys = new BigInteger[jArray.length()];

		this.n_msg = jArray.length();
		
		for(int i = 0; i < jArray.length(); i++)
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
				
				signKey = brSignKey.readLine();
				verifyKey = brVerifyKey.readLine();

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

		//destroy all styates
		if(flag.equalsIgnoreCase("endSession"))
		{
			boolean end = UserList.endSession(ip, port);
			if(!end)
			{
				String response = "invalid_rerquest";
				res.getOutputStream().write(response.getBytes());
				res.getOutputStream().flush();
				res.getOutputStream().close();
				return;
			}
			else
			{
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
			if(UserList.getState(ip, port) > 0)
			{
				String response = "wrong_state";
				res.getOutputStream().write(response.getBytes());
				res.getOutputStream().flush();
				res.getOutputStream().close();
				return;
			}

			UserList.setState(request.getRemoteAddr(), request.getRemotePort(), 1);
			String response = N.toString().concat("\n").concat(E.toString()).concat("\n").concat(verifyKey);
			res.getOutputStream().write(response.getBytes());
			res.getOutputStream().flush();
			res.getOutputStream().close();
		}

		
		else if(flag.equalsIgnoreCase("otKeys"))
		{
			BigInteger[] X =  EvenGoldreichLempel.generateRandomMsg(n_msg, 4);
			boolean status = UserList.putX(ip, port, X);

			if(!status)
			{
				String response = "invalid_request";
				res.getOutputStream().write(response.getBytes());
				res.getOutputStream().flush();
				res.getOutputStream().close();
			}

			JSONObject X_Json = Utils.putInJSON(X);

			res.getOutputStream().write(X_Json.toString(2).getBytes());
			res.getOutputStream().flush();
			res.getOutputStream().close();
		}

		else if(flag.equalsIgnoreCase("otQuery"))
		{
			BigInteger query = new BigInteger(request.getParameter("query"));

			//for(int i = 0; i < base64Keys.length; i++)
			//{
			BigInteger V = query;
			System.out.println("V " + V);
			UserList.putQuery(ip, port, query);
			BigInteger[] X = UserList.getX(ip, port);

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
			
			FileWriter wr = new FileWriter("C:\\logs\\" + Utils.makeAddress(ip, port) + "_log.txt", true);
			wr.append(enc_Json.toString(2));
			wr.close();
			
			//Utils.LZMA_ZIP("C:\\logs\\" + Utils.makeAddress(ip, port) + "_log.txt", "C:\\logs\\" + Utils.makeAddress(ip, port) + "_lzma.xz");

			res.getOutputStream().write(enc_Json.toString(2).getBytes());
			res.getOutputStream().flush();
			res.getOutputStream().close();
			
			System.out.println("here");
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

