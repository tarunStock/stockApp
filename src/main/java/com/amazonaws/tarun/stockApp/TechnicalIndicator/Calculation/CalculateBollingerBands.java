package com.amazonaws.tarun.stockApp.TechnicalIndicator.Calculation;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.apache.log4j.Logger;

import com.amazonaws.tarun.stockApp.TechnicalIndicator.Data.DailyStockData;
import com.amazonaws.tarun.stockApp.Utils.HandleErrorDetails;
import com.amazonaws.tarun.stockApp.Utils.StockUtils;



public class CalculateBollingerBands {
	Connection connection = null;
	static Logger logger = Logger.getLogger(CalculateBollingerBands.class);
	public final static int ACCEPTED_PERCENTAGE_DEVIATION = 10;
	
	public static void main(String[] args) {
		Date dte = new Date();
		logger.debug("CalculateBollingerBands Started");
		System.out.println("Start at -> " + dte.toString());
		CalculateBollingerBands obj = new CalculateBollingerBands();
		//obj.calculateBollingerBands(null);		
		obj.calculateBollingerBands(new Date("12-Jan-2018"));
		HandleErrorDetails.sendErrorsInMail("Calculate Bollinger Band");
		dte = new Date();
		System.out.println("End at -> " + dte.toString());
		logger.debug("CalculateBollingerBands End");
	}
	
	public void calculateBollingerBands(Date calculationDate) {
		ArrayList<String> stockList = null;
		String stockName;
		String bseCode;
		String nseCode;
		if( !StockUtils.marketOpenOnGivenDate(calculationDate))
			return;
		stockList = StockUtils.getStockListFromDB();
		for (String stockCode : stockList) {
			stockName = stockCode.split("!")[1];
			bseCode = stockCode.split("!")[0];
			nseCode = stockCode.split("!")[2];
			//BulkBollingerBandCalculateAndStore(nseCode);
			//calculateBollingerBandsDaily(nseCode, new Date("19-Oct-2017"));
			calculateBollingerBandsDaily(nseCode, calculationDate);
			//System.out.println("stock " + nseCode);
			/*if(getBBIndicationForStockV1(nseCode, calculationDate).equalsIgnoreCase("contracting")) {
				System.out.println("*************stock " + nseCode + " has BB contracting ");
			}*/
		}
	}
	
	private void BulkBollingerBandCalculateAndStore(String stockCode) {
		ArrayList<DailyStockData> objDailyStockDataList;
		int counter = 1;
		float totalPrice = 0;
		String BBDate = null;
		double perioddeviation = 0;
        double BBLower = 0;
        double BBUper = 0;
        double periodBandwidth;
        float simpleMA, tmpvar;
        float closingPrice = 0;
        ArrayList<Float> periodData = null;
        ArrayList<Float> tmpPeriodData;
        
        
		String bbPeriod = getBBPeriod(stockCode);
		if(bbPeriod == null) {
			logger.error("Null Bb Period for stock -> "+stockCode);
			System.out.println("Null Bb Period for stock -> "+stockCode);
			return;
		}
		String[] tmplist = bbPeriod.split(",");
		ArrayList<String> bbPeriodArray = new ArrayList<String> (Arrays.asList(tmplist));
		ArrayList<String> tmpBBPeriodArray;
		System.out.println("Creating BB entry for stock -> " + stockCode);
		objDailyStockDataList = getStockDetailsFromDBForDaily(stockCode, null);
		if(objDailyStockDataList.size()>0) {
			for(int iterationcounter = 0; iterationcounter<200; iterationcounter++) {
				if(objDailyStockDataList.size()<14) {
					break;
				}
				counter = 1;
				totalPrice = 0;
				tmpBBPeriodArray = (ArrayList<String>) bbPeriodArray.clone();
				periodData = new ArrayList<Float>();
				for (DailyStockData objDailyStockData : objDailyStockDataList) {
					totalPrice = totalPrice + objDailyStockData.closePrice;
					if(counter==1) {
						BBDate = objDailyStockData.tradedDate;
						
						closingPrice = objDailyStockData.closePrice;
					}
					periodData.add(objDailyStockData.closePrice);
					if(tmpBBPeriodArray.size()==0) {
						break;
					}
					if( tmpBBPeriodArray.contains(counter+"") ) {	
						tmpBBPeriodArray.remove(counter+"");
						perioddeviation = 0;
		                BBLower = 0;
		                BBUper = 0;
		                tmpPeriodData = new ArrayList<Float>();
		                simpleMA = totalPrice/counter;
		                for(int counter1 = 0; counter1<counter; counter1++) {
		                	tmpPeriodData.add(periodData.get(counter1)-simpleMA);
		                	tmpvar = tmpPeriodData.get(counter1) * tmpPeriodData.get(counter1); 
		                	tmpPeriodData.set(counter1, tmpvar);
		                	perioddeviation = perioddeviation + tmpPeriodData.get(counter1);
		                }
		                perioddeviation = perioddeviation / counter;
		                perioddeviation = Math.sqrt(perioddeviation);
		                BBLower = simpleMA - 2 * perioddeviation;
		                BBUper = simpleMA + 2 * perioddeviation;
		                periodBandwidth = BBUper - BBLower;
		                insertBBToDB(stockCode, BBDate, counter, closingPrice, simpleMA, BBUper, BBLower, periodBandwidth);
					}			
					counter++;	
				}
				objDailyStockDataList.remove(0);
			}
		} else {
			System.out.println("Quote size is 0 for stock -> "+stockCode);
			
		}
		System.out.println("Test");
	}
	
