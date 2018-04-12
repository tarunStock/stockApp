package com.amazonaws.tarun.stockApp.TechnicalIndicator.Data;

import java.util.ArrayList;

public class AccumulatioDistributionData {
	public String stockName;
	public ArrayList<String> tradeddate;
	public ArrayList<Float> closePrice;
	public ArrayList<Float> highPrice;
	public ArrayList<Float> lowPrice;
	public ArrayList<Long> volume;
	public ArrayList<Float> accumulatioDistribution;
	public Float lastEnteredaccumulatioDistribution;
}
