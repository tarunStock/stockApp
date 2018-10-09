package com.amazonaws.tarun.stockApp.TechnicalIndicator.Calculation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.amazonaws.tarun.stockApp.TechnicalIndicator.Data.DailyStockData;
import com.amazonaws.tarun.stockApp.TechnicalIndicator.Data.SMAIndicatorDetails;
import com.amazonaws.tarun.stockApp.TechnicalIndicator.Data.StockComparatorOnSMAPrimeV1;
import com.amazonaws.tarun.stockApp.TechnicalIndicator.Data.StockDetailsForDecision;
import com.amazonaws.tarun.stockApp.Utils.SalesforceIntegration;

public class GenerateIntraDaySuggestions {

	public static void main(String[] args) throws IOException, JSONException {
		getDataandGenerateIndication();
		// System.out.println(combinedData);
		// System.out.println(json.get("id"));
	}

	private static void getDataandGenerateIndication() throws IOException, JSONException {
		String combinedData = "";
		ArrayList<DailyStockData> stockList;
		JSONObject json = readJsonFromUrl(
				"https://www.nseindia.com/live_market/dynaContent/live_watch/stock_watch/niftyStockWatch.json");
		combinedData = combinedData + json.toString();
		json = readJsonFromUrl(
				"https://www.nseindia.com/live_market/dynaContent/live_watch/stock_watch/cnxAutoStockWatch.json");
		combinedData = combinedData + json.toString();
		json = readJsonFromUrl(
				"https://www.nseindia.com/live_market/dynaContent/live_watch/stock_watch/bankNiftyStockWatch.json");
		combinedData = combinedData + json.toString();
		json = readJsonFromUrl(
				"https://www.nseindia.com/live_market/dynaContent/live_watch/stock_watch/cnxEnergyStockWatch.json");
		combinedData = combinedData + json.toString();
		json = readJsonFromUrl(
				"https://www.nseindia.com/live_market/dynaContent/live_watch/stock_watch/cnxFinanceStockWatch.json");
		combinedData = combinedData + json.toString();
		json = readJsonFromUrl(
				"https://www.nseindia.com/live_market/dynaContent/live_watch/stock_watch/cnxFMCGStockWatch.json");
		combinedData = combinedData + json.toString();
		json = readJsonFromUrl(
				"https://www.nseindia.com/live_market/dynaContent/live_watch/stock_watch/cnxitStockWatch.json");
		combinedData = combinedData + json.toString();
		json = readJsonFromUrl(
				"https://www.nseindia.com/live_market/dynaContent/live_watch/stock_watch/cnxMediaStockWatch.json");
		combinedData = combinedData + json.toString();
		json = readJsonFromUrl(
				"https://www.nseindia.com/live_market/dynaContent/live_watch/stock_watch/cnxMetalStockWatch.json");
		combinedData = combinedData + json.toString();
		json = readJsonFromUrl(
				"https://www.nseindia.com/live_market/dynaContent/live_watch/stock_watch/cnxPharmaStockWatch.json");
		combinedData = combinedData + json.toString();
		json = readJsonFromUrl(
				"https://www.nseindia.com/live_market/dynaContent/live_watch/stock_watch/cnxPSUStockWatch.json");
		combinedData = combinedData + json.toString();
		json = readJsonFromUrl(
				"https://www.nseindia.com/live_market/dynaContent/live_watch/stock_watch/cnxRealtyStockWatch.json");
		combinedData = combinedData + json.toString();
		json = readJsonFromUrl(
				"https://www.nseindia.com/live_market/dynaContent/live_watch/stock_watch/niftyPvtBankStockWatch.json");
		combinedData = combinedData + json.toString();
		json = readJsonFromUrl(
				"https://www.nseindia.com/live_market/dynaContent/live_watch/stock_watch/cnxCommoditiesStockWatch.json");
		combinedData = combinedData + json.toString();
		json = readJsonFromUrl(
				"https://www.nseindia.com/live_market/dynaContent/live_watch/stock_watch/cnxConsumptionStockWatch.json");
		combinedData = combinedData + json.toString();
		json = readJsonFromUrl(
				"https://www.nseindia.com/live_market/dynaContent/live_watch/stock_watch/cpseStockWatch.json");
		combinedData = combinedData + json.toString();
		json = readJsonFromUrl(
				"https://www.nseindia.com/live_market/dynaContent/live_watch/stock_watch/cnxInfraStockWatch.json");
		combinedData = combinedData + json.toString();
		json = readJsonFromUrl(
				"https://www.nseindia.com/live_market/dynaContent/live_watch/stock_watch/cnxMNCStockWatch.json");
		combinedData = combinedData + json.toString();
		json = readJsonFromUrl(
				"https://www.nseindia.com/live_market/dynaContent/live_watch/stock_watch/ni15StockWatch.json");
		combinedData = combinedData + json.toString();
		json = readJsonFromUrl(
				"https://www.nseindia.com/live_market/dynaContent/live_watch/stock_watch/cnxPSEStockWatch.json");
		combinedData = combinedData + json.toString();
		json = readJsonFromUrl(
				"https://www.nseindia.com/live_market/dynaContent/live_watch/stock_watch/cnxServiceStockWatch.json");
		combinedData = combinedData + json.toString();
		json = readJsonFromUrl(
				"https://www.nseindia.com/live_market/dynaContent/live_watch/stock_watch/nseliquidStockWatch.json");
		combinedData = combinedData + json.toString();
		json = readJsonFromUrl(
				"https://www.nseindia.com/live_market/dynaContent/live_watch/stock_watch/niftyMidcapLiq15StockWatch.json");
		combinedData = combinedData + json.toString();

		stockList = parseStockNames(combinedData);
		getSuitableStocksNotification(stockList);

		//System.out.println(parseStockNames(combinedData));

	}

