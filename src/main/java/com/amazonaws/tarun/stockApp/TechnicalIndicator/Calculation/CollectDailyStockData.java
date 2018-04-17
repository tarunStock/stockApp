package com.amazonaws.tarun.stockApp.TechnicalIndicator.Calculation;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;
import org.jsoup.parser.Parser;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.amazonaws.tarun.stockApp.TechnicalIndicator.Data.QuotesData;
import com.amazonaws.tarun.stockApp.Utils.HandleErrorDetails;
import com.amazonaws.tarun.stockApp.Utils.SetupBase;
import com.amazonaws.tarun.stockApp.Utils.StockUtils;


public class CollectDailyStockData extends SetupBase {
	final String URL = "https://www.nseindia.com/products/content/equities/equities/archieve_eq.htm";
	final String timeOut = "2000";
	
	QuotesData quotesDataObj;
	Connection connection = null;
	static Logger logger = Logger.getLogger(CollectDailyStockData.class);
	public String downloadFilepath = "c:\\StockApp\\download";
	
	//Date date = new Date(System.currentTimeMillis()-1*24*60*60*1000L);
	Date date = new Date(); //Date(System.currentTimeMillis()-24*60*60*1000);
			
	public static void main(String[] args) {
		Date dte = new Date();
		logger.debug("CollectDailyStockData Started");
		CollectDailyStockData obj = new CollectDailyStockData();
		
		obj.startCollectingDailyData();
		dte = new Date();
		System.out.println("End at -> " + dte.toString());
	}
	
	public void startCollectingDailyData(Date targetDate) {
		collectData(targetDate);
	}
	
	public void startCollectingDailyData() {
		collectData(date);
	}
	
