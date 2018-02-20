package com.amazonaws.tarun.stockApp.TechnicalIndicator.Calculation;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

import com.amazonaws.tarun.stockApp.TechnicalIndicator.Data.OnBalanceVolumeData;
import com.amazonaws.tarun.stockApp.TechnicalIndicator.Data.OnBalanceVolumeIndicator;
import com.amazonaws.tarun.stockApp.Utils.HandleErrorDetails;
import com.amazonaws.tarun.stockApp.Utils.StockUtils;

public class CalculateOnBalanceVolume {
	Connection connection = null;
	static Logger logger = Logger.getLogger(CalculateOnBalanceVolume.class);	
	String stockName;
	String bseCode;
	String nseCode;
	
	public static void main(String[] args) {
		Date dte = new Date();
		logger.debug("CalculateOnBalanceVolume Started");
		System.out.println("Start at -> " + dte.toString());
		CalculateOnBalanceVolume obj = new CalculateOnBalanceVolume();
		obj.OnBalanceVolumeCalculation(new Date("16-Oct-2017"));
		//dte = new Date();
		System.out.println("End at -> " + dte.toString());
		logger.debug("CalculateOnBalanceVolume End");
	}
	
	private void OnBalanceVolumeCalculation(Date calculationDate) {
		ArrayList<String> stockList = null;
		stockList = StockUtils.getStockListFromDB();
		ArrayList<OnBalanceVolumeIndicator> onBalanceSelectedStockList = new ArrayList<OnBalanceVolumeIndicator>();
		OnBalanceVolumeIndicator tmpOnBalanceVolumeIndicator;
		for (String stockCode : stockList) {
			//calculate average on bulk
			//calculateOnBalanceVolume(stockCode);
			stockName = stockCode.split("!")[1];
			bseCode = stockCode.split("!")[0];
			nseCode = stockCode.split("!")[2];
			if(StockUtils.getFinancialIndication(nseCode)) {
				tmpOnBalanceVolumeIndicator = calculateOnBalanceVolumeDaily(nseCode, calculationDate);
				if (tmpOnBalanceVolumeIndicator!=null) {
					onBalanceSelectedStockList.add(tmpOnBalanceVolumeIndicator);
				}
			}
		}
	}
	
	public OnBalanceVolumeIndicator calculateOnBalanceVolumeDaily(String stockCode, Date calculationDate) {
		OnBalanceVolumeData stockDetails = null;
		long onBalanceVolume = 0;
		float lastDayClosingPrice = 0;
		//long volumechangeinlasday;
		boolean continuousVolumeIncrease = true;
		
		stockDetails = getStockDetailsFromDBDaily(stockCode, calculationDate);
		ArrayList<Long> tmpOnBalanceVol = new ArrayList<Long>();
		if (stockDetails == null || stockDetails.tradeddate == null) {
			System.out.println("stock details null for - > "+stockCode);
		}
		for (int counter = 0; counter < stockDetails.tradeddate.size(); counter++) {			
			if(counter == 0) {
				onBalanceVolume = 0;				
			} else {
				if (stockDetails.closePrice.get(counter) >= lastDayClosingPrice) {
					onBalanceVolume = onBalanceVolume + stockDetails.volume.get(counter);
				} else if (stockDetails.closePrice.get(counter) < lastDayClosingPrice) {
					onBalanceVolume = onBalanceVolume - stockDetails.volume.get(counter);
				}
			}
			tmpOnBalanceVol.add(onBalanceVolume);
			lastDayClosingPrice = stockDetails.closePrice.get(counter);
			//storeOnBalanceVolumeinDB(stockCode, stockDetails.tradeddate.get(counter), stockDetails.closePrice.get(counter),onBalanceVolume, stockDetails.volume.get(counter));			
		}
		//if last day volume is down then do not add stock to list
		if(tmpOnBalanceVol.get(tmpOnBalanceVol.size()-1) < tmpOnBalanceVol.get(tmpOnBalanceVol.size()-2)) {
			return null;
		}
		OnBalanceVolumeIndicator tmpOnBalanceVolumeIndicator = new OnBalanceVolumeIndicator();
		tmpOnBalanceVolumeIndicator.stockName = stockCode;
		tmpOnBalanceVolumeIndicator.tradeddate = stockDetails.tradeddate.get(stockDetails.tradeddate.size()-1);
		tmpOnBalanceVolumeIndicator.volumeChangeInLastDay = tmpOnBalanceVol.get(tmpOnBalanceVol.size()-1) - tmpOnBalanceVol.get(tmpOnBalanceVol.size()-2);
		tmpOnBalanceVolumeIndicator.continuousIncreasedVolume = continuousVolumeIncrease;
		//Temporary check to avoid devide by zero error. Needs to be checked why the value is zero
		if(tmpOnBalanceVol.get(tmpOnBalanceVol.size()-2)!=0)
			tmpOnBalanceVolumeIndicator.percentageChangeInLastDay = ((tmpOnBalanceVol.get(tmpOnBalanceVol.size()-1) - tmpOnBalanceVol.get(tmpOnBalanceVol.size()-2))/tmpOnBalanceVol.get(tmpOnBalanceVol.size()-2))*100;
		else
			tmpOnBalanceVolumeIndicator.percentageChangeInLastDay = 0;
		for (int counter = tmpOnBalanceVol.size()-2; counter > 0 ; counter--) {
			if (tmpOnBalanceVol.get(counter) < tmpOnBalanceVol.get(counter-1)) {
				continuousVolumeIncrease = false;
				break;
			}
		}
		return tmpOnBalanceVolumeIndicator;
	}
	
