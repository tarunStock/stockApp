package com.amazonaws.tarun.stockApp.FinancialIndicator.Calculation;

import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.amazonaws.tarun.stockApp.FinancialIndicator.Data.StockFinancialData;
import com.amazonaws.tarun.stockApp.Utils.SetupBase;
import com.amazonaws.tarun.stockApp.Utils.StockUtils;


public class CollectFinancialDataForCompanies extends SetupBase {

	final String URL = "https://www.nseindia.com/corporates/corporateHome.html?id=eqFinResults&radio_btn=company&param=UPL";
	final String timeOut = "4000";
	public String downloadFilepath = "c:\\StockApp\\download";
	Connection connection = null;
	static Logger logger = Logger.getLogger(CollectFinancialDataForCompanies.class);
	Date date = new Date(); //Date(System.currentTimeMillis()-24*60*60*1000);
			
	public static void main(String[] args) {
		Date dte = new Date();
		logger.debug("CollectFinancialDataForCompanies Started");
		CollectFinancialDataForCompanies obj = new CollectFinancialDataForCompanies();
		
		obj.collectAnnualFinancialData();
		dte = new Date();
		System.out.println("End at -> " + dte.toString());
	}
	
	public void collectAnnualFinancialData() {
		
		ArrayList<String> stockList = null;
		stockList = StockUtils.getStockListFromDB();
		String stockName;
		String bseCode;
		String nseCode;
		//calculateRSIForStock("DAAWAT", new Date("13-Oct-2017"));
		logger.debug("Open Selenium");
		setupSelenium(URL, downloadFilepath);
		//switch to frame
		//seleniumBean.getDriver().switchTo().frame(frame1);
		WebElement ele = null;
		ele = driver.findElement(By.xpath("//*[@id='ext-gen281']/iframe"));
		driver.switchTo().frame(ele);
		//collectAndStoreAnnualFinancialDataForStock("ABB");
		int counter = 1;
		for (String stockCode : stockList) {
			
			stockName = stockCode.split("!")[1];
			bseCode = stockCode.split("!")[0];
			nseCode = stockCode.split("!")[2];
			System.out.println("Collecting Annual Financial data for stock - >"+nseCode);
			
			collectAndStoreAnnualFinancialDataForStock(nseCode);
			counter++;
			if(counter==40) break;
		}
		logger.debug("Close Selenium");
		stopSelenium();
	}
	
	private void collectAndStoreAnnualFinancialDataForStock(String stockCode) {
		logger.debug("collectAndStoreAnnualFinancialDataForStock Started");
		try {
			Thread.sleep(3000);
		} catch(Exception ex) {
			System.out.println("Error in waiting for drop down suggestion");
		}
		WebElement ele = null;		
		ele = driver.findElement(By.id("company_name"));
		ele.clear();
		ele.sendKeys(stockCode);
		readPageAndLoadObject(stockCode);
		
		
		
	}
	
	private void readPageAndLoadObject(String stockCode) {
		WebElement ele = null;		
		StockFinancialData objStockFinancialData;
		
		try {
			Thread.sleep(4000);
		} catch(Exception ex) {
			System.out.println("Error in waiting for drop down suggestion");
		}
		
		try {
			//Check if there are more than one company listed in drop down
			ele = driver.findElement(By.xpath("//*[@id='ext-gen105']/div[2]/div/h3/span/a[1]"));
			System.out.println("2 div exist");
			//No error means more than one company listed in drop down. Pass company name along with space to get one single drop down entry
			ele = driver.findElement(By.id("company_name"));
			ele.clear();
			ele.sendKeys(stockCode+" ");
			try {
				Thread.sleep(3000);
			} catch(Exception ex) {
				System.out.println("Error in waiting for drop down suggestion");
			}
		} catch(Exception ex) {
			System.out.println("Error in waiting for drop down suggestion");
		}
		ele = driver.findElement(By.xpath("//*[@id='ext-gen105']/div/div/h3/span/a[2]"));
		ele.click();
		
		ele = driver.findElement(By.id("announce"));
		ele.clear();
		ele.sendKeys("Annual");
		
		
		ele = driver.findElement(By.id("broadcastPeriod"));
		ele.click();
		try {
			Thread.sleep(4000);
		} catch(Exception ex) {
			System.out.println("Error in waiting for drop down suggestion");
		}
		ele = driver.findElement(By.xpath("//*[@id='ext-gen3']/div[8]/div/div[9]/div")); //Click for More than 24 months
		ele.click();
		ele = driver.findElement(By.id("ext-gen18"));
		ele.click();
		try {
			Thread.sleep(4000);
		} catch(Exception ex) {
			System.out.println("Error in waiting for drop down suggestion");
		}
		for(int counter = 1; counter<=3; counter++) {
			try {
				ele = driver.findElement(By.xpath("//*[@id='ext-gen30']/div["+counter+"]/table/tbody/tr/td[6]/div")); 
			} catch (Exception ex) {
				System.out.println("*** No data for Stock -> "+stockCode);
				return;
			}
			String reportType = ele.getText();
			if(reportType!=null && reportType.equalsIgnoreCase("Non-Consolidated")) {
				ele = driver.findElement(By.xpath("//*[@id='ext-gen30']/div["+counter+"]/table/tbody/tr/td[9]/div/span")); 
				ele.click();
				objStockFinancialData = loadObjectWithFinancialData();
				System.out.println("Obj Data -> "+objStockFinancialData);
				//ele = driver.findElement(By.xpath("/html/body/div[11]/div/div/div/div/div"));				
				ele = driver.findElement(By.xpath("//*[@class='x-tool x-tool-close']"));
				ele.click();
			} else {
				System.out.println("Not matched");
			}
			
		}
		
		
		//Last wait
		try {
			Thread.sleep(2000);
		} catch(Exception ex) {
			System.out.println("Error in waiting for drop down suggestion");
		}
	}
	
