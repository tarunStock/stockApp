package com.amazonaws.tarun.stockApp.TechnicalIndicator.Calculation;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import org.apache.log4j.Logger;

import com.amazonaws.tarun.stockApp.TechnicalIndicator.Data.AccumulatioDistributionData;
import com.amazonaws.tarun.stockApp.TechnicalIndicator.Data.StochasticOscillatorData;
import com.amazonaws.tarun.stockApp.Utils.HandleErrorDetails;
import com.amazonaws.tarun.stockApp.Utils.StockUtils;

public class AccumulationDistribution {
	static Logger logger = Logger.getLogger(AccumulationDistribution.class);
	
	public static void main(String[] args) {
		Date dte = new Date();
		logger.debug("AccumulationDistribution - Main Started");
		System.out.println("Start at -> " + dte.toString());
		AccumulationDistribution obj = new AccumulationDistribution();
		//obj.calculateAccumulationDistributionForAllStocks(null);
		obj.calculateAccumulationDistributionForAllStocks(new Date("01-Jun-2018"));
		//obj.getChandelierExitLong("20MICRONS", new Date("16-Oct-2017"));
		HandleErrorDetails.sendErrorsInMail("AccumulationDistribution");
		dte = new Date();
		System.out.println("End at -> " + dte.toString());
		logger.debug("AccumulationDistribution - Main End");
	}
	
	public void calculateAccumulationDistributionForAllStocks(Date calculationDate) {
		ArrayList<String> stockList = null;
		Connection connection = null;
		if( !StockUtils.marketOpenOnGivenDate(calculationDate))
			return;
		
		connection = StockUtils.connectToDB();
		
		stockList = StockUtils.getStockListFromDB(connection);
		String stockName;
		String bseCode;
		String nseCode;

		//Testing
		//calculateAccumulationDistributionForStockInBulk("63MOONS");
		//Testing
		for (String stockCode : stockList) {
			
			stockName = stockCode.split("!")[1];
			bseCode = stockCode.split("!")[0];
			nseCode = stockCode.split("!")[2];
			System.out.println("Calculating Accumulation Distribution for stock - >"+nseCode);
			//calculate RSI on bulk
			//calculateAccumulationDistributionForStockInBulk(nseCode);
			calculateAccumulationDistributionForStockDaily(connection, nseCode);
//			//calculate average on daily basis
			
			//calculateAverageTrueRangeForStockDaily(nseCode, new Date("19-Oct-2017"));
			//calculateAccumulationDistributionForStockDaily(nseCode, calculationDate);
		}
	}
	
