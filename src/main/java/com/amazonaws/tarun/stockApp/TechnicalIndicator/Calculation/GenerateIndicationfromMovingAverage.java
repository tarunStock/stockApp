package com.amazonaws.tarun.stockApp.TechnicalIndicator.Calculation;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import org.apache.log4j.Logger;

import com.amazonaws.tarun.stockApp.TechnicalIndicator.Data.SMAIndicatorDetails;
import com.amazonaws.tarun.stockApp.TechnicalIndicator.Data.SMAIndicatorDetailsComparator;
import com.amazonaws.tarun.stockApp.Utils.HandleErrorDetails;
import com.amazonaws.tarun.stockApp.Utils.StockUtils;

public class GenerateIndicationfromMovingAverage {
	Connection connection = null;
	public static int daysToCheck = 3;
	ArrayList<SMAIndicatorDetails> SMAIndicatorDetailsList;
	ArrayList<SMAIndicatorDetails> SMAIndicatorDetailsBelowHundredList;
	public SMAIndicatorDetails objSMAIndicatorDetails;
	String stockName;
	String bseCode;
	String nseCode;
	static Logger logger = Logger.getLogger(GenerateIndicationfromMovingAverage.class);
	
	public static void main(String[] args) {
		Date dte = new Date();
		Connection connection = null;
		System.out.println("Start at -> " + dte.toString());
		GenerateIndicationfromMovingAverage obj = new GenerateIndicationfromMovingAverage();
		//obj.CalculateAndSendIndicationfromSMA(new Date("13-Oct-2017"));
		obj.CalculateIndicationfromSMA(connection, null);
	}

	public void CalculateIndicationfromSMA(Connection connection, Date calculationDate) {
		logger.debug("CalculateIndicationfromSMA start");
		
		ArrayList<String> stocklist = null;
		
		if(!StockUtils.marketOpenOnGivenDate(calculationDate))
			return;
		//UpdateIndicatedStocks tmpUpdateIndicatedStocks = new UpdateIndicatedStocks();
		if(connection == null) {
			connection = StockUtils.connectToDB();
		}
		stocklist = StockUtils.getStockListFromDB(connection);
		SMAIndicatorDetailsList = new ArrayList<SMAIndicatorDetails>();
		SMAIndicatorDetailsBelowHundredList = new ArrayList<SMAIndicatorDetails>();
		int stockcounter = 1;
		for (String stock : stocklist) {
			stockName = stock.split("!")[1];
			bseCode = stock.split("!")[0];
			nseCode = stock.split("!")[2];
			System.out.println("For Stock -> " + nseCode + " Stock count -> " + stockcounter++);
			if(StockUtils.getFinancialIndication(connection, bseCode)) {	
				
				
				CalculateIndicationfromSMA(connection, nseCode, calculationDate);
				if (objSMAIndicatorDetails.signalPriceToSMA != null || objSMAIndicatorDetails.signalSMAToSMA != null) {
					System.out.println("*****************************Stock Added for indication -> " + nseCode);
					SMAIndicatorDetailsList.add(objSMAIndicatorDetails);
					if(objSMAIndicatorDetails.stockPrice<100) {
						SMAIndicatorDetailsBelowHundredList.add(objSMAIndicatorDetails);
					}
				}
			}
			/*if (stockcounter > 200) {
				break;
			}*/
		}
		logger.debug("CalculateAndSendIndicationfromSMA calculation completed");
		// Collections.sort(SMAIndicatorDetailsList);
		logger.debug("CalculateAndSendIndicationfromSMA start mail");
		Collections.sort(SMAIndicatorDetailsList, new SMAIndicatorDetailsComparator());		
		
		Collections.sort(SMAIndicatorDetailsBelowHundredList, new SMAIndicatorDetailsComparator());
		
		//tmpUpdateIndicatedStocks.updateSMAIndication(SMAIndicatorDetailsList);
		logger.debug("CalculateIndicationfromSMA end");
		System.out.println("End");
	}
	