	private String getBBPeriod(String stockCode) {
		ResultSet resultSet = null;
		Statement statement = null;
		String bbPeriod = null;
		String tmpSQL;
		
		try {
			if (connection != null) {
				connection.close();
				connection = null;
			}
			connection = StockUtils.connectToDB();
			statement = connection.createStatement();
			tmpSQL = "SELECT DAILYBBPERIOD from STOCKWISEPERIODS where stockname='" + stockCode + "';";
			resultSet = statement.executeQuery(tmpSQL);
			while (resultSet.next()) {
				bbPeriod = resultSet.getString(1);
			}
			return bbPeriod;
		} catch (Exception ex) {
			HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("getBBPeriod Error in DB action "+ex);
			logger.error("Error in getBBPeriod  -> ", ex);
			return null;
		} finally {
			try {
				if(resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
			} catch (Exception ex) {
				System.out.println("getBBPeriod Error in closing resultset "+ex);
				logger.error("Error in closing resultset getBBPeriod  -> ", ex);
			}
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getBBPeriod Error in closing statement "+ex);
				logger.error("Error in closing statement getBBPeriod  -> ", ex);
			}
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				} 
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getBBPeriod Error in closing connection "+ex);
				logger.error("Error in closing connection getBBPeriod  -> ", ex);
			}
		}
	}
	
	private ArrayList<DailyStockData> getStockDetailsFromDBForDaily(String stockCode, Date bbDate) {
		ResultSet resultSet = null;
		Statement statement = null;
		ArrayList<DailyStockData> objDailyStockDataList = new ArrayList<DailyStockData>();
		DailyStockData objDailyStockData = null;
		String tmpSQL;
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		try {
			if (connection != null) {
				connection.close();
				connection = null;
			}
			connection = StockUtils.connectToDB();
			statement = connection.createStatement();	
			if(bbDate!=null) {
				tmpSQL = "SELECT CLOSEPRICE, HIGHPRICE, LOWPRICE, VOLUME, TRADEDDATE, OPENPRICE from DAILYSTOCKDATA where stockname='" + stockCode + "' and tradeddate <= '" + dateFormat.format(bbDate) +"' order by tradeddate desc limit 50;";
			} else {
				tmpSQL = "SELECT CLOSEPRICE, HIGHPRICE, LOWPRICE, VOLUME, TRADEDDATE, OPENPRICE from DAILYSTOCKDATA where stockname='" + stockCode + "' order by tradeddate desc limit 50;";
			}
			resultSet = statement.executeQuery(tmpSQL);
			while (resultSet.next()) {
				objDailyStockData = new DailyStockData();
				objDailyStockData.closePrice = Float.parseFloat(resultSet.getString(1));
				objDailyStockData.highPrice =  Float.parseFloat(resultSet.getString(2));
				objDailyStockData.lowPrice =  Float.parseFloat(resultSet.getString(3));
				objDailyStockData.volume =  (long) Double.parseDouble(resultSet.getString(4));
				objDailyStockData.tradedDate =  resultSet.getString(5);
				objDailyStockData.openPrice =  Float.parseFloat(resultSet.getString(6));
				objDailyStockDataList.add(objDailyStockData);
			}
			return objDailyStockDataList;
		} catch (Exception ex) {
			HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("CalculateBollingerBands -> getStockDetailsFromDBForDaily Error in DB action "+ex);
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
				System.out.println("CalculateBollingerBands -> getStockDetailsFromDBForDaily Error in closing resultset "+ex);
				logger.error("CalculateBollingerBands ->  Error in closing resultset getStockDetailsFromDBForDaily  -> ", ex);
			}
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("CalculateBollingerBands ->  getStockDetailsFromDBForDaily Error in closing statement "+ex);
				logger.error("CalculateBollingerBands ->  Error in closing statement getStockDetailsFromDBForDaily  -> ", ex);
			}
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				} 
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("CalculateBollingerBands ->  getStockDetailsFromDBForDaily Error in closing connection "+ex);
				logger.error("CalculateBollingerBands -> Error in closing connection getStockDetailsFromDBForDaily  -> ", ex);
			}
		}
	}
	
	private void insertBBToDB(String stockNSECode, String tradedDate, int period, float closingPrice, double SMA, double BBUpper, double BBLOwer, double bandwidth) {		
		Statement statement = null;
		String tmpSQL;
		
		try {
			if (connection == null) {
				connection = StockUtils.connectToDB();
			}			
			statement = connection.createStatement();
			tmpSQL = "INSERT INTO DAILYBOLLINGERBANDS (TRADEDDATE, STOCKNAME, PERIOD, CLOSINGPRICE, SMA, UPPERBAND, LOWERBAND, BANDWIDTH) VALUES('"
					+ tradedDate + "', '" + stockNSECode + "', " + period + ", " + closingPrice + ", " + SMA + ", " + BBUpper + ", " + BBLOwer + ", " + bandwidth + ");";
			statement.executeUpdate(tmpSQL);			
		} catch (Exception ex) {
			HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("insertBBToDB Error in DB action ->"+ex);
			logger.error("Error in insertBBToDB  -> ", ex);
		} finally {			
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("CalculateBollingerBands -> insertBBToDB Error in closing statement "+ex);
				logger.error("Error in closing statement insertBBToDB  -> ", ex);
			}
		}
	}	
	
	public void calculateBollingerBandsDaily(String stockCode, Date targetDate) {
		ArrayList<DailyStockData> objDailyStockDataList;
		int counter = 1;
		float totalPrice = 0;
		String BBDate = null;
		double perioddeviation = 0;
        double BBLower = 0;
        double BBUper = 0;
        double periodBandwidth;
        float simpleMA, tmpvar;
        float closingPrice = 0;
        ArrayList<Float> periodData = null;
        ArrayList<Float> tmpPeriodData;     
        //Date date = null;
         
		//date = new Date(System.currentTimeMillis()-2*24*60*60*1000);
		
		String bbPeriod = getBBPeriod(stockCode);
		if(bbPeriod == null) {
			logger.error("Null Bb Period for stock -> "+stockCode);
			System.out.println("Null Bb Period for stock -> "+stockCode);
			return;
		}
		String[] tmplist = bbPeriod.split(",");
		ArrayList<String> bbPeriodArray = new ArrayList<String> (Arrays.asList(tmplist));
		//ArrayList<String> tmpBBPeriodArray;
		System.out.println("Creating BB entry for stock -> " + stockCode);
		objDailyStockDataList = getStockDetailsFromDBForDaily(stockCode, targetDate);
		
		if(objDailyStockDataList.size()>0) {			
			counter = 1;
			totalPrice = 0;
			//tmpBBPeriodArray = (ArrayList<String>) bbPeriodArray.clone();
			periodData = new ArrayList<Float>();
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				}
				connection = StockUtils.connectToDB();
				for (DailyStockData objDailyStockData : objDailyStockDataList) {
					totalPrice = totalPrice + objDailyStockData.closePrice;
					if(counter==1) {
						BBDate = objDailyStockData.tradedDate;
						closingPrice = objDailyStockData.closePrice;
					}
					periodData.add(objDailyStockData.closePrice);
					if(bbPeriodArray.size()==0) {
						break;
					}
					if( bbPeriodArray.contains(counter+"") ) {	
						bbPeriodArray.remove(counter+"");
						perioddeviation = 0;
		                BBLower = 0;
		                BBUper = 0;
		                tmpPeriodData = new ArrayList<Float>();
		                simpleMA = totalPrice/counter;
		                for(int counter1 = 0; counter1<counter; counter1++) {
		                	tmpPeriodData.add(periodData.get(counter1)-simpleMA);
		                	tmpvar = tmpPeriodData.get(counter1) * tmpPeriodData.get(counter1); 
		                	tmpPeriodData.set(counter1, tmpvar);
		                	perioddeviation = perioddeviation + tmpPeriodData.get(counter1);
		                }
		                perioddeviation = perioddeviation / counter;
		                perioddeviation = Math.sqrt(perioddeviation);
		                BBLower = simpleMA - 2 * perioddeviation;
		                BBUper = simpleMA + 2 * perioddeviation;
		                periodBandwidth = BBUper - BBLower;
		                insertBBToDB(stockCode, BBDate, counter, closingPrice, simpleMA, BBUper, BBLower, periodBandwidth);
					}			
					counter++;	
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("calculateBollingerBandsDaily Error in DB action"+ex);
				logger.error("Error in calculateBollingerBandsDaily  -> ", ex);
			} finally {
				try {
					if (connection != null) {
						connection.close();
						connection = null;
					} 
				} catch (Exception ex) {
					HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
					System.out.println("calculateBollingerBandsDaily Error in DB action"+ex);
					logger.error("Error in calculateBollingerBandsDaily  -> ", ex);
				}
			}
			
		} else {
			System.out.println("calculateBollingerBandsDaily Quote size is 0 for stock -> "+stockCode);
			
		}
		System.out.println("Test");
	}
	
	public String getBBIndicationForStock(String stockCode, Date targetDate) {
		ResultSet resultSet = null;
		Statement statement = null;
		ArrayList<Float> dailyBandwidth = new ArrayList<Float>();
		String bbContracting = "Contracting";
		float BBcontractingPercentage;
		String tmpSQL;
		boolean onedaydeviation = false;
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		try {
			if (connection != null) {
				connection.close();
				connection = null;
			}
			connection = StockUtils.connectToDB();
			statement = connection.createStatement();	
			if( targetDate != null) {
				tmpSQL = "SELECT BANDWIDTH from DAILYBOLLINGERBANDS where stockname='" + stockCode + "' and period = 20 and tradeddate <='" + dateFormat.format(targetDate) +  "' order by tradeddate desc limit 5;";
			} else {
				tmpSQL = "SELECT BANDWIDTH from DAILYBOLLINGERBANDS where stockname='" + stockCode + "' and period = 20 order by tradeddate desc limit 5;";
			}
			
			resultSet = statement.executeQuery(tmpSQL);
			while (resultSet.next()) {
				dailyBandwidth.add(Float.parseFloat(resultSet.getString(1)));
			}
			for(int counter = 0; counter< dailyBandwidth.size()-1; counter++) {
				BBcontractingPercentage = (dailyBandwidth.get(counter) - dailyBandwidth.get(counter+1))/dailyBandwidth.get(counter+1);
				if(dailyBandwidth.get(counter) > dailyBandwidth.get(counter+1)){
					if(BBcontractingPercentage <= ACCEPTED_PERCENTAGE_DEVIATION) {
						if(onedaydeviation) {
							bbContracting = "expanding";
							break;
						}
						onedaydeviation = true;
					} else {
						bbContracting = "expanding";
						break;	
					}
				} else {
				}
			}
			return bbContracting;
		} catch (Exception ex) {
			HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("getBBIndicationForStock Error in DB action "+ex);
			logger.error("Error in getBBIndicationForStock  -> ", ex);
			return "expanding";
		} finally {
			try {
				if(resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getBBIndicationForStock Error in closing resultset "+ex);
				logger.error("Error in closing resultset getBBIndicationForStock  -> ", ex);
			}
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getBBIndicationForStock Error in closing statement "+ex);
				logger.error("Error in closing statement getBBIndicationForStock  -> ", ex);
			}
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				} 
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getBBIndicationForStock Error in closing connection "+ex);
				logger.error("Error in closing connection getBBIndicationForStock  -> ", ex);
			}
		}
	}
	
	public String getBBIndicationForStockV1(String stockCode, Date targetDate) {
		ResultSet resultSet = null;
		Statement statement = null;
		//ArrayList<Float> dailyBandwidth = new ArrayList<Float>();
		float maxBandwidth=0, minBandwidth=0, currentBandwidth=0,avgBandwidth;
		String bbContracting = "expanding";
		//float BBcontractingPercentage;
		String tmpSQL;
		//boolean onedaydeviation = false;
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date startingDate;
		if(targetDate != null) {
			startingDate = new Date(targetDate.getTime()-365*24*60*60*1000L);
		} else {
			startingDate = new Date();
		}
		
		try {
			if (connection != null) {
				connection.close();
				connection = null;
			}
			connection = StockUtils.connectToDB();
			statement = connection.createStatement();	
			//get max value of bandwidth
			/*if( targetDate != null) {
				tmpSQL = "SELECT max(BANDWIDTH) from DAILYBOLLINGERBANDS where stockname='" + stockCode + "' and period = 20 and tradeddate >='" + dateFormat.format(startingDate) +  "';";
			} else {
				tmpSQL = "SELECT max(BANDWIDTH) from DAILYBOLLINGERBANDS where stockname='" + stockCode + "' and period = 20;";
			}
			
			resultSet = statement.executeQuery(tmpSQL);
			while (resultSet.next()) {
				maxBandwidth = Float.parseFloat(resultSet.getString(1));
			}
			resultSet.close();
			resultSet = null;
			*/
			//get min value of bandwidth
			if( targetDate != null) {
				tmpSQL = "SELECT min(BANDWIDTH) from DAILYBOLLINGERBANDS where stockname='" + stockCode + "' and period = 20 and tradeddate >='" + dateFormat.format(startingDate) +  "';";
			} else {
				tmpSQL = "SELECT min(BANDWIDTH) from DAILYBOLLINGERBANDS where stockname='" + stockCode + "' and period = 20;";
			}
			
			resultSet = statement.executeQuery(tmpSQL);
			while (resultSet.next()) {
				minBandwidth = Float.parseFloat(resultSet.getString(1));
			}
			resultSet.close();
			resultSet = null;
			
			//get given date bandwidth			
			if( targetDate != null) {
				tmpSQL = "SELECT BANDWIDTH from DAILYBOLLINGERBANDS where stockname='" + stockCode + "' and period = 20 and tradeddate ='" + dateFormat.format(targetDate) +  "';";
			} else {
				tmpSQL = "SELECT BANDWIDTH from DAILYBOLLINGERBANDS where stockname='" + stockCode + "' and period = 20 order by tradeddate desc limit 1;";
			}
			
			resultSet = statement.executeQuery(tmpSQL);
			while (resultSet.next()) {
				currentBandwidth = Float.parseFloat(resultSet.getString(1));
			}
			
			//avgBandwidth = (maxBandwidth+minBandwidth)/2;
			
			/*if(currentBandwidth <= (avgBandwidth/2)) {
				bbContracting = "contracting";
			} */
			
			if(currentBandwidth <= (minBandwidth+(minBandwidth*20/100))) {
				bbContracting = "contracting";
			} 
			return bbContracting;
		} catch (Exception ex) {
			HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("getBBIndicationForStock Error in DB action "+ex);
			logger.error("Error in getBBIndicationForStock  -> ", ex);
			return "expanding";
		} finally {
			try {
				if(resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getBBIndicationForStock Error in closing resultset "+ex);
				logger.error("Error in closing resultset getBBIndicationForStock  -> ", ex);
			}
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getBBIndicationForStock Error in closing statement "+ex);
				logger.error("Error in closing statement getBBIndicationForStock  -> ", ex);
			}
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				} 
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getBBIndicationForStock Error in closing connection "+ex);
				logger.error("Error in closing connection getBBIndicationForStock  -> ", ex);
			}
		}
	}
}