	private void calculateAccumulationDistributionForStockInBulk(Connection connection, String stockCode) {
		AccumulatioDistributionData stockDetails = null;
		float MFmultipplier, MFValue, currentCloseLowDifference = 0, currentHighCloseDifference = 0, currentHighAndLowDifference = 0, previoudDayAccumulatioDistribution = 0, accumulatioDistribution = 0;
		//Get stock details from dailystockdata table
		stockDetails = getStockDetailsFromDBForBulk(connection, stockCode);
		ArrayList<Float> highestHighArr, lowestLowArr;
		Comparator<Float> comparatorForLow = Collections.reverseOrder();
		try {
			if(connection == null) {
				connection = StockUtils.connectToDB();
			}
			for (int counter = 0; counter < stockDetails.tradeddate.size(); counter++) {
				currentCloseLowDifference = stockDetails.closePrice.get(counter) - stockDetails.lowPrice.get(counter);
				currentHighCloseDifference = stockDetails.highPrice.get(counter) - stockDetails.closePrice.get(counter);
				currentHighAndLowDifference = stockDetails.highPrice.get(counter) - stockDetails.lowPrice.get(counter);
				if(currentHighAndLowDifference == 0 ) {
					currentHighAndLowDifference = 1;
				}
				
				MFmultipplier = (currentCloseLowDifference - currentHighCloseDifference) / currentHighAndLowDifference;
				MFValue = MFmultipplier * stockDetails.volume.get(counter);
				accumulatioDistribution = MFValue + previoudDayAccumulatioDistribution;
				previoudDayAccumulatioDistribution = accumulatioDistribution;
				storeAccumuationDistributioninDB(connection, stockCode, stockDetails.tradeddate.get(counter), accumulatioDistribution);
			}
		} catch (Exception ex) {
			HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("calculateAverageTrueRangeForStockInBulk Error in DB action "+ex);
			logger.error("Error in calculateAverageTrueRangeForStockInBulk  -> ", ex);
		} finally {
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				} 
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("calculateAverageTrueRangeForStockInBulk Error in DB action ");
				logger.error("Error in calculateAverageTrueRangeForStockInBulk  -> ", ex);
			}
		}
	}
	
	private AccumulatioDistributionData getStockDetailsFromDBForBulk(Connection connection, String stockCode) {
		ResultSet resultSet = null;
		Statement statement = null;
		String tradedDate, sql;
		Float closePrice, highPrice, lowPrice;
		Long volume;
		AccumulatioDistributionData soDataObj = null;
		try {
			soDataObj = new AccumulatioDistributionData();
			soDataObj.closePrice = new ArrayList<Float>();
			soDataObj.tradeddate = new ArrayList<String>();
			soDataObj.highPrice = new ArrayList<Float>();
			soDataObj.lowPrice = new ArrayList<Float>();
			soDataObj.volume = new ArrayList<Long>();
			statement = connection.createStatement();
			soDataObj.stockName = stockCode;
			sql = "SELECT tradeddate, closeprice, HIGHPRICE, LOWPRICE, volume FROM DAILYSTOCKDATA where stockname='"
					+ stockCode + "' and tradeddate >= '2017-01-01' order by tradeddate;";
			resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				tradedDate = resultSet.getString(1);
				closePrice = Float.parseFloat(resultSet.getString(2));
				highPrice = Float.parseFloat(resultSet.getString(3));
				lowPrice = Float.parseFloat(resultSet.getString(4));
				volume = Long.parseLong(resultSet.getString(5));
				soDataObj.closePrice.add(closePrice);
				soDataObj.tradeddate.add(tradedDate);
				soDataObj.highPrice.add(highPrice);
				soDataObj.lowPrice.add(lowPrice);
				soDataObj.volume.add(volume);
			}
			return soDataObj;
		} catch (Exception ex) {
			HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("getStockDetailsFromDBForBulk -> Error in DB action"+ex);
			logger.error("Error in getStockDetailsFromDBForBulk  -> ", ex);
			return null;
		} finally {
			try {
				if(resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getStockDetailsFromDBForBulk Error in closing resultset "+ex);
				logger.error("Error in closing resultset getStockDetailsFromDBForBulk  -> ", ex);
			}
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getStockDetailsFromDBForBulk Error in closing statement "+ex);
				logger.error("Error in closing statement getStockDetailsFromDBForBulk  -> ", ex);
			}
		}
	}
	
	private void calculateAccumulationDistributionForStockDaily(Connection connection, String stockCode) {
		AccumulatioDistributionData stockDetails = null;
		float MFmultipplier, MFValue, currentCloseLowDifference = 0, currentHighCloseDifference = 0, currentHighAndLowDifference = 0, previoudDayAccumulatioDistribution = 0, accumulatioDistribution = 0;
		//Get stock details from dailystockdata table
		stockDetails = getStockDetailsFromDBDaily(connection,stockCode);
		ArrayList<Float> highestHighArr, lowestLowArr;
		Comparator<Float> comparatorForLow = Collections.reverseOrder();
		try {
			if(connection == null) {
				connection = StockUtils.connectToDB();
			}
			previoudDayAccumulatioDistribution = stockDetails.lastEnteredaccumulatioDistribution;
			for (int counter = 0; counter < stockDetails.tradeddate.size(); counter++) {
				currentCloseLowDifference = stockDetails.closePrice.get(counter) - stockDetails.lowPrice.get(counter);
				currentHighCloseDifference = stockDetails.highPrice.get(counter) - stockDetails.closePrice.get(counter);
				currentHighAndLowDifference = stockDetails.highPrice.get(counter) - stockDetails.lowPrice.get(counter);
				if(currentHighAndLowDifference == 0 ) {
					currentHighAndLowDifference = 1;
				}
				
				MFmultipplier = (currentCloseLowDifference - currentHighCloseDifference) / currentHighAndLowDifference;
				MFValue = MFmultipplier * stockDetails.volume.get(counter);
				accumulatioDistribution = MFValue + previoudDayAccumulatioDistribution;
				previoudDayAccumulatioDistribution = accumulatioDistribution;
				storeAccumuationDistributioninDB(connection,stockCode, stockDetails.tradeddate.get(counter), accumulatioDistribution);
			}
		} catch (Exception ex) {
			HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("calculateAverageTrueRangeForStockInBulk Error in DB action "+ex);
			logger.error("Error in calculateAverageTrueRangeForStockInBulk  -> ", ex);
		} 
	}
	
	private AccumulatioDistributionData getStockDetailsFromDBDaily(Connection connection, String stockCode) {
		ResultSet resultSet = null;
		Statement statement = null;
		String tradedDate, sql;
		Float closePrice, highPrice, lowPrice;
		Long volume;
		AccumulatioDistributionData soDataObj = null;
		String LastEnteredDate = null;
		try {
			soDataObj = new AccumulatioDistributionData();
			//Get last entered date from Accul=mulationDistribution Table
			sql = "SELECT tradeddate, ACCUMULATIONDISTRIBUTION FROM DAILY_ACCUMULATION_DISTRIBUTION where stockname='" + stockCode + "' order by tradeddate desc;";
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				LastEnteredDate = resultSet.getString(1);
				soDataObj.lastEnteredaccumulatioDistribution = Float.parseFloat(resultSet.getString(2));
				break;
			}
			resultSet.close();
			
			
			soDataObj.closePrice = new ArrayList<Float>();
			soDataObj.tradeddate = new ArrayList<String>();
			soDataObj.highPrice = new ArrayList<Float>();
			soDataObj.lowPrice = new ArrayList<Float>();
			soDataObj.volume = new ArrayList<Long>();
			
			soDataObj.stockName = stockCode;
			sql = "SELECT tradeddate, closeprice, HIGHPRICE, LOWPRICE, volume FROM DAILYSTOCKDATA where stockname='"
					+ stockCode + "' and tradeddate >'" + LastEnteredDate + "' order by tradeddate;";
			resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				tradedDate = resultSet.getString(1);
				closePrice = Float.parseFloat(resultSet.getString(2));
				highPrice = Float.parseFloat(resultSet.getString(3));
				lowPrice = Float.parseFloat(resultSet.getString(4));
				volume = Long.parseLong(resultSet.getString(5));
				soDataObj.closePrice.add(closePrice);
				soDataObj.tradeddate.add(tradedDate);
				soDataObj.highPrice.add(highPrice);
				soDataObj.lowPrice.add(lowPrice);
				soDataObj.volume.add(volume);
			}
			return soDataObj;
		} catch (Exception ex) {
			HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("getStockDetailsFromDBForBulk -> Error in DB action"+ex);
			logger.error("Error in getStockDetailsFromDBForBulk  -> ", ex);
			return null;
		} finally {
			try {
				if(resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getStockDetailsFromDBForBulk Error in closing resultset "+ex);
				logger.error("Error in closing resultset getStockDetailsFromDBForBulk  -> ", ex);
			}
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getStockDetailsFromDBForBulk Error in closing statement "+ex);
				logger.error("Error in closing statement getStockDetailsFromDBForBulk  -> ", ex);
			}
		}
	}
	
	
	private void storeAccumuationDistributioninDB(Connection connection, String stockName, String tradedDate, float accumuationDistribution) {
		Statement statement = null;
		String tmpsql;
		try {
			statement = connection.createStatement();
			tmpsql = "INSERT INTO DAILY_ACCUMULATION_DISTRIBUTION (STOCKNAME, TRADEDDATE, ACCUMULATIONDISTRIBUTION) VALUES('"
					+ stockName + "','" + tradedDate + "'," + accumuationDistribution + ");";
			statement.executeUpdate(tmpsql);
		} catch (Exception ex) {
			HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("storeAccumuationDistributioninDB for quote -> " + stockName + " and Date - > " + tradedDate
					+ " and Error in DB action" + ex);
			logger.error("Error in storeAccumuationDistributioninDB  ->  storeAccumuationDistributioninDB for quote -> " + stockName + " and Date - > " + tradedDate, ex);
		} finally {
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("storeAccumuationDistributioninDB Error in closing statement "+ex);
				logger.error("Error in closing statement storeAccumuationDistributioninDB  -> ", ex);
			}
		}
	}
	
	public boolean isAccumulationDistributionIncreasing(Connection connection, String stockName, Date calculationDate) {
		ResultSet resultSet = null;
		Statement statement = null;
		String sql;
		//Float closePrice, highPrice, lowPrice;
		ArrayList<Float> accumulationDistributionValues = new ArrayList<Float>();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		int counter = 0;
		try {
			if(connection == null) {
				connection = StockUtils.connectToDB();
			}
			if(calculationDate!=null) {
				sql = "SELECT ACCUMULATIONDISTRIBUTION FROM DAILY_ACCUMULATION_DISTRIBUTION where stockname='" + stockName + "' and tradeddate <='" + dateFormat.format(calculationDate) + "' order by tradeddate desc;";
			} else {
				sql = "SELECT ACCUMULATIONDISTRIBUTION FROM DAILY_ACCUMULATION_DISTRIBUTION where stockname ='"
						+ stockName + "' order by tradeddate desc;";
			}		
			
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				accumulationDistributionValues.add(Float.parseFloat(resultSet.getString(1)));
				counter++;
				if(counter>4)
					break;
			}
			
			if((accumulationDistributionValues.get(0) < accumulationDistributionValues.get(1)) || accumulationDistributionValues.get(0) < accumulationDistributionValues.get(accumulationDistributionValues.size()-1))
			{
				return false;
			} else {
				return true;
			}
		} catch (Exception ex) {
			HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("isAccumulationDistributionIncreasing -> Error in DB action"+ex);
			logger.error("Error in isAccumulationDistributionIncreasing  -> ", ex);
			return false;
		} finally {
			try {
				if(resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getStockDetailsFromDBForBulk Error in closing resultset "+ex);
				logger.error("Error in closing resultset getStockDetailsFromDBForBulk  -> ", ex);
			}
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getStockDetailsFromDBForBulk Error in closing statement "+ex);
				logger.error("Error in closing statement getStockDetailsFromDBForBulk  -> ", ex);
			}
		}
	}
}