	public ArrayList<SMAIndicatorDetails> getIndicationStocks() {
		return SMAIndicatorDetailsList;
	}
	
	public ArrayList<SMAIndicatorDetails> getBelowHunderdIndicationStocks() {
		return SMAIndicatorDetailsBelowHundredList;
	}
	
	public void CalculateAndSendIndicationfromSMA(Connection connection, Date calculationDate) {
		ArrayList<String> stocklist = null;
		
		if( !StockUtils.marketOpenOnGivenDate(calculationDate))
			return;
		
		//UpdateIndicatedStocks tmpUpdateIndicatedStocks = new UpdateIndicatedStocks();
		if(connection == null) {
			connection = StockUtils.connectToDB();
		}
		stocklist = StockUtils.getStockListFromDB(connection);
		SMAIndicatorDetailsList = new ArrayList<SMAIndicatorDetails>();
		SMAIndicatorDetailsBelowHundredList = new ArrayList<SMAIndicatorDetails>();
		int stockcounter = 1;
		for (String stock : stocklist) {
			stockName = stock.split("!")[1];
			bseCode = stock.split("!")[0];
			nseCode = stock.split("!")[2];
			System.out.println("For Stock -> " + nseCode + " Stock count -> " + stockcounter++);
			if(getFinancialIndication(bseCode)) {	
				objSMAIndicatorDetails = new SMAIndicatorDetails();
				objSMAIndicatorDetails.stockCode = nseCode;
				
				CalculateIndicationfromSMA(connection, nseCode, calculationDate);
				if (objSMAIndicatorDetails.signalPriceToSMA != null || objSMAIndicatorDetails.signalSMAToSMA != null) {
					System.out.println("*****************************Stock Added for indication -> " + nseCode);
					SMAIndicatorDetailsList.add(objSMAIndicatorDetails);
					if(objSMAIndicatorDetails.stockPrice<100) {
						SMAIndicatorDetailsBelowHundredList.add(objSMAIndicatorDetails);
					}
				}
			}
			/*if (stockcounter > 200) {
				break;
			}*/
		}
		logger.debug("CalculateAndSendIndicationfromSMA calculation completed");
		// Collections.sort(SMAIndicatorDetailsList);
		logger.debug("CalculateAndSendIndicationfromSMA start mail");
		Collections.sort(SMAIndicatorDetailsList, new SMAIndicatorDetailsComparator());
		Collections.sort(SMAIndicatorDetailsBelowHundredList, new SMAIndicatorDetailsComparator());
		
		//tmpUpdateIndicatedStocks.updateSMAIndication(SMAIndicatorDetailsList);
		logger.debug("CalculateAndSendIndicationfromSMA start mail");
		if(SMAIndicatorDetailsList.size()>0) {
			sendTopStockInMail(SMAIndicatorDetailsList, false);
			sendTopStockInMail(SMAIndicatorDetailsBelowHundredList, true);
		} else {
			logger.error("CalculateAndSendIndicationfromSMA No stock to send in mail");
		}
		
		logger.debug("CalculateAndSendIndicationfromSMA end mail");
		System.out.println("End");
	}

