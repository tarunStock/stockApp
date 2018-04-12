package com.amazonaws.tarun.stockApp.Utils;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.amazonaws.tarun.stockApp.TechnicalIndicator.Data.StockDataForNewApproach;
import com.amazonaws.tarun.stockApp.TechnicalIndicator.Data.StockDetailsForDecision;

public class SalesforceIntegration {
	private static final String LOGINURL = "https://login.salesforce.com/services/oauth2/token";
	//private static final String GRANTTYPE = "/services/oauth2/token?grant_type=password";
	private static final String CLIENTID = "3MVG9d8..z.hDcPImYkL6mLod9wozPALfaFg8NUMnNXv_JPddncDl3h_78jZR1s6ZfXCDIDLpVowi.3JA3meQ";
	private static final String CLIENTSECRET = "8506532603328186976";
	private static final String USERID = "tarunstockcomm@gmail.com";
	//private static final String PASSWORD = "Nov@2017nCkwjqG1silvXylq5DHcnonWI";
	private static final String PASSWORD = "Jan@2018hjpt3nZkkcLQUa9Ntrc6Rt17z";
	//private static final String PASSWORD = "Jan@2018";
	private static final String ACCESSTOKEN = "access_token";
	private static final String INSTANCEURL = "instance_url";

	private String instanceUrl;
	private Header oAuthHeader;
	private static Header printHeader = new BasicHeader("X-PrettyPrint", "1");
	
	private HttpPost httpPost;
	
	public static void main(String[] args) {
		SalesforceIntegration objSalesforceIntegration = new SalesforceIntegration();
		objSalesforceIntegration.connectToSalesforc();
		

		objSalesforceIntegration.httpPost.releaseConnection();
	}

	public void connectToSalesforc() {
		HttpClient httpclient = HttpClientBuilder.create().build();

		httpPost = new HttpPost(LOGINURL);
		httpPost.setHeader("Content-Type","application/x-www-form-urlencoded");
		List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("client_id", CLIENTID));
        //params.add(new BasicNameValuePair("client_id", "3MVG9d8..z.hDcPImYkL6mLod9wozPALfaFg8NUMnNXv_JPddncDl3h_78jZR1s6ZfXCDIDLpVowi.3JA3meQ"));
        params.add(new BasicNameValuePair("client_secret", CLIENTSECRET));
        //params.add(new BasicNameValuePair("client_secret", "8506532603328186976"));
        params.add(new BasicNameValuePair("grant_type", "password"));
        params.add(new BasicNameValuePair("username", USERID));
        //params.add(new BasicNameValuePair("username", "tarunstockcomm@gmail.com"));
        params.add(new BasicNameValuePair("password", PASSWORD));
        //params.add(new BasicNameValuePair("password", "Jan@2018hjpt3nZkkcLQUa9Ntrc6Rt17z"));
        
		HttpResponse httpResponse = null;

		try {
			httpPost.setEntity(new UrlEncodedFormEntity(params));
			httpResponse = httpclient.execute(httpPost);
		} catch (ClientProtocolException clientProtocolException) {
			clientProtocolException.printStackTrace();
		} catch (IOException ioException) {
			ioException.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		final int statusCode = httpResponse.getStatusLine().getStatusCode();
		if (statusCode != HttpStatus.SC_OK) {
			System.out.println("Error authenticating to Salesforce.com platform: " + statusCode);
			return;
		}

		String httpMessage = null;
		try {
			httpMessage = EntityUtils.toString(httpResponse.getEntity());
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}

		JSONObject jsonObject = null;
		String accessToken = null;
		try {
			jsonObject = (JSONObject) new JSONTokener(httpMessage).nextValue();
			accessToken = jsonObject.getString(ACCESSTOKEN);
			instanceUrl = jsonObject.getString(INSTANCEURL);
		} catch (JSONException jsonException) {
			jsonException.printStackTrace();
		}

		oAuthHeader = new BasicHeader("Authorization", "OAuth " + accessToken);
		System.out.println("Auth header -> "+oAuthHeader);
		System.out.println("Auth header -> "+instanceUrl);
	}

