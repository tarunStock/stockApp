package com.amazonaws.tarun.stockApp.TechnicalIndicator.Data;

import java.util.Date;

public class PurchasedStockData {
	public String stockCode;
	public Date purchasedDate;
	public int purchasedQuantity;
	public float purchasedPrice;
	public float brokeragePaid;
	public float currentPrice;
	public float oneDayPreviousPrice;
	public float twoDayPreviousPrice;
	public float threeDayPreviousPrice;
	public float currentVolume;
	public float oneDayPreviousVolume;
	public float twoDayPreviousVolume;
	public float threeDayPreviousVolume;
	public String MACDStatus;
	public String SMAComparison;
	public float RSIValue;
	public String BBChange;
	public float chandleirExit;
	@Override
	public String toString() {
		return "PurchasedStockData [stockCode=" + stockCode + ", purchasedDate=" + purchasedDate
				+ ", purchasedQuantity=" + purchasedQuantity + ", purchasedPrice=" + purchasedPrice + ", brokeragePaid="
				+ brokeragePaid + ", currentPrice=" + currentPrice + ", oneDayPreviousPrice=" + oneDayPreviousPrice
				+ ", twoDayPreviousPrice=" + twoDayPreviousPrice + ", threeDayPreviousPrice=" + threeDayPreviousPrice
				+ ", currentVolume=" + currentVolume + ", oneDayPreviousVolume=" + oneDayPreviousVolume
				+ ", twoDayPreviousVolume=" + twoDayPreviousVolume + ", threeDayPreviousVolume="
				+ threeDayPreviousVolume + ", MACDStatus=" + MACDStatus + ", SMAComparison=" + SMAComparison
				+ ", RSIValue=" + RSIValue + ", BBChange=" + BBChange + ", chandleirExit=" + chandleirExit + "]";
	}	
	
	
}