	public SMAIndicatorDetails CalculateIndicationfromSMA(Connection connection, String stockCode, Date calculationDate) {
		objSMAIndicatorDetails = new SMAIndicatorDetails();
		objSMAIndicatorDetails.stockCode = nseCode;
		ArrayList<Integer> prefPeriod = null;
		ArrayList<Float> lowerSMAPeriodValues = null;
		ArrayList<Float> middleSMAPeriodValues = null;
		ArrayList<Float> higherSMAPeriodValues = null;
		ArrayList<Float> stockPriceValues = null;

		
		prefPeriod = StockUtils.GetPreferredSMA(connection, stockCode);
		if (prefPeriod != null && prefPeriod.size() > 0) {
			lowerSMAPeriodValues = StockUtils.GetSMAData(connection, stockCode, prefPeriod.get(0), calculationDate);
			middleSMAPeriodValues = StockUtils.GetSMAData(connection, stockCode, prefPeriod.get(1), calculationDate);
			higherSMAPeriodValues = StockUtils.GetSMAData(connection, stockCode, prefPeriod.get(2), calculationDate);
			stockPriceValues = GetStockPrices(connection, stockCode, calculationDate);

			//checking Higher SMA is in increasing trend in last 30 days
			/*if(higherSMAPeriodValues.size() > 0) {
				float recentHigherSMAValue = higherSMAPeriodValues.get(0);
				float lastHigherSMAValue = higherSMAPeriodValues.get(higherSMAPeriodValues.size()-1);
				if(recentHigherSMAValue < lastHigherSMAValue)
					return;
			}*/
			
			//checking Middle SMA is in increasing trend in last 30 days
			if(middleSMAPeriodValues.size() > 0) {
				float recentMiddleSMAPeridValues = middleSMAPeriodValues.get(0);
				float lastMiddleSMAPeriodValue = middleSMAPeriodValues.get(middleSMAPeriodValues.size()-1);
				if((middleSMAPeriodValues.get(0) < middleSMAPeriodValues.get(1)) || recentMiddleSMAPeridValues < lastMiddleSMAPeriodValue)
				{
					objSMAIndicatorDetails.IsMiddleSMGrowing = false;
					//return null;
				} else {
					objSMAIndicatorDetails.IsMiddleSMGrowing = true;
				}
			}
			
			//checking smaller SMA is in increasing trend in last 30 days
			if(lowerSMAPeriodValues.size() > 0) {
				float recentSmallerSMAPeridValues = lowerSMAPeriodValues.get(0);
				float lastSmallerSMAPeriodValue = lowerSMAPeriodValues.get(lowerSMAPeriodValues.size()-1);
				if((lowerSMAPeriodValues.get(0) < lowerSMAPeriodValues.get(1)) || recentSmallerSMAPeridValues < lastSmallerSMAPeriodValue)
				{
					objSMAIndicatorDetails.IsSmallerSMGrowing = false;
				} else {
					objSMAIndicatorDetails.IsSmallerSMGrowing = true;
				}
			}
			
			if (middleSMAPeriodValues.size() > 0 && stockPriceValues.size() > 0) {
				calculateIndicationFromMiddleSMAAndPriceV1(middleSMAPeriodValues, stockPriceValues);
			}
			if (lowerSMAPeriodValues.size() > 0 && higherSMAPeriodValues.size() > 0) {
				//changing to comapare 9 and 20 SMA values rather than 9 and 50
				//calculateIndicationFromLowerSMAAndHigherSMAV1(lowerSMAPeriodValues, higherSMAPeriodValues, stockPriceValues);
				calculateIndicationFromLowerSMAAndHigherSMAV1(lowerSMAPeriodValues, higherSMAPeriodValues, stockPriceValues);
			}
			if (lowerSMAPeriodValues.size() > 0 && middleSMAPeriodValues.size() > 0) {
				//changing to comapare 9 and 20 SMA values rather than 9 and 50
				//calculateIndicationFromLowerSMAAndHigherSMAV1(lowerSMAPeriodValues, higherSMAPeriodValues, stockPriceValues);
				calculateIndicationFromLowerSMAAndMiddleSMAV1(lowerSMAPeriodValues, middleSMAPeriodValues, stockPriceValues);
			}
		}
		return objSMAIndicatorDetails;
	}

	

	

