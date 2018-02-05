package com.amazonaws.tarun.stockApp.FinancialIndicator.Calculation;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import com.amazonaws.tarun.stockApp.FinancialIndicator.Data.CompanyAnnualFinancialData;
import com.amazonaws.tarun.stockApp.FinancialIndicator.Data.CompanyFinancialData;
import com.amazonaws.tarun.stockApp.FinancialIndicator.Data.CompanyHalfYearlyFinancialData;
import com.amazonaws.tarun.stockApp.FinancialIndicator.Data.CompanyQuarterFinancialData;
import com.amazonaws.tarun.stockApp.FinancialIndicator.Data.StockFinancialData;
import com.amazonaws.tarun.stockApp.TechnicalIndicator.Data.QuotesData;
import com.amazonaws.tarun.stockApp.Utils.HandleErrorDetails;
import com.amazonaws.tarun.stockApp.Utils.SetupBase;
import com.amazonaws.tarun.stockApp.Utils.StockUtils;

public class CollectFinancialDataForCompanies extends SetupBase {

	final String URL = "https://www.nseindia.com/corporates/corporateHome.html?id=eqFinResults&radio_btn=company&param=UPL";
	final String URLMC = "http://www.moneycontrol.com";
	final String timeOut = "2000";
	public String downloadFilepath = "c:\\StockApp\\download";
	QuotesData quotesDataObj;
	Connection connection = null;
	static Logger logger = Logger.getLogger(CollectFinancialDataForCompanies.class);

	// Date date = new Date(System.currentTimeMillis()-2*24*60*60*1000L);
	Date date = new Date(); // Date(System.currentTimeMillis()-24*60*60*1000);

	public static void main(String[] args) {
		Date dte = new Date();
		logger.debug("CollectFinancialDataForCompanies Started");
		CollectFinancialDataForCompanies obj = new CollectFinancialDataForCompanies();

		obj.collectAnnualFinancialDataMC();
		dte = new Date();
		System.out.println("End at -> " + dte.toString());
	}

