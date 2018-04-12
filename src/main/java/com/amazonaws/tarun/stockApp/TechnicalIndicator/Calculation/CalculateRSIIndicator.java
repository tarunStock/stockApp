package com.amazonaws.tarun.stockApp.TechnicalIndicator.Calculation;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

import com.amazonaws.tarun.stockApp.TechnicalIndicator.Data.RSIData;
import com.amazonaws.tarun.stockApp.Utils.HandleErrorDetails;
import com.amazonaws.tarun.stockApp.Utils.StockUtils;



public class CalculateRSIIndicator {

	Connection connection = null;
	static Logger logger = Logger.getLogger(CalculateRSIIndicator.class);
	public static int RSI_PERIOD = 14;
	
	public static void main(String[] args) {
		Date dte = new Date();
		logger.debug("CalculateRSIIndicator Started");
		System.out.println("Start at -> " + dte.toString());
		CalculateRSIIndicator obj = new CalculateRSIIndicator();
		//obj.CalculateRSIForAllStocks(null);
		obj.CalculateRSIForAllStocks(new Date("26-Mar-2018"));
		dte = new Date();
		System.out.println("End at -> " + dte.toString());
		logger.debug("CalculateRSIIndicator End");
	}
	
	public void CalculateRSIForAllStocks(Date calculationDate) {
		ArrayList<String> stockList = null;
		if( !StockUtils.marketOpenOnGivenDate(calculationDate))
			return;
		stockList = StockUtils.getStockListFromDB();
		String stockName;
		String bseCode;
		String nseCode;
		//calculateRSIForStock("RAMGOPOLY", new Date("15-Mar-2018"));
		for (String stockCode : stockList) {
			
			stockName = stockCode.split("!")[1];
			bseCode = stockCode.split("!")[0];
			nseCode = stockCode.split("!")[2];
			System.out.println("Calculating RSI for stock - >"+nseCode);
			//calculate RSI on bulk
			//calculateRSIForStockInBulk(nseCode);
			//calculate average on daily basis
			//calculateRSIForStock(nseCode, new Date("19-Oct-2017"));
			calculateRSIForStock(nseCode, calculationDate);
			
		}
	}
	
