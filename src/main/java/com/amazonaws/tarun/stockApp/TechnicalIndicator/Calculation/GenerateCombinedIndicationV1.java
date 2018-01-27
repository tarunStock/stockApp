package com.amazonaws.tarun.stockApp.TechnicalIndicator.Calculation;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

import com.amazonaws.tarun.stockApp.TechnicalIndicator.Data.FinalSelectedStock;
import com.amazonaws.tarun.stockApp.TechnicalIndicator.Data.SMAIndicatorDetails;
import com.amazonaws.tarun.stockApp.TechnicalIndicator.Data.StockDetailsForDecision;
import com.amazonaws.tarun.stockApp.Utils.HandleErrorDetails;
import com.amazonaws.tarun.stockApp.Utils.SalesforceIntegration;
import com.amazonaws.tarun.stockApp.Utils.StockUtils;

public class GenerateCombinedIndicationV1 {
	
	public static int daysToCheck = 5;
	public static String YAHOO_URL = "https://in.finance.yahoo.com/chart/";
	
	SMAIndicatorDetails objSMAIndicatorDetails;
	String stockName;
	String bseCode;
	String nseCode;
	static Logger logger = Logger.getLogger(GenerateCombinedIndicationV1.class);
	
	public static void main(String[] args) {
		Date dte = new Date();
		System.out.println("Start at -> " + dte.toString());
		GenerateCombinedIndicationV1 obj = new GenerateCombinedIndicationV1();
		//obj.generateCombinedIndicationForStocks(new Date("29-Dec-2017"));
		obj.generateCombinedIndicationForStocks(null);
	}

