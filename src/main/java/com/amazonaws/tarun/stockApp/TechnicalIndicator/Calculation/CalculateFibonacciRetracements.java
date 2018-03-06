package com.amazonaws.tarun.stockApp.TechnicalIndicator.Calculation;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

import com.amazonaws.tarun.stockApp.Utils.HandleErrorDetails;
import com.amazonaws.tarun.stockApp.Utils.StockUtils;

public class CalculateFibonacciRetracements {
	Connection connection = null;
	static Logger logger = Logger.getLogger(CalculateFibonacciRetracements.class);
	
	public static void main(String[] args) {
		
		StockUtils.getFinancialIndication("MMFL");
		Date dte = new Date();
		logger.debug("CalculateFibonacciRetracements Started");
		System.out.println("Start at -> " + dte.toString());
		CalculateFibonacciRetracements obj = new CalculateFibonacciRetracements();
		ArrayList<Double> supportAndResistanceValues = obj.FibonacciRetracements("CGCL", new Date("8-Jan-2018"));
		
		System.out.println("Support Level -> " + supportAndResistanceValues.get(0));
		System.out.println("Resistance Level -> " + supportAndResistanceValues.get(1));
		HandleErrorDetails.sendErrorsInMail("Calculate Fibonacci Retracements");
		dte = new Date();
		System.out.println("End at -> " + dte.toString());
		logger.debug("CalculateFibonacciRetracements End");
	}
	
	public ArrayList<Double> FibonacciRetracements(String stockCode, Date targetDate) {
		ArrayList<Double> supportAndResistanceValues = null;
		//FirstFibonacciRetracementLevel = 23.6%, secondFibonacciRetracementLevel = 38.2%, ThirdFibonacciRetracementLevel = 50%
		//FourthFibonacciRetracementLevel = 61.8%, FifthFibonacciRetracementLevel = 78.6%
		double startingFibonacciRetracementLevel, FirstFibonacciRetracementLevel, secondFibonacciRetracementLevel, ThirdFibonacciRetracementLevel, FourthFibonacciRetracementLevel, FifthFibonacciRetracementLevel, LastFibonacciRetracementLevel;
		double priceDifference;
		float currentPrice;
		
		supportAndResistanceValues = new ArrayList<Double>();
		ArrayList<Float> priceList = getMaxAndMinStockPricesFromDB(stockCode, targetDate);
		priceDifference = priceList.get(1) - priceList.get(0); 
		startingFibonacciRetracementLevel = priceList.get(0);
		FirstFibonacciRetracementLevel = priceList.get(0) + (priceDifference*23.6/100);
		secondFibonacciRetracementLevel = priceList.get(0) + (priceDifference*38.2/100);
		ThirdFibonacciRetracementLevel = priceList.get(0) + (priceDifference*50/100);
		FourthFibonacciRetracementLevel = priceList.get(0) + (priceDifference*61.8/100);
		FifthFibonacciRetracementLevel = priceList.get(0) + (priceDifference*78.6/100);
		LastFibonacciRetracementLevel = priceList.get(1);
		currentPrice = priceList.get(2);
		if(currentPrice > FirstFibonacciRetracementLevel) {
			if(currentPrice > secondFibonacciRetracementLevel) {
				if(currentPrice > ThirdFibonacciRetracementLevel) {
					if(currentPrice > FourthFibonacciRetracementLevel) {
						if(currentPrice > FifthFibonacciRetracementLevel) {
							supportAndResistanceValues.add(FifthFibonacciRetracementLevel);
							supportAndResistanceValues.add(LastFibonacciRetracementLevel);					
						} else {
							supportAndResistanceValues.add(FourthFibonacciRetracementLevel);
							supportAndResistanceValues.add(FifthFibonacciRetracementLevel);
						}
					} else {
						supportAndResistanceValues.add(ThirdFibonacciRetracementLevel);
						supportAndResistanceValues.add(FourthFibonacciRetracementLevel);
					}
				} else {
					supportAndResistanceValues.add(secondFibonacciRetracementLevel);
					supportAndResistanceValues.add(ThirdFibonacciRetracementLevel);
				}
			} else {
				supportAndResistanceValues.add(FirstFibonacciRetracementLevel);
				supportAndResistanceValues.add(secondFibonacciRetracementLevel);
			}
		} else {
			supportAndResistanceValues.add(startingFibonacciRetracementLevel);
			supportAndResistanceValues.add(FirstFibonacciRetracementLevel);
		}
		return supportAndResistanceValues;
	}
	
	private ArrayList<Float> getMaxAndMinStockPricesFromDB(String stockCode, Date targetDate) {
		ArrayList<Float> priceList = null;
		Connection connection = null;
		ResultSet resultSet = null;
		Statement statement = null;
		String tmpSQL;
		Float closePrice, openPrice;
		Float minPrice = 0.0F, maxPrice = 0.0F;
		float currentPrice = 0.0F;
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		try {			
			priceList = new ArrayList<Float>();		
			connection = StockUtils.connectToDB();
			statement = connection.createStatement();
			if(targetDate!=null) {
				tmpSQL = "SELECT closeprice, openprice FROM DAILYSTOCKDATA where stockname='" + stockCode + "'"
				+ " and tradeddate >='" + dateFormat.format(new Date(targetDate.getTime() - 180*24*60*60*1000L)) + "' and tradeddate <='" + dateFormat.format(new Date(targetDate.getTime())) + "' order by tradeddate desc;";
			} else {
				tmpSQL = "SELECT closeprice, openprice FROM DAILYSTOCKDATA where stockname='" + stockCode + "'"
							+ " and tradeddate >='" + dateFormat.format(new Date(System.currentTimeMillis() - 180*24*60*60*1000L)) + "' order by tradeddate desc;";
			}
			resultSet = statement.executeQuery(tmpSQL);
			while (resultSet.next()) {				
				closePrice = Float.parseFloat(resultSet.getString(1));
				openPrice = Float.parseFloat(resultSet.getString(2));
				if(closePrice < openPrice) {
					openPrice = closePrice + openPrice;
					closePrice = openPrice - closePrice;
					openPrice = openPrice - closePrice;
				}
				if(currentPrice==0) {
					currentPrice = Float.parseFloat(resultSet.getString(1));
					minPrice = openPrice;
					maxPrice = closePrice; 
				}
				if(minPrice>=openPrice) {
					minPrice = openPrice;
				} 
				if(maxPrice<=closePrice) {
					maxPrice = closePrice;
				}
			}
			priceList.add(minPrice);
			priceList.add(maxPrice);
			priceList.add(currentPrice);
			return priceList;
		} catch (Exception ex) {
			HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("getMaxAndMinStockPricesFromDB Error in DB action"+ex);
			logger.error("Error in getMaxAndMinStockPricesFromDB  -> ", ex);
			return null;
		} finally {
			try {
				if(resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getMaxAndMinStockPricesFromDB Error in closing resultset "+ex);
				logger.error("Error in closing resultset getMaxAndMinStockPricesFromDB  -> ", ex);
			}
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getMaxAndMinStockPricesFromDB Error in closing statement "+ex);
				logger.error("Error in closing statement getMaxAndMinStockPricesFromDB  -> ", ex);
			}
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				} 
			} catch (Exception ex) {
				HandleErrorDetails.addError(StockUtils.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getStockListFromDB Error in closing connection "+ex);
				logger.error("Error in closing connection getStockListFromDB  -> ", ex);
			}
		}
	}
}
