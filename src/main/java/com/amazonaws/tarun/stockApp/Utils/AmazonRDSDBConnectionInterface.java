package com.amazonaws.tarun.stockApp.Utils;

public interface AmazonRDSDBConnectionInterface {
	//static AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.US_EAST_2).build();
    //static DynamoDB dynamoDB = new DynamoDB(client);
	public final static String FB_CONNECTION_STRING = "jdbc:firebirdsql://192.168.0.106:3050/D:/Tarun/StockApp_Latest/DB/STOCKAPPDBNEW.FDB?lc_ctype=utf8";
	public final static String FB_USER = "SYSDBA";
	public final static String FB_PASS = "Jan@2017";
	public final static String CONNECTION_STRING = "jdbc:mysql://stockinfodb.cncywoivxzly.us-east-2.rds.amazonaws.com:3306/StockDB";
	public final static String USER = "StockInfoDBUnser";
	public final static String PASS = "Jan_2017";
}