	private void calculateRSIForStockInBulk(String stockCode) {
		RSIData stockDetails = null;
		float sumOfLosses = 0, sumOfGains = 0, priceDifference, avgGain = 0, avgLoss = 0, stockRS = 0, stockRSI = 0;
		//Get stock details from dailystockdata table
		stockDetails = getStockDetailsFromDBForBulk(stockCode);

		try {
			if (connection != null) {
				connection.close();
				connection = null;
			}
			connection = StockUtils.connectToDB();
			for (int counter = 1; counter < stockDetails.tradeddate.size(); counter++) {
				priceDifference = stockDetails.closePrice.get(counter) - stockDetails.closePrice.get(counter-1); 
				if( counter <= RSI_PERIOD+1) {			
					if(priceDifference > 0) {
						sumOfGains = sumOfGains + priceDifference;
					} else if(priceDifference < 0) {
						sumOfLosses = sumOfLosses + (priceDifference * -1);
					}
					if( counter == RSI_PERIOD ) {
						avgGain = sumOfGains / RSI_PERIOD;
						avgLoss = sumOfLosses / RSI_PERIOD;
					}
				} else {
					if(priceDifference > 0) {
						avgGain = ((avgGain * (RSI_PERIOD-1)) + priceDifference) / RSI_PERIOD;
						avgLoss = (avgLoss * (RSI_PERIOD-1)) / RSI_PERIOD;
					} else if(priceDifference < 0) {
						avgLoss = ((avgLoss * (RSI_PERIOD-1)) + (priceDifference * -1)) / RSI_PERIOD;
						avgGain = (avgGain * (RSI_PERIOD-1)) / RSI_PERIOD;
					}
				}
				
				if(counter >= RSI_PERIOD) {
					stockRS = avgGain / avgLoss;
					if( avgLoss == 0 ) {
						stockRSI = 100;
					} else {
						stockRSI = 100 - (100/(1+stockRS));
					}
					//Call method to store RS and RSI with period in DB
					System.out.println("Inserting RSI value in DB");
					storeRSIinDB(stockCode, stockDetails.tradeddate.get(counter), stockRS, stockRSI, counter, avgGain, avgLoss);
				}
			}
		} catch (Exception ex) {
			HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("calculateRSIForStockInBulk Error in DB action"+ex);
			logger.error("Error in calculateRSIForStockInBulk  -> ", ex);
		} finally {
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				} 
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("calculateRSIForStockInBulk Error in DB action"+ex);
				logger.error("Error in calculateRSIForStockInBulk  -> ", ex);
			}
		}
	}
	
	private void calculateRSIForStock(String stockCode, Date targetDate) {
		RSIData stockDetails = null;
		float priceDifference, avgGain = 0, avgLoss = 0, stockRS = 0, stockRSI = 0;
		stockDetails = getStockDetailsFromDBForDaily(stockCode, targetDate);
		if(stockDetails!=null) {
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				}
				connection = StockUtils.connectToDB();
				
				priceDifference = stockDetails.closePrice.get(0) - stockDetails.closePrice.get(1); 
				if(priceDifference >= 0) {
					avgGain = ((stockDetails.previousDayAvgGain * (RSI_PERIOD-1)) + priceDifference) / RSI_PERIOD;
					avgLoss = (stockDetails.previousDayAvgLoss * (RSI_PERIOD-1)) / RSI_PERIOD;
				} else if(priceDifference < 0) {
					avgLoss = ((stockDetails.previousDayAvgLoss * (RSI_PERIOD-1)) + (priceDifference * -1)) / RSI_PERIOD;
					avgGain = (stockDetails.previousDayAvgGain * (RSI_PERIOD-1)) / RSI_PERIOD;
				}
				
				
				stockRS = avgGain / avgLoss;
				if( avgLoss == 0 ) {
					stockRSI = 100;
				} else {
					stockRSI = 100 - (100/(1+stockRS));
				}
				//Call method to store RS and RSI with period in DB
				System.out.println("Inserting RSI value in DB");
				storeRSIinDB(stockCode, stockDetails.tradeddate.get(0), stockRS, stockRSI, RSI_PERIOD, avgGain, avgLoss);
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("calculateRSIForStock Error in DB action"+ex);
				logger.error("Error in calculateRSIForStock  -> ", ex);
			} finally {
				try {
					if (connection != null) {
						connection.close();
						connection = null;
					} 
				} catch (Exception ex) {
					HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
					System.out.println("calculateRSIForStock Error in DB action"+ex);
					logger.error("Error in calculateRSIForStock  -> ", ex);
				}
			}
		}
	}
	
	private RSIData getStockDetailsFromDBForBulk(String stockCode) {
		ResultSet resultSet = null;
		Statement statement = null;
		String tradedDate;
		Float closePrice;
		RSIData smaDataObj = null;
		try {
			if (connection != null) {
				connection.close();
				connection = null;
			}
			connection = StockUtils.connectToDB();
			smaDataObj = new RSIData();
			smaDataObj.closePrice = new ArrayList<Float>();
			smaDataObj.tradeddate = new ArrayList<String>();
			statement = connection.createStatement();
			smaDataObj.stockName = stockCode;
			resultSet = statement.executeQuery("SELECT tradeddate, closeprice FROM DAILYSTOCKDATA where stockname='"
					+ stockCode + "' and tradeddate >= '1-Jun-2016' order by tradeddate;");
			while (resultSet.next()) {
				tradedDate = resultSet.getString(1);
				closePrice = Float.parseFloat(resultSet.getString(2));
				smaDataObj.closePrice.add(closePrice);
				smaDataObj.tradeddate.add(tradedDate);
			}
			return smaDataObj;
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
	
	private RSIData getStockDetailsFromDBForDaily(String stockCode, Date SMDate) {
		ResultSet resultSet = null;
		Statement statement = null;
		String tradedDate;
		Float closePrice, avgGain, avgLoss;
		RSIData rsiDataObj = null;
		String tmpSQL;
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		DateFormat dateFormat1 = new SimpleDateFormat("dd-MMM-yyyy");
		
		try {
			if (connection != null) {
				connection.close();
				connection = null;
			}
			connection = StockUtils.connectToDB();
			rsiDataObj = new RSIData();
			rsiDataObj.closePrice = new ArrayList<Float>();
			rsiDataObj.tradeddate = new ArrayList<String>();
			statement = connection.createStatement();
			rsiDataObj.stockName = stockCode;
			if(SMDate!=null) {
				tmpSQL = "SELECT tradeddate, closeprice FROM DAILYSTOCKDATA where stockname='"
						+ stockCode + "' and tradeddate<='" + dateFormat.format(SMDate) +"' order by tradeddate desc limit 2;";
			} else {
				tmpSQL = "SELECT tradeddate, closeprice FROM DAILYSTOCKDATA where stockname='"
							+ stockCode + "' order by tradeddate desc limit 2;";
			}
			resultSet = statement.executeQuery(tmpSQL);
			boolean readFlag = false;
			while (resultSet.next()) {
				tradedDate = resultSet.getString(1);
				Date DBDate = dateFormat.parse(tradedDate);
				Date todayDate = new Date();
				if(!readFlag && SMDate!=null && SMDate.compareTo(DBDate)!=0) {					
					throw new Exception("Stock data not collected for stock =" + stockCode +" for given date ="+SMDate);
				} else if (!readFlag &&  SMDate == null && todayDate.compareTo(DBDate)!=0) {
					throw new Exception("Stock data not collected for stock =" + stockCode +" for current date ="+todayDate);
				}
				readFlag = true;
				closePrice = Float.parseFloat(resultSet.getString(2));
				rsiDataObj.closePrice.add(closePrice);
				rsiDataObj.tradeddate.add(tradedDate);
			}
			resultSet.close();
			resultSet = null;
			statement.close();
			statement = null;
			
			//Get previous day avg gain and loss
			statement = connection.createStatement();
			if(SMDate!=null) {
				tmpSQL = "SELECT tradeddate, avg_gain,avg_loss FROM DAILY_RELATIVE_STRENGTH_INDEX where stockname='"
						+ stockCode + "' and tradeddate<'" + dateFormat.format(SMDate) +"' order by tradeddate desc limit 1;";
			} else {
				tmpSQL = "SELECT tradeddate, avg_gain,avg_loss FROM DAILY_RELATIVE_STRENGTH_INDEX where stockname='"
							+ stockCode + "' order by tradeddate desc limit 1;";
			}
			resultSet = statement.executeQuery(tmpSQL);
			while (resultSet.next()) {
				tradedDate = resultSet.getString(1);
				avgGain = Float.parseFloat(resultSet.getString(2));
				avgLoss = Float.parseFloat(resultSet.getString(3));
				rsiDataObj.previousDayAvgGain = avgGain;
				rsiDataObj.previousDayAvgLoss = avgLoss;
			}
			return rsiDataObj;
		} catch (Exception ex) {
			HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("getStockDetailsFromDBForDaily Error in DB action"+ex);
			logger.error("Error in getStockDetailsFromDBForDaily  -> ", ex);
			return null;
		} finally {
			try {
				if(resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getStockDetailsFromDBForDaily Error in closing resultset "+ex);
				logger.error("Error in closing resultset getStockDetailsFromDBForDaily  -> ", ex);
			}
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getStockDetailsFromDBForBulk Error in closing statement "+ex);
				logger.error("Error in closing statement getStockDetailsFromDBForDaily  -> ", ex);
			}
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				} 
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getStockDetailsFromDBForBulk Error in closing connection "+ex);
				logger.error("Error in closing connection getStockDetailsFromDBForDaily  -> ", ex);
			}
		}
	}
	
	private void storeRSIinDB(String stockName, String tradedDate, float RS, float RSI, int period, float avgGain, float avgLoss) {
		Statement statement = null;
		String tmpsql;
		try {
			statement = connection.createStatement();
			tmpsql = "INSERT INTO DAILY_RELATIVE_STRENGTH_INDEX (STOCKNAME, TRADEDDATE, PERIOD, STOCKRS, STOCKRSI, AVG_GAIN, AVG_LOSS) VALUES('"
					+ stockName + "','" + tradedDate + "'," + period + "," + RS + "," + RSI + "," + avgGain + "," + avgLoss + ");";
			statement.executeUpdate(tmpsql);
			
		} catch (Exception ex) {
			HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("storeRSIinDB for quote -> " + stockName + " and Date - > " + tradedDate
					+ " and period  - > " + period + " Error in DB action" + ex);
			logger.error("Error in storeRSIinDB  ->  storeRSIinDB for quote -> " + stockName + " and Date - > " + tradedDate
					+ " and period  - > " + period, ex);
		} finally {
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("storeRSIinDB Error in closing statement "+ex);
				logger.error("Error in closing statement storeRSIinDB  -> ", ex);
			}
		}
	}
	
	public float getRSIValue(String stockCode, LocalDate objDate) {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		//DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
		String tmpSQL;
		float stockRSI = 0;
		
		try {
			connection = StockUtils.connectToDB();
			if(objDate!=null) {
				tmpSQL = "SELECT STOCKRSI FROM DAILY_RELATIVE_STRENGTH_INDEX where stockname='"	+ stockCode + "' and tradeddate='" + objDate.toString() +"' and period =" + RSI_PERIOD + ";";
			} else {
				tmpSQL = "SELECT STOCKRSI FROM DAILY_RELATIVE_STRENGTH_INDEX where stockname='"	+ stockCode + "' and period =" + RSI_PERIOD + " order by tradeddate desc limit 1;";
			}
			
			statement = connection.createStatement();
			resultSet = statement.executeQuery(tmpSQL);
			while (resultSet.next()) {
				stockRSI = Float.parseFloat(resultSet.getString(1));
			}
			return stockRSI;
		} catch (Exception ex) {
			HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("getRSIValue Error in DB action"+ex);
			logger.error("Error in getRSIValue  -> ", ex);
			return 0;
		} finally {
			try {
				if(resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getRSIValue Error in closing resultset "+ex);
				logger.error("Error in closing resultset getRSIValue  -> ", ex);
			}
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getRSIValue Error in closing statement "+ex);
				logger.error("Error in closing statement getRSIValue  -> ", ex);
			}
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
	}
	
}