	private void collectData(Date targetDate) {

		BufferedReader br = null;
		String[] stockData = null;
		File inputFileForDeletion = null;	
		try{
			if(!StockUtils.marketOpenOnGivenDate(targetDate))
				return;
			logger.debug("startCollectingDailyData Started");
			setupSelenium(URL, downloadFilepath);
			logger.debug("Selenium Setup Completed");
			getDailyDataFile(targetDate);
			stopSelenium();
			File inputFolder = new File(downloadFilepath);			
			File[] inputFileList = inputFolder.listFiles();
			
			if (connection != null) {
				connection.close();
				connection = null;
			}
			connection = StockUtils.connectToDB();
			for (File inputFile : inputFileList) {
				if (inputFile.getName().contains(".zip")) {
					ZipFile zipFile = new ZipFile(inputFile);
					inputFileForDeletion = inputFile;
				    Enumeration<? extends ZipEntry> entries = zipFile.entries();

				    while(entries.hasMoreElements()){
				        ZipEntry entry = entries.nextElement();
				        InputStream stream = zipFile.getInputStream(entry);
				        br = new BufferedReader(new InputStreamReader(stream));
				        String line = br.readLine();
						line = br.readLine();
						while (line != null) {
							stockData = line.split(",");
							quotesDataObj = new QuotesData();
							//quotesDataObj.stockName = Parser.unescapeEntities(stockData[0].substring(1, stockData[0].length()-1).trim(), false);						
							quotesDataObj.stockName = Parser.unescapeEntities(stockData[0].trim(), false);
							//quotesDataObj.openPrice = Float.parseFloat(stockData[2].substring(1, stockData[2].length()-1).trim());
							quotesDataObj.openPrice = Float.parseFloat(stockData[2].trim());
							//quotesDataObj.dailyHigh = Float.parseFloat(stockData[3].substring(1, stockData[3].length()-1).trim());
							quotesDataObj.dailyHigh = Float.parseFloat(stockData[3].trim());
							//quotesDataObj.dailyLow = Float.parseFloat(stockData[4].substring(1, stockData[4].length()-1).trim());
							quotesDataObj.dailyLow = Float.parseFloat(stockData[4].trim());							
							//quotesDataObj.closingPrice = Float.parseFloat(stockData[5].substring(1, stockData[5].length()-1).trim());						
							quotesDataObj.closingPrice = Float.parseFloat(stockData[5].trim());
							//quotesDataObj.volume = Long.parseLong(stockData[8].substring(1, stockData[8].length()-1).trim());
							quotesDataObj.volume = Long.parseLong(stockData[8].trim());
							//quotesDataObj.quoteDate = stockData[10].substring(1, stockData[10].length()-1).trim();
							quotesDataObj.quoteDate = stockData[10].trim();
							storeQuotestoDB();
							line = br.readLine();
						}
				        br.close();
				        stream.close();
				    }
				    zipFile.close();
				    Files.move(Paths.get(inputFile.getAbsolutePath()), Paths.get("C:/StockApp/downlodProcessed/" + inputFile.getName()));
				    logger.debug("startCollectingDailyData End");			    
				}
			}			
		} catch (Exception ex) {
			HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("Error in reading zip fil "+ex);
			logger.error("Error in startCollectingDailyData - > "+ex);
			inputFileForDeletion.delete();	
		} finally {
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				} 
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("Error in closing connection in startCollectingDailyData "+ex);
				logger.error("Error in closing connection in startCollectingDailyData  -> ", ex);
			}
		}	
	}
	
	private void getDailyDataFile (Date targetDate) {		
		logger.debug("getDailyDataFile Started");
		WebElement ele = null;
		ele = driver.findElement(By.id("h_filetype"));
		Select select= new Select(ele);
		
		String text = driver.findElement(By.xpath("//option[contains(text(), 'Bhavcopy')]")).getText();
		
		//select.selectByVisibleText("Bhavcopy");
		select.selectByVisibleText(text);
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy"); 
		
		ele = driver.findElement(By.id("date"));
		ele.clear();
		ele.sendKeys(dateFormat.format(targetDate));
		ele = driver.findElement(By.xpath("//*[@id='wrapper_btm']/div[1]/div[4]/div/div[1]/div/div[3]"));
		ele.click();
		try {
			Thread.sleep(3000);
		} catch(Exception ex) {
			HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("Error in waiting for drop down suggestion");
		}
		ele = driver.findElement(By.xpath("//*[@id='wrapper_btm']/div[1]/div[4]/div/div[1]/div/div[4]/input[3]"));
		ele.click();
		waitForPageLoad(10000);
		ele = driver.findElement(By.xpath("//*[@id='spanDisplayBox']/table/tbody/tr/td/a"));		
		ele.click();			
		try {
			Thread.sleep(7000);
		} catch(Exception ex) {
			HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("Error in waiting for drop down suggestion");
		}
		logger.debug("getDailyDataFile End");
	}
	
	private void storeQuotestoDB() {
		Statement statement = null; 
        String tmpsql;
        DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd"); 
        
        try {
        	statement = connection.createStatement();
        	tmpsql = "INSERT INTO DAILYSTOCKDATA (STOCKNAME, CLOSEPRICE, HIGHPRICE, LOWPRICE, OpenPrice, VOLUME, TRADEDDATE) VALUES('" + 
        				quotesDataObj.stockName + "'," + quotesDataObj.closingPrice + "," + quotesDataObj.dailyHigh + "," + 
        				quotesDataObj.dailyLow + "," + quotesDataObj.openPrice + "," + quotesDataObj.volume + ",'" + dateFormat.format(new Date(quotesDataObj.quoteDate)) + "');";
        	statement.executeUpdate(tmpsql);
        } catch(Exception ex){
        	HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
        	System.out.println("storeQuotestoDB for quote -> " + quotesDataObj.stockName + " and Date - > " + quotesDataObj.quoteDate + " Error in DB action"+ex);
        	logger.error("Error in storeQuotestoDB -> ", ex);
        } finally {
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getStockDetailsFromDBForBulk Error in closing statement "+ex);
				logger.error("Error in closing statement getStockDetailsFromDB  -> ", ex);
			}
		}
	}
	
	public void sendNotificationForDailyStockDataCollection() {
		Statement statement = null;
		String tmpSQL, lastDate = null, msgSubject = null, mailBody="Stock Quote Data Notification";
		Date dbDate, todaysDate;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		ResultSet resultSet = null;
		if( !StockUtils.marketOpenOnGivenDate(null))
			return;
		
		try { 
			if (connection == null) {
				connection = StockUtils.connectToDB();
			}	
			todaysDate = sdf.parse((sdf.format(new Date())));
			statement = connection.createStatement();
			tmpSQL = "select max(tradeddate) from DAILYSTOCKDATA;";
			resultSet = statement.executeQuery(tmpSQL);	
			while (resultSet.next()) {
				lastDate = resultSet.getString(1);
			}
			if(lastDate!=null) {
				dbDate = sdf.parse(lastDate);
				
				if(dbDate.compareTo(todaysDate) == 0) {
					msgSubject = "Stock Data collected for -> " + todaysDate;
				} else {
					msgSubject = "Stock Data ***NOT*** collected for -> " + todaysDate;
				}
			} else {
				msgSubject = "Stock Data ***NOT*** collected for -> " + todaysDate;
			}
			new SendSuggestedStockInMail("tarunstockcomm@gmail.com",msgSubject,mailBody);			
		} catch (Exception ex) {
			HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("insertBBToDB Error in DB action ->"+ex);
			logger.error("Error in insertBBToDB  -> ", ex);
		} finally {			
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("CalculateBollingerBands -> insertBBToDB Error in closing statement "+ex);
				logger.error("Error in closing statement insertBBToDB  -> ", ex);
			}
		}
	}
}