	private ArrayList<Float> GetStockPrices(Connection connection, String stockCode, Date calculationDate) {
		ArrayList<Float> priceData = null;
		ResultSet resultSet = null;
		Statement statement = null;
		String price;
		String tmpSQL;
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		try {
			priceData = new ArrayList<Float>();			
			if(connection == null) {
				connection = StockUtils.connectToDB();
			}
			statement = connection.createStatement();

			if(calculationDate!=null) {
				tmpSQL = "SELECT closeprice, tradeddate FROM DAILYSTOCKDATA where stockname='" + stockCode + "' and tradeddate<= '" + dateFormat.format(calculationDate) + "' order by tradeddate desc limit 20;";
			} else {
				tmpSQL = "SELECT closeprice, tradeddate FROM DAILYSTOCKDATA where stockname='" + stockCode + "' order by tradeddate desc limit 20;";
			}
			resultSet = statement.executeQuery(tmpSQL);
			objSMAIndicatorDetails.signalDate = null;
			// DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
			while (resultSet.next()) {
				price = resultSet.getString(1);
				priceData.add(Float.parseFloat(price));
				if (objSMAIndicatorDetails.signalDate == null) {
					objSMAIndicatorDetails.signalDate = LocalDate.parse(resultSet.getString(2)); // new
																									// Date(dateFormat.format(resultSet.getString(2)));
				}
				// System.out.println("StockNme - " + stockNSECode);
			}
		} catch (Exception ex) {
			HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("Error in getting price = " + ex);			
			return null;
		} finally {
			try {
				if(resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("GetStockPrices Error in closing resultset "+ex);
				logger.error("Error in closing resultset GetStockPrices  -> ", ex);
			}
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("GetStockPrices Error in closing statement "+ex);
				logger.error("Error in closing statement GetStockPrices  -> ", ex);
			}
		}
		return priceData;
	}

	private void calculateIndicationFromMiddleSMAAndPriceV1(ArrayList<Float> middleSMAPeriodValues, ArrayList<Float> stockPriceValues) {

		float percentagePriceChange = 0;
		float priceToSMAPercentageDeviation = 0;
		float lowerLevelDifference = 0;
		boolean continuousGrowth = true;
		
		//Removing return condition to incorporate put calculation
		// Logic to get buy condition
		/*if (stockPriceValues.get(0) < stockPriceValues.get(1)) { 
			return;
		}*/
		objSMAIndicatorDetails.stockPrice = stockPriceValues.get(0);
		if (stockPriceValues.size() >= daysToCheck && middleSMAPeriodValues.size() >= daysToCheck) {
			//Removing return condition to incorporate put calculation
			//last day price is less than price daytocheck before
			/*if (stockPriceValues.get(0) < stockPriceValues.get(daysToCheck-1)) { 
				return;
			}
			//price trending below middle SMA then stock is not good to buy
			if (stockPriceValues.get(0) - middleSMAPeriodValues.get(0) < 0) { 
				return;
			}
			//Last day price lower than previous day means down trend
			if (stockPriceValues.get(0) < stockPriceValues.get(1)) { 
				return;
			}*/
			
			for (int counter = 1 ; counter < daysToCheck ; counter++ ) {
				if (stockPriceValues.get(counter-1) - middleSMAPeriodValues.get(counter-1) < stockPriceValues.get(counter) - middleSMAPeriodValues.get(counter)) {
					continuousGrowth = false;
				}
				if (stockPriceValues.get(counter) - middleSMAPeriodValues.get(counter) < 0) { 
					objSMAIndicatorDetails.PNSMAcrossover = true;
				}
			}
			if(continuousGrowth) {
				objSMAIndicatorDetails.PNSMcontinuousGrowth = true;
			}
			if (stockPriceValues.get(0) - middleSMAPeriodValues.get(0) > 0 && (stockPriceValues.get(0) - middleSMAPeriodValues.get(0) > stockPriceValues.get(daysToCheck-1) - middleSMAPeriodValues.get(daysToCheck-1))) {
				objSMAIndicatorDetails.signalPriceToSMA = "buy";
				percentagePriceChange = ((stockPriceValues.get(0) - stockPriceValues.get(daysToCheck-1)) / stockPriceValues.get(daysToCheck-1))*100;
				objSMAIndicatorDetails.percentagePriceChange = percentagePriceChange;
				if ((stockPriceValues.get(daysToCheck-1) - middleSMAPeriodValues.get(daysToCheck-1))<0) {
					lowerLevelDifference = 1;
				} else {
					lowerLevelDifference = stockPriceValues.get(daysToCheck-1) - middleSMAPeriodValues.get(daysToCheck-1);
				}
				priceToSMAPercentageDeviation = ((stockPriceValues.get(0) - middleSMAPeriodValues.get(0)) - (stockPriceValues.get(daysToCheck-1) - middleSMAPeriodValues.get(daysToCheck-1))) / lowerLevelDifference;				
				objSMAIndicatorDetails.priceToSMApercentageDeviation = priceToSMAPercentageDeviation;
			} else {
				objSMAIndicatorDetails.PNSMAcrossover = false;
			}
		}
	}
	
