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

import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;

import com.amazonaws.tarun.stockApp.TechnicalIndicator.Data.StochasticOscillatorData;
import com.amazonaws.tarun.stockApp.Utils.HandleErrorDetails;
import com.amazonaws.tarun.stockApp.Utils.StockUtils;

public class CalculateAverageTrueRange {
	Connection connection = null;
	static Logger logger = Logger.getLogger(CalculateAverageTrueRange.class);
	public static int ATR_PERIOD = 14;
	public static int CHANDLIER_MULTIPLIER = 3;
	
	public static void main(String[] args) {
		Date dte = new Date();
		logger.debug("CalculateAverageTrueRange Started");
		System.out.println("Start at -> " + dte.toString());
		CalculateAverageTrueRange obj = new CalculateAverageTrueRange();
		//obj.calculateAverageTrueRangeForAllStocks(null);
		obj.calculateAverageTrueRangeForAllStocks(new Date("26-Mar-2018"));
		//obj.getChandelierExitLong("20MICRONS", new Date("16-Oct-2017"));
		HandleErrorDetails.sendErrorsInMail("Calculate Average True Range");
		dte = new Date();
		System.out.println("End at -> " + dte.toString());
		logger.debug("CalculateAverageTrueRange End");
	}
	
	public void calculateAverageTrueRangeForAllStocks(Date calculationDate) {
		ArrayList<String> stockList = null;
		if( !StockUtils.marketOpenOnGivenDate(calculationDate))
			return;
		stockList = StockUtils.getStockListFromDB();
		String stockName;
		String bseCode;
		String nseCode;

		for (String stockCode : stockList) {
			
			stockName = stockCode.split("!")[1];
			bseCode = stockCode.split("!")[0];
			nseCode = stockCode.split("!")[2];
			System.out.println("Calculating Average True Range for stock - >"+nseCode);
			//calculate RSI on bulk
			//calculateAverageTrueRangeForStockInBulk(nseCode);
//			//calculate average on daily basis
			
			//calculateAverageTrueRangeForStockDaily(nseCode, new Date("19-Oct-2017"));
			calculateAverageTrueRangeForStockDaily(nseCode, calculationDate);
		}
	}
	
