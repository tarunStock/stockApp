package com.amazonaws.tarun.stockApp.TechnicalIndicator.Calculation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import org.apache.log4j.Logger;

import com.amazonaws.tarun.stockApp.TechnicalIndicator.Data.FinalSelectedStock;
import com.amazonaws.tarun.stockApp.TechnicalIndicator.Data.SMAIndicatorDetails;
import com.amazonaws.tarun.stockApp.TechnicalIndicator.Data.StockComparatorOnSMAPrimeV1;
import com.amazonaws.tarun.stockApp.TechnicalIndicator.Data.StockDetailsForDecision;
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
		//obj.generateCombinedIndicationForStocks(new Date("26-Mar-2018"));
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
				/*if(selectedCounter==20) {
					break;
				}*/
			} 
		} 
		Collections.sort(objFinalSelectedStockList, new StockComparatorOnSMAPrimeV1());
		SalesforceIntegration objSalesforceIntegration = new SalesforceIntegration();
		
		objSalesforceIntegration.connectToSalesforc();
		objSalesforceIntegration.createSuggestedStocks(objFinalSelectedStockList);
		
		System.out.println("********* - process below hundred seleted stocks to send mail");
		selectedCounter = 0;
		for(int counter = 0; counter<SMAIndicatorDetailsBelowHundredList.size(); counter++){
			objFinalSelectedBelowHunderdStock = new StockDetailsForDecision();
			objFinalSelectedBelowHunderdStock.stockCode = SMAIndicatorDetailsBelowHundredList.get(counter).stockCode;
			if(objFinalSelectedStockList.contains(objFinalSelectedBelowHunderdStock)) {
				objFinalSelectedBelowHunderdStock = objFinalSelectedStockList.get(objFinalSelectedStockList.indexOf(objFinalSelectedBelowHunderdStock));
				objFinalSelectedBelowHunderdStock.TypeofSuggestedStock = "Below 100";
				objFinalSelectedBelowHundredStockList.add(objFinalSelectedBelowHunderdStock);
				selectedCounter = selectedCounter +1;
				if(selectedCounter==20) {
					break;
				}
			} else {
				objFinalSelectedBelowHunderdStock = getAlldetails(SMAIndicatorDetailsBelowHundredList.get(counter), calculationDate);				
				if(objFinalSelectedBelowHunderdStock!=null) {
					objFinalSelectedBelowHunderdStock.TypeofSuggestedStock = "Below 100";
					objFinalSelectedBelowHundredStockList.add(objFinalSelectedBelowHunderdStock);
					selectedCounter = selectedCounter +1;
					if(selectedCounter==20) {
						break;
					}
				} 
			}			
		}
		objSalesforceIntegration.createSuggestedStocks(objFinalSelectedBelowHundredStockList);
		logger.debug("generateCombinedIndicationForStocks End");
	}
	
	
	
	
	private StockDetailsForDecision getAlldetails (SMAIndicatorDetails objSMAIndicatorDetails, Date calculationDate) {
		StockDetailsForDecision objFinalSelectedStock = null;
		//CalculateOnBalanceVolume objCalculateOnBalanceVolume;
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
		if(!StockUtils.getFinancialIndication(objSMAIndicatorDetails.stockCode)) {
			return null;
		}
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
		objFinalSelectedStock = StockUtils.getPriceAndVolumeDetails(objFinalSelectedStock,calculationDate);
		objFinalSelectedStock.TypeofSuggestedStock = "All";
		CalculateFibonacciRetracements obj = new CalculateFibonacciRetracements();
		ArrayList<Double> supportAndResistanceValues = obj.FibonacciRetracements(objSMAIndicatorDetails.stockCode, calculationDate);
		if(supportAndResistanceValues!=null) {
			objFinalSelectedStock.supportLevel = supportAndResistanceValues.get(0);
			objFinalSelectedStock.resistanceLevel = supportAndResistanceValues.get(1);
		}
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
}
