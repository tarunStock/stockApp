package com.amazonaws.tarun.stockApp.TechnicalIndicator.Calculation;

import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.amazonaws.tarun.stockApp.Common.StoreStockList;
import com.amazonaws.tarun.stockApp.FinancialIndicator.Calculation.CollectFinancialDataForCompanies;
import com.amazonaws.tarun.stockApp.Utils.HandleErrorDetails;

public class QuoteApplication {
	final String URL = "https://www.nseindia.com/products/content/equities/equities/archieve_eq.htm";
	final String timeOut = "2000";
	static Logger logger = Logger.getLogger(QuoteApplication.class);

	public static void main(String[] args) {
		// String log4jConfigFile = System.getProperty("user.dir")
		// + File.separator + "log4j.properties";
		String log4jConfigFile = System.getProperty("log4j.configuration");
		System.out.println("*************Prop -> " + log4jConfigFile);
		PropertyConfigurator.configure(log4jConfigFile);

		logger.debug("QuoteApplication Started");
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("CollectStockDetails")) {
				// Will be called once a month on Saturday
				logger.debug("Stock details Collection Started");
				StoreStockList obj = new StoreStockList();
				obj.StoreAndCollectStockDetails();
				HandleErrorDetails.sendErrorsInMail("Stock details");
				logger.debug("Stock details Collection End");
			} else if (args[0].equalsIgnoreCase("quote")) {
				logger.debug("Daily Quote Collection Started");
				CollectDailyStockData obj = new CollectDailyStockData();
				obj.startCollectingDailyData();
				HandleErrorDetails.sendErrorsInMail("Collect Quote");
				logger.debug("Daily Quote Collection End");
			} else if (args[0].equalsIgnoreCase("quoteNotification")) {
				logger.debug("Quote Notification Started");
				CollectDailyStockData obj = new CollectDailyStockData();
				obj.sendNotificationForDailyStockDataCollection();
				;
				HandleErrorDetails.sendErrorsInMail("Quote Notification");
				logger.debug("Quote Notification End");
			} else if (args[0].equalsIgnoreCase("movingaveragecalculation")) {
				logger.debug("MA Calculation Started");
				CalculateSimpleAndExpoMovingAvg obj = new CalculateSimpleAndExpoMovingAvg();
				obj.MovingAverageCalculation(null);
				HandleErrorDetails.sendErrorsInMail("Calculate Moving Average");
				logger.debug("MA Calculation End");
			} else if (args[0].equalsIgnoreCase("movingaverageindicator")) {
				logger.debug("MA Indication Started");
				GenerateIndicationfromMovingAverage obj = new GenerateIndicationfromMovingAverage();
				obj.CalculateAndSendIndicationfromSMA(null, null);
				HandleErrorDetails.sendErrorsInMail("Calculate Indication from MA");
				logger.debug("MA Indication End");
			} else if (args[0].equalsIgnoreCase("volumeindicator")) {
				logger.debug("Volume Indication Started");
				OnBalanceVolumeUpdated obj = new OnBalanceVolumeUpdated();
				obj.OnBalanceVolumeCalculation(null);
				HandleErrorDetails.sendErrorsInMail("Calculate On Balance Indicator");
				logger.debug("Volume Indication End");
			} else if (args[0].equalsIgnoreCase("calculateBB")) {
				logger.debug("calculateBB Started");
				CalculateBollingerBands obj = new CalculateBollingerBands();
				obj.calculateBollingerBands(null);
				HandleErrorDetails.sendErrorsInMail("Calculate Bollinger Bands");
				logger.debug("calculateBB End");
			} /*else if (args[0].equalsIgnoreCase("combined")) {
				logger.debug("Combined Indication Started");
				GenerateCombinedIndication obj = new GenerateCombinedIndication();
				obj.generateCombinedIndicationForStocks(null);
				HandleErrorDetails.sendErrorsInMail("Generate Combined Indication");
				logger.debug("Combined Indication End");
			} else if (args[0].equalsIgnoreCase("combinedForPortal")) {
				logger.debug("Combined Indication Started");
				GenerateCombinedIndicationV1 obj = new GenerateCombinedIndicationV1();
				obj.generateCombinedIndicationForStocks(null);
				HandleErrorDetails.sendErrorsInMail("Generate Combined Indication");
				logger.debug("Combined Indication End");
			}*/ else if (args[0].equalsIgnoreCase("calculateRSI")) {
				logger.debug("CalculateRSIIndicator Started");
				CalculateRSIIndicator obj = new CalculateRSIIndicator();
				obj.CalculateRSIForAllStocks(null);
				HandleErrorDetails.sendErrorsInMail("Calculate RSI Indicator");
				logger.debug("CalculateRSIIndicator End");
			} else if (args[0].equalsIgnoreCase("calculateStochastic")) {
				logger.debug("CalculateStochasticIndicator Started");
				CalculateStochasticOscillator obj = new CalculateStochasticOscillator();
				obj.CalculateStochasticOscillatorForAllStocks(null);
				HandleErrorDetails.sendErrorsInMail("Calculate Stochastic Indicator");
				logger.debug("CalculateStochasticIndicator End");
			} else if (args[0].equalsIgnoreCase("calculateATR")) {
				logger.debug("CalculateATR Started");
				CalculateAverageTrueRange obj = new CalculateAverageTrueRange();
				obj.calculateAverageTrueRangeForAllStocks(null);
				HandleErrorDetails.sendErrorsInMail("Calculate Average True Range");
				logger.debug("CalculateATR End");
			} else if (args[0].equalsIgnoreCase("calculateMACD")) {
				logger.debug("CalculateMACD Started");
				GenerateIndicationFromMACD obj = new GenerateIndicationFromMACD();
				obj.calculateSignalAndMACDBulkForAllStocks(null);
				HandleErrorDetails.sendErrorsInMail("Calculate MACD");
				logger.debug("CalculateMACD End");
			} /*else if (args[0].equalsIgnoreCase("combinedFromMACD")) {
				logger.debug("MACD Indication Started");
				GenerateIndicationFromMACDV1 obj = new GenerateIndicationFromMACDV1();
				obj.CalculateIndicationfromMACD(null);
				HandleErrorDetails.sendErrorsInMail("Generate MACD Indication");
				logger.debug("MACD Indication End");
			} */else if (args[0].equalsIgnoreCase("combinedFromMACDForPortal")) {
				logger.debug("New Logic Indication Started");
				IdentifyBullishStocks obj = new IdentifyBullishStocks();
				obj.CalculateIndication(null);
				HandleErrorDetails.sendErrorsInMail("New Logic Indication");
				logger.debug("MACD Indication End");
			} else if (args[0].equalsIgnoreCase("calculateAccumulationDistribution")) {
				logger.debug("calculateAccumulationDistribution Started");
				AccumulationDistribution obj = new AccumulationDistribution();
				obj.calculateAccumulationDistributionForAllStocks(null);
				HandleErrorDetails.sendErrorsInMail("calculate Accumulation Distribution");
				logger.debug("calculateAccumulationDistribution End");
			} else if (args[0].equalsIgnoreCase("financialData")) {
				logger.debug("Financialdata collection Started");
				CollectFinancialDataForCompanies obj = new CollectFinancialDataForCompanies();
				obj.collectAnnualFinancialDataMC();
				HandleErrorDetails.sendErrorsInMail("Financialdata collection Indication");
				logger.debug("Financialdata collection End");
			}
		} else {
			System.out.println("No Args specified");
		}
		logger.debug("QuoteApplication end");
	}

	public void invokeAction(String args, String targetDate) {
		// String log4jConfigFile = System.getProperty("user.dir")
		// + File.separator + "log4j.properties";
		/*String log4jConfigFile = System.getProperty("log4j.configuration");
		System.out.println("*************Prop -> " + log4jConfigFile);
		PropertyConfigurator.configure(log4jConfigFile);

		logger.debug("QuoteApplication Started");*/
		Date calculationDate;
		if(targetDate!=null) {
			calculationDate = new Date(targetDate);
		} else {
			calculationDate = null;
		}
		if (args!=null) {
			if (args.equalsIgnoreCase("CollectStockDetails")) {
				// Will be called once a month on Saturday
				logger.debug("Stock details Collection Started");
				StoreStockList obj = new StoreStockList();
				obj.StoreAndCollectStockDetails();
				HandleErrorDetails.sendErrorsInMail("Stock details");
				logger.debug("Stock details Collection End");
			} else if (args.equalsIgnoreCase("quote")) {
				logger.debug("Daily Quote Collection Started");
				CollectDailyStockData obj = new CollectDailyStockData();
				obj.startCollectingDailyData(calculationDate);
				HandleErrorDetails.sendErrorsInMail("Collect Quote");
				logger.debug("Daily Quote Collection End");
			} else if (args.equalsIgnoreCase("quoteNotification")) {
				logger.debug("Quote Notification Started");
				CollectDailyStockData obj = new CollectDailyStockData();
				obj.sendNotificationForDailyStockDataCollection();
				HandleErrorDetails.sendErrorsInMail("Quote Notification");
				logger.debug("Quote Notification End");
			} else if (args.equalsIgnoreCase("movingaveragecalculation")) {
				logger.debug("MA Calculation Started");
				CalculateSimpleAndExpoMovingAvg obj = new CalculateSimpleAndExpoMovingAvg();
				obj.MovingAverageCalculation(calculationDate);
				HandleErrorDetails.sendErrorsInMail("Calculate Moving Average");
				logger.debug("MA Calculation End");
			}/* else if (args.equalsIgnoreCase("movingaverageindicator")) {
				logger.debug("MA Indication Started");
				GenerateIndicationfromMovingAverage obj = new GenerateIndicationfromMovingAverage();
				obj.CalculateAndSendIndicationfromSMA(null, calculationDate);
				HandleErrorDetails.sendErrorsInMail("Calculate Indication from MA");
				logger.debug("MA Indication End");
			}*/ else if (args.equalsIgnoreCase("volumeindicator")) {
				logger.debug("Volume Indication Started");
				OnBalanceVolumeUpdated obj = new OnBalanceVolumeUpdated();
				obj.OnBalanceVolumeCalculation(null);
				HandleErrorDetails.sendErrorsInMail("Calculate On Balance Indicator");
				logger.debug("Volume Indication End");
			} else if (args.equalsIgnoreCase("calculateBB")) {
				logger.debug("calculateBB Started");
				CalculateBollingerBands obj = new CalculateBollingerBands();
				obj.calculateBollingerBands(calculationDate);
				HandleErrorDetails.sendErrorsInMail("Calculate Bollinger Bands");
				logger.debug("calculateBB End");
			} /*else if (args.equalsIgnoreCase("combined")) {
				logger.debug("Combined Indication Started");
				GenerateCombinedIndication obj = new GenerateCombinedIndication();
				obj.generateCombinedIndicationForStocks(calculationDate);
				HandleErrorDetails.sendErrorsInMail("Generate Combined Indication");
				logger.debug("Combined Indication End");
			} else if (args.equalsIgnoreCase("combinedForPortal")) {
				logger.debug("Combined Indication Started");
				GenerateCombinedIndicationV1 obj = new GenerateCombinedIndicationV1();
				obj.generateCombinedIndicationForStocks(calculationDate);
				HandleErrorDetails.sendErrorsInMail("Generate Combined Indication");
				logger.debug("Combined Indication End");
			}*/ else if (args.equalsIgnoreCase("calculateRSI")) {
				logger.debug("CalculateRSIIndicator Started");
				CalculateRSIIndicator obj = new CalculateRSIIndicator();
				obj.CalculateRSIForAllStocks(calculationDate);
				HandleErrorDetails.sendErrorsInMail("Calculate RSI Indicator");
				logger.debug("CalculateRSIIndicator End");
			} else if (args.equalsIgnoreCase("calculateStochastic")) {
				logger.debug("CalculateStochasticIndicator Started");
				CalculateStochasticOscillator obj = new CalculateStochasticOscillator();
				obj.CalculateStochasticOscillatorForAllStocks(calculationDate);
				HandleErrorDetails.sendErrorsInMail("Calculate Stochastic Indicator");
				logger.debug("CalculateStochasticIndicator End");
			} else if (args.equalsIgnoreCase("calculateATR")) {
				logger.debug("CalculateATR Started");
				CalculateAverageTrueRange obj = new CalculateAverageTrueRange();
				obj.calculateAverageTrueRangeForAllStocks(calculationDate);
				HandleErrorDetails.sendErrorsInMail("Calculate Average True Range");
				logger.debug("CalculateATR End");
			} else if (args.equalsIgnoreCase("calculateMACD")) {
				logger.debug("CalculateMACD Started");
				GenerateIndicationFromMACD obj = new GenerateIndicationFromMACD();
				obj.calculateSignalAndMACDBulkForAllStocks(calculationDate);
				HandleErrorDetails.sendErrorsInMail("Calculate MACD");
				logger.debug("CalculateMACD End");
			} /*else if (args.equalsIgnoreCase("combinedFromMACD")) {
				logger.debug("MACD Indication Started");
				GenerateIndicationFromMACD obj = new GenerateIndicationFromMACD();
				obj.CalculateIndicationfromMACD(calculationDate);
				HandleErrorDetails.sendErrorsInMail("Generate MACD Indication");
				logger.debug("MACD Indication End");
			} else if (args.equalsIgnoreCase("combinedFromMACDForPortal")) {
				logger.debug("MACD Indication Started");
				GenerateIndicationFromMACDV1 obj = new GenerateIndicationFromMACDV1();
				obj.CalculateIndicationfromMACD(calculationDate);
				HandleErrorDetails.sendErrorsInMail("Generate MACD Indication");
				logger.debug("MACD Indication End");
			}*/ else if (args.equalsIgnoreCase("combinedFromMACDForPortal")) {
				logger.debug("New Logic Indication Started");
				IdentifyBullishStocks obj = new IdentifyBullishStocks();
				obj.CalculateIndication(calculationDate);
				HandleErrorDetails.sendErrorsInMail("New Logic Indication");
				logger.debug("MACD Indication End");
			} else if (args.equalsIgnoreCase("calculateAccumulationDistribution")) {
				logger.debug("calculateAccumulationDistribution Started");
				AccumulationDistribution obj = new AccumulationDistribution();
				obj.calculateAccumulationDistributionForAllStocks(calculationDate);
				HandleErrorDetails.sendErrorsInMail("calculate Accumulation Distribution");
				logger.debug("calculateAccumulationDistribution End");
			} else if (args.equalsIgnoreCase("financialData")) {
				logger.debug("Financialdata collection Started");
				CollectFinancialDataForCompanies obj = new CollectFinancialDataForCompanies();
				obj.collectAnnualFinancialDataMC();
				HandleErrorDetails.sendErrorsInMail("Financialdata collection Indication");
				logger.debug("Financialdata collection End");
			}
		} else {
			System.out.println("No Args specified");
		}
		//logger.debug("QuoteApplication end");
	}
}
