package com.amazonaws.tarun.stockApp.TechnicalIndicator.Calculation;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;
import java.time.LocalDate;

import com.amazonaws.tarun.stockApp.TechnicalIndicator.Data.FinalSelectedStock;
import com.amazonaws.tarun.stockApp.TechnicalIndicator.Data.MACDData;
import com.amazonaws.tarun.stockApp.TechnicalIndicator.Data.OnBalanceVolumeIndicator;
import com.amazonaws.tarun.stockApp.TechnicalIndicator.Data.SMAIndicatorDetails;
import com.amazonaws.tarun.stockApp.Utils.HandleErrorDetails;
import com.amazonaws.tarun.stockApp.Utils.StockUtils;

public class GenerateIndicationFromMACD {
	Connection connection = null;
	static Logger logger = Logger.getLogger(GenerateIndicationFromMACD.class);
	ArrayList<SMAIndicatorDetails> SMAIndicatorDetailsList;
	ArrayList<SMAIndicatorDetails> SMAIndicatorDetailsBelowHundredList;
	public static int daysToCheck = 5;
	public static String YAHOO_URL = "https://in.finance.yahoo.com/chart/";
	
	public static void main(String[] args) {
		Date dte = new Date();
		System.out.println("Start at -> " + dte.toString());
		GenerateIndicationFromMACD obj = new GenerateIndicationFromMACD();
		//obj.isSignalCrossedInMACD("20MICRONS", null);
		//To get indication from MACD
		//obj.CalculateIndicationfromMACD(null);
		//obj.CalculateIndicationfromMACD(new Date("25-Jan-2018"));		
		//To calculate MACD values and store
		/*for(int i = 257; i>=0; i--) {
			Date date = new Date(System.currentTimeMillis()-i*24*60*60*1000L);
			if(StockUtils.marketOpenOnGivenDate(date))*/
				obj.calculateSignalAndMACDBulkForAllStocks(new Date("01-Jun-2017"));
			
		//}
	}

