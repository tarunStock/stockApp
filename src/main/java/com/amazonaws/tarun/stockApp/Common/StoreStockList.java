package com.amazonaws.tarun.stockApp.Common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.amazonaws.tarun.stockApp.TechnicalIndicator.Data.QuotesData;
import com.amazonaws.tarun.stockApp.Utils.HandleErrorDetails;
import com.amazonaws.tarun.stockApp.Utils.SetupBase;
import com.amazonaws.tarun.stockApp.Utils.StockUtils;

public class StoreStockList extends SetupBase {
	final String URL = "https://www.nseindia.com/corporates/content/securities_info.htm";
	final String timeOut = "2000";
	
	QuotesData quotesDataObj;
	Connection connection = null;
	static Logger logger = Logger.getLogger(StoreStockList.class);
	public String downloadFilepath = "c:\\StockApp\\download";
	//Date date = new Date(System.currentTimeMillis()-2*24*60*60*1000L);
	Date date = new Date(); //Date(System.currentTimeMillis()-24*60*60*1000);
			
	public static void main(String[] args) {
		Date dte = new Date();
		logger.debug("Collect stockDetails Started");
		StoreStockList obj = new StoreStockList();
		
		obj.StoreAndCollectStockDetails();
		dte = new Date();
		System.out.println("End at -> " + dte.toString());
	}
	
	public void StoreAndCollectStockDetails() {
		logger.debug("StoreAndCollectStockDetails Started");
		BufferedReader br = null;
		String[] stockData = null;		
		StockDetails objStockDetails = null;
		ArrayList<StockDetails> objStockDetailsList  = new ArrayList<StockDetails>();
		try{
			setupSelenium(URL, downloadFilepath);
			logger.debug("Selenium Setup Completed");
			getStockDetailsFile();
			stopSelenium();
			File inputFolder = new File(downloadFilepath);			
			File[] inputFileList = inputFolder.listFiles();
			for (File inputFile : inputFileList) {
				if (inputFile.getName().contains(".csv")) {
					FileInputStream obj = new FileInputStream(inputFile);
					br = new BufferedReader(new InputStreamReader(obj));
			        String line = br.readLine();
					line = br.readLine();
					int counter = 1;
					//line = br.readLine();
					while (line != null) {
						stockData = line.split(",");
						objStockDetails = new StockDetails();
						objStockDetails.stockCode = stockData[0].trim();
						objStockDetails.companyName = stockData[1].trim();
						if(objStockDetails.companyName.contains("'")) {
							
							objStockDetails.companyName = objStockDetails.companyName.replace("'", "''");
							//System.out.println("company -> "+ objStockDetails.companyName);
						}
						objStockDetails.isinNo = stockData[6].trim();
						if(!objStockDetailsList.contains(objStockDetails)) {
							//System.out.println("" + counter++ + " -> " + objStockDetails);						
							objStockDetailsList.add(objStockDetails);
						}
						line = br.readLine();
					}
					br.close();
					obj.close();
					storeStockDetails(objStockDetailsList);
				}				 
				//Files.move(Paths.get(inputFile.getAbsolutePath()), Paths.get("C:/StockApp/downlodProcessed/" + inputFile.getName()));
				inputFile.delete();
			}
			
		} catch (Exception ex) {
			HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("Error -> "+ex);
		}
	}
	
	private void getStockDetailsFile() {
		logger.debug("getStockDetailsFile Started");
		WebElement ele = null;
		ele = driver.findElement(By.xpath("//*[@id='wrapper_btm']/div[1]/div[4]/div/ul/li[1]/a"));
		ele.click();
		try {
			Thread.sleep(7000);
		} catch(Exception ex) {
			HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("File link click error");
		}
		logger.debug("getStockDetailsFile End");
		
	}
	
	private void storeStockDetails (ArrayList<StockDetails> objStockDetailsList) {
		logger.debug("storeStockDetails Start");
		Connection connection = null;		
		connection = StockUtils.connectToDB();		
		Statement statement = null; 
        String tmpsql;
        StockDetails objStockDetails = null;
        
        try {
        	statement = connection.createStatement();
        	tmpsql = "delete from STOCKDETAILSNEW;";
        	statement.executeUpdate(tmpsql);
        	for(int counter = 0; counter < objStockDetailsList.size(); counter++) {
        		objStockDetails = objStockDetailsList.get(counter);
        		tmpsql = "INSERT INTO STOCKDETAILSNEW (STOCKNAME,NSECODE,ISINCODE) VALUES('" + 
        				objStockDetails.companyName + "','" + objStockDetails.stockCode + "','" + objStockDetails.isinNo + "');";
        		statement.executeUpdate(tmpsql);
        	}    
        	logger.debug("storeStockDetails End");
        } catch(Exception ex){
        	HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());        	
        	System.out.println("error "+ ex);
        	logger.error("Error in storeStockDetails -> ", ex);
        } finally {
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());				
				logger.error("Error in closing statement storeStockDetails  -> ", ex);
			}
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				} 
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());				
				logger.error("Error in closing connection in storeStockDetails  -> ", ex);
			}
		}
	}
}