	private void calculateAverageTrueRangeForStockInBulk(String stockCode) {
		StochasticOscillatorData stockDetails = null;
		float currentHighLowDifference = 0, currentHighAndPreviousCloseDifference = 0, currentLowAndPreviousCloseDifference = 0, trueRange = 0, sumOfTrueRange = 0, averageTrueRange = 0;
		//Get stock details from dailystockdata table
		stockDetails = getStockDetailsFromDBForBulk(stockCode);
		ArrayList<Float> highestHighArr, lowestLowArr;
		Comparator<Float> comparatorForLow = Collections.reverseOrder();
		try {
			if (connection != null) {
				connection.close();
				connection = null;
			} 
			connection = StockUtils.connectToDB();
			for (int counter = 0; counter < stockDetails.tradeddate.size(); counter++) {
				if(counter < ATR_PERIOD) {
					currentHighLowDifference = stockDetails.highPrice.get(counter) - stockDetails.lowPrice.get(counter);
					if(counter > 0) {
						currentHighAndPreviousCloseDifference = Math.abs(stockDetails.highPrice.get(counter) - stockDetails.closePrice.get(counter-1));
						currentLowAndPreviousCloseDifference =  Math.abs(stockDetails.lowPrice.get(counter) - stockDetails.closePrice.get(counter-1));
					}
					trueRange =  Math.max(currentLowAndPreviousCloseDifference, Math.max(currentHighLowDifference, currentHighAndPreviousCloseDifference));
					sumOfTrueRange = sumOfTrueRange + trueRange;
					if(counter == ATR_PERIOD-1) {
						System.out.println("Sum of true range -> "+sumOfTrueRange);
						averageTrueRange = sumOfTrueRange / ATR_PERIOD;
						//Store ATR in DB
						System.out.println("Inserting oschillator value in DB Date-> "+ stockDetails.tradeddate.get(counter) + " ATR -> "+ averageTrueRange );
						storeATRinDB(stockCode, stockDetails.tradeddate.get(counter), ATR_PERIOD, averageTrueRange);
					}
				} else {
					currentHighLowDifference = stockDetails.highPrice.get(counter) - stockDetails.lowPrice.get(counter);
					currentHighAndPreviousCloseDifference = Math.abs(stockDetails.highPrice.get(counter) - stockDetails.closePrice.get(counter-1));
					currentLowAndPreviousCloseDifference =  Math.abs(stockDetails.lowPrice.get(counter) - stockDetails.closePrice.get(counter-1));
					trueRange =  Math.max(currentLowAndPreviousCloseDifference, Math.max(currentHighLowDifference, currentHighAndPreviousCloseDifference));
					averageTrueRange = ((averageTrueRange* (ATR_PERIOD-1)) + trueRange) / ATR_PERIOD;
					System.out.println("Inserting oschillator value in DB Date-> "+ stockDetails.tradeddate.get(counter) + " ATR -> "+ averageTrueRange );
					
					storeATRinDB(stockCode, stockDetails.tradeddate.get(counter), ATR_PERIOD, averageTrueRange);
				}
				
			
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
	
	private StochasticOscillatorData getStockDetailsFromDBForBulk(String stockCode) {
		ResultSet resultSet = null;
		Statement statement = null;
		String tradedDate;
		Float closePrice, highPrice, lowPrice;
		StochasticOscillatorData soDataObj = null;
		try {
			if (connection != null) {
				connection.close();
				connection = null;
			}
			connection = StockUtils.connectToDB();
			soDataObj = new StochasticOscillatorData();
			soDataObj.closePrice = new ArrayList<Float>();
			soDataObj.tradeddate = new ArrayList<String>();
			soDataObj.highPrice = new ArrayList<Float>();
			soDataObj.lowPrice = new ArrayList<Float>();			
			statement = connection.createStatement();
			soDataObj.stockName = stockCode;
			resultSet = statement.executeQuery("SELECT tradeddate, closeprice, HIGHPRICE, LOWPRICE FROM DAILYSTOCKDATA where stockname='"
					+ stockCode + "' and tradeddate >= '1-Jun-2016' order by tradeddate;");
			while (resultSet.next()) {
				tradedDate = resultSet.getString(1);
				closePrice = Float.parseFloat(resultSet.getString(2));
				highPrice = Float.parseFloat(resultSet.getString(3));
				lowPrice = Float.parseFloat(resultSet.getString(4));
				soDataObj.closePrice.add(closePrice);
				soDataObj.tradeddate.add(tradedDate);
				soDataObj.highPrice.add(highPrice);
				soDataObj.lowPrice.add(lowPrice);
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
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				} 
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getStockDetailsFromDBForBulk Error in closing connection "+ex);
				logger.error("Error in closing connection getStockDetailsFromDBForBulk  -> ", ex);
			}
		}
	}
	
	private void calculateAverageTrueRangeForStockDaily(String stockCode, Date targetDate) {
		StochasticOscillatorData stockDetails = null;
		float currentHighLowDifference = 0, currentHighAndPreviousCloseDifference = 0, currentLowAndPreviousCloseDifference = 0, trueRange = 0, averageTrueRange = 0, previousDayATR = 0;;
		
		try {
			if (connection != null) {
				connection.close();
				connection = null;
			}
			connection = StockUtils.connectToDB();
			//Get stock details from dailystockdata table'
			stockDetails = getStockDetailsFromDBDaily(stockCode, targetDate);
			//Get previous day ATR
			if(targetDate != null) {
				previousDayATR = getATR (stockCode, DateUtils.addDays(targetDate,-1));
			} else {
				previousDayATR = getATR (stockCode, DateUtils.addDays(new Date(),-1));
			}			
			currentHighLowDifference = stockDetails.highPrice.get(0) - stockDetails.lowPrice.get(0);
			currentHighAndPreviousCloseDifference = Math.abs(stockDetails.highPrice.get(0) - stockDetails.closePrice.get(1));
			currentLowAndPreviousCloseDifference =  Math.abs(stockDetails.lowPrice.get(0) - stockDetails.closePrice.get(1));
			trueRange =  Math.max(currentLowAndPreviousCloseDifference, Math.max(currentHighLowDifference, currentHighAndPreviousCloseDifference));
			averageTrueRange = ((previousDayATR * (ATR_PERIOD-1)) + trueRange) / ATR_PERIOD;
			System.out.println("Inserting ATR value in DB Date-> "+ stockDetails.tradeddate.get(0) + " ATR -> "+ averageTrueRange );
					
			//Call method to store stochastic oscillator with period in DB
			System.out.println("Inserting ATR value in DB");
			storeATRinDB(stockCode, stockDetails.tradeddate.get(0), ATR_PERIOD, averageTrueRange);
		} catch (Exception ex) {
			HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("calculateAverageTrueRangeForStockDaily Error in DB action "+ex);
			logger.error("Error in calculateAverageTrueRangeForStockDaily  -> ", ex);
		} finally {
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				} 
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("calculateAverageTrueRangeForStockDaily Error in DB action ");
				logger.error("Error in calculateAverageTrueRangeForStockDaily  -> ", ex);
			}
		}
	}
	