	public void calculateSignalAndMACDBulkForAllStocks(Date calculationDate) {
		logger.debug("CalculateIndicationfromMACD start");
		ArrayList<String> stocklist = null;
		String nseCode;
		if( !StockUtils.marketOpenOnGivenDate(calculationDate))
			return;
		stocklist = StockUtils.getStockListFromDB();
		SMAIndicatorDetailsList = new ArrayList<SMAIndicatorDetails>();
		SMAIndicatorDetailsBelowHundredList = new ArrayList<SMAIndicatorDetails>();
		
		
		int stockcounter = 1;
		try {
			
			if (connection != null) {
				connection.close();
				connection = null;
			}
			connection = StockUtils.connectToDB();
			//Remove after testing
			//CalculateAndStoreMACDStockWise("MINDACORP", calculationDate);
			for (String stock : stocklist) {
				nseCode = stock.split("!")[2];
				System.out.println("For Stock -> " + nseCode + " Stock count -> " + stockcounter++);
				CalculateAndStoreMACDStockWise(nseCode, calculationDate);
			}
		}catch (Exception ex) {
			HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("calculateSignalAndMACDBulkForAllStocks Error in DB action"+ex);
			logger.error("Error in calculateSignalAndMACDBulkForAllStocks  -> ", ex);
		} finally {
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				} 
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("calculateSignalAndMACDBulkForAllStocks Error in DB action"+ex);
				logger.error("Error in calculateSignalAndMACDBulkForAllStocks  -> ", ex);
			}
		}
	}
	
	private void CalculateAndStoreMACDStockWise(String stockCode, Date calculationDate) throws Exception {
		MACDData objMACDData = null;
		float previousDaySignalValue;
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		
		objMACDData = GetEMAData(stockCode, calculationDate);
		objMACDData.MACDValues = new ArrayList<Float>();
		objMACDData.signalValues = new ArrayList<Float>();
		
		
		//Calculate signal line values
		float nineEMASMA, signalEMA = 0, nineDayEMASum = 0;
		int counter = 0;
		try {
			for (counter = 0 ; counter < objMACDData.twelveEMAPeriodValues.size()  ; counter++ ) {
				if( !StockUtils.marketOpenOnGivenDate(df.parse(objMACDData.tradeddate.get(counter)))) {
					objMACDData.signalValues.add(counter, null);
					objMACDData.MACDValues.add(counter, null);
					continue;
				}
				if(counter ==97)
						System.out.println("Test");
				objMACDData.MACDValues.add(objMACDData.twelveEMAPeriodValues.get(counter) - objMACDData.twentySixEMAPeriodValues.get(counter));
				previousDaySignalValue = getSignalFromDB(stockCode, df.parse(objMACDData.tradeddate.get(counter)));
				if (previousDaySignalValue == -9999) {
					signalEMA = -9999;
				} else {
					signalEMA = (2 / ((float) 10)) * (objMACDData.MACDValues.get(counter) - previousDaySignalValue) + previousDaySignalValue;
					objMACDData.signalValues.add(signalEMA);
				}
				if(signalEMA != -9999)
					storeMACDDatatoDB(stockCode, objMACDData, counter);
				/* Block for Bulk calculation. Not needed now
				 * nineDayEMASum = nineDayEMASum + objMACDData.MACDValues.get(counter);
				if(counter<8) {
					objMACDData.signalValues.add(null);
				}
				if(counter==8) {
					nineEMASMA = nineDayEMASum / (counter+1);
					objMACDData.signalValues.add(nineEMASMA);
					
				} else if (counter>8) {
					
					signalEMA = (2 / ((float) 10)) * (objMACDData.MACDValues.get(counter) - objMACDData.signalValues.get(objMACDData.signalValues.size()-1)) + objMACDData.signalValues.get(objMACDData.signalValues.size()-1);
					objMACDData.signalValues.add(signalEMA);
				}	*/		
			}
		}catch (Exception ex) {
			HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("CalculateAndStoreMACDStockWise Error in calculation"+ex);
			logger.error("Error in CalculateAndStoreMACDStockWise  -> ", ex);
		}
		
	}
	//created method to get previous day Signal call this from method above and revome buld calculation
	private float getSignalFromDB(String stockName, Date tragetDate) {

		ResultSet resultSet = null;
		Statement statement = null;
		float eMA = -1;
		String tmpSQL;
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");		
		
		try {
			statement = connection.createStatement();

			if(tragetDate!=null) {
				tmpSQL = "SELECT MACDSIGNAL, tradeddate FROM Daily_MACD where stockName ='"
						+ stockName + "' and tradeddate<'" + dateFormat.format(tragetDate) + "' order by tradeddate desc;";
			} else {
				tmpSQL = "SELECT MACDSIGNAL, tradeddate FROM Daily_MACD where stockName ='"
						+ stockName + "' order by tradeddate desc;";
			}
			
			resultSet = statement.executeQuery(tmpSQL);
			while (resultSet.next()) {
				/*if(tragetDate==null) {
					Calendar c = Calendar.getInstance();
					Date tmpTodayDate = new Date("28-Oct-2017");
					c.setTime(tmpTodayDate);
					c.get(Calendar.DAY_OF_WEEK);
					//if()
					tmpTodayDate = new Date(System.currentTimeMillis()-1*24*60*60*1000L);
					tragetDate = new Date(Date.parse(resultSet.getString(2)));
					if(tragetDate.compareTo(tmpTodayDate)==0) {
						eMA = Float.parseFloat(resultSet.getString(1));
					} else {
						eMA = -9999;
					}
				} else {*/
					eMA = Float.parseFloat(resultSet.getString(1));
				//}
				break;
				// System.out.println("StockNme - " + stockNSECode);
			}
			return eMA;
		} catch (Exception ex) {
			HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("getSignalFromDB Error in DB action"+ex);
			logger.error("Error in getSignalFromDB", ex);
			return eMA;
		} finally {
			try {
				if(resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getSignalFromDB Error in closing resultset "+ex);
				logger.error("Error in closing resultset getSignalFromDB  -> ", ex);
			}
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getSignalFromDB Error in closing statement "+ex);
				logger.error("Error in closing statement getSignalFromDB  -> ", ex);
			}
		}
	}
	
	private void storeMACDDatatoDB(String stockCode, MACDData objMACDData, int counter) {
		Statement statement = null;
		String tmpsql;
		try {			
			//connection = StockUtils.connectToDB();
			statement = connection.createStatement();
			//for (int counter = 0 ; counter < objMACDData.signalValues.size()  ; counter++ ) {
				if(objMACDData.signalValues.get(counter)!=null) {
					tmpsql = "INSERT INTO Daily_MACD (STOCKNAME, TRADEDDATE, MACD, MACDSIGNAL) VALUES ('"
							+ stockCode + "','" + objMACDData.tradeddate.get(counter) + "'," + objMACDData.MACDValues.get(counter) + "," + objMACDData.signalValues.get(counter) + ");";
					statement.executeUpdate(tmpsql);
				}
			//}
			
			
		} catch (Exception ex) {
			HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("storeMACDDatatoDB for stock -> " + stockCode + " Error in DB action" + ex);
			logger.error("Error in storeMACDDatatoDB  ->  storeMACDDatatoDB for stock -> " + stockCode, ex);
		} finally {
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("storeMACDDatatoDB Error in closing statement "+ex);
				logger.error("Error in closing statement storeMACDDatatoDB  -> ", ex);
			}
			
		}
	}
	
	public void CalculateIndicationfromMACD(Date calculationDate) {
		logger.debug("CalculateIndicationfromMACD start");
		ArrayList<String> stocklist = null;
		String nseCode, bseCode;
		ArrayList<String> stocklistSMA = null;
		SMAIndicatorDetails objSMAIndicatorDetails;
		FinalSelectedStock objFinalSelectedStock = null;
		ArrayList<FinalSelectedStock> objFinalSelectedStockList = new ArrayList<FinalSelectedStock>();
		if(!StockUtils.marketOpenOnGivenDate(calculationDate))
			return;
		//UpdateIndicatedStocks tmpUpdateIndicatedStocks = new UpdateIndicatedStocks();
		try {
			stocklist = StockUtils.getStockListFromDB();
			GenerateIndicationfromMovingAverage obj = new GenerateIndicationfromMovingAverage();
			obj.CalculateIndicationfromSMA(calculationDate);
			SMAIndicatorDetailsList = obj.getIndicationStocks();
			SMAIndicatorDetailsBelowHundredList = obj.getBelowHunderdIndicationStocks();
			/*if (connection != null) {
				connection.close();
				connection = null;
			}
			connection = StockUtils.connectToDB();*/
			
			//SMAIndicatorDetailsList = new ArrayList<SMAIndicatorDetails>();
			//SMAIndicatorDetailsBelowHundredList = new ArrayList<SMAIndicatorDetails>();
			int stockcounter = 1;
			int stockwithMACDCrossed = 1;
			int stockwithMACDCrossedAndGood = 1;
			stocklistSMA = getStockList(SMAIndicatorDetailsList);
			for (String stock : stocklist) {
				//stockName = stock.split("!")[1];
				bseCode = stock.split("!")[0];
				nseCode = stock.split("!")[2];
				System.out.println("For Stock -> " + nseCode + " Stock count -> " + stockcounter++);
				if(StockUtils.getFinancialIndication(bseCode)) {	
					//objSMAIndicatorDetails = new SMAIndicatorDetails();
					//objSMAIndicatorDetails.stockCode = nseCode;
					//System.out.println("Under finacial check");
					if(isSignalCrossedInMACD(nseCode, calculationDate)) {
						System.out.println("*****************************Stock Added for indication -> " + nseCode);
						stockwithMACDCrossed++;
						//SMAIndicatorDetailsList.add(objSMAIndicatorDetails);
						if(stocklistSMA.contains(nseCode)) {
							
							objFinalSelectedStock = getAlldetails(SMAIndicatorDetailsList.get(stocklistSMA.indexOf(nseCode)), calculationDate);
							if(objFinalSelectedStock!=null) {
								objFinalSelectedStockList.add(objFinalSelectedStock);
								stockwithMACDCrossedAndGood++;
	/*							if(objSMAIndicatorDetails.stockPrice<100) {
									System.out.println("****Added for below 100");
									SMAIndicatorDetailsBelowHundredList.add(objSMAIndicatorDetails);
								}
	*/						}
						}
					}
					
				}
				/*if (stockcounter > 100) {
					break;
				}*/
			}
			//Send top stock in mail
			System.out.println("Send mail");
			sendTopStockInMail(objFinalSelectedStockList, false, "Combined MACD Crossed -> Stocklist on ");
			System.out.println("MACD crossed stoks -> "+stockwithMACDCrossed);
			System.out.println("MACD crossed stoks and good -> "+stockwithMACDCrossedAndGood);
		}catch (Exception ex) {
			HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("CalculateIndicationfromMACD Error in DB action"+ex);
			logger.error("Error in CalculateIndicationfromMACD  -> ", ex);
		} 
		
		
		
		//tmpUpdateIndicatedStocks.updateSMAIndication(SMAIndicatorDetailsList);
		logger.debug("CalculateIndicationfromMACD end");
	}
	
	public boolean isSignalCrossedInMACD(String stockCode, Date calculationDate) {
		MACDData objMACDData = null;
		System.out.println("CHecking cross");
		if(stockCode.equalsIgnoreCase("AGARIND")) {
			System.out.println("test");;
		}
		objMACDData = getMACDData(stockCode, calculationDate);
		//System.out.println("CHecking cross2"+objMACDData);
		if(!(objMACDData!=null && objMACDData.MACDValues!=null && objMACDData.MACDValues.size()>2))
			return false;
		
		for (int counter = 0 ; counter < objMACDData.MACDValues.size()-1 ; counter++ ) {
			if((objMACDData.MACDValues.get(counter)>objMACDData.signalValues.get(counter)) && (objMACDData.MACDValues.get(counter+1)<objMACDData.signalValues.get(counter+1))) {
				//System.out.println("CHecked cross");
				return true;
			}
		}
		//System.out.println("CHecked cross");
		return false;
	}
	
	private MACDData GetEMAData(String stockCode, Date targetDate) {
		ArrayList<Float> SMAData = null;
		ResultSet resultSet = null;
		Statement statement = null;
		String SMAvalue, tmpSQL, tradeddate;
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		MACDData objMACDData = new MACDData();
		try {
			SMAData = new ArrayList<Float>();
			/*if (connection != null) {
				connection.close();
				connection = null;
			}
			connection = StockUtils.connectToDB();*/
			statement = connection.createStatement();
			//For 12 period
			if(targetDate!=null) {
				tmpSQL = "SELECT EMA, tradeddate FROM DAILYSNEMOVINGAVERAGES where stockname='" + stockCode + "' and period = 12" 
						  + " and tradeddate >='" + dateFormat.format(targetDate) + "';";// + "' order by tradeddate limit " + (daysToCheck+18) + ";";
			} else {
				tmpSQL = "SELECT EMA, tradeddate FROM DAILYSNEMOVINGAVERAGES where stockname='" + stockCode + "' and period = 12 order by tradeddate desc limit 1;" ;
				  //+ " order by tradeddate limit " + (daysToCheck+18) + ";";
			}
			resultSet = statement.executeQuery(tmpSQL);
			objMACDData.twelveEMAPeriodValues = new ArrayList<Float>();
			objMACDData.tradeddate = new ArrayList<String>();
			while (resultSet.next()) {
				SMAvalue = resultSet.getString(1);
				tradeddate = resultSet.getString(2);
				objMACDData.twelveEMAPeriodValues.add(Float.parseFloat(SMAvalue));
				objMACDData.tradeddate.add(tradeddate);
				//SMAData.add(Float.parseFloat(SMAvalue));
				// System.out.println("StockNme - " + stockNSECode);
			}
			resultSet.close();
			//For 26 period
			if(targetDate!=null) {
				tmpSQL = "SELECT EMA FROM DAILYSNEMOVINGAVERAGES where stockname='" + stockCode + "' and period = 26" 
						  + " and tradeddate >='" + dateFormat.format(targetDate) + "';";// + "' order by tradeddate desc limit " + (daysToCheck+18) + ";";
			} else {
				tmpSQL = "SELECT EMA FROM DAILYSNEMOVINGAVERAGES where stockname='" + stockCode + "' and period = 26 order by tradeddate desc limit 1;" ;
				  //+ " order by tradeddate desc limit " + (daysToCheck+18) + ";";
			}
			resultSet = statement.executeQuery(tmpSQL);
			objMACDData.twentySixEMAPeriodValues = new ArrayList<Float>();
			while (resultSet.next()) {
				SMAvalue = resultSet.getString(1);
				objMACDData.twentySixEMAPeriodValues.add(Float.parseFloat(SMAvalue));
				//SMAData.add(Float.parseFloat(SMAvalue));
				// System.out.println("StockNme - " + stockNSECode);
			}
			resultSet.close();
			resultSet = null;
			/*connection.close();
			connection = null;*/
			return objMACDData;
		} catch (Exception ex) {
			HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("GetEMAData Error in getting EMA values  error = " + ex);
			return null;
		} finally {
			try {
				if(resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("GetEMAData Error in closing resultset "+ex);
				logger.error("Error in closing resultset GetEMAData  -> ", ex);
			}
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("GetEMAData Error in closing statement "+ex);
				logger.error("Error in closing statement GetEMAData  -> ", ex);
			}
		}
	}
	
	private MACDData getMACDData(String stockCode, Date targetDate) {
		MACDData objMACDData = null;
		ResultSet resultSet = null;
		Statement statement = null;
		String SMAvalue, tmpSQL, tradeddate;
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		objMACDData = new MACDData();
		objMACDData.MACDValues = new ArrayList<Float>();
		objMACDData.signalValues = new ArrayList<Float>();
		try {
			if (connection != null) {
				connection.close();
				connection = null;
			}
			connection = StockUtils.connectToDB();
			statement = connection.createStatement();
			if(targetDate!=null) {
				tmpSQL = "SELECT MACDSignal, MACD, tradeddate FROM Daily_MACD where stockname='" + stockCode + "' " 
						  + " and tradeddate >='" + dateFormat.format(new Date(targetDate.getTime() - daysToCheck*24*60*60*1000)) + "' order by tradeddate desc limit " + (daysToCheck) + ";";
			} else {
				tmpSQL = "SELECT MACDSignal, MACD, tradeddate FROM Daily_MACD where stockname='" + stockCode + "' order by tradeddate desc limit " + (daysToCheck) + ";";
				  //+ " order by tradeddate limit " + (daysToCheck+18) + ";";
			}
			resultSet = statement.executeQuery(tmpSQL);
			objMACDData.twelveEMAPeriodValues = new ArrayList<Float>();
			objMACDData.tradeddate = new ArrayList<String>();
			while (resultSet.next()) {
				SMAvalue = resultSet.getString(1);
				objMACDData.signalValues.add(Float.parseFloat(SMAvalue));
				SMAvalue = resultSet.getString(2);
				objMACDData.MACDValues.add(Float.parseFloat(SMAvalue));
				tradeddate = resultSet.getString(3);
				objMACDData.tradeddate.add(tradeddate);
			}
			return objMACDData;
		} catch (Exception ex) {
			HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("getMACDData - Error in getting MACD values  error = " + ex);
			return null;
		} finally {
			try {
				if(resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getMACDData Error in closing resultset "+ex);
				logger.error("Error in closing resultset getMACDData  -> ", ex);
			}
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getMACDData Error in closing statement "+ex);
				logger.error("Error in closing statement getMACDData  -> ", ex);
			}
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				} 
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getMACDData Error in closing connection "+ex);
				logger.error("Error in closing connection getMACDData  -> ", ex);
			}
		}
	}
	
	public boolean isMACDIncreasing(String stockCode, Date calculationDate) {
		MACDData objMACDData = null;
		
		objMACDData = getMACDData(stockCode, calculationDate);
		if(!(objMACDData != null && objMACDData.MACDValues !=null && objMACDData.MACDValues.size()>3)) {
			return false;
		}
		for (int counter = 0 ; counter < 3 ; counter++ ) {
			if(objMACDData.MACDValues.get(counter)<objMACDData.MACDValues.get(counter+1)) {
				return false;
			}
		}
		return true;
	}

	private FinalSelectedStock getAlldetails (SMAIndicatorDetails objSMAIndicatorDetails, Date calculationDate) {
		FinalSelectedStock objFinalSelectedStock = null;
		CalculateOnBalanceVolume objCalculateOnBalanceVolume;
		OnBalanceVolumeIndicator objOnBalanceVolumeIndicator;
		CalculateBollingerBands objCalculateBollingerBands;
		CalculateRSIIndicator objCalculateRSIIndicator;
		CalculateStochasticOscillator objCalculateStochasticOscillator;
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String bbIndicator;
		float rsiIndication;
		float chandelierExitLong;
		boolean MACDCross;
		//float chandelierExitShort;
		System.out.println("Get All Details");
		GenerateIndicationFromMACD objGenerateIndicationFromMACD = new GenerateIndicationFromMACD();
		objCalculateStochasticOscillator = new CalculateStochasticOscillator();
		if(!objCalculateStochasticOscillator.getStochasticIndicator(objSMAIndicatorDetails.stockCode, calculationDate)) {
			return null;
		}
		if(!objGenerateIndicationFromMACD.isMACDIncreasing(objSMAIndicatorDetails.stockCode, calculationDate)) {
			return null;
		}
		objFinalSelectedStock = new FinalSelectedStock();
		//add selcted stock
		objCalculateOnBalanceVolume = new CalculateOnBalanceVolume();
		objOnBalanceVolumeIndicator = objCalculateOnBalanceVolume.calculateOnBalanceVolumeDaily(objSMAIndicatorDetails.stockCode, calculationDate);
		
		objCalculateBollingerBands = new CalculateBollingerBands();
		bbIndicator = objCalculateBollingerBands.getBBIndicationForStockV1(objSMAIndicatorDetails.stockCode, calculationDate);
		
		CalculateAverageTrueRange objCalculateAverageTrueRange = new CalculateAverageTrueRange();
		chandelierExitLong = objCalculateAverageTrueRange.getChandelierExitLong(objSMAIndicatorDetails.stockCode, calculationDate);
		//chandelierExitShort =  objCalculateAverageTrueRange.getChandelierExitShort(objSMAIndicatorDetails.stockCode, calculationDate);
		System.out.println("adding RSI");
		objCalculateRSIIndicator = new CalculateRSIIndicator();
		if(calculationDate!=null) { 
			rsiIndication= objCalculateRSIIndicator.getRSIValue(objSMAIndicatorDetails.stockCode, LocalDate.parse(dateFormat.format(calculationDate).toString()));
		} else {
			rsiIndication= objCalculateRSIIndicator.getRSIValue(objSMAIndicatorDetails.stockCode, null);
		}
		
		//MACDCross = objGenerateIndicationFromMACD.isSignalCrossedInMACD(objSMAIndicatorDetails.stockCode, calculationDate);
		
		objFinalSelectedStock.stockCode = objSMAIndicatorDetails.stockCode;
		objFinalSelectedStock.stockPrice = objSMAIndicatorDetails.stockPrice;
		objFinalSelectedStock.tradeddate = objSMAIndicatorDetails.signalDate;
		objFinalSelectedStock.percentagePriceChange = objSMAIndicatorDetails.percentagePriceChange;
		objFinalSelectedStock.PNSMAcrossover = objSMAIndicatorDetails.PNSMAcrossover;
		objFinalSelectedStock.SMNSMcrossover = objSMAIndicatorDetails.SMNSMcrossover;
		if(objOnBalanceVolumeIndicator!=null) {
			objFinalSelectedStock.percentageChangeInVolumeInLastDay = objOnBalanceVolumeIndicator.percentageChangeInLastDay;
		}
		
		objFinalSelectedStock.BBIndicator = bbIndicator;
		objFinalSelectedStock.rsiValue = rsiIndication;
		objFinalSelectedStock.chandelierExitLong = chandelierExitLong;
		objFinalSelectedStock.MACDCross = true;
		//objFinalSelectedStock.chandelierExitShort = chandelierExitShort;
		
		return objFinalSelectedStock;
	}

	private void sendTopStockInMail(ArrayList<FinalSelectedStock> objFinalSelectedStockList, Boolean belowHunderd, String subject) {
		logger.debug("sendTopStockInMail Started");
		StringBuilder mailBody = new StringBuilder();
		mailBody.append("<html><body><table border='1'><tr><th>Sr. No.</th><th>Date</th><th>Stock code</th>");
		mailBody.append("<th>Stock Price</th><th>9 to 50 SM Cross</th><th>Price crossed 20 SMA</th><th>% Price Change</th><th>% Volume Increase</th><th>BB Indication</th>"
				+ "<th>RSI Indication</th><th>Chandelier Exit</th><th>MACD Crossed</th><th>Accumulation/ Distribution Line</th></tr>");			
		for (int counter = 0; counter <(objFinalSelectedStockList.size()>30?30:objFinalSelectedStockList.size()-1); counter++) {
			mailBody.append("<tr><td>" + (counter+1) + "</td>");
			mailBody.append("<td>" + objFinalSelectedStockList.get(counter).tradeddate + "</td>");
			mailBody.append("<td><a href='"+ YAHOO_URL + objFinalSelectedStockList.get(counter).stockCode + ".NS'>" + objFinalSelectedStockList.get(counter).stockCode + "</a></td>");
			mailBody.append("<td>" + objFinalSelectedStockList.get(counter).stockPrice + "</td>");
			if(objFinalSelectedStockList.get(counter).SMNSMcrossover) {
				mailBody.append("<td bgcolor='green'>" + objFinalSelectedStockList.get(counter).SMNSMcrossover + "</td>");
			} else {
				mailBody.append("<td>" + objFinalSelectedStockList.get(counter).SMNSMcrossover + "</td>");
			}
			if(objFinalSelectedStockList.get(counter).PNSMAcrossover) {
				mailBody.append("<td bgcolor='green'>" + objFinalSelectedStockList.get(counter).PNSMAcrossover + "</td>");
			} else {
				mailBody.append("<td>" + objFinalSelectedStockList.get(counter).PNSMAcrossover + "</td>");
			}
			
			mailBody.append("<td>" + objFinalSelectedStockList.get(counter).percentagePriceChange + "</td>");
			mailBody.append("<td>" + objFinalSelectedStockList.get(counter).percentageChangeInVolumeInLastDay + "</td>");
			if(objFinalSelectedStockList.get(counter).BBIndicator.equalsIgnoreCase("Contracting")) {
				mailBody.append("<td bgcolor='green'>" + objFinalSelectedStockList.get(counter).BBIndicator + "</td>");
			} else {
				mailBody.append("<td>" + objFinalSelectedStockList.get(counter).BBIndicator + "</td>");
			}
			if(objFinalSelectedStockList.get(counter).rsiValue>= 30 && objFinalSelectedStockList.get(counter).rsiValue <=70) {
				mailBody.append("<td bgcolor='green'>" + objFinalSelectedStockList.get(counter).rsiValue + "</td>");
			} else {
				mailBody.append("<td>" + objFinalSelectedStockList.get(counter).rsiValue + "</td>");
			}
			
			String chandelierExitColValue = objFinalSelectedStockList.get(counter).chandelierExitLong + "";
			if(objFinalSelectedStockList.get(counter).stockPrice>= objFinalSelectedStockList.get(counter).chandelierExitLong) {
				mailBody.append("<td bgcolor='green'>" + chandelierExitColValue + "</td>");
			} else {
				mailBody.append("<td bgcolor='red'>" /*+ chandelierExitColValue*/ + "</td>");
			}
			
			//MACDCross
			if(objFinalSelectedStockList.get(counter).MACDCross) {
				mailBody.append("<td bgcolor='green'>" + objFinalSelectedStockList.get(counter).MACDCross + "</td>");
			} else {
				mailBody.append("<td>" + objFinalSelectedStockList.get(counter).MACDCross + "</td>");
			}
			mailBody.append("<td>" +  "</td></tr>");
		}
		mailBody.append("</table></body></html>");
		if(objFinalSelectedStockList.size() > 0) {
        	new SendSuggestedStockInMail("tarunstockcomm@gmail.com",subject+" "+objFinalSelectedStockList.get(0).tradeddate.toString(),mailBody.toString());
        } 
        /*if(objFinalSelectedStockList.size() > 0) {
        	new SendSuggestedStockInMail("tarunstockcomm@gmail.com",subject+" "+new Date(),mailBody.toString());
        }*/
        logger.debug("sendTopStockInMail end");
	}

	private ArrayList<String> getStockList(ArrayList<SMAIndicatorDetails> objSMAIndicatorDetailsList) {
		ArrayList<String> stocklistSMA = new ArrayList<String>();
		for(int counter = 0;counter < objSMAIndicatorDetailsList.size(); counter++ ) {
			if(objSMAIndicatorDetailsList.get(counter).stockCode!=null) {
				stocklistSMA.add(objSMAIndicatorDetailsList.get(counter).stockCode);
			}
		}
		return stocklistSMA;
		
	}
	
}
