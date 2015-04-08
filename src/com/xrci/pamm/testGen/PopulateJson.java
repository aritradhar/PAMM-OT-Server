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

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONObject;

import com.xeci.pamm.Util.ENV;

public class PopulateJson 
{
	public static void populate() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, IOException 
	{
		Class.forName(ENV.driver).newInstance();
		Connection conn = DriverManager.getConnection(ENV.url+ENV.dbName,ENV.userName,ENV.password);


		String sql = "SELECT string from adv";

		PreparedStatement ps = conn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery(sql);

		JSONObject MAIN = new JSONObject();
		JSONArray jArray = new JSONArray();
		
		int i = 0;
		while (rs.next()) 
		{
			String string = rs.getString("string");
			
			JSONObject jObject = new JSONObject();
			
			jObject.put("index", i);
			jObject.put("string", string);
			
			jArray.put(jObject);
			
			i++;
		}
		
		MAIN.put("StringDB", jArray);
		
		FileWriter fw = new FileWriter(ENV.STRING_DB);
		fw.append(MAIN.toString(2));
		fw.close();
		
		
		ps.close();
		conn.close();
		
		System.out.println("done..");
	}
}