	public void generateCombinedIndicationForStocks(Date calculationDate) {
		logger.debug("generateCombinedIndicationForStocks Started");
		int selectedCounter=0;
		ArrayList<SMAIndicatorDetails> SMAIndicatorDetailsList;
		ArrayList<SMAIndicatorDetails> SMAIndicatorDetailsBelowHundredList;
		if( !StockUtils.marketOpenOnGivenDate(calculationDate)) {
			System.out.println("Returned due to weekend");
			return;
		}			
		ArrayList<StockDetailsForDecision> objFinalSelectedStockList = new ArrayList<StockDetailsForDecision>();
		ArrayList<StockDetailsForDecision> objFinalSelectedBelowHundredStockList = new ArrayList<StockDetailsForDecision>();
		ArrayList<StockDetailsForDecision> objFinalSelectedStockListWithLowRSI = new ArrayList<StockDetailsForDecision>();
		ArrayList<StockDetailsForDecision> objFinalSelectedBelowHundredStockListWithLowRSI = new ArrayList<StockDetailsForDecision>();
		StockDetailsForDecision objFinalSelectedStock = null;
		StockDetailsForDecision objFinalSelectedBelowHunderdStock;
		System.out.println("********* - Get seleted stocks based on SMA");
		GenerateIndicationfromMovingAverage obj = new GenerateIndicationfromMovingAverage();
		obj.CalculateIndicationfromSMA(calculationDate);
		SMAIndicatorDetailsList = obj.getIndicationStocks();
		SMAIndicatorDetailsBelowHundredList = obj.getBelowHunderdIndicationStocks();
		System.out.println("********* - process seleted stocks to send mail");
		for(int counter = 0; counter<SMAIndicatorDetailsList.size(); counter++){			
			//add selected stock				
			objFinalSelectedStock = getAlldetails(SMAIndicatorDetailsList.get(counter), calculationDate);
			if(objFinalSelectedStock!=null) {
				objFinalSelectedStockList.add(objFinalSelectedStock);
				selectedCounter = selectedCounter +1;
				if(selectedCounter==20) {
					break;
				}
			} 
		} 
		
		SalesforceIntegration objSalesforceIntegration = new SalesforceIntegration();
		
		objSalesforceIntegration.connectToSalesforc();
		objSalesforceIntegration.createSuggestedStocks(objFinalSelectedStockList);
		
		/*
		//Send top stock in mail
		sendTopStockInMail(objFinalSelectedStockList, false, "Combined -> Stocklist on ");
		//CreateWatchListForTopStock(objFinalSelectedStockList, false);
		System.out.println("********* - process below hundred seleted stocks to send mail");
		selectedCounter = 0;
		for(int counter = 0; counter<SMAIndicatorDetailsBelowHundredList.size(); counter++){
			objFinalSelectedBelowHunderdStock = new FinalSelectedStock();
			objFinalSelectedBelowHunderdStock.stockCode = SMAIndicatorDetailsBelowHundredList.get(counter).stockCode;
			if(objFinalSelectedStockList.contains(objFinalSelectedBelowHunderdStock)) {
				objFinalSelectedBelowHunderdStock = objFinalSelectedStockList.get(objFinalSelectedStockList.indexOf(objFinalSelectedBelowHunderdStock));
				objFinalSelectedBelowHundredStockList.add(objFinalSelectedBelowHunderdStock);
				selectedCounter = selectedCounter +1;
				if(selectedCounter==20) {
					break;
				}
			} else {
				objFinalSelectedBelowHunderdStock = getAlldetails(SMAIndicatorDetailsBelowHundredList.get(counter), calculationDate);						
				if(objFinalSelectedBelowHunderdStock!=null) {
					objFinalSelectedBelowHundredStockList.add(objFinalSelectedBelowHunderdStock);
					selectedCounter = selectedCounter +1;
					if(selectedCounter==20) {
						break;
					}
				} 
			}			
		}
		//Send top below 100 stock in mail
		sendTopStockInMail(objFinalSelectedBelowHundredStockList, true, "Combined -> Below 100 Stocklist on ");
		//CreateWatchListForTopStock(objFinalSelectedBelowHundredStockList, true);
		//Get Low RSI stocks
		objFinalSelectedStockListWithLowRSI = getLowRSIStocks(SMAIndicatorDetailsList, calculationDate);
		//Send top low RSI stock in mail
		sendTopStockInMail(objFinalSelectedStockListWithLowRSI, true, "Combined -> Low RSI Stocklist on ");
		//Get Low RSI below 100 stocks
		objFinalSelectedBelowHundredStockListWithLowRSI = getLowRSIStocks(SMAIndicatorDetailsBelowHundredList, calculationDate);
		//Send top low RSI beow 100 stock in mail
		sendTopStockInMail(objFinalSelectedBelowHundredStockListWithLowRSI, true, "Combined -> Low RSI below 100 Stocklist on ");*/
		logger.debug("generateCombinedIndicationForStocks End");
	}
	
	
	
	
	private StockDetailsForDecision getAlldetails (SMAIndicatorDetails objSMAIndicatorDetails, Date calculationDate) {
		StockDetailsForDecision objFinalSelectedStock = null;
		CalculateOnBalanceVolume objCalculateOnBalanceVolume;
		CalculateBollingerBands objCalculateBollingerBands;
		CalculateRSIIndicator objCalculateRSIIndicator;
		CalculateStochasticOscillator objCalculateStochasticOscillator;
			
		String bbIndicator;
		float rsiIndication;
		float chandelierExitLong;
		boolean MACDCross;
		//float chandelierExitShort;
		GenerateIndicationFromMACD objGenerateIndicationFromMACD = new GenerateIndicationFromMACD();
		objCalculateStochasticOscillator = new CalculateStochasticOscillator();
		if(!objCalculateStochasticOscillator.getStochasticIndicator(objSMAIndicatorDetails.stockCode, calculationDate)) {
			return null;
		}
		if(!objGenerateIndicationFromMACD.isMACDIncreasing(objSMAIndicatorDetails.stockCode, calculationDate)) {
			return null;
		}
		objFinalSelectedStock = new StockDetailsForDecision();
		//add selcted stock
		//objCalculateOnBalanceVolume = new CalculateOnBalanceVolume();
		//objOnBalanceVolumeIndicator = objCalculateOnBalanceVolume.calculateOnBalanceVolumeDaily(objSMAIndicatorDetails.stockCode, calculationDate);
		
		objCalculateBollingerBands = new CalculateBollingerBands();
		bbIndicator = objCalculateBollingerBands.getBBIndicationForStockV1(objSMAIndicatorDetails.stockCode, calculationDate);
		
		CalculateAverageTrueRange objCalculateAverageTrueRange = new CalculateAverageTrueRange();
		chandelierExitLong = objCalculateAverageTrueRange.getChandelierExitLong(objSMAIndicatorDetails.stockCode, calculationDate);
		//chandelierExitShort =  objCalculateAverageTrueRange.getChandelierExitShort(objSMAIndicatorDetails.stockCode, calculationDate);
		
		objCalculateRSIIndicator = new CalculateRSIIndicator();
		rsiIndication= objCalculateRSIIndicator.getRSIValue(objSMAIndicatorDetails.stockCode, objSMAIndicatorDetails.signalDate);
		
		
		MACDCross = objGenerateIndicationFromMACD.isSignalCrossedInMACD(objSMAIndicatorDetails.stockCode, calculationDate);
		
		objFinalSelectedStock.stockCode = objSMAIndicatorDetails.stockCode;
		objFinalSelectedStock.CurrentPrice = objSMAIndicatorDetails.stockPrice;
		objFinalSelectedStock.suggestedDate = objSMAIndicatorDetails.signalDate;
		//objFinalSelectedStock.percentagePriceChange = objSMAIndicatorDetails.percentagePriceChange;
		if(objSMAIndicatorDetails.PNSMAcrossover)
			objFinalSelectedStock.SMAToPriceComparison = "Crossed";
		else
			objFinalSelectedStock.SMAToPriceComparison = "NotCrossed";
		
		if(objSMAIndicatorDetails.SMNSMcrossover)
			objFinalSelectedStock.SMAComparison = "Crossed";
		else
			objFinalSelectedStock.SMAComparison = "NotCrossed";
		//objFinalSelectedStock.percentageChangeInVolumeInLastDay = objOnBalanceVolumeIndicator.percentageChangeInLastDay;
		objFinalSelectedStock.BBTrend = bbIndicator;
		objFinalSelectedStock.RSIValue = rsiIndication;
		objFinalSelectedStock.ChandelierExit = chandelierExitLong;
		if(MACDCross)
			objFinalSelectedStock.MACDStatus = "Crossed";
		else
			objFinalSelectedStock.MACDStatus = "NotCrossed";
		//objFinalSelectedStock.chandelierExitShort = chandelierExitShort;
		objFinalSelectedStock = getPriceAndVolumeDetails(objFinalSelectedStock,calculationDate);
		objFinalSelectedStock.TypeofSuggestedStock = "All";
		return objFinalSelectedStock;
	}
	