	private StockFinancialData loadObjectWithFinancialData() {
		StockFinancialData objStockFinancialData = new StockFinancialData();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Thread.sleep(4000);
		} catch(Exception ex) {
			System.out.println("Error in waiting for drop down suggestion");
		}
		WebElement ele = null;		
		//ele = driver.findElement(By.xpath("//*[@id='ext-gen154']/table/tbody/tr/td/table/tbody/tr/td[2]/center/table/tbody/tr[4]/td[2]"));
		
		//ele = driver.findElement(By.xpath("/html/body/div[11]/div[2]/div/div/div/div/table/tbody/tr/td/table/tbody/tr/td[2]/center/table/tbody/tr[4]/td[2]"));
		ele = driver.findElement(By.xpath("//*[@class=' x-window x-resizable-pinned']/div[2]/div/div/div/div/table/tbody/tr/td/table/tbody/tr/td[2]/center/table/tbody/tr[4]/td[2]"));
		objStockFinancialData.statementPeriod = ele.getText();		
		ele = driver.findElement(By.xpath("//*[@class=' x-window x-resizable-pinned']/div[2]/div/div/div/div/table/tbody/tr/td/table/tbody/tr/td[2]/center/table/tbody/tr[5]/td[4]"));
		objStockFinancialData.periodEnded = new Date(ele.getText());		
		ele = driver.findElement(By.xpath("//*[@class=' x-window x-resizable-pinned']/div[2]/div/div/div/div/table/tbody/tr/td/table/tbody/tr/td[2]/table/tbody/tr/td[1]/table/tbody/tr/td/table/tbody/tr[4]/td[2]"));
		objStockFinancialData.netSales = Float.parseFloat(ele.getText());
		ele = driver.findElement(By.xpath("//*[@class=' x-window x-resizable-pinned']/div[2]/div/div/div/div/table/tbody/tr/td/table/tbody/tr/td[2]/table/tbody/tr/td[1]/table/tbody/tr/td/table/tbody/tr[5]/td[2]"));
		objStockFinancialData.otherOperatingIncome = Float.parseFloat(ele.getText());
		ele = driver.findElement(By.xpath("//*[@class=' x-window x-resizable-pinned']/div[2]/div/div/div/div/table/tbody/tr/td/table/tbody/tr/td[2]/table/tbody/tr/td[1]/table/tbody/tr/td/table/tbody/tr[24]/td[2]"));
		objStockFinancialData.netProfit = Float.parseFloat(ele.getText());		
		ele = driver.findElement(By.xpath("//*[@class=' x-window x-resizable-pinned']/div[2]/div/div/div/div/table/tbody/tr/td/table/tbody/tr/td[2]/table/tbody/tr/td[1]/table/tbody/tr/td/table/tbody/tr[26]/td[2]"));
		objStockFinancialData.netProfitAfterTaxes = Float.parseFloat(ele.getText());
		
		return objStockFinancialData;
	}
}