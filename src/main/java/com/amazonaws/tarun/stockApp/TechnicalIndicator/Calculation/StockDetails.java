package com.amazonaws.tarun.stockApp.TechnicalIndicator.Calculation;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

import org.apache.log4j.Logger;

import com.amazonaws.tarun.stockApp.TechnicalIndicator.Data.StockDetailsForDecision;
import com.amazonaws.tarun.stockApp.Utils.StockUtils;
import com.amazonaws.tarun.stockApp.externalOperations.StockOperations;

public class StockDetails {
	static Logger logger = Logger.getLogger(StockOperations.class);
	
	public static void main(String[] args) {
		StockDetails objStockDetails = new StockDetails();
		StockDetailsForDecision objStockDetailsForDecision = objStockDetails.getStockDetails("UPL",  new Date(Date.parse("30-Jan-2018")));
		System.out.println("DOne");
	}
	
	public StockDetailsForDecision getStockDetails(String stockCode, Date calculationDate) {
		StockDetailsForDecision objStockDetailsForDecision = new StockDetailsForDecision(); 
		CalculateBollingerBands objCalculateBollingerBands;
		CalculateRSIIndicator objCalculateRSIIndicator;
		try {
			GenerateIndicationfromMovingAverage obj = new GenerateIndicationfromMovingAverage();
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			objStockDetailsForDecision.stockCode = stockCode;
			obj.CalculateIndicationfromSMA(stockCode, calculationDate);			
			if(obj.objSMAIndicatorDetails.SMNSMcrossover)
				objStockDetailsForDecision.SMAComparison = "Crossed";
			else
				objStockDetailsForDecision.SMAComparison = "NotCrossed";
			
			if(obj.objSMAIndicatorDetails.PNSMAcrossover)
				objStockDetailsForDecision.SMAToPriceComparison = "Crossed";
			else
				objStockDetailsForDecision.SMAToPriceComparison = "NotCrossed";
			
			GenerateIndicationFromMACDV1 objGenerateIndicationFromMACDV1 = new GenerateIndicationFromMACDV1();
			if(objGenerateIndicationFromMACDV1.isSignalCrossedInMACD(stockCode, calculationDate))
				objStockDetailsForDecision.MACDStatus = "Crossed";
			else
				objStockDetailsForDecision.MACDStatus = "NotCrossed";
			objStockDetailsForDecision = StockUtils.getPriceAndVolumeDetails(objStockDetailsForDecision,calculationDate);
			
			objCalculateBollingerBands = new CalculateBollingerBands();
			objStockDetailsForDecision.BBTrend = objCalculateBollingerBands.getBBIndicationForStockV1(stockCode, calculationDate);
			
			CalculateAverageTrueRange objCalculateAverageTrueRange = new CalculateAverageTrueRange();
			objStockDetailsForDecision.ChandelierExit = objCalculateAverageTrueRange.getChandelierExitLong(stockCode, calculationDate);
			
			objCalculateRSIIndicator = new CalculateRSIIndicator();
			if(calculationDate!=null) { 
				objStockDetailsForDecision.RSIValue= objCalculateRSIIndicator.getRSIValue(stockCode, LocalDate.parse(dateFormat.format(calculationDate).toString()));
			} else {
				objStockDetailsForDecision.RSIValue= objCalculateRSIIndicator.getRSIValue(stockCode, null);
			}
			return objStockDetailsForDecision;			
		} catch (Exception ex) {
			System.out.println("Error");
			return null;
		}
		
	}
}
