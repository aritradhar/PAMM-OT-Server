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

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

import org.apache.commons.codec.binary.Base64;

import com.xrci.pamm.Util.ENV;

public class MakeDB 
{
	public static void updateUserDB(String str) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		Class.forName(ENV.driver).newInstance();
		Connection conn = DriverManager.getConnection(ENV.url+ENV.dbName,ENV.userName,ENV.password);

		String sql = new String();

		sql = "INSERT INTO adv (string)" +
				"VALUES (?)";

		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, str);

		ps.execute();
		ps.close();
		conn.close();
	}
	
	//test
	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, IOException 
	{
		Class.forName(ENV.driver).newInstance();
		Connection conn = DriverManager.getConnection(ENV.url+ENV.dbName,ENV.userName,ENV.password);
		
		String sql = new String();

		sql = "TRUNCATE TABLE adv";

		PreparedStatement ps = conn.prepareStatement(sql);
		ps.execute();
		ps.close();
		conn.close();
		
		
		Random rand = new Random();
		byte[] b = new byte[256];
		String s = null;
		
		for(int i = 0; i < 10000; i++)
		{
			rand.nextBytes(b);
			s = Base64.encodeBase64URLSafeString(b);
			updateUserDB(s);
		}
		
		System.out.println("done...");
		
		PopulateJson.populate();
		
		System.out.println("Pupulate JSON done...");
	}
}