	private void calculateIndicationFromLowerSMAAndHigherSMAV1(ArrayList<Float> lowerSMAPeriodValues, ArrayList<Float> higherSMAPeriodValues, ArrayList<Float> stockPriceValues) {

		float lowerLevelDifference = 0;
		float SMAToSMAPercentageDeviation = 0;
		boolean continuousGrowth = true;
		
		// Logic to get buy condition
		if (lowerSMAPeriodValues.size() >= daysToCheck && higherSMAPeriodValues.size() >= daysToCheck && stockPriceValues.size() >= daysToCheck) {
			//price trending below middle SMA then stock is not good to buy
			if (lowerSMAPeriodValues.get(0) - higherSMAPeriodValues.get(0) < 0) { 
				return;
			}
			//last day price is less than price daytocheck before
			if (stockPriceValues.get(0) < stockPriceValues.get(daysToCheck-1)) { 
				return;
			}
			//Last day price lower than previous day means down trend
			if (stockPriceValues.get(0) < stockPriceValues.get(1)) { 
				return;
			}
			for (int counter = 1 ; counter < daysToCheck ; counter++ ) {
				if (lowerSMAPeriodValues.get(counter-1) - higherSMAPeriodValues.get(counter-1) < lowerSMAPeriodValues.get(counter) - higherSMAPeriodValues.get(counter)) {
					continuousGrowth = false;
				}
				if (lowerSMAPeriodValues.get(counter) - higherSMAPeriodValues.get(counter) < 0) { 
					objSMAIndicatorDetails.SMNSMcrossover = true;
					objSMAIndicatorDetails.lowerToHigherSMcrossover = true;
				}
			}
			if(continuousGrowth) {
				objSMAIndicatorDetails.SMNSMcontinuousGrowth = true;
			}
			if (lowerSMAPeriodValues.get(0) - higherSMAPeriodValues.get(0) > 0 && (lowerSMAPeriodValues.get(0) - higherSMAPeriodValues.get(0) > lowerSMAPeriodValues.get(daysToCheck-1) - higherSMAPeriodValues.get(daysToCheck-1))) {
				objSMAIndicatorDetails.signalSMAToSMA = "buy";
				if ((lowerSMAPeriodValues.get(daysToCheck-1) - higherSMAPeriodValues.get(daysToCheck-1))<0) {
					lowerLevelDifference = 1;
				} else {
					lowerLevelDifference = lowerSMAPeriodValues.get(daysToCheck-1) - higherSMAPeriodValues.get(daysToCheck-1);
				}
				SMAToSMAPercentageDeviation = ((lowerSMAPeriodValues.get(0) - higherSMAPeriodValues.get(0)) - (lowerSMAPeriodValues.get(daysToCheck-1) - higherSMAPeriodValues.get(daysToCheck-1))) / lowerLevelDifference;
				objSMAIndicatorDetails.SMAToSMApercentageDeviation = SMAToSMAPercentageDeviation;
			} else {
				objSMAIndicatorDetails.SMNSMcrossover = false;
				objSMAIndicatorDetails.lowerToHigherSMcrossover = false;
			}
		}	
/*//Put condition later
		// Logic to get put condition
		if (lowerSMAPeriodValues.size() > daysToCheck && higherSMAPeriodValues.size() > daysToCheck) {
			if ((lowerSMAPeriodValues.get(0) - lowerSMAPeriodValues.get(daysToCheck) < 0) && (higherSMAPeriodValues.get(0) - higherSMAPeriodValues.get(daysToCheck) < 0)) {
				if (lowerSMAPeriodValues.get(0) - higherSMAPeriodValues.get(0) < lowerSMAPeriodValues.get(daysToCheck) - higherSMAPeriodValues.get(daysToCheck)) {
					if (lowerSMAPeriodValues.get(0) - higherSMAPeriodValues.get(0) < 0) {
						objSMAIndicatorDetails.signalSMAToSMA = "put";
						//percentagePriceChange = (stockPriceValues.get(0) - stockPriceValues.get(daysToCheck)) / stockPriceValues.get(daysToCheck);
						if ((lowerSMAPeriodValues.get(daysToCheck) - higherSMAPeriodValues.get(daysToCheck))<0) {
							lowerLevelDifference = 1;
						} else {
							lowerLevelDifference = lowerSMAPeriodValues.get(daysToCheck) - higherSMAPeriodValues.get(daysToCheck);
						}
						SMAToSMAPercentageDeviation = ((lowerSMAPeriodValues.get(0) - higherSMAPeriodValues.get(0)) - (lowerSMAPeriodValues.get(daysToCheck) - higherSMAPeriodValues.get(daysToCheck))) / lowerLevelDifference;
						//objSMAIndicatorDetails.percentagePriceChange = percentagePriceChange;
						objSMAIndicatorDetails.SMAToSMApercentageDeviation = SMAToSMAPercentageDeviation;
					}
				}
			}
		}
*/				
	}
	
