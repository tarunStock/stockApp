package com.amazonaws.tarun.stockApp.Utils;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

public class StockUtils implements AmazonRDSDBConnectionInterface{
	static Logger logger = Logger.getLogger(StockUtils.class);	
	
	public static Connection connectToDB () {
		Connection connection = null;
		boolean connectToFirebird = false;
		try {
			if(connectToFirebird) {
				Class.forName("org.firebirdsql.jdbc.FBDriver").newInstance();
				//System.out.println("Connecting");
				//Class.forName("com.mysql.jdbc.Driver").newInstance();
				connection = DriverManager.getConnection("jdbc:firebirdsql://192.168.0.106:3050/D:/Tarun/StockApp_Latest/DB/STOCKAPPDBNEW.FDB?lc_ctype=utf8", "SYSDBA", "Jan@2017");
				//System.out.println("Connected");
			}
			else {
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				connection = DriverManager.getConnection(CONNECTION_STRING, USER, PASS);
			}		
			
		} catch (Exception ex) {
			System.out.println("connectToDB Error in DB action ->"+ex);
			logger.error("Error in getStockListFromDB  -> ", ex);
		}
		return connection;
	}
	
	public static boolean getFinancialIndication(String bseCode) {
		Connection connection = null;
		ResultSet resultSet = null;
		Statement statement = null;
		String indication;

		try {
			//priceData = new ArrayList<Float>();
			connection = StockUtils.connectToDB();
			statement = connection.createStatement();

			resultSet = statement.executeQuery("SELECT ANNUALSALESINDICATOR FROM STOCK_FINANCIAL_TRACKING where bsecode='" + bseCode + "';");
			
			// DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
			while (resultSet.next()) {
				indication = resultSet.getString(1);
				if(indication.equalsIgnoreCase("good")){
					return true;
				} else {
					return false;
				}
			}
		} catch (Exception ex) {
			System.out.println("getFinancialIndication Error in getting indication = " + ex);
			return true;
		} finally {
			try {
				if(resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
			} catch (Exception ex) {
				System.out.println("getFinancialIndication Error in closing resultset "+ex);
				logger.error("Error in closing resultset getFinancialIndication  -> ", ex);
			}
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				System.out.println("getFinancialIndication Error in closing statement "+ex);
				logger.error("Error in closing statement getFinancialIndication  -> ", ex);
			}
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				} 
			} catch (Exception ex) {
				System.out.println("getFinancialIndication Error in closing connection "+ex);
				logger.error("Error in closing connection getFinancialIndication  -> ", ex);
			}
		}
		//Returning true in case of no data to avoid loosing good stock
		return true;
	}
	
	public static ArrayList<String> getStockListFromDB() {
		Connection connection = null;
		ResultSet resultSet = null;
		Statement statement = null;
		ArrayList<String> stockList = null;
		String stockBSECode;
		
		try {
			stockList = new ArrayList<String>();
			connection = StockUtils.connectToDB();
			statement = connection.createStatement();

			resultSet = statement.executeQuery("SELECT BSECODE, stockname, NSECODE FROM STOCKDETAILS;");
			while (resultSet.next()) {
				stockBSECode = resultSet.getString(1);
				stockBSECode = stockBSECode + "!" + resultSet.getString(2);
				stockBSECode = stockBSECode + "!" + resultSet.getString(3);
				stockList.add(stockBSECode);
				// System.out.println("StockNme - " + stockNSECode);
			}
			return stockList;
		} catch (Exception ex) {
			System.out.println("Error in DB action");
			return null;
		} finally {
			try {
				if(resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
			} catch (Exception ex) {
				System.out.println("getStockListFromDB Error in closing resultset "+ex);
				logger.error("Error in closing resultset getStockListFromDB  -> ", ex);
			}
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				System.out.println("getStockListFromDB Error in closing statement "+ex);
				logger.error("Error in closing statement getStockListFromDB  -> ", ex);
			}
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				} 
			} catch (Exception ex) {
				System.out.println("getStockListFromDB Error in closing connection "+ex);
				logger.error("Error in closing connection getStockListFromDB  -> ", ex);
			}
		}
	}
	
	public static boolean marketOpenOnGivenDate(Date targetDate) {
		if(targetDate.getDay() == 0 || targetDate.getDay() == 6)
			return false;
		else
			return true;
	}
}
