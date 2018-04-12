package com.amazonaws.tarun.stockApp.TechnicalIndicator.Calculation;
import java.sql.Connection;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

import com.amazonaws.tarun.stockApp.TechnicalIndicator.Data.FinalSelectedStock;
import com.amazonaws.tarun.stockApp.TechnicalIndicator.Data.OnBalanceVolumeIndicator;
import com.amazonaws.tarun.stockApp.TechnicalIndicator.Data.SMAIndicatorDetails;
import com.amazonaws.tarun.stockApp.Utils.StockUtils;

public class GenerateCombinedIndication {
	
	public static int daysToCheck = 5;
	public static String YAHOO_URL = "https://in.finance.yahoo.com/chart/";
	
	SMAIndicatorDetails objSMAIndicatorDetails;
	String stockName;
	String bseCode;
	String nseCode;
	static Logger logger = Logger.getLogger(GenerateCombinedIndication.class);
	
	public static void main(String[] args) {
		Date dte = new Date();
		System.out.println("Start at -> " + dte.toString());
		GenerateCombinedIndication obj = new GenerateCombinedIndication();
		//obj.generateCombinedIndicationForStocks(new Date("25-Jan-2018"));
		obj.generateCombinedIndicationForStocks(null);
	}

	public void generateCombinedIndicationForStocks(Date calculationDate) {
		logger.debug("generateCombinedIndicationForStocks Started");
		int selectedCounter=0;
		Connection connection = null;
		ArrayList<SMAIndicatorDetails> SMAIndicatorDetailsList;
		ArrayList<SMAIndicatorDetails> SMAIndicatorDetailsBelowHundredList;
		if( !StockUtils.marketOpenOnGivenDate(calculationDate)) {
			System.out.println("Returned due to weekend");
			return;
		}	
		connection = StockUtils.connectToDB();
		ArrayList<FinalSelectedStock> objFinalSelectedStockList = new ArrayList<FinalSelectedStock>();
		ArrayList<FinalSelectedStock> objFinalSelectedBelowHundredStockList = new ArrayList<FinalSelectedStock>();
		ArrayList<FinalSelectedStock> objFinalSelectedStockListWithLowRSI = new ArrayList<FinalSelectedStock>();
		ArrayList<FinalSelectedStock> objFinalSelectedBelowHundredStockListWithLowRSI = new ArrayList<FinalSelectedStock>();
		FinalSelectedStock objFinalSelectedStock = null;
		FinalSelectedStock objFinalSelectedBelowHunderdStock;
		System.out.println("********* - Get seleted stocks based on SMA");
		GenerateIndicationfromMovingAverage obj = new GenerateIndicationfromMovingAverage();
		obj.CalculateIndicationfromSMA(connection,calculationDate);
		SMAIndicatorDetailsList = obj.getIndicationStocks();
		SMAIndicatorDetailsBelowHundredList = obj.getBelowHunderdIndicationStocks();
		System.out.println("********* - process seleted stocks to send mail");
		for(int counter = 0; counter<SMAIndicatorDetailsList.size(); counter++){			
			//add selected stock				
			objFinalSelectedStock = getAlldetails(connection, SMAIndicatorDetailsList.get(counter), calculationDate);
			if(objFinalSelectedStock!=null) {
				objFinalSelectedStockList.add(objFinalSelectedStock);
				selectedCounter = selectedCounter +1;
				if(selectedCounter==20) {
					break;
				}
			} 
		} 
			
		
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
				objFinalSelectedBelowHunderdStock = getAlldetails(connection,SMAIndicatorDetailsBelowHundredList.get(counter), calculationDate);						
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
		objFinalSelectedStockListWithLowRSI = getLowRSIStocks(connection, SMAIndicatorDetailsList, calculationDate);
		//Send top low RSI stock in mail
		sendTopStockInMail(objFinalSelectedStockListWithLowRSI, true, "Combined -> Low RSI Stocklist on ");
		//Get Low RSI below 100 stocks
		objFinalSelectedBelowHundredStockListWithLowRSI = getLowRSIStocks(connection, SMAIndicatorDetailsBelowHundredList, calculationDate);
		//Send top low RSI beow 100 stock in mail
		sendTopStockInMail(objFinalSelectedBelowHundredStockListWithLowRSI, true, "Combined -> Low RSI below 100 Stocklist on ");
		logger.debug("generateCombinedIndicationForStocks End");
	}
	
	
	private ArrayList<FinalSelectedStock> getLowRSIStocks(Connection connection, ArrayList<SMAIndicatorDetails> SMAIndicatorDetailsList, Date calculationDate) {
		ArrayList<FinalSelectedStock> objFinalSelectedStockListWithLowRSI = new ArrayList<FinalSelectedStock>();
		FinalSelectedStock objFinalSelectedStock;
		CalculateRSIIndicator objCalculateRSIIndicator = new CalculateRSIIndicator();
		float rsiIndication;
		int totalStocksAdded = 0;
		
		for(int counter = 0; counter<SMAIndicatorDetailsList.size(); counter++){
			objSMAIndicatorDetails = SMAIndicatorDetailsList.get(counter);
			rsiIndication= objCalculateRSIIndicator.getRSIValue(objSMAIndicatorDetails.stockCode, objSMAIndicatorDetails.signalDate);
			if(rsiIndication < 50) {
				objFinalSelectedStock = getAlldetailsExceptRSI(connection, SMAIndicatorDetailsList.get(counter), calculationDate);
				objFinalSelectedStock.rsiValue = rsiIndication;
				objFinalSelectedStockListWithLowRSI.add(objFinalSelectedStock);
				totalStocksAdded = totalStocksAdded + 1;
				if(totalStocksAdded==20) {
					break;
				}
			}
		}
		//Send top below 100 stock in mail
		//sendTopStockInMail(objFinalSelectedStockListWithLowRSI, true, "Combined -> Low RSI Stocklist on ");
		return objFinalSelectedStockListWithLowRSI;
	}
	
