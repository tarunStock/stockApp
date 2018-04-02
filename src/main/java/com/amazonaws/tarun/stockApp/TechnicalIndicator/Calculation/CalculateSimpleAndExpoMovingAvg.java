package com.amazonaws.tarun.stockApp.TechnicalIndicator.Calculation;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import org.apache.log4j.Logger;
import com.amazonaws.tarun.stockApp.TechnicalIndicator.Data.SMAData;
import com.amazonaws.tarun.stockApp.Utils.HandleErrorDetails;
import com.amazonaws.tarun.stockApp.Utils.StockUtils;

public class CalculateSimpleAndExpoMovingAvg {
	Connection connection = null;
	static Logger logger = Logger.getLogger(CalculateSimpleAndExpoMovingAvg.class);
	float simpleMovingAverageForTwelve = 0;
	float simpleMovingAverageForTwentySix = 0;
	
	public static void main(String[] args) {
		Date dte = new Date();
		logger.debug("CalculateSimpleAndExpoMovingAvg Started");
		System.out.println("Start at -> " + dte.toString());
		CalculateSimpleAndExpoMovingAvg obj = new CalculateSimpleAndExpoMovingAvg();
		obj.MovingAverageCalculation(new Date("26-Mar-2018"));
		//obj.MovingAverageCalculation(null);
		dte = new Date();
		System.out.println("End at -> " + dte.toString());
		logger.debug("CalculateSimpleAndExpoMovingAvg End");
	}
	
	public void MovingAverageCalculation(Date calculationDate){
		ArrayList<String> stockList = null;
		if( !StockUtils.marketOpenOnGivenDate(calculationDate))
			return;
		stockList = StockUtils.getStockListFromDB();
		String stockName;
		String bseCode;
		String nseCode;

		//For Testing
		//calculateSimpleMovingAverageDaily("MINDACORP", new Date("31-Oct-2017"));
		
		for (String stockCode : stockList) {
			stockName = stockCode.split("!")[1];
			bseCode = stockCode.split("!")[0];
			nseCode = stockCode.split("!")[2];
			//calculate average on bulk
			//calculateSimpleMovingAverage(nseCode, calculationDate);
			//calculateExpMovingAverageForMACD(nseCode, calculationDate);
			//calculate average on daily basis
			
			//calculateSimpleMovingAverageDaily(nseCode, new Date("19-Oct-2017"));
			calculateSimpleMovingAverageDaily(nseCode, calculationDate);
		}
	}