	public void collectAnnualFinancialDataMC() {

		ArrayList<String> stockList = null;
		ArrayList<String> processedStockList = null;
		ArrayList<String> errorStockList = null;
		stockList = StockUtils.getStockListFromDB();
		processedStockList = getProcessedStockList();
		errorStockList = getErroredStockList();
		ArrayList<String> newProcessedStockList = new ArrayList<String>();
		ArrayList<String> newErrorStockList = new ArrayList<String>();
		String stockName;
		String bseCode;
		String nseCode = null;
		// calculateRSIForStock("DAAWAT", new Date("13-Oct-2017"));
		logger.debug("Open Selenium");
		setupSelenium(URLMC, downloadFilepath);
		try {
			Thread.sleep(10000);
		} catch (Exception ex) {
			System.out.println("Error in waiting for pge load");
		}
		// switch to frame
		// seleniumBean.getDriver().switchTo().frame(frame1);

		try {
			//collectAndStoreAnnualFinancialDataForStockMC("BANCOINDIA");
			//collectAndStoreAnnualFinancialDataForStockMC("ABB");
			
			/*collectAndStoreAnnualFinancialDataForStockMC("5PAISA");
			collectAndStoreAnnualFinancialDataForStockMC("ASTRON");
			collectAndStoreAnnualFinancialDataForStockMC("FSC");*/
			
			 int counter = 1; 
			 for (String stockCode : stockList) {				 
				 stockName = stockCode.split("!")[1]; 
				 bseCode = stockCode.split("!")[0]; 
				 nseCode = stockCode.split("!")[2];
				 if((processedStockList==null || !processedStockList.contains(nseCode)) && (errorStockList==null || !errorStockList.contains(nseCode))) {
					 //System.out.println("Collecting Annual Financial data for stock - >"+nseCode);
					 try {
						 collectAndStoreFinancialDataForStockMC(nseCode);
						 newProcessedStockList.add(nseCode);
					 } catch (Exception ex) {
						 newErrorStockList.add(nseCode);
						 HandleErrorDetails.addError(StockUtils.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), "nseCode -->" + ex.toString());
						 System.out.println("Error in collecting financial data for stock -> "+nseCode+" end the error is = "+ex);
					 }
					 counter++;
				 }
				 if(counter==31) break;
			}
			System.out.println("*************************Successfully processed stock count -> "+counter);
			
		} catch (Exception ex) {
			HandleErrorDetails.addError(StockUtils.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), "nseCode -->" + ex.toString());
			System.out.println(" Error with stock ->" + nseCode + " Error -> " + ex);
		} finally {
			storeProcessedStockList(newProcessedStockList);
			storeErroredStockList(newErrorStockList);
			HandleErrorDetails.sendErrorsInMail("Financialdata collection Indication");
		}
		logger.debug("Close Selenium");
		stopSelenium();
	}

	private void collectAndStoreFinancialDataForStockMC(String stockCode) {
		logger.debug("collectAndStoreAnnualFinancialDataForStock Started");
		CompanyFinancialData objCompanyFinancialData;
		WebElement ele = null;
		ele = driver.findElement(By.id("search_str"));
		ele.clear();
		ele.sendKeys(stockCode);
		try {
			ele = driver.findElement(By.xpath("//html/body/div/header/div[1]/div/div[2]/div[2]/div[1]/a"));
			ele.click();
		} catch (NoSuchElementException ex) {
			ele = driver.findElement(By.xpath("//html/body/header/div[1]/div/div[2]/div[2]/div[1]/a"));
			ele.click();
			//System.out.println("Search link xpath changed");
		}

		try {
			Thread.sleep(7000);
		} catch (Exception ex) {
			System.out.println("Error in waiting for drop down suggestion");
		}
		try {
			ele = driver.findElement(By.xpath("//*[@id='mc_mainWrapper']/div[3]/div[2]/div/table/tbody/tr[1]/td[1]/p/a"));
			ele.click();
			Thread.sleep(7000);
		} catch (Exception ex) {
			System.out.println("Comapny details directly opened or Error in clicking company name in the search list");
		}
		// Click on Financial link
		ele = driver.findElement(By.xpath("//*[@id='slider']/dt[7]/a"));
		ele.click();
		try {
			Thread.sleep(7000);
		} catch (Exception ex) {
			System.out.println("Error in clicking financial link");
		}
		/*commenting to only collect yearly data on first go
		// Click on Profit Loss link
		ele = driver.findElement(By.xpath("//*[@id='slider']/dd[3]/ul/li[2]/a"));
		ele.click();
		try {
			Thread.sleep(7000);
		} catch (Exception ex) {
			System.out.println("Error in clicking profit loss link");
		}*/
		objCompanyFinancialData = readPageAndLoadObjectMC(stockCode);
		storeCompanyFinancialData(objCompanyFinancialData);
	}

	private CompanyFinancialData readPageAndLoadObjectMC(String stockCode) {

		CompanyFinancialData objCompanyFinancialData = new CompanyFinancialData();
		objCompanyFinancialData.nseCode = stockCode;
		System.out.println("*********************Stockcode -> " + stockCode);
		objCompanyFinancialData.objCompanyAnnualFinancialDataList = readPageAndLoadAnnualDataMC();
		objCompanyFinancialData.objCompanyAnnualFinancialDataList = readPageAndLoadCashFlowDataMC(objCompanyFinancialData.objCompanyAnnualFinancialDataList);
		/*commenting to only collect yearly data on first go
		objCompanyFinancialData.objCompanyHalfYearlyFinancialDataList = readPageAndLoadHalfYearlyDataMC();
		objCompanyFinancialData.objCompanyQuarterFinancialDataList = readPageAndLoadQuarterlyDataMC();*/

		return objCompanyFinancialData;
	}

	private ArrayList<CompanyAnnualFinancialData> readPageAndLoadAnnualDataMC() {
		ArrayList<CompanyAnnualFinancialData> objCompanyAnnualFinancialDataList = new ArrayList<CompanyAnnualFinancialData>();
		CompanyAnnualFinancialData objCompanyAnnualFinancialData = null;
		WebElement ele = null;
		String temp, rowHeading;
		/*commenting to only collect yearly data on first go
		// Click on Old Format Link
		ele = driver.findElement(By.xpath("//*[@id='mc_mainWrapper']/div[3]/div[2]/div[3]/div[2]/div[2]/div[1]/div[1]/div/ul/li[2]/a"));
		ele.click();
		try {
			Thread.sleep(4000);
		} catch (Exception ex) {
			System.out.println("Error in wait after old format click");
		}*/
		
		//Click on Yearly link
		ele = driver.findElement(By.xpath("//*[@id='slider']/dd[3]/ul/li[6]/a"));
		ele.click();
		try {
			Thread.sleep(7000);
		} catch (Exception ex) {
			System.out.println("Error in clicking yearly link");
		}
		
		for (int counter = 2; counter <= 4; counter++) {
			objCompanyAnnualFinancialData = new CompanyAnnualFinancialData();
			//System.out.println("Getting year month");
			// Get Year and Month
			ele = driver.findElement(By.xpath("//*[@id='mc_mainWrapper']/div[3]/div[2]/div[3]/div[2]/div[2]/div[2]/div[1]/table[2]/tbody/tr[3]/td[" + counter + "]"));
			temp = ele.getText();
			//System.out.println("Year -> " + temp);
			objCompanyAnnualFinancialData.resultMonth = temp.split("'")[0].trim();
			objCompanyAnnualFinancialData.resultYear = Integer.parseInt(temp.split("'")[1].trim());
			//System.out.println("Getting Net Sales");
			for (int rowCounter = 5; rowCounter <= 45; rowCounter++) {
				ele = driver.findElement(By.xpath("//*[@id='mc_mainWrapper']/div[3]/div[2]/div[3]/div[2]/div[2]/div[2]/div[1]/table[2]/tbody/tr[" + rowCounter + "]/td[1]"));
				rowHeading = ele.getText();
				// Net Sales
				if (rowHeading.contains("Net Sales")) {
					// Net Sales
					ele = driver.findElement(By.xpath("//*[@id='mc_mainWrapper']/div[3]/div[2]/div[3]/div[2]/div[2]/div[2]/div[1]/table[2]/tbody/tr[" + rowCounter + "]/td[" + counter + "]"));
					temp = ele.getText().contains("--") ? "0" : ele.getText().replaceAll(",", "");
					objCompanyAnnualFinancialData.netSales = Double.parseDouble(temp);
				}
				if (rowHeading.contains("Other Operating Income")) {
					// Other Income
					ele = driver.findElement(By.xpath("//*[@id='mc_mainWrapper']/div[3]/div[2]/div[3]/div[2]/div[2]/div[2]/div[1]/table[2]/tbody/tr[" + rowCounter + "]/td[" + counter + "]"));
					temp = ele.getText().contains("--") ? "0" : ele.getText().replaceAll(",", "");
					objCompanyAnnualFinancialData.otherIncome = Double.parseDouble(temp);
				}
				if (rowHeading.contains("P/L Before Other Inc. , Int., Excpt. Items & Tax")) {
					// pbdit
					ele = driver.findElement(By.xpath("//*[@id='mc_mainWrapper']/div[3]/div[2]/div[3]/div[2]/div[2]/div[2]/div[1]/table[2]/tbody/tr[" + rowCounter + "]/td[" + counter + "]"));
					temp = ele.getText().contains("--") ? "0" : ele.getText().replaceAll(",", "");
					objCompanyAnnualFinancialData.pbdit = Double.parseDouble(temp);
				}
				if (rowHeading.contains("Interest")) {
					// interest
					ele = driver.findElement(By.xpath("//*[@id='mc_mainWrapper']/div[3]/div[2]/div[3]/div[2]/div[2]/div[2]/div[1]/table[2]/tbody/tr[" + rowCounter + "]/td[" + counter + "]"));
					temp = ele.getText().contains("--") ? "0" : ele.getText().replaceAll(",", "");
					objCompanyAnnualFinancialData.interest = Double.parseDouble(temp);
				}
				if (rowHeading.contains("P/L Before Exceptional Items & Tax")) {
					// pbdt
					ele = driver.findElement(By.xpath("//*[@id='mc_mainWrapper']/div[3]/div[2]/div[3]/div[2]/div[2]/div[2]/div[1]/table[2]/tbody/tr[" + rowCounter + "]/td[" + counter + "]"));
					temp = ele.getText().contains("--") ? "0" : ele.getText().replaceAll(",", "");
					objCompanyAnnualFinancialData.pbdt = Double.parseDouble(temp);
				}
				if (rowHeading.contains("P/L Before Tax")) {
					// profitBeforeTax
					ele = driver.findElement(By.xpath("//*[@id='mc_mainWrapper']/div[3]/div[2]/div[3]/div[2]/div[2]/div[2]/div[1]/table[2]/tbody/tr[" + rowCounter + "]/td[" + counter + "]"));
					temp = ele.getText().contains("--") ? "0" : ele.getText().replaceAll(",", "");
					objCompanyAnnualFinancialData.profitBeforeTax = Double.parseDouble(temp);
				}
				if (rowHeading.contains("Net Profit/(Loss) For the Period")) {
					// netProfit
					ele = driver.findElement(By.xpath("//*[@id='mc_mainWrapper']/div[3]/div[2]/div[3]/div[2]/div[2]/div[2]/div[1]/table[2]/tbody/tr[" + rowCounter + "]/td[" + counter + "]"));
					temp = ele.getText().contains("--") ? "0" : ele.getText().replaceAll(",", "");
					objCompanyAnnualFinancialData.netProfit = Double.parseDouble(temp);
				}
				if (rowHeading.equalsIgnoreCase("Basic EPS")) {
					// basicEPS
					ele = driver.findElement(By.xpath("//*[@id='mc_mainWrapper']/div[3]/div[2]/div[3]/div[2]/div[2]/div[2]/div[1]/table[2]/tbody/tr[" + rowCounter + "]/td[" + counter + "]"));
					temp = ele.getText().contains("--") ? "0" : ele.getText().replaceAll(",", "");
					objCompanyAnnualFinancialData.basicEPS = Float.parseFloat(temp);
					break;
				}				
			}
			
			/*Commenting as changing the focus to yearly data. Might need profit loss later to get operating profit and marging
			for (int rowCounter = 3; rowCounter <= 38; rowCounter++) {

				ele = driver.findElement(By.xpath("//*[@id='mc_mainWrapper']/div[3]/div[2]/div[3]/div[2]/div[2]/div[2]/div[1]/table[2]/tbody/tr[" + rowCounter + "]/td[1]"));
				rowHeading = ele.getText();

				// Net Sales
				if (rowHeading.contains("Net Sales")) {
					ele = driver.findElement(By.xpath("//*[@id='mc_mainWrapper']/div[3]/div[2]/div[3]/div[2]/div[2]/div[2]/div[1]/table[2]/tbody/tr[" + rowCounter + "]/td[" + counter + "]"));
					temp = ele.getText().contains("--") ? "0" : ele.getText().replaceAll(",", "");
					objCompanyAnnualFinancialData.netSales = Double.parseDouble(temp);
				}

				if (rowHeading.contains("Other Income")) {
					//System.out.println("Getting other income");
					// Other Income
					ele = driver.findElement(By.xpath("//*[@id='mc_mainWrapper']/div[3]/div[2]/div[3]/div[2]/div[2]/div[2]/div[1]/table[2]/tbody/tr[" + rowCounter + "]/td[" + counter + "]"));
					temp = ele.getText().contains("--") ? "0" : ele.getText().replaceAll(",", "");
					objCompanyAnnualFinancialData.otherIncome = Double.parseDouble(temp);
				}
				if (rowHeading.contains("Total Expenses")) {
					// Total Expenses
					ele = driver.findElement(By.xpath("//*[@id='mc_mainWrapper']/div[3]/div[2]/div[3]/div[2]/div[2]/div[2]/div[1]/table[2]/tbody/tr[" + rowCounter + "]/td[" + counter + "]"));
					temp = ele.getText().contains("--") ? "0" : ele.getText().replaceAll(",", "");
					objCompanyAnnualFinancialData.totalExpenses = Double.parseDouble(temp);
				}
				if (rowHeading.contains("Operating Profit")) {
					// operatingProfit
					ele = driver.findElement(By.xpath("//*[@id='mc_mainWrapper']/div[3]/div[2]/div[3]/div[2]/div[2]/div[2]/div[1]/table[2]/tbody/tr[" + rowCounter + "]/td[" + counter + "]"));
					temp = ele.getText().contains("--") ? "0" : ele.getText().replaceAll(",", "");
					objCompanyAnnualFinancialData.operatingProfit = Double.parseDouble(temp);
				}
				if (rowHeading.contains("PBDIT")) {
					// pbdit
					ele = driver.findElement(By.xpath("//*[@id='mc_mainWrapper']/div[3]/div[2]/div[3]/div[2]/div[2]/div[2]/div[1]/table[2]/tbody/tr[" + rowCounter + "]/td[" + counter + "]"));
					temp = ele.getText().contains("--") ? "0" : ele.getText().replaceAll(",", "");
					objCompanyAnnualFinancialData.pbdit = Double.parseDouble(temp);
				}
				if (rowHeading.contains("Interest")) {
					// interest
					ele = driver.findElement(By.xpath("//*[@id='mc_mainWrapper']/div[3]/div[2]/div[3]/div[2]/div[2]/div[2]/div[1]/table[2]/tbody/tr[" + rowCounter + "]/td[" + counter + "]"));
					temp = ele.getText().contains("--") ? "0" : ele.getText().replaceAll(",", "");
					objCompanyAnnualFinancialData.interest = Double.parseDouble(temp);
				}
				if (rowHeading.contains("PBDT")) {
					// pbdt
					ele = driver.findElement(By.xpath("//*[@id='mc_mainWrapper']/div[3]/div[2]/div[3]/div[2]/div[2]/div[2]/div[1]/table[2]/tbody/tr[" + rowCounter + "]/td[" + counter + "]"));
					temp = ele.getText().contains("--") ? "0" : ele.getText().replaceAll(",", "");
					objCompanyAnnualFinancialData.pbdt = Double.parseDouble(temp);
				}
				if (rowHeading.contains("Profit Before Tax")) {
					// profitBeforeTax
					ele = driver.findElement(By.xpath("//*[@id='mc_mainWrapper']/div[3]/div[2]/div[3]/div[2]/div[2]/div[2]/div[1]/table[2]/tbody/tr[" + rowCounter + "]/td[" + counter + "]"));
					temp = ele.getText().contains("--") ? "0" : ele.getText().replaceAll(",", "");
					objCompanyAnnualFinancialData.profitBeforeTax = Double.parseDouble(temp);
				}
				if (rowHeading.contains("Reported Net Profit")) {
					// netProfit
					ele = driver.findElement(By.xpath("//*[@id='mc_mainWrapper']/div[3]/div[2]/div[3]/div[2]/div[2]/div[2]/div[1]/table[2]/tbody/tr[" + rowCounter + "]/td[" + counter + "]"));
					temp = ele.getText().contains("--") ? "0" : ele.getText().replaceAll(",", "");
					objCompanyAnnualFinancialData.netProfit = Double.parseDouble(temp);
				}
				if (rowHeading.contains("Book Value")) {
					// bookValue
					ele = driver.findElement(By.xpath("//*[@id='mc_mainWrapper']/div[3]/div[2]/div[3]/div[2]/div[2]/div[2]/div[1]/table[2]/tbody/tr[" + rowCounter + "]/td[" + counter + "]"));
					temp = ele.getText().contains("--") ? "0" : ele.getText().replaceAll(",", "");
					objCompanyAnnualFinancialData.bookValue = Double.parseDouble(temp);
				}
				if (rowHeading.contains("Equity Dividend (%)")) {
					// equitiDividendRatio
					ele = driver.findElement(By.xpath("//*[@id='mc_mainWrapper']/div[3]/div[2]/div[3]/div[2]/div[2]/div[2]/div[1]/table[2]/tbody/tr[" + rowCounter + "]/td[" + counter + "]"));
					temp = ele.getText().contains("--") ? "0" : ele.getText().replaceAll(",", "");
					objCompanyAnnualFinancialData.equitiDividendRatio = Float.parseFloat(temp);
				}
				if (rowHeading.contains("Earning Per Share (Rs)")) {
					// basicEPS
					ele = driver.findElement(By.xpath("//*[@id='mc_mainWrapper']/div[3]/div[2]/div[3]/div[2]/div[2]/div[2]/div[1]/table[2]/tbody/tr[" + rowCounter + "]/td[" + counter + "]"));
					temp = ele.getText().contains("--") ? "0" : ele.getText().replaceAll(",", "");
					objCompanyAnnualFinancialData.basicEPS = Float.parseFloat(temp);
				}
			}*/
			
			//System.out.println("&&&&&&&&Annual -> " + objCompanyAnnualFinancialData);
			objCompanyAnnualFinancialDataList.add(objCompanyAnnualFinancialData);
		}
		return objCompanyAnnualFinancialDataList;
	}

	private ArrayList<CompanyAnnualFinancialData> readPageAndLoadCashFlowDataMC(ArrayList<CompanyAnnualFinancialData> objCompanyAnnualFinancialDataList) {
		WebElement ele = null;
		String temp;
		String rowHeading;
		//System.out.println("readPageAndLoadCashFlowDataMC");
		// Click on cash flow Link
		ele = driver.findElement(By.xpath("//*[@id='slider']/dd[3]/ul/li[7]/a"));
		ele.click();
		try {
			Thread.sleep(4000);
		} catch (Exception ex) {
			System.out.println("Error in wait after cash flow click");
		}

		for (int counter = 2; counter <= 4; counter++) {
			for (int rowCounter = 3; rowCounter <= 12; rowCounter++) {
				ele = driver.findElement(By.xpath("//*[@id='mc_mainWrapper']/div[3]/div[2]/div[3]/div[2]/div[2]/div[2]/div[1]/table[2]/tbody/tr[" + rowCounter + "]/td[1]"));
				
				rowHeading = ele.getText();
				if(rowHeading!=null && rowHeading.equalsIgnoreCase("Net Inc/Dec In Cash And Cash Equivalents")) {
					ele = driver.findElement(By.xpath("//*[@id='mc_mainWrapper']/div[3]/div[2]/div[3]/div[2]/div[2]/div[2]/div[1]/table[2]/tbody/tr[" + rowCounter + "]/td[" + counter + "]"));
					temp = ele.getText().contains("--") ? "0" : ele.getText().replaceAll(",", "");
					objCompanyAnnualFinancialDataList.get(counter - 2).netCashFlow = Float.parseFloat(temp);
					
				}
			}
			
		}
		//System.out.println("readPageAndLoadCashFlowDataMC End");
		return objCompanyAnnualFinancialDataList;
	}

	private ArrayList<CompanyHalfYearlyFinancialData> readPageAndLoadHalfYearlyDataMC() {
		ArrayList<CompanyHalfYearlyFinancialData> objCompanyHalfYearlyFinancialDataList = new ArrayList<CompanyHalfYearlyFinancialData>();

		CompanyHalfYearlyFinancialData objCompanyHalfYearlyFinancialData = null;
		WebElement ele = null;
		String temp;
		String rowHeading;
		//System.out.println("readPageAndLoadHalfYearlyDataMC");
		// Click onhalf yearly Link
		ele = driver.findElement(By.xpath("//*[@id='slider']/dd[3]/ul/li[4]/a"));
		ele.click();
		try {
			Thread.sleep(4000);
		} catch (Exception ex) {
			System.out.println("Error in wait after old format click");
		}
		for (int counter = 2; counter <= 6; counter++) {
			objCompanyHalfYearlyFinancialData = new CompanyHalfYearlyFinancialData();
			// Get Year and Month
			ele = driver.findElement(By.xpath("//*[@id='mc_mainWrapper']/div[3]/div[2]/div[3]/div[2]/div[2]/div[2]/div[1]/table[2]/tbody/tr[3]/td[" + counter + "]"));
			temp = ele.getText();
			//System.out.println("Year -> " + temp);
			objCompanyHalfYearlyFinancialData.resultMonth = temp.split("'")[0].trim();
			objCompanyHalfYearlyFinancialData.resultYear = Integer.parseInt(temp.split("'")[1].trim());

			for (int rowCounter = 3; rowCounter <= 38; rowCounter++) {
				ele = driver.findElement(By.xpath("//*[@id='mc_mainWrapper']/div[3]/div[2]/div[3]/div[2]/div[2]/div[2]/div[1]/table[2]/tbody/tr[" + rowCounter + "]/td[1]"));
				rowHeading = ele.getText();

				// Net Sales
				if (rowHeading.contains("Net Sales")) {
					// Net Sales
					ele = driver.findElement(By.xpath("//*[@id='mc_mainWrapper']/div[3]/div[2]/div[3]/div[2]/div[2]/div[2]/div[1]/table[2]/tbody/tr[" + rowCounter + "]/td[" + counter + "]"));
					temp = ele.getText().contains("--") ? "0" : ele.getText().replaceAll(",", "");
					objCompanyHalfYearlyFinancialData.netSales = Double.parseDouble(temp);
				}
				if (rowHeading.contains("Other Operating Income")) {
					// Other Income
					ele = driver.findElement(By.xpath("//*[@id='mc_mainWrapper']/div[3]/div[2]/div[3]/div[2]/div[2]/div[2]/div[1]/table[2]/tbody/tr[" + rowCounter + "]/td[" + counter + "]"));
					temp = ele.getText().contains("--") ? "0" : ele.getText().replaceAll(",", "");
					objCompanyHalfYearlyFinancialData.otherIncome = Double.parseDouble(temp);
				}
				if (rowHeading.contains("P/L Before Other Inc. , Int., Excpt. Items & Tax")) {
					// pbdit
					ele = driver.findElement(By.xpath("//*[@id='mc_mainWrapper']/div[3]/div[2]/div[3]/div[2]/div[2]/div[2]/div[1]/table[2]/tbody/tr[" + rowCounter + "]/td[" + counter + "]"));
					temp = ele.getText().contains("--") ? "0" : ele.getText().replaceAll(",", "");
					objCompanyHalfYearlyFinancialData.pbdit = Double.parseDouble(temp);
				}
				if (rowHeading.contains("Interest")) {
					// interest
					ele = driver.findElement(By.xpath("//*[@id='mc_mainWrapper']/div[3]/div[2]/div[3]/div[2]/div[2]/div[2]/div[1]/table[2]/tbody/tr[" + rowCounter + "]/td[" + counter + "]"));
					temp = ele.getText().contains("--") ? "0" : ele.getText().replaceAll(",", "");
					objCompanyHalfYearlyFinancialData.interest = Double.parseDouble(temp);
				}
				if (rowHeading.contains("P/L Before Exceptional Items & Tax")) {
					// pbdt
					ele = driver.findElement(By.xpath("//*[@id='mc_mainWrapper']/div[3]/div[2]/div[3]/div[2]/div[2]/div[2]/div[1]/table[2]/tbody/tr[" + rowCounter + "]/td[" + counter + "]"));
					temp = ele.getText().contains("--") ? "0" : ele.getText().replaceAll(",", "");
					objCompanyHalfYearlyFinancialData.pbdt = Double.parseDouble(temp);
				}
				if (rowHeading.contains("P/L Before Tax")) {
					// profitBeforeTax
					ele = driver.findElement(By.xpath("//*[@id='mc_mainWrapper']/div[3]/div[2]/div[3]/div[2]/div[2]/div[2]/div[1]/table[2]/tbody/tr[" + rowCounter + "]/td[" + counter + "]"));
					temp = ele.getText().contains("--") ? "0" : ele.getText().replaceAll(",", "");
					objCompanyHalfYearlyFinancialData.profitBeforeTax = Double.parseDouble(temp);
				}
				if (rowHeading.contains("Net Profit/(Loss) For the Period")) {
					// netProfit
					ele = driver.findElement(By.xpath("//*[@id='mc_mainWrapper']/div[3]/div[2]/div[3]/div[2]/div[2]/div[2]/div[1]/table[2]/tbody/tr[" + rowCounter + "]/td[" + counter + "]"));
					temp = ele.getText().contains("--") ? "0" : ele.getText().replaceAll(",", "");
					objCompanyHalfYearlyFinancialData.netProfit = Double.parseDouble(temp);
				}
				if (rowHeading.equalsIgnoreCase("Basic EPS")) {
					// basicEPS
					ele = driver.findElement(By.xpath("//*[@id='mc_mainWrapper']/div[3]/div[2]/div[3]/div[2]/div[2]/div[2]/div[1]/table[2]/tbody/tr[" + rowCounter + "]/td[" + counter + "]"));
					temp = ele.getText().contains("--") ? "0" : ele.getText().replaceAll(",", "");
					objCompanyHalfYearlyFinancialData.basicEPS = Float.parseFloat(temp);
				}				
			}
			//System.out.println("Half Yearly -> " + objCompanyHalfYearlyFinancialData);
			objCompanyHalfYearlyFinancialDataList.add(objCompanyHalfYearlyFinancialData);
		}
		//System.out.println("readPageAndLoadHalfYearlyDataMC End");
		return objCompanyHalfYearlyFinancialDataList;
	}

	private ArrayList<CompanyQuarterFinancialData> readPageAndLoadQuarterlyDataMC() {
		ArrayList<CompanyQuarterFinancialData> objCompanyQuarterFinancialDataList = new ArrayList<CompanyQuarterFinancialData>();
		CompanyQuarterFinancialData objCompanyQuarterFinancialData = null;
		WebElement ele = null;
		String temp;
		String rowHeading;
		// Click on quarterly Link
		ele = driver.findElement(By.xpath("//*[@id='slider']/dd[3]/ul/li[3]/a"));
		ele.click();
		try {
			Thread.sleep(4000);
		} catch (Exception ex) {
			System.out.println("Error in wait after quaertrly click");
		}
		for (int counter = 2; counter <= 6; counter++) {
			objCompanyQuarterFinancialData = new CompanyQuarterFinancialData();
			// Get Year and Month
			ele = driver.findElement(By.xpath("//*[@id='mc_mainWrapper']/div[3]/div[2]/div[3]/div[2]/div[2]/div[2]/div[1]/table[2]/tbody/tr[3]/td[" + counter + "]"));

			temp = ele.getText();
			//System.out.println("Year -> " + temp);
			objCompanyQuarterFinancialData.resultMonth = temp.split("'")[0].trim();
			objCompanyQuarterFinancialData.resultYear = Integer.parseInt(temp.split("'")[1].trim());

			for (int rowCounter = 3; rowCounter <= 45; rowCounter++) {
				ele = driver.findElement(By.xpath("//*[@id='mc_mainWrapper']/div[3]/div[2]/div[3]/div[2]/div[2]/div[2]/div[1]/table[2]/tbody/tr[" + rowCounter + "]/td[1]"));
				rowHeading = ele.getText();

				// Net Sales
				if (rowHeading.contains("Net Sales")) {
					// Net Sales
					ele = driver.findElement(By.xpath("//*[@id='mc_mainWrapper']/div[3]/div[2]/div[3]/div[2]/div[2]/div[2]/div[1]/table[2]/tbody/tr[" + rowCounter + "]/td[" + counter + "]"));
					temp = ele.getText().contains("--") ? "0" : ele.getText().replaceAll(",", "");
					objCompanyQuarterFinancialData.netSales = Double.parseDouble(temp);
				}
				if (rowHeading.contains("Other Operating Income")) {
					// Other Income
					ele = driver.findElement(By.xpath("//*[@id='mc_mainWrapper']/div[3]/div[2]/div[3]/div[2]/div[2]/div[2]/div[1]/table[2]/tbody/tr[" + rowCounter + "]/td[" + counter + "]"));
					temp = ele.getText().contains("--") ? "0" : ele.getText().replaceAll(",", "");
					objCompanyQuarterFinancialData.otherIncome = Double.parseDouble(temp);
				}
				if (rowHeading.contains("P/L Before Other Inc. , Int., Excpt. Items & Tax")) {
					// pbdit
					ele = driver.findElement(By.xpath("//*[@id='mc_mainWrapper']/div[3]/div[2]/div[3]/div[2]/div[2]/div[2]/div[1]/table[2]/tbody/tr[" + rowCounter + "]/td[" + counter + "]"));
					temp = ele.getText().contains("--") ? "0" : ele.getText().replaceAll(",", "");
					objCompanyQuarterFinancialData.pbdit = Double.parseDouble(temp);
				}
				if (rowHeading.contains("Interest")) {
					// interest
					ele = driver.findElement(By.xpath("//*[@id='mc_mainWrapper']/div[3]/div[2]/div[3]/div[2]/div[2]/div[2]/div[1]/table[2]/tbody/tr[" + rowCounter + "]/td[" + counter + "]"));
					temp = ele.getText().contains("--") ? "0" : ele.getText().replaceAll(",", "");
					objCompanyQuarterFinancialData.interest = Double.parseDouble(temp);
				}
				if (rowHeading.contains("P/L Before Exceptional Items & Tax")) {
					// pbdt
					ele = driver.findElement(By.xpath("//*[@id='mc_mainWrapper']/div[3]/div[2]/div[3]/div[2]/div[2]/div[2]/div[1]/table[2]/tbody/tr[" + rowCounter + "]/td[" + counter + "]"));
					temp = ele.getText().contains("--") ? "0" : ele.getText().replaceAll(",", "");
					objCompanyQuarterFinancialData.pbdt = Double.parseDouble(temp);
				}
				if (rowHeading.contains("P/L Before Tax")) {
					// profitBeforeTax
					ele = driver.findElement(By.xpath("//*[@id='mc_mainWrapper']/div[3]/div[2]/div[3]/div[2]/div[2]/div[2]/div[1]/table[2]/tbody/tr[" + rowCounter + "]/td[" + counter + "]"));
					temp = ele.getText().contains("--") ? "0" : ele.getText().replaceAll(",", "");
					objCompanyQuarterFinancialData.profitBeforeTax = Double.parseDouble(temp);
				}
				if (rowHeading.contains("Net Profit/(Loss) For the Period")) {
					// netProfit
					ele = driver.findElement(By.xpath("//*[@id='mc_mainWrapper']/div[3]/div[2]/div[3]/div[2]/div[2]/div[2]/div[1]/table[2]/tbody/tr[" + rowCounter + "]/td[" + counter + "]"));
					temp = ele.getText().contains("--") ? "0" : ele.getText().replaceAll(",", "");
					objCompanyQuarterFinancialData.netProfit = Double.parseDouble(temp);
				}
				if (rowHeading.contains("Basic EPS")) {
					// basicEPS
					ele = driver.findElement(By.xpath("//*[@id='mc_mainWrapper']/div[3]/div[2]/div[3]/div[2]/div[2]/div[2]/div[1]/table[2]/tbody/tr[" + rowCounter + "]/td[" + counter + "]"));
					temp = ele.getText().contains("--") ? "0" : ele.getText().replaceAll(",", "");
					objCompanyQuarterFinancialData.basicEPS = Float.parseFloat(temp);
				}				
			}	
			//System.out.println("Quarter -> " + objCompanyQuarterFinancialData);
			objCompanyQuarterFinancialDataList.add(objCompanyQuarterFinancialData);
		}
		return objCompanyQuarterFinancialDataList;
	}

	private StockFinancialData loadObjectWithFinancialDataNSE() {
		StockFinancialData objStockFinancialData = new StockFinancialData();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Thread.sleep(2000);
		} catch (Exception ex) {
			System.out.println("Error in waiting for drop down suggestion");
		}
		WebElement ele = null;
		// ele =
		// driver.findElement(By.xpath("//*[@id='ext-gen154']/table/tbody/tr/td/table/tbody/tr/td[2]/center/table/tbody/tr[4]/td[2]"));
		ele = driver.findElement(By.xpath("//html/body/div[11]/div[2]/div/div/div/div/table/tbody/tr/td/table/tbody/tr/td[2]/center/table/tbody/tr[4]/td[2]"));
		objStockFinancialData.statementPeriod = ele.getText();
		ele = driver.findElement(By.xpath("/html/body/div[11]/div[2]/div/div/div/div/table/tbody/tr/td/table/tbody/tr/td[2]/center/table/tbody/tr[5]/td[4]"));
		objStockFinancialData.periodEnded = new Date(ele.getText());
		ele = driver.findElement(By.xpath("/html/body/div[11]/div[2]/div/div/div/div/table/tbody/tr/td/table/tbody/tr/td[2]/table/tbody/tr/td[1]/table/tbody/tr/td/table/tbody/tr[4]/td[2]"));
		objStockFinancialData.netSales = Float.parseFloat(ele.getText());
		ele = driver.findElement(By.xpath("/html/body/div[11]/div[2]/div/div/div/div/table/tbody/tr/td/table/tbody/tr/td[2]/table/tbody/tr/td[1]/table/tbody/tr/td/table/tbody/tr[5]/td[2]"));
		objStockFinancialData.otherOperatingIncome = Float.parseFloat(ele.getText());
		ele = driver.findElement(By.xpath("/html/body/div[11]/div[2]/div/div/div/div/table/tbody/tr/td/table/tbody/tr/td[2]/table/tbody/tr/td[1]/table/tbody/tr/td/table/tbody/tr[24]/td[2]"));
		objStockFinancialData.netProfit = Float.parseFloat(ele.getText());
		ele = driver.findElement(By.xpath("/html/body/div[11]/div[2]/div/div/div/div/table/tbody/tr/td/table/tbody/tr/td[2]/table/tbody/tr/td[1]/table/tbody/tr/td/table/tbody/tr[26]/td[2]"));
		objStockFinancialData.netProfitAfterTaxes = Float.parseFloat(ele.getText());

		return objStockFinancialData;
	}

	private ArrayList<String> getProcessedStockList() {

		Connection connection = null;
		ResultSet resultSet = null;
		Statement statement = null;
		ArrayList<String> stockList = null;
		String stockBSECode;
		
		try {
			stockList = new ArrayList<String>();
			connection = StockUtils.connectToDB();
			statement = connection.createStatement();

			resultSet = statement.executeQuery("SELECT STOCKCODE FROM PROCESSEDSTOCKS;");
			while (resultSet.next()) {
				stockBSECode = resultSet.getString(1);				
				stockList.add(stockBSECode);
				// System.out.println("StockNme - " + stockNSECode);
			}
			return stockList;
		} catch (Exception ex) {
			HandleErrorDetails.addError(StockUtils.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("Error in getProcessedStockList "+ex);
			return null;
		} finally {
			try {
				if(resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(StockUtils.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getProcessedStockList Error in closing resultset "+ex);
				logger.error("Error in closing resultset getProcessedStockList  -> ", ex);
			}
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(StockUtils.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getProcessedStockList Error in closing statement "+ex);
				logger.error("Error in closing statement getProcessedStockList  -> ", ex);
			}
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				} 
			} catch (Exception ex) {
				HandleErrorDetails.addError(StockUtils.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getProcessedStockList Error in closing connection "+ex);
				logger.error("Error in closing connection getProcessedStockList  -> ", ex);
			}
		}
	}
	
	private ArrayList<String> getErroredStockList() {

		Connection connection = null;
		ResultSet resultSet = null;
		Statement statement = null;
		ArrayList<String> stockList = null;
		String stockBSECode;
		
		try {
			stockList = new ArrayList<String>();
			connection = StockUtils.connectToDB();
			statement = connection.createStatement();

			resultSet = statement.executeQuery("SELECT STOCKCODE FROM ERRORSTOCKS;");
			while (resultSet.next()) {
				stockBSECode = resultSet.getString(1);				
				stockList.add(stockBSECode);
				// System.out.println("StockNme - " + stockNSECode);
			}
			return stockList;
		} catch (Exception ex) {
			HandleErrorDetails.addError(StockUtils.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("Error in getErroredStockList "+ex);
			return null;
		} finally {
			try {
				if(resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(StockUtils.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getErroredStockList Error in closing resultset "+ex);
				logger.error("Error in closing resultset getErroredStockList  -> ", ex);
			}
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(StockUtils.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getErroredStockList Error in closing statement "+ex);
				logger.error("Error in closing statement getErroredStockList  -> ", ex);
			}
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				} 
			} catch (Exception ex) {
				HandleErrorDetails.addError(StockUtils.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getErroredStockList Error in closing connection "+ex);
				logger.error("Error in closing connection getErroredStockList  -> ", ex);
			}
		}
	}
	
	
	private void storeProcessedStockList(ArrayList<String> stockList) {

		Connection connection = null;
		ResultSet resultSet = null;
		Statement statement = null;
		
		try {
			//stockList = new ArrayList<String>();
			connection = StockUtils.connectToDB();
			statement = connection.createStatement();
			for (String stockCode : stockList) {
				statement.executeUpdate("INSERT INTO PROCESSEDSTOCKS (STOCKCODE) VALUES ('" + stockCode + "');");
			}
		} catch (Exception ex) {
			HandleErrorDetails.addError(StockUtils.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("Error in storing procese stocks");
		} finally {
			try {
				if(resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(StockUtils.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("storeProcessedStockList Error in closing resultset "+ex);
				logger.error("Error in closing resultset storeProcessedStockList  -> ", ex);
			}
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(StockUtils.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("storeProcessedStockList Error in closing statement "+ex);
				logger.error("Error in closing statement storeProcessedStockList  -> ", ex);
			}
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				} 
			} catch (Exception ex) {
				HandleErrorDetails.addError(StockUtils.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("storeProcessedStockList Error in closing connection "+ex);
				logger.error("Error in closing connection storeProcessedStockList  -> ", ex);
			}
		}
	}
	
	private void storeErroredStockList(ArrayList<String> stockList) {

		Connection connection = null;
		ResultSet resultSet = null;
		Statement statement = null;
		
		try {
			//stockList = new ArrayList<String>();
			connection = StockUtils.connectToDB();
			statement = connection.createStatement();
			for (String stockCode : stockList) {
				statement.executeUpdate("INSERT INTO ERRORSTOCKS (STOCKCODE) VALUES ('" + stockCode + "');");
			}
		} catch (Exception ex) {
			HandleErrorDetails.addError(StockUtils.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("Error in storing error stocks");
		} finally {
			try {
				if(resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(StockUtils.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("storeErroredStockList Error in closing resultset "+ex);
				logger.error("Error in closing resultset storeErroredStockList  -> ", ex);
			}
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(StockUtils.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("storeErroredStockList Error in closing statement "+ex);
				logger.error("Error in closing statement storeErroredStockList  -> ", ex);
			}
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				} 
			} catch (Exception ex) {
				HandleErrorDetails.addError(StockUtils.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("storeErroredStockList Error in closing connection "+ex);
				logger.error("Error in closing connection storeErroredStockList  -> ", ex);
			}
		}
	}
	
	private void storeCompanyFinancialData(CompanyFinancialData objCompanyFinancialData) {
		//Connection connection = null;
		try {
			connection = StockUtils.connectToDB();
			storeComapnyAnnualFinacialData(objCompanyFinancialData.nseCode, objCompanyFinancialData.objCompanyAnnualFinancialDataList);
			/*commenting to only collect yearly data on first go
			storeComapnyHalfYearlyFinacialData(objCompanyFinancialData.nseCode, objCompanyFinancialData.objCompanyHalfYearlyFinancialDataList);
			storeComapnyQuarterlyFinacialData(objCompanyFinancialData.nseCode, objCompanyFinancialData.objCompanyQuarterFinancialDataList);*/
		} catch (Exception ex) {
			HandleErrorDetails.addError(StockUtils.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("Error in storing finacial data of company");
			
		} finally {
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				} 
			} catch (Exception ex) {
				HandleErrorDetails.addError(StockUtils.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getProcessedStockList Error in closing connection "+ex);
				logger.error("Error in closing connection getProcessedStockList  -> ", ex);
			}
		}
	}
	
	private void storeComapnyAnnualFinacialData(String stockCode, ArrayList<CompanyAnnualFinancialData> objCompanyAnnualFinancialDataList) {
		ResultSet resultSet = null;
		Statement statement = null;	
		String tmpSQL;
		try {			
			statement = connection.createStatement();
			for (CompanyAnnualFinancialData objCompanyAnnualFinancialData : objCompanyAnnualFinancialDataList) {
				tmpSQL = "INSERT INTO STOCKANNUALFINANCIALDATA (STOCKCODE,resultMonth,resultYear,netSales,otherIncome,totalExpenses,operatingProfit,pbdit,interest,pbdt,profitBeforeTax,netProfit,bookValue,equitiDividendRatio,basicEPS,netCashFlow) " +
						" VALUES('"+stockCode+"','"+ objCompanyAnnualFinancialData.resultMonth +"',"+ objCompanyAnnualFinancialData.resultYear +", "+ objCompanyAnnualFinancialData.netSales +", " +
						objCompanyAnnualFinancialData.otherIncome +", "+ objCompanyAnnualFinancialData.totalExpenses +", "+ objCompanyAnnualFinancialData.operatingProfit +", "+ objCompanyAnnualFinancialData.pbdit + ", "+
						objCompanyAnnualFinancialData.interest +", "+ objCompanyAnnualFinancialData.pbdt +", "+ objCompanyAnnualFinancialData.profitBeforeTax +", "+ objCompanyAnnualFinancialData.netProfit +", "+
						objCompanyAnnualFinancialData.bookValue +", "+ objCompanyAnnualFinancialData.equitiDividendRatio +", "+ objCompanyAnnualFinancialData.basicEPS +", "+
						objCompanyAnnualFinancialData.netCashFlow +");"; 
						
				statement.executeUpdate(tmpSQL);
			}
		} catch (Exception ex) {
			HandleErrorDetails.addError(StockUtils.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("Error in storeComapnyAnnualFinacialData"+ex);
			
		} finally {
			try {
				if(resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(StockUtils.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("storeComapnyAnnualFinacialData Error in closing resultset "+ex);
				logger.error("Error in closing resultset storeComapnyAnnualFinacialData  -> ", ex);
			}
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(StockUtils.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("storeComapnyAnnualFinacialData Error in closing statement "+ex);
				logger.error("Error in closing statement storeComapnyAnnualFinacialData  -> ", ex);
			}
		}
	}
	
	private void storeComapnyHalfYearlyFinacialData(String stockCode, ArrayList<CompanyHalfYearlyFinancialData> objCompanyHalfYearlyFinancialDataList) {
		ResultSet resultSet = null;
		Statement statement = null;	
		String tmpSQL;
		try {			
			statement = connection.createStatement();
			for (CompanyHalfYearlyFinancialData objCompanyHalfYearlyFinancialData : objCompanyHalfYearlyFinancialDataList) {
				tmpSQL = "INSERT INTO STOCKHALFYEARLYFINANCIALDATA (STOCKCODE,resultMonth,resultYear,netSales,otherIncome,pbdit,interest,pbdt,profitBeforeTax,netProfit,basicEPS) " +
						" VALUES('"+stockCode+"','"+ objCompanyHalfYearlyFinancialData.resultMonth +"',"+ objCompanyHalfYearlyFinancialData.resultYear +", "+ objCompanyHalfYearlyFinancialData.netSales +", " +
						objCompanyHalfYearlyFinancialData.otherIncome +", "+ objCompanyHalfYearlyFinancialData.pbdit + ", "+
						objCompanyHalfYearlyFinancialData.interest +", "+ objCompanyHalfYearlyFinancialData.pbdt +", "+ objCompanyHalfYearlyFinancialData.profitBeforeTax +", "+ objCompanyHalfYearlyFinancialData.netProfit +", "+
						objCompanyHalfYearlyFinancialData.basicEPS +");"; 
						
				statement.executeUpdate(tmpSQL);
			}
		} catch (Exception ex) {
			HandleErrorDetails.addError(StockUtils.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("Error in storeComapnyHalfYearlyFinacialData"+ex);
			
		} finally {
			try {
				if(resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(StockUtils.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("storeComapnyHalfYearlyFinacialData Error in closing resultset "+ex);
				logger.error("Error in closing resultset storeComapnyHalfYearlyFinacialData  -> ", ex);
			}
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(StockUtils.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("storeComapnyHalfYearlyFinacialData Error in closing statement "+ex);
				logger.error("Error in closing statement storeComapnyHalfYearlyFinacialData  -> ", ex);
			}
		}
	}
	
	private void storeComapnyQuarterlyFinacialData(String stockCode, ArrayList<CompanyQuarterFinancialData> objCompanyQuarterFinancialDataList) {
		ResultSet resultSet = null;
		Statement statement = null;	
		String tmpSQL;
		try {			
			statement = connection.createStatement();
			for (CompanyQuarterFinancialData objCompanyQuarterFinancialData : objCompanyQuarterFinancialDataList) {
				tmpSQL = "INSERT INTO STOCKQUARTERLYFINANCIALDATA (STOCKCODE,resultMonth,resultYear,netSales,otherIncome,pbdit,interest,pbdt,profitBeforeTax,netProfit,basicEPS) " +
						" VALUES('"+stockCode+"','"+ objCompanyQuarterFinancialData.resultMonth +"',"+ objCompanyQuarterFinancialData.resultYear +", "+ objCompanyQuarterFinancialData.netSales +", " +
						objCompanyQuarterFinancialData.otherIncome +", "+ objCompanyQuarterFinancialData.pbdit + ", "+
						objCompanyQuarterFinancialData.interest +", "+ objCompanyQuarterFinancialData.pbdt +", "+ objCompanyQuarterFinancialData.profitBeforeTax +", "+ objCompanyQuarterFinancialData.netProfit +", "+
						objCompanyQuarterFinancialData.basicEPS +");"; 
						
				statement.executeUpdate(tmpSQL);
			}
		} catch (Exception ex) {
			HandleErrorDetails.addError(StockUtils.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("Error in storeComapnyQuarterlyFinacialData"+ex);
			
		} finally {
			try {
				if(resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(StockUtils.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("storeComapnyQuarterlyFinacialData Error in closing resultset "+ex);
				logger.error("Error in closing resultset storeComapnyQuarterlyFinacialData  -> ", ex);
			}
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(StockUtils.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("storeComapnyQuarterlyFinacialData Error in closing statement "+ex);
				logger.error("Error in closing statement storeComapnyQuarterlyFinacialData  -> ", ex);
			}
		}
	}
}