	private void calculateIndicationFromLowerSMAAndMiddleSMAV1(ArrayList<Float> lowerSMAPeriodValues, ArrayList<Float> middleSMAPeriodValues, ArrayList<Float> stockPriceValues) {

		float lowerLevelDifference = 0;
		float SMAToSMAPercentageDeviation = 0;
		boolean continuousGrowth = true;
		
		// Logic to get buy condition
		if (lowerSMAPeriodValues.size() >= daysToCheck && middleSMAPeriodValues.size() >= daysToCheck && stockPriceValues.size() >= daysToCheck) {
			//price trending below middle SMA then stock is not good to buy
			if (lowerSMAPeriodValues.get(0) - middleSMAPeriodValues.get(0) < 0) { 
				return;
			}
			//last day price is less than price daytocheck before
			if (stockPriceValues.get(0) < stockPriceValues.get(daysToCheck-1)) { 
				return;
			}
			//Last day price lower than previous day means down trend
			if (stockPriceValues.get(0) < stockPriceValues.get(1)) { 
				return;
			}
			for (int counter = 1 ; counter < daysToCheck ; counter++ ) {
				if (lowerSMAPeriodValues.get(counter-1) - middleSMAPeriodValues.get(counter-1) < lowerSMAPeriodValues.get(counter) - middleSMAPeriodValues.get(counter)) {
					continuousGrowth = false;
				}
				if (lowerSMAPeriodValues.get(counter) - middleSMAPeriodValues.get(counter) < 0) { 
					objSMAIndicatorDetails.SMNSMcrossover = true;
					objSMAIndicatorDetails.lowerToMiddleSMcrossover = true;
				}
			}
			if(continuousGrowth) {
				objSMAIndicatorDetails.SMNSMcontinuousGrowth = true;
			}
			if (lowerSMAPeriodValues.get(0) - middleSMAPeriodValues.get(0) > 0 && (lowerSMAPeriodValues.get(0) - middleSMAPeriodValues.get(0) > lowerSMAPeriodValues.get(daysToCheck-1) - middleSMAPeriodValues.get(daysToCheck-1))) {
				objSMAIndicatorDetails.signalSMAToSMA = "buy";
				if ((lowerSMAPeriodValues.get(daysToCheck-1) - middleSMAPeriodValues.get(daysToCheck-1))<0) {
					lowerLevelDifference = 1;
				} else {
					lowerLevelDifference = lowerSMAPeriodValues.get(daysToCheck-1) - middleSMAPeriodValues.get(daysToCheck-1);
				}
				SMAToSMAPercentageDeviation = ((lowerSMAPeriodValues.get(0) - middleSMAPeriodValues.get(0)) - (lowerSMAPeriodValues.get(daysToCheck-1) - middleSMAPeriodValues.get(daysToCheck-1))) / lowerLevelDifference;
				objSMAIndicatorDetails.SMAToSMApercentageDeviation = SMAToSMAPercentageDeviation;
			} else {
				objSMAIndicatorDetails.SMNSMcrossover = false;
				objSMAIndicatorDetails.lowerToMiddleSMcrossover = false;
			}
		}	
/*//Put condition later
		// Logic to get put condition
		if (lowerSMAPeriodValues.size() > daysToCheck && higherSMAPeriodValues.size() > daysToCheck) {
			if ((lowerSMAPeriodValues.get(0) - lowerSMAPeriodValues.get(daysToCheck) < 0) && (higherSMAPeriodValues.get(0) - higherSMAPeriodValues.get(daysToCheck) < 0)) {
				if (lowerSMAPeriodValues.get(0) - higherSMAPeriodValues.get(0) < lowerSMAPeriodValues.get(daysToCheck) - higherSMAPeriodValues.get(daysToCheck)) {
					if (lowerSMAPeriodValues.get(0) - higherSMAPeriodValues.get(0) < 0) {
						objSMAIndicatorDetails.signalSMAToSMA = "put";
						//percentagePriceChange = (stockPriceValues.get(0) - stockPriceValues.get(daysToCheck)) / stockPriceValues.get(daysToCheck);
						if ((lowerSMAPeriodValues.get(daysToCheck) - higherSMAPeriodValues.get(daysToCheck))<0) {
							lowerLevelDifference = 1;
						} else {
							lowerLevelDifference = lowerSMAPeriodValues.get(daysToCheck) - higherSMAPeriodValues.get(daysToCheck);
						}
						SMAToSMAPercentageDeviation = ((lowerSMAPeriodValues.get(0) - higherSMAPeriodValues.get(0)) - (lowerSMAPeriodValues.get(daysToCheck) - higherSMAPeriodValues.get(daysToCheck))) / lowerLevelDifference;
						//objSMAIndicatorDetails.percentagePriceChange = percentagePriceChange;
						objSMAIndicatorDetails.SMAToSMApercentageDeviation = SMAToSMAPercentageDeviation;
					}
				}
			}
		}
*/				
	}
	
