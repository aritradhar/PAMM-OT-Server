package com.xrci.pamm.Util;

public class ENV 
{
	public static final String url = "jdbc:mysql://localhost:3306/";
	public static final String dbName = "pamm?rewriteBatchedStatements=true";
	public static final String driver = "com.mysql.jdbc.Driver";
	public static final String userName = "root"; 
	public static final String password = "root";
	
	public static final String KEYBASE_N = "C:\\KeyBase\\N.key";
	public static final String KEYBASE_D = "C:\\KeyBase\\D.key";
	public static final String KEYBASE_E = "C:\\KeyBase\\E.key";
	
	public static final String KEYBASE_EC_PK = "C:\\KeyBase\\EC_PK.key";
	public static final String KEYBASE_EC_SK = "C:\\KeyBase\\EC_SK.key";
	
	public static final String STRING_DB = "C:\\String\\String.json";
	public static final String STRING_DB_counter = "C:\\String\\Counter.txt";
	
	public static final boolean TRAFFIC_COMPRESSION = false;
	public static final int LZMA_TRAFFIC_COMPRESSION_LEVEL = 1;
	
	public static final boolean USE_SESSION_TOKEN = true;
	
	public static int counter_total_request;
}
