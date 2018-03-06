package com.amazonaws.tarun.stockApp.TechnicalIndicator.Data;

import java.time.LocalDate;

public class StockDetailsForDecision {
	public String stockCode;
	public LocalDate suggestedDate; 
	
	public float ChandelierExit;
	public float CurrentPrice;
	public long CurrentVolume;
	public float OneDayPreviousPrice;
	public long OneDayPreviousVolume;
	public float RSIValue;
	public String BBTrend;
	public float ThreeDayPreviousPrice;
	public float TwoDayPreviousPrice;
	public long TwoDayPreviousVolume;
	public long ThreeDayPreviousVolume;	
	public String MACDStatus;
	public String SMAComparison;
	public String SMAToPriceComparison;	
	public String TypeofSuggestedStock;
	public double supportLevel;
	public double resistanceLevel;
}