	private void sendTopStockInMail(ArrayList<SMAIndicatorDetails> SMAIndicatorDetailsList, Boolean belowHunderd) {
		logger.debug("sendTopStockInMail Started");
		StringBuilder mailBody = new StringBuilder();
		mailBody.append("<html><body><table border='1'><tr><th>Sr. No.</th><th>Date</th><th>Stock code</th>");
		mailBody.append("<th>signalSMAToSMA</th><th>SMNSMcrossover</th><th>SMNSMcontinuousGrowth</th><th>SMAToSMApercentageDeviation</th><th>signalPriceToSMA</th><th>PNSMAcrossover</th>"
				+ "<th>PNSMcontinuousGrowth</th><th>priceToSMApercentageDeviation</th><th>percentagePriceChange</th></tr>");
		
		for (int counter = 0; counter <(SMAIndicatorDetailsList.size()>20?20:SMAIndicatorDetailsList.size()); counter++) {
			mailBody.append("<tr><td>" + (counter+1) + "</td>");
			mailBody.append("<td>" + SMAIndicatorDetailsList.get(counter).signalDate + "</td>");
			mailBody.append("<td>" + SMAIndicatorDetailsList.get(counter).stockCode + "</td>");
			mailBody.append("<td>" + SMAIndicatorDetailsList.get(counter).signalSMAToSMA + "</td>");
			mailBody.append("<td>" + SMAIndicatorDetailsList.get(counter).SMNSMcrossover + "</td>");
			mailBody.append("<td>" + SMAIndicatorDetailsList.get(counter).SMNSMcontinuousGrowth + "</td>");
			mailBody.append("<td>" + SMAIndicatorDetailsList.get(counter).SMAToSMApercentageDeviation + "</td>");
			mailBody.append("<td>" + SMAIndicatorDetailsList.get(counter).signalPriceToSMA + "</td>");
			mailBody.append("<td>" + SMAIndicatorDetailsList.get(counter).PNSMAcrossover + "</td>");
			mailBody.append("<td>" + SMAIndicatorDetailsList.get(counter).PNSMcontinuousGrowth + "</td>");
			mailBody.append("<td>" + SMAIndicatorDetailsList.get(counter).priceToSMApercentageDeviation + "</td>");
			mailBody.append("<td>" + SMAIndicatorDetailsList.get(counter).percentagePriceChange + "</td></tr>");
		}
		mailBody.append("</table></body></html>");
		SendSuggestedStockInMail mailSender;
        if(belowHunderd && SMAIndicatorDetailsList.size() > 0) {
        	mailSender = new SendSuggestedStockInMail("tarunstockcomm@gmail.com","SMA -> Below 100 Stocklist on "+SMAIndicatorDetailsList.get(0).signalDate.toString(),mailBody.toString());
        } else if( SMAIndicatorDetailsList.size() > 0 ){
        	mailSender = new SendSuggestedStockInMail("tarunstockcomm@gmail.com","SMA -> Stocklist on "+SMAIndicatorDetailsList.get(0).signalDate.toString(),mailBody.toString());
        }
        logger.debug("sendTopStockInMail end");
	}
	