	private OnBalanceVolumeData getStockDetailsFromDBDaily(String stockCode, Date calculationDate) {
		ResultSet resultSet = null;
		Statement statement = null;
		String tradedDate, tmpSQL;
		Float closePrice;
		long volume;
		OnBalanceVolumeData onBalanceVolumeDataObj = null;
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		try {
			if (connection != null) {
				connection.close();
				connection = null;
			}
			connection = StockUtils.connectToDB();
			onBalanceVolumeDataObj = new OnBalanceVolumeData();
			onBalanceVolumeDataObj.closePrice = new ArrayList<Float>();
			onBalanceVolumeDataObj.tradeddate = new ArrayList<String>();
			onBalanceVolumeDataObj.volume = new ArrayList<Long>();
			onBalanceVolumeDataObj.onBalanceVolume = new ArrayList<Long>();
			statement = connection.createStatement();
			onBalanceVolumeDataObj.stockName = stockCode;
			if(calculationDate!=null) {
				tmpSQL = "Select * from (SELECT tradeddate, closeprice, volume FROM DAILYSTOCKDATA where stockname='"
						+ stockCode + "' and tradeddate <='" + dateFormat.format(calculationDate) + "' order by tradeddate desc limit 10) As stockdata order by TRADEDDATE;";
			} else {
				tmpSQL = "Select * from (SELECT tradeddate, closeprice, volume FROM DAILYSTOCKDATA where stockname='"
						+ stockCode + "' order by tradeddate desc limit 10) As stockdata order by TRADEDDATE;";
			}			
			resultSet = statement.executeQuery(tmpSQL);
			while (resultSet.next()) {
				tradedDate = resultSet.getString(1);
				/*if (tradedDate.equalsIgnoreCase("2015-06-08")) {
					System.out.println("Tet");
				}*/
				//BigDecimal.valueOf(resultSet.getString(3));
				closePrice = Float.parseFloat(resultSet.getString(2));
				//volume =  Long.parseLong((resultSet.getString(3).substring(0, resultSet.getString(3).length()-2)));
				volume = BigDecimal.valueOf(resultSet.getDouble(3)).longValue();
				onBalanceVolumeDataObj.closePrice.add(closePrice);
				onBalanceVolumeDataObj.tradeddate.add(tradedDate);
				onBalanceVolumeDataObj.volume.add(volume);
			}
			return onBalanceVolumeDataObj;
		} catch (Exception ex) {
			HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			try{
				System.out.println("getStockDetailsFromDBDaily Error in DB action Date = " + resultSet.getString(1));
			} catch(Exception ex1) { }
			logger.error("Error in getStockDetailsFromDBDaily  -> ", ex);
			return null;
		}  finally {
			try {
				if(resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getStockDetailsFromDBForBulk Error in closing resultset "+ex);
				logger.error("Error in closing resultset getStockDetailsFromDB  -> ", ex);
			}
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getStockDetailsFromDBForBulk Error in closing statement "+ex);
				logger.error("Error in closing statement getStockDetailsFromDB  -> ", ex);
			}
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				} 
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getStockDetailsFromDBForBulk Error in closing connection "+ex);
				logger.error("Error in closing connection getStockDetailsFromDB  -> ", ex);
			}
		}
	}	
}
