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
	
	public static boolean getFinancialIndication(String nseCode) {
		Connection connection = null;
		ResultSet resultSet = null;
		Statement statement = null;
		String indication;
		float netSales, netProfit, netCashFlow;
		ArrayList<Float> netSalesList = new ArrayList<Float>();
		ArrayList<Float> netProfitList = new ArrayList<Float>();
		ArrayList<Float> netCashFlowList = new ArrayList<Float>();
		try {
			//priceData = new ArrayList<Float>();
			connection = StockUtils.connectToDB();
			statement = connection.createStatement();;
			

			resultSet = statement.executeQuery("SELECT netSales, netProfit, netCash FROM STOCKANNUALFINANCIALDATA where STOCKCODE='" + nseCode + "' order by resultYear;");
			
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
				HandleErrorDetails.addError(StockUtils.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getFinancialIndication Error in closing connection "+ex);
				logger.error("Error in closing connection getFinancialIndication  -> ", ex);
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

			resultSet = statement.executeQuery("SELECT NSECODE, stockname, NSECODE FROM STOCKDETAILS;");
			while (resultSet.next()) {
				stockBSECode = resultSet.getString(1);
				stockBSECode = stockBSECode + "!" + resultSet.getString(2);
				stockBSECode = stockBSECode + "!" + resultSet.getString(3);
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
	
	public static boolean marketOpenOnGivenDate(Date targetDate) {
		if(targetDate == null) {
			targetDate = new Date();
		}
		if(targetDate.getDay() == 0 || targetDate.getDay() == 6) {
			return false;
		} else if(holidayList.contains(targetDate)) {
			return false;
		} else {
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
			connection = StockUtils.connectToDB();
			statement = connection.createStatement();
			if(targetDate!=null) {
				tmpSQL = "SELECT closeprice, Volume  FROM DAILYSTOCKDATA where stockname='" + objFinalSelectedStock.stockCode + "' " 
						  + " and tradeddate >'" + dateFormat.format(new Date(targetDate.getTime() - 9*24*60*60*1000)) + "' order by tradeddate desc limit 4;";
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
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				} 
			} catch (Exception ex) {
				HandleErrorDetails.addError(StockUtils.class.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getPriceAndVolumeDetails Error in closing connection "+ex);
				logger.error("Error in closing connection getPriceAndVolumeDetails  -> ", ex);
			}
		}
	}

}
