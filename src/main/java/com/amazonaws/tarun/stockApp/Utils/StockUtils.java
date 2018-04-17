package com.amazonaws.tarun.stockApp.Utils;
import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

import com.amazonaws.tarun.stockApp.TechnicalIndicator.Data.StockDetailsForDecision;

public class StockUtils implements AmazonRDSDBConnectionInterface{
	static Logger logger = Logger.getLogger(StockUtils.class);	
	static ArrayList<Date> holidayList;
	static {
		
		
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader("C:\\StockApp\\Holidays.txt"));
		    String line = br.readLine();
		    holidayList = new ArrayList<Date>();
		    while (line != null) {
		    	holidayList.add(new Date(Date.parse(line)));		    	
		        line = br.readLine();
		    }
		} catch (Exception ex) {
			System.out.println("Error in reading holiday file in static block ->"+ex);
			logger.error("Error in reading holiday file in static block  -> ", ex);
		} finally {
			try {
				br.close();
			} catch (Exception ex) {
				System.out.println("Error in closing BufferedReader ->"+ex);
				logger.error("Error in closing BufferedReader  -> ", ex);
			}
		}
	}
	public static Connection connectToDB () {
		Connection connection = null;
		boolean connectToFirebird = false;
		try {
			if(connectToFirebird) {
				Class.forName("org.firebirdsql.jdbc.FBDriver").newInstance();
				//System.out.println("Connecting");
				//Class.forName("com.mysql.jdbc.Driver").newInstance();
				connection = DriverManager.getConnection("jdbc:firebirdsql://192.168.0.106:3050/D:/Tarun/StockApp_Latest/DB/STOCKAPPDBNEW.FDB?lc_ctype=utf8", "SYSDBA", "Jan@2017");
				//System.out.println("Connected");
			}
			else {
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				connection = DriverManager.getConnection(CONNECTION_STRING, USER, PASS);
			}		
			
		} catch (Exception ex) {
			HandleErrorDetails.addError(StockUtils.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("connectToDB Error in DB action ->"+ex);
			logger.error("Error in getStockListFromDB  -> ", ex);
		}
		return connection;
	}
	
	public static boolean getFinancialIndication(Connection connection, String nseCode) {
		ResultSet resultSet = null;
		Statement statement = null;
		String tmpSQL;
		float netSales, netProfit, netCashFlow;
		ArrayList<Float> netSalesList = new ArrayList<Float>();
		ArrayList<Float> netProfitList = new ArrayList<Float>();
		ArrayList<Float> netCashFlowList = new ArrayList<Float>();
		try {
			//priceData = new ArrayList<Float>();
			if(connection == null) {
				connection = StockUtils.connectToDB();
			}
			statement = connection.createStatement();;
			
			tmpSQL = "SELECT netSales, netProfit, netCashFlow FROM STOCKANNUALFINANCIALDATA where STOCKCODE='" + nseCode + "' order by resultYear desc;";
			resultSet = statement.executeQuery(tmpSQL);
			
			// DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
			while (resultSet.next()) {
				netSales = Float.parseFloat(resultSet.getString(1));
				netProfit = Float.parseFloat(resultSet.getString(2));
				netCashFlow = Float.parseFloat(resultSet.getString(3));
				netSalesList.add(netSales);
				netProfitList.add(netProfit);
				netCashFlowList.add(netCashFlow);
			}
			
			if(netSalesList.size()>0) {
				if(netSalesList.size()>2) {
					if(netSalesList.get(2)>=netSalesList.get(1) && netProfitList.get(2) >= netProfitList.get(1)) {
						return true;
					} else {
						return false;
					}
				} else if(netSalesList.size()>1) {
					if(netSalesList.get(1)>=netSalesList.get(0) && netProfitList.get(1) >= netProfitList.get(0)) {
						return true;
					} else {
						return false;
					}
				}
			} else {
				return true;
			}
		} catch (Exception ex) {
			HandleErrorDetails.addError(StockUtils.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("getFinancialIndication Error in getting indication = " + ex);
			return true;
		} finally {
			try {
				if(resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(StockUtils.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getFinancialIndication Error in closing resultset "+ex);
				logger.error("Error in closing resultset getFinancialIndication  -> ", ex);
			}
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(StockUtils.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getFinancialIndication Error in closing statement "+ex);
				logger.error("Error in closing statement getFinancialIndication  -> ", ex);
			}
		}
		//Returning true in case of no data to avoid loosing good stock
		return true;
	}
	
	public static boolean getFinancialIndication(String nseCode) {
		Connection connection = null;
		ResultSet resultSet = null;
		Statement statement = null;
		String tmpSQL;
		float netSales, netProfit, netCashFlow;
		ArrayList<Float> netSalesList = new ArrayList<Float>();
		ArrayList<Float> netProfitList = new ArrayList<Float>();
		ArrayList<Float> netCashFlowList = new ArrayList<Float>();
		try {
			//priceData = new ArrayList<Float>();
			if(connection == null) {
				connection = StockUtils.connectToDB();
			}
			statement = connection.createStatement();;
			
			tmpSQL = "SELECT netSales, netProfit, netCashFlow FROM STOCKANNUALFINANCIALDATA where STOCKCODE='" + nseCode + "' order by resultYear desc;";
			resultSet = statement.executeQuery(tmpSQL);
			
			// DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
			while (resultSet.next()) {
				netSales = Float.parseFloat(resultSet.getString(1));
				netProfit = Float.parseFloat(resultSet.getString(2));
				netCashFlow = Float.parseFloat(resultSet.getString(3));
				netSalesList.add(netSales);
				netProfitList.add(netProfit);
				netCashFlowList.add(netCashFlow);
			}
			
			if(netSalesList.size()>0) {
				if(netSalesList.size()>2) {
					if(netSalesList.get(2)>=netSalesList.get(1) && netProfitList.get(2) >= netProfitList.get(1)) {
						return true;
					} else {
						return false;
					}
				} else if(netSalesList.size()>1) {
					if(netSalesList.get(1)>=netSalesList.get(0) && netProfitList.get(1) >= netProfitList.get(0)) {
						return true;
					} else {
						return false;
					}
				}
			} else {
				return true;
			}
		} catch (Exception ex) {
			HandleErrorDetails.addError(StockUtils.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("getFinancialIndication Error in getting indication = " + ex);
			return true;
		} finally {
			try {
				if(resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(StockUtils.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getFinancialIndication Error in closing resultset "+ex);
				logger.error("Error in closing resultset getFinancialIndication  -> ", ex);
			}
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(StockUtils.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getFinancialIndication Error in closing statement "+ex);
				logger.error("Error in closing statement getFinancialIndication  -> ", ex);
			}
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				} 
			} catch (Exception ex) {
				HandleErrorDetails.addError(StockUtils.class.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getRSIValue Error in closing connection "+ex);
				logger.error("Error in closing connection getRSIValue  -> ", ex);
			}
		}
		//Returning true in case of no data to avoid loosing good stock
		return true;
	}
	
	public static ArrayList<String> getStockListFromDB() {
		Connection connection = null;
		ResultSet resultSet = null;
		Statement statement = null;
		ArrayList<String> stockList = null;
		String stockBSECode;
		
		try {
			stockList = new ArrayList<String>();
			connection = StockUtils.connectToDB();
			statement = connection.createStatement();

			resultSet = statement.executeQuery("SELECT NSECODE, stockname, NSECODE, ISINCODE FROM STOCKDETAILS;");
			while (resultSet.next()) {
				stockBSECode = resultSet.getString(1);
				stockBSECode = stockBSECode + "!" + resultSet.getString(2);
				stockBSECode = stockBSECode + "!" + resultSet.getString(3);
				stockBSECode = stockBSECode + "!" + resultSet.getString(4);
				stockList.add(stockBSECode);
				// System.out.println("StockNme - " + stockNSECode);
			}
			return stockList;
		} catch (Exception ex) {
			HandleErrorDetails.addError(StockUtils.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("Error in DB action");
			return null;
		} finally {
			try {
				if(resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(StockUtils.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getStockListFromDB Error in closing resultset "+ex);
				logger.error("Error in closing resultset getStockListFromDB  -> ", ex);
			}
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(StockUtils.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getStockListFromDB Error in closing statement "+ex);
				logger.error("Error in closing statement getStockListFromDB  -> ", ex);
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
	
	public static ArrayList<String> getStockListFromDB(Connection connection) {
		ResultSet resultSet = null;
		Statement statement = null;
		ArrayList<String> stockList = null;
		String stockBSECode;
		
		try {
			stockList = new ArrayList<String>();
			//connection = StockUtils.connectToDB();
			statement = connection.createStatement();

			resultSet = statement.executeQuery("SELECT NSECODE, stockname, NSECODE, ISINCODE FROM STOCKDETAILS;");
			while (resultSet.next()) {
				stockBSECode = resultSet.getString(1);
				stockBSECode = stockBSECode + "!" + resultSet.getString(2);
				stockBSECode = stockBSECode + "!" + resultSet.getString(3);
				stockBSECode = stockBSECode + "!" + resultSet.getString(4);
				stockList.add(stockBSECode);
				// System.out.println("StockNme - " + stockNSECode);
			}
			return stockList;
		} catch (Exception ex) {
			HandleErrorDetails.addError(StockUtils.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("Error in DB action");
			return null;
		} finally {
			try {
				if(resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(StockUtils.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getStockListFromDB Error in closing resultset "+ex);
				logger.error("Error in closing resultset getStockListFromDB  -> ", ex);
			}
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(StockUtils.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getStockListFromDB Error in closing statement "+ex);
				logger.error("Error in closing statement getStockListFromDB  -> ", ex);
			}
		}
	}
	
	public static boolean marketOpenOnGivenDate(Date targetDate) {
		System.out.println("Market Open or not?");
		if(targetDate == null) {
			targetDate = new Date();
		}
		if(targetDate.getDay() == 0 || targetDate.getDay() == 6) {
			System.out.println("Market Closed");
			return false;
		} else if(holidayList.contains(targetDate)) {
			System.out.println("Market Closed");
			return false;
		} else {
			System.out.println("Market Open");
			return true;
		}	
	}

	public static StockDetailsForDecision getPriceAndVolumeDetails(StockDetailsForDecision objFinalSelectedStock, Date targetDate) {
		Connection connection = null;
		ResultSet resultSet = null;
		Statement statement = null;
		String tmpSQL;
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		try {
			//if(connection==null) {
				connection = StockUtils.connectToDB();
			//}			
			statement = connection.createStatement();
			if(targetDate!=null) {
				tmpSQL = "SELECT closeprice, Volume  FROM DAILYSTOCKDATA where stockname='" + objFinalSelectedStock.stockCode + "' " 
						  + " and tradeddate >'" + dateFormat.format(new Date(targetDate.getTime() - 9*24*60*60*1000)) + "' and tradeddate <='" + dateFormat.format(new Date(targetDate.getTime())) + "' order by tradeddate desc limit 4;";
			} else {
				tmpSQL = "SELECT closeprice, Volume  FROM DAILYSTOCKDATA where stockname='" + objFinalSelectedStock.stockCode + "' order by tradeddate desc limit 4;";
				  //+ " order by tradeddate limit " + (daysToCheck+18) + ";";
			}
			resultSet = statement.executeQuery(tmpSQL);
			
			//while (resultSet.next()) {
			if(!resultSet.next())
				return objFinalSelectedStock;
			objFinalSelectedStock.CurrentPrice = Float.parseFloat(resultSet.getString(1));
			objFinalSelectedStock.CurrentVolume = Long.parseLong(resultSet.getString(2));
			
			if(!resultSet.next())
				return objFinalSelectedStock;
			objFinalSelectedStock.OneDayPreviousPrice = Float.parseFloat(resultSet.getString(1));
			objFinalSelectedStock.OneDayPreviousVolume = Long.parseLong(resultSet.getString(2));
			
			if(!resultSet.next())
				return objFinalSelectedStock;
			objFinalSelectedStock.TwoDayPreviousPrice = Float.parseFloat(resultSet.getString(1));
			objFinalSelectedStock.TwoDayPreviousVolume = Long.parseLong(resultSet.getString(2));
			if(!resultSet.next())
				return objFinalSelectedStock;
			objFinalSelectedStock.ThreeDayPreviousPrice = Float.parseFloat(resultSet.getString(1));
			objFinalSelectedStock.ThreeDayPreviousVolume = Long.parseLong(resultSet.getString(2));
				
			//}
			return objFinalSelectedStock;
		} catch (Exception ex) {
			HandleErrorDetails.addError(StockUtils.class.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("getPriceAndVolumeDetails - Error in getting price values  error = " + ex);
			return null;
		} finally {
			try {
				if(resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(StockUtils.class.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getPriceAndVolumeDetails Error in closing resultset "+ex);
				logger.error("Error in closing resultset getPriceAndVolumeDetails  -> ", ex);
			}
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(StockUtils.class.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getPriceAndVolumeDetails Error in closing statement "+ex);
				logger.error("Error in closing statement getPriceAndVolumeDetails  -> ", ex);
			}
			/*try {
				if (connection != null) {
					connection.close();
					connection = null;
				} 
			} catch (Exception ex) {
				HandleErrorDetails.addError(StockUtils.class.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getPriceAndVolumeDetails Error in closing connection "+ex);
				logger.error("Error in closing connection getPriceAndVolumeDetails  -> ", ex);
			}*/
		}
	}

	public static ArrayList<Integer> GetPreferredSMA(Connection connection, String stockCode) {
		ArrayList<Integer> prefPeriod = null;
		ResultSet resultSet = null;
		Statement statement = null;
		String[] prefPeriodsInDB;

		try {
			prefPeriod = new ArrayList<Integer>();
			if(connection == null) {
				connection = StockUtils.connectToDB();
			}
			statement = connection.createStatement();
			resultSet = statement.executeQuery("SELECT PREFDAILYSMAPERIODS FROM STOCKWISEPERIODS where stockname = '" + stockCode + "';");
			while (resultSet.next()) {
				prefPeriodsInDB = resultSet.getString(1).split(",");
				for (int counter = 0; counter < prefPeriodsInDB.length; counter++) {
					prefPeriod.add(new Integer(prefPeriodsInDB[counter]));
				}
				// System.out.println("StockNme - " + stockNSECode);
			}
			resultSet.close();
		} catch (Exception ex) {
			HandleErrorDetails.addError(StockUtils.class.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("Error in getting preferred period from DB" + ex);
			return null;
		} finally {
			try {
				if(resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(StockUtils.class.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("GetPreferredSMA Error in closing resultset "+ex);
				logger.error("Error in closing resultset GetPreferredSMA  -> ", ex);
			}
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(StockUtils.class.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("GetPreferredSMA Error in closing statement "+ex);
				logger.error("Error in closing statement GetPreferredSMA  -> ", ex);
			}
		}
		return prefPeriod;
	}
	
	public static ArrayList<Float> GetSMAData(Connection connection, String stockCode, Integer period, Date targetDate) {
		ArrayList<Float> SMAData = null;
		ResultSet resultSet = null;
		Statement statement = null;
		String SMAvalue, tmpSQL;
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		int limitquery=0;
		try {
			SMAData = new ArrayList<Float>();
			if(period>10) {
				limitquery = period -10; 
			} else {
				limitquery = period;
			}
			if(connection == null) {
				connection = StockUtils.connectToDB();
			}
			statement = connection.createStatement();
			if(targetDate!=null) {
				tmpSQL = "SELECT SMA FROM DAILYSNEMOVINGAVERAGES where stockname='" + stockCode + "' and period = " + period.intValue() 
						  + " and tradeddate <='" + dateFormat.format(targetDate) + "' order by tradeddate desc limit " + limitquery + ";";
			} else {
				tmpSQL = "SELECT SMA FROM DAILYSNEMOVINGAVERAGES where stockname='" + stockCode + "' and period = " + period.intValue() 
				  + " order by tradeddate desc limit " + limitquery + ";";
			}
			resultSet = statement.executeQuery(tmpSQL);
			while (resultSet.next()) {
				SMAvalue = resultSet.getString(1);
				SMAData.add(Float.parseFloat(SMAvalue));
				// System.out.println("StockNme - " + stockNSECode);
			}
			resultSet.close();
		} catch (Exception ex) {
			HandleErrorDetails.addError(StockUtils.class.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("Error in getting SMA values for period = " + period + " error = " + ex);
			return null;
		} finally {
			try {
				if(resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(StockUtils.class.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("GetSMAData Error in closing resultset "+ex);
				logger.error("Error in closing resultset GetSMAData  -> ", ex);
			}
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(StockUtils.class.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("GetSMAData Error in closing statement "+ex);
				logger.error("Error in closing statement GetSMAData  -> ", ex);
			}
		}
		return SMAData;
	}
	
	public static int TotalHolidaysBetweenDates(Date startDate, Date endDate) {
		int totalHolidays = 0;
		for(int counter = 0; counter< holidayList.size(); counter++) {
			if((startDate.before(holidayList.get(counter)) || startDate.equals(holidayList.get(counter)))  && (endDate.after(holidayList.get(counter)) || endDate.equals(holidayList.get(counter)))) {
				totalHolidays++;
			}
		}
		return totalHolidays;
	}
	
	
}
