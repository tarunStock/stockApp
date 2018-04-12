package com.amazonaws.tarun.stockApp.TechnicalIndicator.Data;

import java.util.Comparator;

public class CompareBearishStockForAllIndicators  implements Comparator<StockDataForNewApproach>{

	@Override	
	public int compare(StockDataForNewApproach obj1, StockDataForNewApproach obj2) {
		
		if(obj1.MACDStatus.equalsIgnoreCase("crossed") && !obj2.MACDStatus.equalsIgnoreCase("crossed")) {
			return 1;
		} else if (!obj1.MACDStatus.equalsIgnoreCase("crossed") && obj2.MACDStatus.equalsIgnoreCase("crossed")) {
			return -1;
		} else if(obj1.lowerToMiddleSMcrossover && !obj2.lowerToMiddleSMcrossover) {
			return 1;
		} else if(!obj1.lowerToMiddleSMcrossover && obj2.lowerToMiddleSMcrossover) {
			return -1;
		} else if(obj1.lowerToHigherSMcrossover && !obj2.lowerToHigherSMcrossover) {
			return 1;
		} else if(!obj1.lowerToHigherSMcrossover && obj2.lowerToHigherSMcrossover) {
			return -1;
		} else if(obj1.priceToMiddleSMcrossover && !obj2.priceToMiddleSMcrossover) {
			return 1;
		} else if(!obj1.priceToMiddleSMcrossover && obj2.priceToMiddleSMcrossover) {
			return -1;
		} else if(obj1.middleSMAIncreasing && !obj2.middleSMAIncreasing) {
			return 1;
		} else if(!obj1.middleSMAIncreasing && obj2.middleSMAIncreasing) {
			return -1;
		} else if(obj1.smallerSMAIncreasing && !obj2.smallerSMAIncreasing) {
			return 1;
		} else if(!obj1.smallerSMAIncreasing && obj2.smallerSMAIncreasing) {
			return -1;
		} else if(obj1.accumulationDistributionIncreasing && !obj2.accumulationDistributionIncreasing) {
			return 1;
		} else if(!obj1.accumulationDistributionIncreasing && obj2.accumulationDistributionIncreasing) {
			return -1;
		} else if(obj1.BBTrend.equalsIgnoreCase("contracting") && !obj2.BBTrend.equalsIgnoreCase("contracting")) {
			return 1;
		} else if(!obj1.BBTrend.equalsIgnoreCase("contracting") && obj2.BBTrend.equalsIgnoreCase("contracting")) {
			return -1;
		} 
		return 0;
	}
	
}