	private void calculateSimpleMovingAverage(String stockCode, Date targetDate) {
		SMAData stockDetails = null;
		float simpleMovingAverage = 0;
		int period = 1;
		float sumOfClosingPrices = 0;
		float expMovingAvg = 0;
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		
		
		ArrayList<Integer> smaPeriods;
		try {
			if (connection != null) {
				connection.close();
				connection = null;
			}
			connection = StockUtils.connectToDB();
			stockDetails = getStockDetailsFromDB(stockCode, targetDate);
			smaPeriods = GetSMAPeriodsFromDB(stockCode);
			for (int counter = 0; counter < stockDetails.tradeddate.size(); counter++) {
				period = 1;
				sumOfClosingPrices = 0;
				System.out.println(" Stock -> " + stockCode + " Date -> " + stockDetails.tradeddate.get(counter));
				
				for (int counter1 = 0; counter1 < smaPeriods.size(); counter1++) {
					//sumOfClosingPrices = sumOfClosingPrices + stockDetails.closePrice.get(counter1);
					//if(smaPeriods.contains(period)) {
					/*if (period != 3 && period != 5 && period != 9 && period != 14 && period != 20 && period != 50
							&& period != 200) {*/
				//For testing	
				System.out.print(" ** Period -> "+ smaPeriods.get(counter1));	
					simpleMovingAverage = sumOfClosingPrices / period;
					//For testing	
					expMovingAvg = calculateExpMvingAvg(stockCode, stockDetails.closePrice.get(counter), smaPeriods.get(counter1), (Date)formatter.parse(stockDetails.tradeddate.get(counter)));
					
					//Remove after testing
					//expMovingAvg = calculateExpMvingAvg(stockCode, stockDetails.closePrice.get(counter), 20, (Date)formatter.parse(stockDetails.tradeddate.get(counter)));
						if (expMovingAvg == -1) {
							expMovingAvg = simpleMovingAverage;
						}
						updateOrInsertExpMovingAverageinDB(stockCode, stockDetails.tradeddate.get(counter), 
								smaPeriods.get(counter1), stockDetails.closePrice.get(counter).floatValue(), expMovingAvg);
						/*storeMovingAverageinDB(stockCode, stockDetails.tradeddate.get(counter1), simpleMovingAverage,
								period, stockDetails.closePrice.get(counter1).floatValue(), expMovingAvg);*/
					//}
					/*period++;
					if (period > 200) {
						break;
					}*/

				}
			}
		}catch (Exception ex) {
			HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("calculateSimpleMovingAverage Error in DB action"+ex);
			logger.error("Error in calculateSimpleMovingAverage  -> ", ex);
		} finally {
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				} 
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("calculateSimpleMovingAverage Error in DB action"+ex);
				logger.error("Error in calculateSimpleMovingAverage  -> ", ex);
			}
		}
	}

	private void calculateExpMovingAverageForMACD(String stockCode, Date targetDate) {
		SMAData stockDetails = null;
		
		int period = 1;
		float sumOfClosingPrices = 0;
		float expMovingAvg = 0;
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		
		ArrayList<Integer> smaPeriods= new ArrayList<Integer>();
		try {
			if (connection != null) {
				connection.close();
				connection = null;
			}
			connection = StockUtils.connectToDB();
			stockDetails = getStockDetailsFromDB(stockCode, targetDate);
			smaPeriods.add(12);
			smaPeriods.add(26);
			
			calculateTwelveAndTwentySixSMA(stockCode);
			//smaPeriods = GetSMAPeriodsFromDB(stockCode);
			for (int counter = 0; counter < stockDetails.tradeddate.size(); counter++) {
				period = 1;
				sumOfClosingPrices = 0;
				//System.out.println(" Stock -> " + stockCode + " Date -> " + stockDetails.tradeddate.get(counter));
				
				for (int counter1 = 0; counter1 < smaPeriods.size(); counter1++) {
					//sumOfClosingPrices = sumOfClosingPrices + stockDetails.closePrice.get(counter1);
					//if(smaPeriods.contains(period)) {
					/*if (period != 3 && period != 5 && period != 9 && period != 14 && period != 20 && period != 50
							&& period != 200) {*/
				//For testing	
				//System.out.print(" ** Period -> "+ smaPeriods.get(counter1));	
					//simpleMovingAverage = sumOfClosingPrices / period;
					//For testing	
					expMovingAvg = calculateExpMvingAvg(stockCode, stockDetails.closePrice.get(counter), smaPeriods.get(counter1), (Date)formatter.parse(stockDetails.tradeddate.get(counter)));
					
					//Remove after testing
					//expMovingAvg = calculateExpMvingAvg(stockCode, stockDetails.closePrice.get(counter), 20, (Date)formatter.parse(stockDetails.tradeddate.get(counter)));
						if (expMovingAvg == -1 && smaPeriods.get(counter1) == 12) {
							expMovingAvg = simpleMovingAverageForTwelve;
						} else if (expMovingAvg == -1 && smaPeriods.get(counter1) == 26) {
							expMovingAvg = simpleMovingAverageForTwentySix;
						}
						System.out.println(" Date -> " + stockDetails.tradeddate.get(counter) + " period = "+smaPeriods.get(counter1) + " EMA->" + expMovingAvg);
						/*updateOrInsertExpMovingAverageinDB(stockCode, stockDetails.tradeddate.get(counter), 
								smaPeriods.get(counter1), stockDetails.closePrice.get(counter).floatValue(), expMovingAvg);*/
						storeMovingAverageinDB(stockCode, stockDetails.tradeddate.get(counter), 0,
								smaPeriods.get(counter1), stockDetails.closePrice.get(counter).floatValue(), expMovingAvg);
					//}
					/*period++;
					if (period > 200) {
						break;
					}*/

				}
			}
		}catch (Exception ex) {
			HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("calculateExpMovingAverageForMACD Error in DB action"+ex);
			logger.error("Error in calculateExpMovingAverageForMACD  -> ", ex);
		} finally {
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				} 
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("calculateExpMovingAverageForMACD Error in DB action"+ex);
				logger.error("Error in calculateExpMovingAverageForMACD  -> ", ex);
			}
		}
	}

	private void calculateTwelveAndTwentySixSMA(String stockCode) {
		ResultSet resultSet = null;
		Statement statement = null;
		String tradedDate, tmpSQL;
		Float closePrice;
		float sumofPrice;
		int counter =0;
		
		
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		try {			
			
			statement = connection.createStatement();
			tmpSQL = "SELECT tradeddate, closeprice FROM DAILYSTOCKDATA where stockname='"
					+ stockCode + "' and tradeddate<='2017-01-02' order by tradeddate desc;";
			resultSet = statement.executeQuery(tmpSQL);
			sumofPrice = 0;
			while (resultSet.next()) {
				
				tradedDate = resultSet.getString(1);
				closePrice = Float.parseFloat(resultSet.getString(2));
				sumofPrice = sumofPrice + closePrice;
				
				counter++;
				if(counter == 12) {
					simpleMovingAverageForTwelve = sumofPrice / counter;
				}
				if(counter == 26) {
					simpleMovingAverageForTwentySix = sumofPrice / counter;
					break;
				}
					
			}
		} catch (Exception ex) {
			HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("calculateTwelveAndTwentySixSMA Error in DB action"+ex);
			logger.error("Error in calculateTwelveAndTwentySixSMA  -> ", ex);
		} finally {
			try {
				if(resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("calculateTwelveAndTwentySixSMA Error in closing resultset "+ex);
				logger.error("Error in closing resultset calculateTwelveAndTwentySixSMA  -> ", ex);
			}
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("calculateTwelveAndTwentySixSMA Error in closing statement "+ex);
				logger.error("Error in closing statement calculateTwelveAndTwentySixSMA  -> ", ex);
			}
		}
	}
	
	private void calculateSimpleMovingAverageDaily(String stockCode, Date tragetDate) {
		SMAData stockDetails = null;
		float simpleMovingAverage = 0;
		int period = 1;
		float sumOfClosingPrices = 0;
		float expMovingAvg = 0;
		//Date date = null;
		ArrayList<Integer> smaPeriods;
		
		try {
			if (connection != null) {
				connection.close();
				connection = null;
			}
			connection = StockUtils.connectToDB();
			stockDetails = getStockDetailsFromDBForDaily(stockCode, tragetDate);
			smaPeriods = GetSMAPeriodsFromDB(stockCode);
			
			for (int counter = 0; counter < stockDetails.tradeddate.size(); counter++) {
				sumOfClosingPrices = sumOfClosingPrices + stockDetails.closePrice.get(counter);
				if(smaPeriods.contains(period)) {
				//if (period == 3 || period == 5 || period == 9 || period == 14 || period == 20 || period == 50
				//		|| period == 200) {
					simpleMovingAverage = sumOfClosingPrices / period;
					System.out.println(" Stock -> " + stockCode + " Period -> " + (counter+1));
					expMovingAvg = calculateExpMvingAvg(stockCode, stockDetails.closePrice.get(0), period, tragetDate);
					if (expMovingAvg == -1) {
						expMovingAvg = simpleMovingAverage;
					}
					storeMovingAverageinDB(stockCode, stockDetails.tradeddate.get(0), simpleMovingAverage, period,
							stockDetails.closePrice.get(0).floatValue(), expMovingAvg);
				}
				period++;
				if (period > 200) {
					break;
				}
			} 
		}catch (Exception ex) {
			HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("calculateSimpleMovingAverageDaily Error in DB action"+ex);
			logger.error("Error in calculateSimpleMovingAverageDaily  -> ", ex);
		} finally {
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				} 
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("calculateSimpleMovingAverageDaily Error in DB action"+ex);
				logger.error("Error in calculateSimpleMovingAverageDaily  -> ", ex);
			}
		}
	}

	private SMAData getStockDetailsFromDB(String stockCode, Date targetDate) {
		ResultSet resultSet = null;
		Statement statement = null;
		String tradedDate, tmpSQL;
		Float closePrice;
		SMAData smaDataObj = null;
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		try {			
			smaDataObj = new SMAData();
			smaDataObj.closePrice = new ArrayList<Float>();
			smaDataObj.tradeddate = new ArrayList<String>();
			statement = connection.createStatement();
			smaDataObj.stockName = stockCode;
			if(targetDate!=null) {
				tmpSQL = "SELECT tradeddate, closeprice FROM DAILYSTOCKDATA where stockname='"
						+ stockCode + "' and tradeddate>='" + dateFormat.format(targetDate) +"' order by tradeddate;";
			} else {
				tmpSQL = "SELECT tradeddate, closeprice FROM DAILYSTOCKDATA where stockname='"
							+ stockCode + "' order by tradeddate;";
			}
			resultSet = statement.executeQuery(tmpSQL);
			while (resultSet.next()) {
				tradedDate = resultSet.getString(1);
				closePrice = Float.parseFloat(resultSet.getString(2));
				smaDataObj.closePrice.add(closePrice);
				smaDataObj.tradeddate.add(tradedDate);
			}
			return smaDataObj;
		} catch (Exception ex) {
			HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("getStockDetailsFromDB Error in DB action"+ex);
			logger.error("Error in getStockDetailsFromDB  -> ", ex);
			return null;
		} finally {
			try {
				if(resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getStockDetailsFromDB Error in closing resultset "+ex);
				logger.error("Error in closing resultset getStockDetailsFromDB  -> ", ex);
			}
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getStockDetailsFromDB Error in closing statement "+ex);
				logger.error("Error in closing statement getStockDetailsFromDB  -> ", ex);
			}
		}
	}

	private SMAData getStockDetailsFromDBForDaily(String stockCode, Date SMDate) {
		ResultSet resultSet = null;
		Statement statement = null;
		String tradedDate;
		Float closePrice;
		SMAData smaDataObj = null;
		String tmpSQL;
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		try {
			
			smaDataObj = new SMAData();
			smaDataObj.closePrice = new ArrayList<Float>();
			smaDataObj.tradeddate = new ArrayList<String>();
			statement = connection.createStatement();
			smaDataObj.stockName = stockCode;
			if(SMDate!=null) {
				tmpSQL = "SELECT tradeddate, closeprice FROM DAILYSTOCKDATA where stockname='"
						+ stockCode + "' and tradeddate<='" + dateFormat.format(SMDate) +"' order by tradeddate desc limit 200;";
			} else {
				tmpSQL = "SELECT tradeddate, closeprice FROM DAILYSTOCKDATA where stockname='"
							+ stockCode + "' order by tradeddate desc limit 200;";
			}
			resultSet = statement
					.executeQuery(tmpSQL);
			while (resultSet.next()) {
				tradedDate = resultSet.getString(1);
				closePrice = Float.parseFloat(resultSet.getString(2));
				smaDataObj.closePrice.add(closePrice);
				smaDataObj.tradeddate.add(tradedDate);
			}
			return smaDataObj;
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
				System.out.println("getStockDetailsFromDBForDaily Error in closing statement "+ex);
				logger.error("Error in closing statement getStockDetailsFromDBForDaily  -> ", ex);
			}
		}
	}

	private void storeMovingAverageinDB(String stockName, String tradedDate, float simpMovingAverage, int period,
			float closingPrice, float expMovingAverage) {
		Statement statement = null;
		String tmpsql;
		try {
			
			//connection = StockUtils.connectToDB();
			statement = connection.createStatement();
			tmpsql = "INSERT INTO DAILYSNEMOVINGAVERAGES (STOCKNAME, TRADEDDATE, SMA, EMA, PERIOD, CLOSINGPRICE) VALUES('"
					+ stockName + "','" + tradedDate + "'," + simpMovingAverage + "," + expMovingAverage + "," + period
					+ "," + closingPrice + ");";
			statement.executeUpdate(tmpsql);
		} catch (Exception ex) {
			HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("storeMovingAverageinDB for quote -> " + stockName + " and Date - > " + tradedDate
					+ " and period  - > " + period + " Error in DB action" + ex);
			logger.error("Error in storeMovingAverageinDB  ->  storeMovingAverageinDB for quote -> " + stockName + " and Date - > " + tradedDate
					+ " and period  - > " + period, ex);
		} finally {
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getStockDetailsFromDBForDaily Error in closing statement "+ex);
				logger.error("Error in closing statement getStockDetailsFromDBForDaily  -> ", ex);
			}
			
		}
	}

	private void updateOrInsertExpMovingAverageinDB(String stockName, String tradedDate, int period,
			float closingPrice, float expMovingAverage) {
		Statement statement = null;
		ResultSet resultSet = null;
		String tmpSQL;
		try {
			
			//connection = StockUtils.connectToDB();
			statement = connection.createStatement();
			tmpSQL = "SELECT EMA FROM DAILYSNEMOVINGAVERAGES where stockName ='"
					+ stockName + "' and PERIOD = " + period + " and tradeddate='" + tradedDate + "' order by tradeddate desc;";
			resultSet = statement.executeQuery(tmpSQL);
			if (resultSet.next()) {
				tmpSQL = "update DAILYSNEMOVINGAVERAGES set EMA=" + expMovingAverage + " where stockName ='"
						+ stockName + "' and PERIOD = " + period + " and tradeddate='" + tradedDate + "'";
			} else {
				tmpSQL = "INSERT INTO DAILYSNEMOVINGAVERAGES (STOCKNAME, TRADEDDATE, SMA, EMA, PERIOD, CLOSINGPRICE) VALUES('"
						+ stockName + "','" + tradedDate + "',0," + expMovingAverage + "," + period
						+ "," + closingPrice + ");";
			}
			
			statement.executeUpdate(tmpSQL);
		} catch (Exception ex) {
			HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("updateOrInsertExpMovingAverageinDB for quote -> " + stockName + " and Date - > " + tradedDate
					+ " and period  - > " + period + " Error in DB action" + ex);
			logger.error("Error in updateOrInsertExpMovingAverageinDB  ->  updateOrInsertExpMovingAverageinDB for quote -> " + stockName + " and Date - > " + tradedDate
					+ " and period  - > " + period, ex);
		} finally {
			try {
				if(resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("updateOrInsertExpMovingAverageinDB Error in closing resultset "+ex);
				logger.error("Error in closing resultset updateOrInsertExpMovingAverageinDB  -> ", ex);
			}
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("updateOrInsertExpMovingAverageinDB Error in closing statement "+ex);
				logger.error("Error in closing statement updateOrInsertExpMovingAverageinDB  -> ", ex);
			}
			
		}
	}
	
	private float calculateExpMvingAvg(String stockName, float closePrice, int period, Date tragetDate) {
		float eMA;
		float lastExpMovingAvgStored;

		lastExpMovingAvgStored = getExpMovingAverageFromDB(stockName, period, tragetDate);

		if (lastExpMovingAvgStored != -1) {
			eMA = (2 / ((float) period + 1)) * (closePrice - lastExpMovingAvgStored) + lastExpMovingAvgStored;
		} else {
			eMA = -1;
		}
		return eMA;
	}

	private float getExpMovingAverageFromDB(String stockName, int period, Date tragetDate) {

		ResultSet resultSet = null;
		Statement statement = null;
		float eMA = -1;
		String tmpSQL;
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		try {
			statement = connection.createStatement();

			if(tragetDate!=null) {
				tmpSQL = "SELECT EMA, tradeddate FROM DAILYSNEMOVINGAVERAGES where stockName ='"
						+ stockName + "' and PERIOD = " + period + " and tradeddate<'" + dateFormat.format(tragetDate) + "' order by tradeddate desc;";
			} else {
				tmpSQL = "SELECT EMA, tradeddate FROM DAILYSNEMOVINGAVERAGES where stockName ='"
						+ stockName + "' and PERIOD = " + period + " order by tradeddate desc;";
			}
			
			resultSet = statement.executeQuery(tmpSQL);
			while (resultSet.next()) {
				eMA = Float.parseFloat(resultSet.getString(1));
				break;
				// System.out.println("StockNme - " + stockNSECode);
			}
			return eMA;
		} catch (Exception ex) {
			HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("getExpMovingAverageFromDB Error in DB action"+ex);
			logger.error("Error in getExpMovingAverageFromDB", ex);
			return eMA;
		} finally {
			try {
				if(resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getExpMovingAverageFromDB Error in closing resultset "+ex);
				logger.error("Error in closing resultset getExpMovingAverageFromDB  -> ", ex);
			}
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getExpMovingAverageFromDB Error in closing statement "+ex);
				logger.error("Error in closing statement getExpMovingAverageFromDB  -> ", ex);
			}
		}
	}
	
	private ArrayList<Integer> GetSMAPeriodsFromDB(String stockCode) {
		ArrayList<Integer> prefPeriod = null;
		ResultSet resultSet = null;
		Statement statement = null;
		String[] prefPeriodsInDB;

		try {
			prefPeriod = new ArrayList<Integer>();
			
			statement = connection.createStatement();
			resultSet = statement.executeQuery("SELECT DAILYSMAPERIOD FROM STOCKWISEPERIODS where stockname = '" + stockCode + "';");
			while (resultSet.next()) {
				prefPeriodsInDB = resultSet.getString(1).split(",");
				for (int counter = 0; counter < prefPeriodsInDB.length; counter++) {
					/*if (!prefPeriodsInDB[counter].equals("3") && !prefPeriodsInDB[counter].equals("5") && !prefPeriodsInDB[counter].equals("9") && !prefPeriodsInDB[counter].equals("14") && !prefPeriodsInDB[counter].equals("20") && !prefPeriodsInDB[counter].equals("50")
							&& !prefPeriodsInDB[counter].equals("200")) {*/
						prefPeriod.add(new Integer(prefPeriodsInDB[counter]));
					//}
				}
				// System.out.println("StockNme - " + stockNSECode);
			}
			resultSet.close();
		} catch (Exception ex) {
			HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("Error in getting SMA period from DB" + ex);
			return null;
		} finally {
			try {
				if(resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("GetSMAPeriodsFromDB Error in closing resultset "+ex);
				logger.error("Error in closing resultset GetSMAPeriodsFromDB  -> ", ex);
			}
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("GetSMAPeriodsFromDB Error in closing statement "+ex);
				logger.error("Error in closing statement GetSMAPeriodsFromDB  -> ", ex);
			}
		}
		return prefPeriod;
	}
}
