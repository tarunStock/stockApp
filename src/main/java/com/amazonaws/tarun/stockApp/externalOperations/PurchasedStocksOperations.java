package com.amazonaws.tarun.stockApp.externalOperations;

import java.sql.Connection;
import java.sql.Date;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;

import com.amazonaws.tarun.stockApp.TechnicalIndicator.Calculation.CollectDailyStockData;
import com.amazonaws.tarun.stockApp.TechnicalIndicator.Data.PurchasedStockData;
import com.amazonaws.tarun.stockApp.Utils.StockUtils;

public class PurchasedStocksOperations {
	
	static Logger logger = Logger.getLogger(CollectDailyStockData.class);
	
	public static void main(String[] args) {
		PurchasedStockData objPurchasedStockData = new PurchasedStockData();
		objPurchasedStockData.stockCode = "TEst";
		objPurchasedStockData.purchasedDate = new Date(Date.parse("10-Dec-2017"));
		objPurchasedStockData.purchasedPrice = Float.parseFloat("20.3");
		objPurchasedStockData.purchasedQuantity = Integer.parseInt("21");
		objPurchasedStockData.brokeragePaid = Float.parseFloat("5.5");
		
		PurchasedStocksOperations objAddPurchasedStocks = new PurchasedStocksOperations();
		objAddPurchasedStocks.addPurchasedStock(objPurchasedStockData);
	}
	
	public boolean addPurchasedStock(PurchasedStockData objPurchasedStockData) {
		
		Statement statement = null; 
        String tmpsql;
        Connection connection = null;
        DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd"); 
        System.out.println("Starting to create connection");
        try {        	
			connection = StockUtils.connectToDB();
			System.out.println("connection1");
        	statement = connection.createStatement();
        	System.out.println("");
        	tmpsql = "INSERT INTO PURCHASEDSTOCKDATA (STOCKNAME, PURCHASEDPRICE, PURCHASEDQUANTITY, PURCHASEDDATE, BROKERAGEPAID) VALUES('" + 
        			objPurchasedStockData.stockCode + "'," + objPurchasedStockData.purchasedPrice + "," + objPurchasedStockData.purchasedQuantity + ",'" + 
        			objPurchasedStockData.purchasedDate.toString() + "'," + objPurchasedStockData.brokeragePaid + ");";
        	System.out.println("SQL -> "+tmpsql);
        	statement.executeUpdate(tmpsql);
        	return true;
        } catch(Exception ex){
        	
        	System.out.println("addPurchasedStock for Stock -> " + objPurchasedStockData.stockCode + " Error in DB action"+ex);
        	logger.error("Error in addPurchasedStock -> ", ex);
        	return false;
        } finally {
        	try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				System.out.println("getBBPeriod Error in closing statement "+ex);
				logger.error("Error in closing statement getBBPeriod  -> ", ex);
			}
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				} 
			} catch (Exception ex) {
				System.out.println("getBBPeriod Error in closing connection "+ex);
				logger.error("Error in closing connection getBBPeriod  -> ", ex);
			}
		}
		
	}
}