package com.amazonaws.tarun.stockApp.TechnicalIndicator.Calculation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

public class GenerateIntraDaySuggestions {
	
	public static void main(String[] args) throws IOException, JSONException {
		  
		  //System.out.println(combinedData);
	    //System.out.println(json.get("id"));
	  }
	
	private static void getDataandGenerateIndication() throws IOException, JSONException {
		String combinedData = "";
		  JSONObject json = readJsonFromUrl("https://www.nseindia.com/live_market/dynaContent/live_watch/stock_watch/niftyStockWatch.json");
		  combinedData = combinedData + json.toString();
		  json = readJsonFromUrl("https://www.nseindia.com/live_market/dynaContent/live_watch/stock_watch/cnxAutoStockWatch.json");
		  combinedData = combinedData + json.toString();
		  json = readJsonFromUrl("https://www.nseindia.com/live_market/dynaContent/live_watch/stock_watch/bankNiftyStockWatch.json");
		  combinedData = combinedData + json.toString();
		  json = readJsonFromUrl("https://www.nseindia.com/live_market/dynaContent/live_watch/stock_watch/cnxEnergyStockWatch.json");
		  combinedData = combinedData + json.toString();
		  json = readJsonFromUrl("https://www.nseindia.com/live_market/dynaContent/live_watch/stock_watch/cnxFinanceStockWatch.json");
		  combinedData = combinedData + json.toString();
		  json = readJsonFromUrl("https://www.nseindia.com/live_market/dynaContent/live_watch/stock_watch/cnxFMCGStockWatch.json");
		  combinedData = combinedData + json.toString();
		  json = readJsonFromUrl("https://www.nseindia.com/live_market/dynaContent/live_watch/stock_watch/cnxitStockWatch.json");
		  combinedData = combinedData + json.toString();
		  json = readJsonFromUrl("https://www.nseindia.com/live_market/dynaContent/live_watch/stock_watch/cnxMediaStockWatch.json");
		  combinedData = combinedData + json.toString();
		  json = readJsonFromUrl("https://www.nseindia.com/live_market/dynaContent/live_watch/stock_watch/cnxMetalStockWatch.json");
		  combinedData = combinedData + json.toString();
		  json = readJsonFromUrl("https://www.nseindia.com/live_market/dynaContent/live_watch/stock_watch/cnxPharmaStockWatch.json");
		  combinedData = combinedData + json.toString();
		  json = readJsonFromUrl("https://www.nseindia.com/live_market/dynaContent/live_watch/stock_watch/cnxPSUStockWatch.json");
		  combinedData = combinedData + json.toString();
		  json = readJsonFromUrl("https://www.nseindia.com/live_market/dynaContent/live_watch/stock_watch/cnxRealtyStockWatch.json");
		  combinedData = combinedData + json.toString();
		  json = readJsonFromUrl("https://www.nseindia.com/live_market/dynaContent/live_watch/stock_watch/niftyPvtBankStockWatch.json");
		  combinedData = combinedData + json.toString();
		  json = readJsonFromUrl("https://www.nseindia.com/live_market/dynaContent/live_watch/stock_watch/cnxCommoditiesStockWatch.json");
		  combinedData = combinedData + json.toString();
		  json = readJsonFromUrl("https://www.nseindia.com/live_market/dynaContent/live_watch/stock_watch/cnxConsumptionStockWatch.json");
		  combinedData = combinedData + json.toString();
		  json = readJsonFromUrl("https://www.nseindia.com/live_market/dynaContent/live_watch/stock_watch/cpseStockWatch.json");
		  combinedData = combinedData + json.toString();
		  json = readJsonFromUrl("https://www.nseindia.com/live_market/dynaContent/live_watch/stock_watch/cnxInfraStockWatch.json");
		  combinedData = combinedData + json.toString();
		  json = readJsonFromUrl("https://www.nseindia.com/live_market/dynaContent/live_watch/stock_watch/cnxMNCStockWatch.json");
		  combinedData = combinedData + json.toString();
		  json = readJsonFromUrl("https://www.nseindia.com/live_market/dynaContent/live_watch/stock_watch/ni15StockWatch.json");
		  combinedData = combinedData + json.toString();
		  json = readJsonFromUrl("https://www.nseindia.com/live_market/dynaContent/live_watch/stock_watch/cnxPSEStockWatch.json");
		  combinedData = combinedData + json.toString();
		  json = readJsonFromUrl("https://www.nseindia.com/live_market/dynaContent/live_watch/stock_watch/cnxServiceStockWatch.json");
		  combinedData = combinedData + json.toString();
		  json = readJsonFromUrl("https://www.nseindia.com/live_market/dynaContent/live_watch/stock_watch/nseliquidStockWatch.json");
		  combinedData = combinedData + json.toString();
		  json = readJsonFromUrl("https://www.nseindia.com/live_market/dynaContent/live_watch/stock_watch/niftyMidcapLiq15StockWatch.json");
		  combinedData = combinedData + json.toString();
		  
		  System.out.println(parseStockNames(combinedData));
		  
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
	  
	  public static ArrayList<String> parseStockNames(String stockList) {
		  ArrayList<String> stockArray = new ArrayList<String>();
		  int symbolIndex, count = 0;
		  String StockSymbol;
		  while ((symbolIndex = stockList.indexOf("symbol"))>=0) {
			  count++;
			  stockList = stockList.substring(symbolIndex+9);
			  StockSymbol = stockList.substring(0, stockList.indexOf("\""));
			  if(!stockArray.contains(StockSymbol)) {
				  stockArray.add(StockSymbol);  
			  }		  
		  }	  
		  System.out.println("Count = "+count);
		  return stockArray;
	  }

	  
}
