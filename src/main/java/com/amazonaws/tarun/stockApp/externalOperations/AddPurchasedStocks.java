package com.amazonaws.tarun.stockApp.externalOperations;

import java.sql.Connection;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import com.amazonaws.tarun.stockApp.TechnicalIndicator.Calculation.CollectDailyStockData;
import com.amazonaws.tarun.stockApp.TechnicalIndicator.Data.PurchasedStockData;
import com.amazonaws.tarun.stockApp.Utils.StockUtils;

public class AddPurchasedStocks {
	
	static Logger logger = Logger.getLogger(CollectDailyStockData.class);
	
	public boolean addPurchasedStock(PurchasedStockData objPurchasedStockData) {
		
		Statement statement = null; 
        String tmpsql;
        Connection connection = null;
        DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd"); 
        
        try {        	
			connection = StockUtils.connectToDB();
        	statement = connection.createStatement();
        	tmpsql = "INSERT INTO PURCHASEDSTOCKDATA (STOCKNAME, PURCHASEDPRICE, PURCHASEDQUANTITY, PURCHASEDDATE, BROKERAGEPAID) VALUES('" + 
        			objPurchasedStockData.stockCode + "'," + objPurchasedStockData.purchasedPrice + "," + objPurchasedStockData.purchasedQuantity + ",'" + 
        			dateFormat.format(new Date(objPurchasedStockData.purchasedDate.toString())) + "'," + objPurchasedStockData.brokeragePaid + ");";
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