	private boolean getFinancialIndication(String stockname) {
		//ArrayList<Float> priceData = null;
		ResultSet resultSet = null;
		Statement statement = null;
		String indication;

		try {
			if (connection != null) {
				connection.close();
				connection = null;
			}
			connection = StockUtils.connectToDB();
			statement = connection.createStatement();

			resultSet = statement.executeQuery("SELECT ANNUALSALESINDICATOR FROM STOCK_FINANCIAL_TRACKING where bsecode='" + stockname + "';");
			
			// DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
			while (resultSet.next()) {
				indication = resultSet.getString(1);
				if(indication.equalsIgnoreCase("good")){
					return true;
				} else {
					return false;
				}
			}
			
		} catch (Exception ex) {
			HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
			System.out.println("getFinancialIndication Error in getting indication = " + ex);
			return true;
		} finally {
			try {
				if(resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getFinancialIndication Error in closing resultset "+ex);
				logger.error("Error in closing resultset getFinancialIndication  -> ", ex);
			}
			try {
				if(statement != null) {
					statement.close();
					statement = null;
				}
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getFinancialIndication Error in closing statement "+ex);
				logger.error("Error in closing statement getFinancialIndication  -> ", ex);
			}
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				} 
			} catch (Exception ex) {
				HandleErrorDetails.addError(this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex.toString());
				System.out.println("getFinancialIndication Error in closing connection "+ex);
				logger.error("Error in closing connection getFinancialIndication  -> ", ex);
			}
		}
		//Returning true in case of no data to avoid loosing good stock
		return true;
	}
}
