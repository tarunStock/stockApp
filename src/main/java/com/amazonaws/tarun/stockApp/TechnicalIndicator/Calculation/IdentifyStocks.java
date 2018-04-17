package com.amazonaws.tarun.stockApp.TechnicalIndicator.Calculation;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import org.apache.log4j.Logger;

import com.amazonaws.tarun.stockApp.TechnicalIndicator.Data.CompareBearishStockForAllIndicators;
import com.amazonaws.tarun.stockApp.TechnicalIndicator.Data.CompareBullishStockForAllIndicators;
import com.amazonaws.tarun.stockApp.TechnicalIndicator.Data.SMAIndicatorDetails;
import com.amazonaws.tarun.stockApp.TechnicalIndicator.Data.StockDataForNewApproach;
import com.amazonaws.tarun.stockApp.Utils.HandleErrorDetails;
import com.amazonaws.tarun.stockApp.Utils.SalesforceIntegration;
import com.amazonaws.tarun.stockApp.Utils.StockUtils;

public class IdentifyStocks {
	Connection connection = null;
	static Logger logger = Logger.getLogger(IdentifyStocks.class);
	
	public static void main(String[] args) {
		Date dte = new Date();
		System.out.println("Start at -> " + dte.toString());
		IdentifyStocks obj = new IdentifyStocks();
		
		//To get indication from MACD
		obj.CalculateIndication(new Date("16-Apr-2018"));
		//obj.CalculateIndicationfromMACD(new Date("26-Mar-2018"));		
		//To calculate MACD values and store
		//obj.calculateSignalAndMACDBulkForAllStocks(new Date("25-Jan-2018"));
	}
	
