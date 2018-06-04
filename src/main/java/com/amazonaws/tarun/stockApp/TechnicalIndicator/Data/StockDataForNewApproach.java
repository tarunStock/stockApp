package com.amazonaws.tarun.stockApp.TechnicalIndicator.Data;

public class StockDataForNewApproach extends StockDetailsForDecision implements Cloneable {
	public boolean MACDIncreasing;
	public boolean smallerSMAIncreasing;
	public boolean middleSMAIncreasing;
	public boolean lowerToMiddleSMcrossover;
	public boolean lowerToHigherSMcrossover;
	public boolean priceToMiddleSMcrossover;
	public boolean accumulationDistributionIncreasing;
	public boolean stochasticInLine;
	public boolean OBVIncreasing;
	@Override
	public String toString() {
		return "StockDataForNewApproach -> stockCode=" + stockCode + " MACDCrossed = "+  MACDStatus + " MACDIncreasing=" + MACDIncreasing + ", smallerSMAIncreasing="
				+ smallerSMAIncreasing + ", middleSMAIncreasing=" + middleSMAIncreasing + ", accumulationDistributionIncreasing="
				+ accumulationDistributionIncreasing;
	}
	
	public Object clone()throws CloneNotSupportedException{  
		return super.clone();  
		}  
	
	public String getString() {
		return stockCode + "," +  SMAComparison + "," + SMAToPriceComparison + "," + MACDStatus + "," + MACDIncreasing + "," + smallerSMAIncreasing + "," + middleSMAIncreasing + "," + accumulationDistributionIncreasing + System.lineSeparator();
	}
}