	private static void getSuitableStocksNotification(ArrayList<DailyStockData> stockList) {
		//ArrayList<DailyStockData> suitableBullishStockList = new ArrayList<DailyStockData>();
		GenerateIndicationfromMovingAverage objGenerateIndicationfromMovingAverage = new GenerateIndicationfromMovingAverage();
		SMAIndicatorDetails objSMAIndicatorDetails;
		ArrayList<StockDetailsForDecision> objFinalSelectedStockList = new ArrayList<StockDetailsForDecision>();
		StockDetailsForDecision objFinalSelectedStock = null;
		GenerateCombinedIndicationV1 objGenerateCombinedIndicationV1 = new GenerateCombinedIndicationV1();
		
		for(DailyStockData stock : stockList) {
			if(stock.closePrice >= stock.openPrice && stock.lowPrice >= stock.openPrice) {
				//suitableBullishStockList.add(stock);
				//objSMAIndicatorDetails = objGenerateIndicationfromMovingAverage.CalculateIndicationfromSMA(null, stock.stockName, new Date("25-Sep-2018"));
				objSMAIndicatorDetails = objGenerateIndicationfromMovingAverage.CalculateIndicationfromSMA(null, stock.stockName, null);
				if (objSMAIndicatorDetails.signalPriceToSMA != null || objSMAIndicatorDetails.signalSMAToSMA != null) {
					objFinalSelectedStock = objGenerateCombinedIndicationV1.getAlldetails(null, objSMAIndicatorDetails, null);
					if(objFinalSelectedStock!=null) {
						objFinalSelectedStock.TypeofSuggestedStock = "Low RSI";
						objFinalSelectedStockList.add(objFinalSelectedStock);
					}
				}
			}
		}
		
		Collections.sort(objFinalSelectedStockList, new StockComparatorOnSMAPrimeV1());
		SalesforceIntegration objSalesforceIntegration = new SalesforceIntegration();
		
		objSalesforceIntegration.connectToSalesforc();
		objSalesforceIntegration.createSuggestedStocks(objFinalSelectedStockList);
		//return suitableBullishStockList;
	}
	
	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

	public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
		InputStream is = new URL(url).openStream();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			JSONObject json = new JSONObject(jsonText);
			return json;
		} finally {
			is.close();
		}
	}

	public static ArrayList<DailyStockData> parseStockNames(String stockList) {
		ArrayList<DailyStockData> stockArray = new ArrayList<DailyStockData>();
		DailyStockData objDailyStockData;
		int symbolIndex, count = 0;
		float openPrice, lastTradedPice, lowPrice, highPrice;
		String StockSymbol, stringValue;
		stockList = stockList.substring(stockList.indexOf("[{")+2);
		while ((symbolIndex = stockList.indexOf("symbol")) >= 0) {
			count++;
			//stockList = stockList.substring(symbolIndex + 9);
			StockSymbol = stockList.substring(symbolIndex + 9, symbolIndex + 9 + stockList.substring(symbolIndex + 9).indexOf("\""));
			symbolIndex = stockList.indexOf("open");
			//stockList = stockList.substring(symbolIndex + 7);
			stringValue = stockList.substring(symbolIndex + 7, symbolIndex + 7 + stockList.substring(symbolIndex + 7).indexOf("\""));
			stringValue = stringValue.replaceAll(",", "");
			openPrice = Float.parseFloat(stringValue.trim());
			
			symbolIndex = stockList.indexOf("high");
			//stockList = stockList.substring(symbolIndex + 7);
			stringValue = stockList.substring(symbolIndex + 7, symbolIndex + 7 + stockList.substring(symbolIndex + 7).indexOf("\""));
			stringValue = stringValue.replaceAll(",", "");
			highPrice = Float.parseFloat(stringValue.trim());
			symbolIndex = stockList.indexOf("low");
			//stockList = stockList.substring(symbolIndex + 6);
			stringValue = stockList.substring(symbolIndex + 6, symbolIndex + 6 + stockList.substring(symbolIndex + 6).indexOf("\""));
			stringValue = stringValue.replaceAll(",", "");
			lowPrice = Float.parseFloat(stringValue.trim());
			symbolIndex = stockList.indexOf("ltP");
			//stockList = stockList.substring(symbolIndex + 6);
			stringValue = stockList.substring(symbolIndex + 6, symbolIndex + 6 + stockList.substring(symbolIndex + 6).indexOf("\""));
			stringValue = stringValue.replaceAll(",", "");
			lastTradedPice = Float.parseFloat(stringValue.trim());
			
			stockList = stockList.substring(stockList.indexOf("{") + 1);
			
			if (!stockArray.contains(StockSymbol)) {
				objDailyStockData = new DailyStockData();
				objDailyStockData.stockName = StockSymbol;
				objDailyStockData.highPrice = highPrice;
				objDailyStockData.lowPrice = lowPrice;
				objDailyStockData.closePrice = lastTradedPice;
				objDailyStockData.openPrice = openPrice;
				stockArray.add(objDailyStockData);
			}
		}
		System.out.println("Count = " + count);
		return stockArray;
	}

}