	public void CalculateIndication(Date calculationDate) {
		logger.debug("CalculateIndication start");
		ArrayList<String> stocklist = null;
		String stock, bseCode, stockName;
		int totalSelectedStocks=0, totalBelowHundredSelectedStocks = 0, macdcrossedStocks = 0;
		ArrayList<StockDataForNewApproach> objFinalSelectedStockList = new ArrayList<StockDataForNewApproach>();
		ArrayList<StockDataForNewApproach> objBelowHundredFinalSelectedStockList = new ArrayList<StockDataForNewApproach>();
		ArrayList<StockDataForNewApproach> objFinalSelectedBearishStockList = new ArrayList<StockDataForNewApproach>();
		StockDataForNewApproach objFinalSelectedStock = null;
		StockDataForNewApproach objFinalSelectedStockBelowHundred = null;
		StockDataForNewApproach objFinalSelectedStockBearish = null;
		SMAIndicatorDetails objSMAIndicatorDetails;
		GenerateIndicationfromMovingAverage objSMA = new GenerateIndicationfromMovingAverage();
		GenerateIndicationFromMACDV1 objMACD = new GenerateIndicationFromMACDV1();
		AccumulationDistribution objACDLIne = new AccumulationDistribution();
		CalculateBollingerBands objCalculateBollingerBands = new CalculateBollingerBands();
		CalculateRSIIndicator objCalculateRSIIndicator = new CalculateRSIIndicator();
		CalculateFibonacciRetracements obj = new CalculateFibonacciRetracements();
		CalculateAverageTrueRange objCalculateAverageTrueRange = new CalculateAverageTrueRange();
		CalculateStochasticOscillator objCalculateStochasticOscillator = new CalculateStochasticOscillator();
		Connection connection = null;
		
		if(!StockUtils.marketOpenOnGivenDate(calculationDate))
			return;
		
		try {
			connection = StockUtils.connectToDB();
			stocklist = StockUtils.getStockListFromDB(connection);
			for (String stockCode : stocklist) {
				stockName = stockCode.split("!")[1];
				bseCode = stockCode.split("!")[0];
				stock = stockCode.split("!")[2];
				
//				if(!StockUtils.getFinancialIndication(stock)) {
//					continue;
//				}
				//stock="FSL";
				System.out.println("Analyzing Stock -> "+stock);
				objFinalSelectedStock = new StockDataForNewApproach();
				objFinalSelectedStock.stockCode = stock;
				
				//Simple Moving Average data creation 
				objSMAIndicatorDetails = objSMA.CalculateIndicationfromSMA(connection, stock, calculationDate);				
				if(objSMAIndicatorDetails.signalSMAToSMA!=null && objSMAIndicatorDetails.signalSMAToSMA.equalsIgnoreCase("buy")) {
					objFinalSelectedStock.SMAComparison = "Crossed";
				} else {
					objFinalSelectedStock.SMAComparison = "NotCrossed";
				}
					
				objFinalSelectedStock.middleSMAIncreasing = objSMAIndicatorDetails.IsMiddleSMGrowing;
				objFinalSelectedStock.smallerSMAIncreasing = objSMAIndicatorDetails.IsSmallerSMGrowing;
				if(objSMAIndicatorDetails.signalPriceToSMA!=null && objSMAIndicatorDetails.signalPriceToSMA.equalsIgnoreCase("buy")) {
					objFinalSelectedStock.SMAToPriceComparison = "Crossed";
				} else {
					objFinalSelectedStock.SMAToPriceComparison = "NotCrossed";
				}
				 
				objFinalSelectedStock.lowerToHigherSMcrossover = objSMAIndicatorDetails.lowerToHigherSMcrossover;
				objFinalSelectedStock.lowerToMiddleSMcrossover = objSMAIndicatorDetails.lowerToMiddleSMcrossover;
				objFinalSelectedStock.priceToMiddleSMcrossover = objSMAIndicatorDetails.PNSMAcrossover;
				objFinalSelectedStock.suggestedDate = objSMAIndicatorDetails.signalDate;
				
				//MACD data creation
				if(objMACD.isSignalCrossedInMACD(connection, stock, calculationDate)) {
					objFinalSelectedStock.MACDStatus = "Crossed";
					macdcrossedStocks++;
				} else {
					objFinalSelectedStock.MACDStatus = "NotCrossed";
				}
				objFinalSelectedStock.MACDIncreasing = objMACD.isMACDIncreasing(connection, stock, calculationDate);
				
				//Accumulation Distribution Line
				objFinalSelectedStock.accumulationDistributionIncreasing = objACDLIne.isAccumulationDistributionIncreasing(connection, stock, calculationDate);
				
				//BBTRend
				objFinalSelectedStock.BBTrend = objCalculateBollingerBands.getBBIndicationForStockV1(connection, stock, calculationDate);
				
				//Chandleir exit				
				objFinalSelectedStock.ChandelierExit = objCalculateAverageTrueRange.getChandelierExitLong(connection, stock, calculationDate);
				
				//Support and resistance values 				
				ArrayList<Double> supportAndResistanceValues = obj.FibonacciRetracements(connection, stock, calculationDate);
				if(supportAndResistanceValues!=null) {
					objFinalSelectedStock.supportLevel = supportAndResistanceValues.get(0);
					objFinalSelectedStock.resistanceLevel = supportAndResistanceValues.get(1);
				}
				//RSI				
				objFinalSelectedStock.RSIValue = objCalculateRSIIndicator.getRSIValue(stock, objSMAIndicatorDetails.signalDate);
				
				/*//Stochastic Oscillator
				if(stock.equalsIgnoreCase("PRAENG")) {
					System.out.println("test");
				}*/
				objFinalSelectedStock.stochasticInLine = objCalculateStochasticOscillator.getStochasticIndicator(connection, stock, calculationDate);
				
				//System.out.println("calculation done -> "+objFinalSelectedStock);
				/*try {
					Thread.sleep(3000);
				} catch(Exception ex) {
					HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
					System.out.println("Error in waiting for drop down suggestion");
				}*/
				if(objFinalSelectedStock.MACDIncreasing && objFinalSelectedStock.accumulationDistributionIncreasing &&
						objFinalSelectedStock.middleSMAIncreasing && objFinalSelectedStock.smallerSMAIncreasing && objFinalSelectedStock.stochasticInLine) {
					objFinalSelectedStock = (StockDataForNewApproach) StockUtils.getPriceAndVolumeDetails(objFinalSelectedStock,calculationDate);
					objFinalSelectedStock.TypeofSuggestedStock = "All";
					System.out.println("***** Adding Stock -> "+stock);
					objFinalSelectedStockList.add(objFinalSelectedStock);
					objFinalSelectedStockBelowHundred = (StockDataForNewApproach) objFinalSelectedStock.clone();
					objFinalSelectedStockBearish = (StockDataForNewApproach) objFinalSelectedStock.clone();
					objFinalSelectedStockBearish.TypeofSuggestedStock = "BB Contracting";
					objFinalSelectedBearishStockList.add(objFinalSelectedStockBearish);
					totalSelectedStocks++;
					System.out.println(totalSelectedStocks + "***** Adding Stock -> "+stock);
					if(objFinalSelectedStock.CurrentPrice<100) {
						objFinalSelectedStockBelowHundred.TypeofSuggestedStock = "Below 100";
						objBelowHundredFinalSelectedStockList.add(objFinalSelectedStockBelowHundred);
						 totalBelowHundredSelectedStocks++;
						 System.out.println(totalBelowHundredSelectedStocks + "***** Adding below 100 Stock -> "+stock);
					}
				}
			}
			
			
			Collections.sort(objFinalSelectedStockList, new CompareBullishStockForAllIndicators());
			Collections.sort(objBelowHundredFinalSelectedStockList, new CompareBullishStockForAllIndicators());
			Collections.sort(objFinalSelectedBearishStockList, new CompareBearishStockForAllIndicators());
			SalesforceIntegration objSalesforceIntegration = new SalesforceIntegration();
			System.out.println("Final selected stocks - > "+ objFinalSelectedStockList);
			objSalesforceIntegration.connectToSalesforc();
			objSalesforceIntegration.createSuggestedStocks1(objFinalSelectedStockList);
			objSalesforceIntegration.createSuggestedStocks1(objBelowHundredFinalSelectedStockList);
			//objSalesforceIntegration.createSuggestedStocks1(objFinalSelectedBearishStockList);
			System.out.println("Total Selected stocks -> "+totalSelectedStocks);
			System.out.println("Total Selected stocks below hundred -> "+totalBelowHundredSelectedStocks);
			System.out.println("MACD Crossed stocks count " + macdcrossedStocks);
		}catch (Exception ex) {
			HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("CalculateIndicationfromMACD Error in DB action"+ex);
			logger.error("Error in CalculateIndicationfromMACD  -> ", ex);
		} finally {
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				} 
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getRSIValue Error in closing connection "+ex);
				logger.error("Error in closing connection getRSIValue  -> ", ex);
			}
		}
		
		
		
		//tmpUpdateIndicatedStocks.updateSMAIndication(SMAIndicatorDetailsList);
		logger.debug("CalculateIndicationfromMACD end");
	}
}