	private StochasticOscillatorData getStockDetailsFromDBDaily(String stockCode, Date calculationDate) {
		ResultSet resultSet = null;
		Statement statement = null;
		String tradedDate;
		Float closePrice, highPrice, lowPrice;
		StochasticOscillatorData soDataObj = null;
		String tmpSQL;
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		try {
			soDataObj = new StochasticOscillatorData();
			soDataObj.closePrice = new ArrayList<Float>();
			soDataObj.tradeddate = new ArrayList<String>();
			soDataObj.highPrice = new ArrayList<Float>();
			soDataObj.lowPrice = new ArrayList<Float>();			
			statement = connection.createStatement();
			soDataObj.stockName = stockCode;
			if(calculationDate!=null) {
				tmpSQL = "SELECT tradeddate, closeprice, HIGHPRICE, LOWPRICE FROM DAILYSTOCKDATA where stockname='"
						+ stockCode + "'  and tradeddate<='" + dateFormat.format(calculationDate) +"' order by tradeddate desc limit " + ATR_PERIOD + ";";
			} else {
				tmpSQL = "SELECT tradeddate, closeprice, HIGHPRICE, LOWPRICE FROM DAILYSTOCKDATA where stockname='"
						+ stockCode + "' order by tradeddate desc limit " + ATR_PERIOD + ";";
			}
			resultSet = statement.executeQuery(tmpSQL);
			while (resultSet.next()) {
				tradedDate = resultSet.getString(1);
				closePrice = Float.parseFloat(resultSet.getString(2));
				highPrice = Float.parseFloat(resultSet.getString(3));
				lowPrice = Float.parseFloat(resultSet.getString(4));
				soDataObj.closePrice.add(closePrice);
				soDataObj.tradeddate.add(tradedDate);
				soDataObj.highPrice.add(highPrice);
				soDataObj.lowPrice.add(lowPrice);
			}
			return soDataObj;
		} catch (Exception ex) {
			HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("getStockDetailsFromDBDaily -> Error in DB action"+ex);
			logger.error("Error in getStockDetailsFromDBDaily  -> ", ex);
			return null;
		} finally {
			try {
				if(resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getStockDetailsFromDBDaily Error in closing resultset "+ex);
				logger.error("Error in closing resultset getStockDetailsFromDBDaily  -> ", ex);
			}
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getStockDetailsFromDBDaily Error in closing statement "+ex);
				logger.error("Error in closing statement getStockDetailsFromDBDaily  -> ", ex);
			}
		}
	}
	
	private float getATR(String stockCode, Date targetDate) {
		ResultSet resultSet = null;
		Statement statement = null;
		//Date tradedDate = DateUtils.addDays(targetDate, -1);//new DateTime(targetDate).minusDays(1).toDate();
		float previousDayATR = 0;
		String tmpSQL;
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		try {		
			statement = connection.createStatement();
			tmpSQL = "SELECT ATR FROM DAILY_AVERAGE_TRUE_RANGE where stockname='"
					+ stockCode + "'  and tradeddate<='" + dateFormat.format(targetDate) +"' order by tradeddate desc limit 1;";
			resultSet = statement.executeQuery(tmpSQL);
			while (resultSet.next()) {
				previousDayATR = Float.parseFloat(resultSet.getString(1));
			}
			return previousDayATR;
		} catch (Exception ex) {
			HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("getATR -> Error in DB action"+ex);
			logger.error("Error in getATR  -> ", ex);
			return 0;
		} finally {
			try {
				if(resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getATR Error in closing resultset "+ex);
				logger.error("Error in closing resultset getATR  -> ", ex);
			}
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getATR Error in closing statement "+ex);
				logger.error("Error in closing statement getATR  -> ", ex);
			}
		}
	}
	