	private StockDetailsForDecision getPriceAndVolumeDetails(StockDetailsForDecision objFinalSelectedStock, Date targetDate) {
		Connection connection = null;
		ResultSet resultSet = null;
		Statement statement = null;
		String tmpSQL;
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		try {
			connection = StockUtils.connectToDB();
			statement = connection.createStatement();
			if(targetDate!=null) {
				tmpSQL = "SELECT closeprice, Volume  FROM DAILYSTOCKDATA where stockname='" + objFinalSelectedStock.stockCode + "' " 
						  + " and tradeddate >'" + dateFormat.format(new Date(targetDate.getTime() - 7*24*60*60*1000)) + "' order by tradeddate desc limit 3;";
			} else {
				tmpSQL = "SELECT closeprice, Volume  FROM DAILYSTOCKDATA where stockname='" + objFinalSelectedStock.stockCode + "' order by tradeddate desc limit 3;";
				  //+ " order by tradeddate limit " + (daysToCheck+18) + ";";
			}
			resultSet = statement.executeQuery(tmpSQL);
			
			//while (resultSet.next()) {
			if(!resultSet.next())
				return objFinalSelectedStock;
			objFinalSelectedStock.CurrentPrice = Float.parseFloat(resultSet.getString(1));
			objFinalSelectedStock.CurrentVolume = Long.parseLong(resultSet.getString(2));
			
			if(!resultSet.next())
				return objFinalSelectedStock;
			objFinalSelectedStock.OneDayPreviousPrice = Float.parseFloat(resultSet.getString(1));
			objFinalSelectedStock.OneDayPreviousVolume = Long.parseLong(resultSet.getString(2));
			
			if(!resultSet.next())
				return objFinalSelectedStock;
			objFinalSelectedStock.TwoDayPreviousPrice = Float.parseFloat(resultSet.getString(1));
			objFinalSelectedStock.TwoDayPreviousVolume = Long.parseLong(resultSet.getString(2));
			if(!resultSet.next())
				return objFinalSelectedStock;
			objFinalSelectedStock.ThreeDayPreviousPrice = Float.parseFloat(resultSet.getString(1));
			objFinalSelectedStock.ThreeDayPreviousVolume = Long.parseLong(resultSet.getString(2));
				
			//}
			return objFinalSelectedStock;
		} catch (Exception ex) {
			HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("getPriceAndVolumeDetails - Error in getting MACD values  error = " + ex);
			return null;
		} finally {
			try {
				if(resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getPriceAndVolumeDetails Error in closing resultset "+ex);
				logger.error("Error in closing resultset getPriceAndVolumeDetails  -> ", ex);
			}
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getPriceAndVolumeDetails Error in closing statement "+ex);
				logger.error("Error in closing statement getPriceAndVolumeDetails  -> ", ex);
			}
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				} 
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getPriceAndVolumeDetails Error in closing connection "+ex);
				logger.error("Error in closing connection getPriceAndVolumeDetails  -> ", ex);
			}
		}
	}
	
	private void sendTopStockInMail(ArrayList<FinalSelectedStock> objFinalSelectedStockList, Boolean belowHunderd, String subject) {
		logger.debug("sendTopStockInMail Started");
		StringBuilder mailBody = new StringBuilder();
		System.out.println("Stocks to send in mail -> " + (objFinalSelectedStockList.size()>20?20:objFinalSelectedStockList.size()));
		mailBody.append("<html><body><table border='1'><tr><th>Sr. No.</th><th>Date</th><th>Stock code</th>");
		mailBody.append("<th>Stock Price</th><th>9 to 50 SM Cross</th><th>Price crossed 20 SMA</th><th>% Price Change</th><th>% Volume Increase</th><th>BB Indication</th>"
				+ "<th>RSI Indication</th><th>Chandelier Exit</th><th>MACD Crossed</th><th>Accumulation/ Distribution Line</th></tr>");			
		for (int counter = 0; counter <(objFinalSelectedStockList.size()>20?20:objFinalSelectedStockList.size()); counter++) {
			mailBody.append("<tr><td>" + (counter+1) + "</td>");
			mailBody.append("<td>" + objFinalSelectedStockList.get(counter).tradeddate + "</td>");
			mailBody.append("<td><a href='"+ YAHOO_URL + objFinalSelectedStockList.get(counter).stockCode + ".NS'>" + objFinalSelectedStockList.get(counter).stockCode + "</a></td>");
			mailBody.append("<td>" + objFinalSelectedStockList.get(counter).stockPrice + "</td>");
			if(objFinalSelectedStockList.get(counter).SMNSMcrossover) {
				mailBody.append("<td bgcolor='green'>" + objFinalSelectedStockList.get(counter).SMNSMcrossover + "</td>");
			} else {
				mailBody.append("<td>" + objFinalSelectedStockList.get(counter).SMNSMcrossover + "</td>");
			}
			if(objFinalSelectedStockList.get(counter).PNSMAcrossover) {
				mailBody.append("<td bgcolor='green'>" + objFinalSelectedStockList.get(counter).PNSMAcrossover + "</td>");
			} else {
				mailBody.append("<td>" + objFinalSelectedStockList.get(counter).PNSMAcrossover + "</td>");
			}
			
			mailBody.append("<td>" + objFinalSelectedStockList.get(counter).percentagePriceChange + "</td>");
			mailBody.append("<td>" + objFinalSelectedStockList.get(counter).percentageChangeInVolumeInLastDay + "</td>");
			if(objFinalSelectedStockList.get(counter).BBIndicator.equalsIgnoreCase("Contracting")) {
				mailBody.append("<td bgcolor='green'>" + objFinalSelectedStockList.get(counter).BBIndicator + "</td>");
			} else {
				mailBody.append("<td>" + objFinalSelectedStockList.get(counter).BBIndicator + "</td>");
			}
			if(objFinalSelectedStockList.get(counter).rsiValue>= 30 && objFinalSelectedStockList.get(counter).rsiValue <=70) {
				mailBody.append("<td bgcolor='green'>" + objFinalSelectedStockList.get(counter).rsiValue + "</td>");
			} else {
				mailBody.append("<td>" + objFinalSelectedStockList.get(counter).rsiValue + "</td>");
			}
			
			String chandelierExitColValue = objFinalSelectedStockList.get(counter).chandelierExitLong + "";
			if(objFinalSelectedStockList.get(counter).stockPrice>= objFinalSelectedStockList.get(counter).chandelierExitLong) {
				mailBody.append("<td bgcolor='green'>" + chandelierExitColValue + "</td>");
			} else {
				mailBody.append("<td bgcolor='red'>" + chandelierExitColValue + "</td>");
			}
			
			//MACDCross
			if(objFinalSelectedStockList.get(counter).MACDCross) {
				mailBody.append("<td bgcolor='green'>" + objFinalSelectedStockList.get(counter).MACDCross + "</td>");
			} else {
				mailBody.append("<td>" + objFinalSelectedStockList.get(counter).MACDCross + "</td>");
			}
			mailBody.append("<td>" +  "</td></tr>");
		}
		mailBody.append("</table></body></html>");
        if(objFinalSelectedStockList.size() > 0) {
        	new SendSuggestedStockInMail("tarunstockcomm@gmail.com",subject+" "+objFinalSelectedStockList.get(0).tradeddate.toString(),mailBody.toString());
        	System.out.println("Mail Sent");
        } /*else if( objFinalSelectedStockList.size() > 0 ){
        	new SendSuggestedStockInMail("tarunstockcomm@gmail.com",subject+" "+objFinalSelectedStockList.get(0).tradeddate.toString(),mailBody.toString());
        }*/
        logger.debug("sendTopStockInMail end");
	}	
}