	private FinalSelectedStock getAlldetails (Connection connection, SMAIndicatorDetails objSMAIndicatorDetails, Date calculationDate) {
		FinalSelectedStock objFinalSelectedStock = null;
		CalculateOnBalanceVolume objCalculateOnBalanceVolume;
		OnBalanceVolumeIndicator objOnBalanceVolumeIndicator;
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
		if(!objCalculateStochasticOscillator.getStochasticIndicator(connection, objSMAIndicatorDetails.stockCode, calculationDate)) {
			return null;
		} 
		if(!objGenerateIndicationFromMACD.isMACDIncreasing(connection, objSMAIndicatorDetails.stockCode, calculationDate)) {
			return null;
		}
		objFinalSelectedStock = new FinalSelectedStock();
		//add selcted stock
		objCalculateOnBalanceVolume = new CalculateOnBalanceVolume();
		objOnBalanceVolumeIndicator = objCalculateOnBalanceVolume.calculateOnBalanceVolumeDaily(objSMAIndicatorDetails.stockCode, calculationDate);
		
		objCalculateBollingerBands = new CalculateBollingerBands();
		bbIndicator = objCalculateBollingerBands.getBBIndicationForStockV1(connection, objSMAIndicatorDetails.stockCode, calculationDate);
		
		CalculateAverageTrueRange objCalculateAverageTrueRange = new CalculateAverageTrueRange();
		chandelierExitLong = objCalculateAverageTrueRange.getChandelierExitLong(connection, objSMAIndicatorDetails.stockCode, calculationDate);
		//chandelierExitShort =  objCalculateAverageTrueRange.getChandelierExitShort(objSMAIndicatorDetails.stockCode, calculationDate);
		
		objCalculateRSIIndicator = new CalculateRSIIndicator();
		rsiIndication= objCalculateRSIIndicator.getRSIValue(objSMAIndicatorDetails.stockCode, objSMAIndicatorDetails.signalDate);
		
		
		MACDCross = objGenerateIndicationFromMACD.isSignalCrossedInMACD(connection, objSMAIndicatorDetails.stockCode, calculationDate);
		
		objFinalSelectedStock.stockCode = objSMAIndicatorDetails.stockCode;
		objFinalSelectedStock.stockPrice = objSMAIndicatorDetails.stockPrice;
		objFinalSelectedStock.tradeddate = objSMAIndicatorDetails.signalDate;
		objFinalSelectedStock.percentagePriceChange = objSMAIndicatorDetails.percentagePriceChange;
		objFinalSelectedStock.PNSMAcrossover = objSMAIndicatorDetails.PNSMAcrossover;
		objFinalSelectedStock.SMNSMcrossover = objSMAIndicatorDetails.SMNSMcrossover;
		objFinalSelectedStock.percentageChangeInVolumeInLastDay = objOnBalanceVolumeIndicator.percentageChangeInLastDay;
		objFinalSelectedStock.BBIndicator = bbIndicator;
		objFinalSelectedStock.rsiValue = rsiIndication;
		objFinalSelectedStock.chandelierExitLong = chandelierExitLong;
		objFinalSelectedStock.MACDCross = MACDCross;
		//objFinalSelectedStock.chandelierExitShort = chandelierExitShort;
		
		return objFinalSelectedStock;
	}
	
