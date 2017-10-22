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

import com.amazonaws.tarun.stockApp.TechnicalIndicator.Data.StochasticOscillatorData;
import com.amazonaws.tarun.stockApp.Utils.StockUtils;

public class CalculateStochasticOscillator {
	Connection connection = null;
	static Logger logger = Logger.getLogger(CalculateStochasticOscillator.class);
	public static int STOCHASTIC_PERIOD = 14;
	
	public static void main(String[] args) {
		Date dte = new Date();
		logger.debug("CalculateStochasticOscillator Started");
		System.out.println("Start at -> " + dte.toString());
		CalculateStochasticOscillator obj = new CalculateStochasticOscillator();
		obj.CalculateStochasticOscillatorForAllStocks(null);
		//obj.getStochasticIndicator("HATSUN",new Date("04-Oct-2017"));
		dte = new Date();
		System.out.println("End at -> " + dte.toString());
		logger.debug("CalculateStochasticOscillator End");
	}
	
	public void CalculateStochasticOscillatorForAllStocks(Date calculationDate) {
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
			System.out.println("Calculating Stochastic oscillator for stock - >"+nseCode);
			//calculate RSI on bulk
			//calculateStochasticOscillatorForStockInBulk(nseCode);
			//calculate average on daily basis
			//calculateStochasticOscillatorForStockDaily(nseCode, new Date("19-Oct-2017"));
			calculateStochasticOscillatorForStockDaily(nseCode, calculationDate);
		}
	}
	
	private void calculateStochasticOscillatorForStockInBulk(String stockCode) {
		StochasticOscillatorData stockDetails = null;
		float lowestLow = 0, highestHigh = 0, stochasticOscillator;
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
			for (int counter = STOCHASTIC_PERIOD-1; counter < stockDetails.tradeddate.size(); counter++) {
				highestHighArr =  new ArrayList<>(stockDetails.highPrice.subList((counter+1) - STOCHASTIC_PERIOD, counter+1));
				lowestLowArr =   new ArrayList<>(stockDetails.lowPrice.subList((counter+1) - STOCHASTIC_PERIOD, counter+1));
				Collections.sort(highestHighArr, comparatorForLow);
				Collections.sort(lowestLowArr);
				
				highestHigh = highestHighArr.get(0);
				lowestLow = lowestLowArr.get(0);				
				
				stochasticOscillator = ((stockDetails.closePrice.get(counter) - lowestLow)/(highestHigh - lowestLow)) * 100;
				//Call method to store stochastic oscillator with period in DB
				System.out.println("Inserting oschillator value in DB");
				storeStochasticOscillatorinDB(stockCode, stockDetails.tradeddate.get(counter), STOCHASTIC_PERIOD, stochasticOscillator);
			
			}
		} catch (Exception ex) {
			System.out.println("calculateStochasticOscillatorForStockInBulk Error in DB action "+ex);
			logger.error("Error in getBBIndicationForStock  -> ", ex);
		} finally {
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				} 
			} catch (Exception ex) {
				System.out.println("calculateStochasticOscillatorForStockInBulk Error in DB action ");
				logger.error("Error in getStockDetailsFromDB  -> ", ex);
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
				System.out.println("getStockDetailsFromDBForBulk Error in closing resultset "+ex);
				logger.error("Error in closing resultset getStockDetailsFromDB  -> ", ex);
			}
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				System.out.println("getStockDetailsFromDBForBulk Error in closing statement "+ex);
				logger.error("Error in closing statement getStockDetailsFromDB  -> ", ex);
			}
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				} 
			} catch (Exception ex) {
				System.out.println("getStockDetailsFromDBForBulk Error in closing connection "+ex);
				logger.error("Error in closing connection getStockDetailsFromDB  -> ", ex);
			}
		}
	}
	
	private void calculateStochasticOscillatorForStockDaily(String stockCode, Date targetDate) {
		StochasticOscillatorData stockDetails = null;
		float lowestLow = 0, highestHigh = 0, stochasticOscillator;
		//Get stock details from dailystockdata table
		stockDetails = getStockDetailsFromDBDaily(stockCode, targetDate);
		Comparator<Float> comparatorForLow = Collections.reverseOrder();
		try {
			if (connection != null) {
				connection.close();
				connection = null;
			}
			connection = StockUtils.connectToDB();
			Collections.sort(stockDetails.highPrice, comparatorForLow);
			Collections.sort(stockDetails.lowPrice);
			
			highestHigh = stockDetails.highPrice.get(0);
			lowestLow = stockDetails.lowPrice.get(0);				
			
			stochasticOscillator = ((stockDetails.closePrice.get(0) - lowestLow)/(highestHigh - lowestLow)) * 100;
			//Call method to store stochastic oscillator with period in DB
			System.out.println("Inserting oschillator value in DB");
			storeStochasticOscillatorinDB(stockCode, stockDetails.tradeddate.get(0), STOCHASTIC_PERIOD, stochasticOscillator);
		} catch (Exception ex) {
			System.out.println("calculateStochasticOscillatorForStockInBulk Error in DB action "+ex);
			logger.error("Error in getBBIndicationForStock  -> ", ex);
		} finally {
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				} 
			} catch (Exception ex) {
				System.out.println("calculateStochasticOscillatorForStockInBulk Error in DB action ");
				logger.error("Error in getStockDetailsFromDB  -> ", ex);
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
			if(calculationDate!=null) {
				tmpSQL = "SELECT tradeddate, closeprice, HIGHPRICE, LOWPRICE FROM DAILYSTOCKDATA where stockname='"
						+ stockCode + "'  and tradeddate<='" + dateFormat.format(calculationDate) +"' order by tradeddate desc limit " + STOCHASTIC_PERIOD + ";";
			} else {
				tmpSQL = "SELECT tradeddate, closeprice, HIGHPRICE, LOWPRICE FROM DAILYSTOCKDATA where stockname='"
						+ stockCode + "' order by tradeddate desc limit " + STOCHASTIC_PERIOD + ";";
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
				System.out.println("getStockDetailsFromDBForBulk Error in closing resultset "+ex);
				logger.error("Error in closing resultset getStockDetailsFromDB  -> ", ex);
			}
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				System.out.println("getStockDetailsFromDBForBulk Error in closing statement "+ex);
				logger.error("Error in closing statement getStockDetailsFromDB  -> ", ex);
			}
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				} 
			} catch (Exception ex) {
				System.out.println("getStockDetailsFromDBForBulk Error in closing connection "+ex);
				logger.error("Error in closing connection getStockDetailsFromDB  -> ", ex);
			}
		}
	}
	
	private void storeStochasticOscillatorinDB(String stockName, String tradedDate, int period, float stochasticOscillator) {
		Statement statement = null;
		String tmpsql;
		try {
			statement = connection.createStatement();
			tmpsql = "INSERT INTO DAILY_STOCHASTIC_OSCILLATOR (STOCKNAME, TRADEDDATE, PERIOD, STOCHASTIC_OSCILLATOR) VALUES('"
					+ stockName + "','" + tradedDate + "'," + period + "," + stochasticOscillator + ");";
			statement.executeUpdate(tmpsql);			
		} catch (Exception ex) {
			System.out.println("storeStochasticOscillatorinDB for quote -> " + stockName + " and Date - > " + tradedDate
					+ " and period  - > " + period + " Error in DB action" + ex);
			logger.error("Error in storeStochasticOscillatorinDB  ->  storeRSIinDB for quote -> " + stockName + " and Date - > " + tradedDate
					+ " and period  - > " + period, ex);
		} finally {
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				System.out.println("storeStochasticOscillatorinDB Error in closing statement "+ex);
				logger.error("Error in closing statement storeStochasticOscillatorinDB  -> ", ex);
			}
		}
	}
	
	public boolean getStochasticIndicator (String stockCode, Date targetDate) {
		ResultSet resultSet = null;
		Statement statement = null;
		
		float recentStochasticOscVal=0, recentClosePrice=0, previousStochasticOscVal=0, previousClosePrice=0;
		String tmpSQL;
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		try {
			if (connection != null) {
				connection.close();
				connection = null;
			}
			connection = StockUtils.connectToDB();						
			statement = connection.createStatement();
			if(targetDate!=null) {
				tmpSQL = "SELECT dsp.closeprice, osc.STOCHASTIC_OSCILLATOR FROM DAILY_STOCHASTIC_OSCILLATOR as osc, DAILYSTOCKDATA as dsp where osc.stockname='"
						+ stockCode + "' and dsp.stockname='" + stockCode + "' and osc.tradeddate<='" + dateFormat.format(targetDate) +"' and dsp.tradeddate<='" 
						+ dateFormat.format(targetDate) +"' and osc.TRADEDDATE = dsp.TRADEDDATE order by dsp.tradeddate desc limit 2;";
			} else {
				tmpSQL = "SELECT dsp.closeprice, osc.STOCHASTIC_OSCILLATOR FROM DAILYDAILY_STOCHASTIC_OSCILLATORSTOCKDATA as osc, DAILYSTOCKDATA as dsp where osc.stockname='"
						+ stockCode + "' and dsp.stockname='" + stockCode + "' osc.TRADEDDATE = dsp.TRADEDDATE order by dsp.tradeddate desc limit 2;";
			}
			resultSet = statement.executeQuery(tmpSQL);
			if( resultSet.next()) {
				recentStochasticOscVal = Float.parseFloat(resultSet.getString(2));
				recentClosePrice = Float.parseFloat(resultSet.getString(1));
			}
			if( resultSet.next()) {
				previousStochasticOscVal = Float.parseFloat(resultSet.getString(2));
				previousClosePrice = Float.parseFloat(resultSet.getString(1));
			}
			if( (recentClosePrice - previousClosePrice>0) && (recentStochasticOscVal-previousStochasticOscVal<0) ) {
				return false;
			} else {
				return true;
			}
		} catch (Exception ex) {
			System.out.println("getStockDetailsFromDBForBulk -> Error in DB action"+ex);
			logger.error("Error in getStockDetailsFromDBForBulk  -> ", ex);
			return false;
		} finally {
			try {
				if(resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
			} catch (Exception ex) {
				System.out.println("getStockDetailsFromDBForBulk Error in closing resultset "+ex);
				logger.error("Error in closing resultset getStockDetailsFromDB  -> ", ex);
			}
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				System.out.println("getStockDetailsFromDBForBulk Error in closing statement "+ex);
				logger.error("Error in closing statement getStockDetailsFromDB  -> ", ex);
			}
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				} 
			} catch (Exception ex) {
				System.out.println("getStockDetailsFromDBForBulk Error in closing connection "+ex);
				logger.error("Error in closing connection getStockDetailsFromDB  -> ", ex);
			}
		}
	}
}