	private void storeATRinDB(String stockName, String tradedDate, int period, float atr) {
		Statement statement = null;
		String tmpsql;
		try {
			statement = connection.createStatement();
			tmpsql = "INSERT INTO DAILY_AVERAGE_TRUE_RANGE (STOCKNAME, TRADEDDATE, PERIOD, ATR) VALUES('"
					+ stockName + "','" + tradedDate + "'," + period + "," + atr + ");";
			statement.executeUpdate(tmpsql);
		} catch (Exception ex) {
			HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("storeATRinDB for quote -> " + stockName + " and Date - > " + tradedDate
					+ " and period  - > " + period + " Error in DB action" + ex);
			logger.error("Error in storeATRinDB  ->  storeRSIinDB for quote -> " + stockName + " and Date - > " + tradedDate
					+ " and period  - > " + period, ex);
		} finally {
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("storeATRinDB Error in closing statement "+ex);
				logger.error("Error in closing statement storeATRinDB  -> ", ex);
			}
		}
	}
	
	public float getChandelierExitLong(String stockCode, Date targetDate) {
		
		StochasticOscillatorData stockDetails = null;
		float dayATR = 0;;
		float chandelierExitLong = 0, highestHigh = 0;
		try {
			if (connection != null) {
				connection.close();
				connection = null;
			}
			connection = StockUtils.connectToDB();
			//Get stock details from dailystockdata table'
			stockDetails = getStockDetailsFromDBDaily(stockCode, targetDate);
			Comparator<Float> comparatorForLow = Collections.reverseOrder();
			Collections.sort(stockDetails.highPrice, comparatorForLow);
			
			highestHigh = stockDetails.highPrice.get(0);
			
			//Get previous day ATR
			if(targetDate != null) {
				dayATR = getATR (stockCode, targetDate);
			} else {
				dayATR = getATR (stockCode, new Date());
			}			
			chandelierExitLong = highestHigh - (dayATR * CHANDLIER_MULTIPLIER);
			
			System.out.println("chandelierExitLong-> "+ chandelierExitLong );					
			return chandelierExitLong;
		} catch (Exception ex) {
			HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("getChandelierExitLong Error in DB action "+ex);
			logger.error("Error in getChandelierExitLong  -> ", ex);
			return 0;
		} finally {
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				} 
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getChandelierExitLong Error in DB action ");
				logger.error("Error in getChandelierExitLong  -> ", ex);
			}
		}
	}
	
	public float getChandelierExitShort(String stockCode, Date targetDate) {
		StochasticOscillatorData stockDetails = null;
		float dayATR = 0;;
		float chandelierExitShort = 0, lowestLow = 0;
		try {
			if (connection != null) {
				connection.close();
				connection = null;
			}
			connection = StockUtils.connectToDB();
			//Get stock details from dailystockdata table'
			stockDetails = getStockDetailsFromDBDaily(stockCode, targetDate);
			Collections.sort(stockDetails.lowPrice);
			
			lowestLow = stockDetails.lowPrice.get(0);
			
			//Get previous day ATR
			if(targetDate != null) {
				dayATR = getATR (stockCode, targetDate);
			} else {
				dayATR = getATR (stockCode, new Date());
			}			
			chandelierExitShort = lowestLow + (dayATR * CHANDLIER_MULTIPLIER);
			
			System.out.println("chandelierExitShort-> "+ chandelierExitShort );					
			return chandelierExitShort;
		} catch (Exception ex) {
			HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("getChandelierExitShort Error in DB action "+ex);
			logger.error("Error in getChandelierExitShort  -> ", ex);
			return 0;
		} finally {
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				} 
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getChandelierExitShort Error in DB action ");
				logger.error("Error in getChandelierExitShort  -> ", ex);
			}
		}
	}
}