	public static JSONObject getJsonObject( StockDetailsForDecision objStockDetailsForDecision) {
		JSONObject stockDetails;
		stockDetails = new JSONObject();
		double tempVar;
		try {
			stockDetails.put("BBTrend", objStockDetailsForDecision.BBTrend);
			tempVar = Math.round(objStockDetailsForDecision.ChandelierExit * 100.0) / 100.0;
					
			stockDetails.put("ChandelierExit", tempVar);
			tempVar = Math.round(objStockDetailsForDecision.CurrentPrice * 100.0) / 100.0;
			stockDetails.put("CurrentPrice", tempVar);
			stockDetails.put("CurrentVolume", objStockDetailsForDecision.CurrentVolume);			
			stockDetails.put("MACDStatus", objStockDetailsForDecision.MACDStatus);
			tempVar = Math.round(objStockDetailsForDecision.OneDayPreviousPrice * 100.0) / 100.0;
			stockDetails.put("OneDayPreviousPrice", tempVar);
			stockDetails.put("OneDayPreviousVolume", objStockDetailsForDecision.OneDayPreviousVolume);
			tempVar = Math.round(objStockDetailsForDecision.RSIValue * 100.0) / 100.0;
			stockDetails.put("RSIValue", tempVar);
			stockDetails.put("SMAComparison", objStockDetailsForDecision.SMAComparison);
			stockDetails.put("SMAToPriceComparison", objStockDetailsForDecision.SMAToPriceComparison);
			stockDetails.put("StockCode", objStockDetailsForDecision.stockCode);
			stockDetails.put("SuggestedDate", objStockDetailsForDecision.suggestedDate);
			tempVar = Math.round(objStockDetailsForDecision.ThreeDayPreviousPrice * 100.0) / 100.0;
			stockDetails.put("ThreeDayPreviousPrice", tempVar);
			stockDetails.put("ThreeDayPreviousVolume", objStockDetailsForDecision.ThreeDayPreviousVolume);
			tempVar = Math.round(objStockDetailsForDecision.TwoDayPreviousPrice * 100.0) / 100.0;
			stockDetails.put("TwoDayPreviousPrice", tempVar);
			stockDetails.put("TwoDayPreviousVolume", objStockDetailsForDecision.TwoDayPreviousVolume);
			stockDetails.put("TypeofSuggestedStock", objStockDetailsForDecision.TypeofSuggestedStock);
			stockDetails.put("SupportLevel", objStockDetailsForDecision.supportLevel);
			stockDetails.put("ResistanceLevel", objStockDetailsForDecision.resistanceLevel);
		} catch (JSONException jsonException) {
			System.out.println("Issue creating JSON or processing results");
			jsonException.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return stockDetails;
	}
	public void createSuggestedStocks(List<StockDetailsForDecision> objStockDetailsForDecisionList) {
		System.out.println("****************Suggested Stock Creation**************");
		double tempVar;
		String finalURI = instanceUrl + "/services/apexrest/SuggestedStock/v1/";
		System.out.println("******************* Final URL ->"+finalURI);
		System.out.println("OAuth -> "+oAuthHeader);
		DecimalFormat df = new DecimalFormat("#.00");
		//System.out.println(df.format(f));
		try {
			JSONArray jArray = new JSONArray();
			JSONObject newSuggestedStock;
			for(int counter = 0; counter < (objStockDetailsForDecisionList.size()>20?20:objStockDetailsForDecisionList.size()); counter++) {
				jArray.put(getJsonObject(objStockDetailsForDecisionList.get(counter)));
			}
			JSONObject mainObj = new JSONObject();
			mainObj.put("StockDetails", jArray);
			System.out.println("JSON for Suggested stock record to be inserted:\n" + mainObj.toString(1));

			HttpClient httpClient = HttpClientBuilder.create().build();

			HttpPost httpPost = new HttpPost(finalURI);
			httpPost.addHeader(oAuthHeader);
			httpPost.addHeader(printHeader);
			StringEntity entityBody = new StringEntity(mainObj.toString(1));
			entityBody.setContentType("application/json");
			httpPost.setEntity(entityBody);

			HttpResponse httpResponse = httpClient.execute(httpPost);

			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (statusCode == 200) {
				String responseString = EntityUtils.toString(httpResponse.getEntity());
				
			} else {
				System.out.println("Insertion unsuccessful. Status code returned is " + statusCode);
			}
		} catch (JSONException jsonException) {
			System.out.println("Issue creating JSON or processing results");
			jsonException.printStackTrace();
		} catch (IOException ioException) {
			ioException.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	public void createSuggestedStocks1(List<StockDataForNewApproach> objStockDetailsForDecisionList) {
		System.out.println("****************Suggested Stock Creation**************");
		double tempVar;
		String finalURI = instanceUrl + "/services/apexrest/SuggestedStock/v1/";
		System.out.println("******************* Final URL ->"+finalURI);
		System.out.println("OAuth -> "+oAuthHeader);
		DecimalFormat df = new DecimalFormat("#.00");
		//System.out.println(df.format(f));
		try {
			JSONArray jArray = new JSONArray();
			JSONObject newSuggestedStock;
			for(int counter = 0; counter < (objStockDetailsForDecisionList.size()>20?20:objStockDetailsForDecisionList.size()); counter++) {
				jArray.put(getJsonObject(objStockDetailsForDecisionList.get(counter)));
			}
			JSONObject mainObj = new JSONObject();
			mainObj.put("StockDetails", jArray);
			System.out.println("JSON for Suggested stock record to be inserted:\n" + mainObj.toString(1));

			HttpClient httpClient = HttpClientBuilder.create().build();

			HttpPost httpPost = new HttpPost(finalURI);
			httpPost.addHeader(oAuthHeader);
			httpPost.addHeader(printHeader);
			StringEntity entityBody = new StringEntity(mainObj.toString(1));
			entityBody.setContentType("application/json");
			httpPost.setEntity(entityBody);

			HttpResponse httpResponse = httpClient.execute(httpPost);

			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (statusCode == 200) {
				String responseString = EntityUtils.toString(httpResponse.getEntity());
				
			} else {
				System.out.println("Insertion unsuccessful. Status code returned is " + statusCode);
			}
		} catch (JSONException jsonException) {
			System.out.println("Issue creating JSON or processing results");
			jsonException.printStackTrace();
		} catch (IOException ioException) {
			ioException.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}
	
	
}