	private FinalSelectedStock getAlldetailsExceptRSI (Connection connection, SMAIndicatorDetails objSMAIndicatorDetails, Date calculationDate) {
		FinalSelectedStock objFinalSelectedStock = null;
		CalculateOnBalanceVolume objCalculateOnBalanceVolume;
		OnBalanceVolumeIndicator objOnBalanceVolumeIndicator;
		CalculateBollingerBands objCalculateBollingerBands;
		CalculateRSIIndicator objCalculateRSIIndicator;
		
		String bbIndicator;
		float rsiIndication;
		float chandelierExitLong;
		float chandelierExitShort;
		
		objFinalSelectedStock = new FinalSelectedStock();
		//add selcted stock
		objCalculateOnBalanceVolume = new CalculateOnBalanceVolume();
		objOnBalanceVolumeIndicator = objCalculateOnBalanceVolume.calculateOnBalanceVolumeDaily(objSMAIndicatorDetails.stockCode, calculationDate);
		
		objCalculateBollingerBands = new CalculateBollingerBands();
		bbIndicator = objCalculateBollingerBands.getBBIndicationForStockV1(connection,objSMAIndicatorDetails.stockCode, calculationDate);
		
		CalculateAverageTrueRange objCalculateAverageTrueRange = new CalculateAverageTrueRange();
		chandelierExitLong = objCalculateAverageTrueRange.getChandelierExitLong(connection,objSMAIndicatorDetails.stockCode, null);
		//chandelierExitShort =  objCalculateAverageTrueRange.getChandelierExitShort(objSMAIndicatorDetails.stockCode, null);
		
//		objCalculateRSIIndicator = new CalculateRSIIndicator();
//		rsiIndication= objCalculateRSIIndicator.getRSIValue(objSMAIndicatorDetails.stockCode, objSMAIndicatorDetails.signalDate);
		
		objFinalSelectedStock.stockCode = objSMAIndicatorDetails.stockCode;
		objFinalSelectedStock.stockPrice = objSMAIndicatorDetails.stockPrice;
		objFinalSelectedStock.tradeddate = objSMAIndicatorDetails.signalDate;
		objFinalSelectedStock.percentagePriceChange = objSMAIndicatorDetails.percentagePriceChange;
		objFinalSelectedStock.PNSMAcrossover = objSMAIndicatorDetails.PNSMAcrossover;
		objFinalSelectedStock.SMNSMcrossover = objSMAIndicatorDetails.SMNSMcrossover;
		objFinalSelectedStock.percentageChangeInVolumeInLastDay = objOnBalanceVolumeIndicator.percentageChangeInLastDay;
		objFinalSelectedStock.BBIndicator = bbIndicator;
		//objFinalSelectedStock.rsiValue = rsiIndication;
		objFinalSelectedStock.chandelierExitLong = chandelierExitLong;
		//objFinalSelectedStock.chandelierExitShort = chandelierExitShort;
		
		return objFinalSelectedStock;
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
	
	private void CreateWatchListForTopStock(ArrayList<FinalSelectedStock> objFinalSelectedStockList, Boolean belowHunderd) {
		logger.debug("CreateWatchListForTopStock Started");
		DateTimeFormatter formatters = DateTimeFormatter.ofPattern("dd-MMM");
		CreateWatchListYahoo objCreateWatchListYahoo = new CreateWatchListYahoo();
		
		for (int counter = (objFinalSelectedStockList.size()>20?20:objFinalSelectedStockList.size()-1); counter > 0; counter--) {
			if(counter == objFinalSelectedStockList.size()-1 || counter == 20) {
				if(!belowHunderd){
					objCreateWatchListYahoo.creatWatchList(objFinalSelectedStockList.get(counter).tradeddate.format(formatters) + " All", belowHunderd);
				} else {
					objCreateWatchListYahoo.creatWatchList(objFinalSelectedStockList.get(counter).tradeddate.format(formatters) + " Below 100", belowHunderd);
				}
			}
			objCreateWatchListYahoo.addStocksToWatchList(objFinalSelectedStockList.get(counter).stockCode);
		}
        //objCreateWatchListYahoo.stopSelenium();
        logger.debug("CreateWatchListForTopStock end");
	}
	
}
