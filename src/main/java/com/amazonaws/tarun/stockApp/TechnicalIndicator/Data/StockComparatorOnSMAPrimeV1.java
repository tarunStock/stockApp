package com.amazonaws.tarun.stockApp.TechnicalIndicator.Data;

import java.util.Comparator;

public class StockComparatorOnSMAPrimeV1  implements Comparator<StockDetailsForDecision> {

	@Override
	public int compare(StockDetailsForDecision obj1, StockDetailsForDecision obj2) {
		//Comaprison for first object SMAComparison not null and Buy and second object SMAComparison null or not buy
		
		if((obj1!=null && obj1.SMAComparison.equalsIgnoreCase("Crossed")) && (obj2==null || (obj2!=null && !obj2.SMAComparison.equalsIgnoreCase("Crossed"))))	{			
			return -1;
		}
		
		if((obj2!=null && obj2.SMAComparison.equalsIgnoreCase("Crossed")) && (obj1==null || (obj1!=null && !obj1.SMAComparison.equalsIgnoreCase("Crossed"))))	{
			return 1;
		}
		
		if((obj2!=null && obj2.SMAComparison.equalsIgnoreCase("Crossed")) && (obj1!=null && obj1.SMAComparison.equalsIgnoreCase("Crossed")))	{
			
			if((obj1!=null && obj1.SMAToPriceComparison.equalsIgnoreCase("Crossed")) && (obj2==null || (obj2!=null && !obj2.SMAToPriceComparison.equalsIgnoreCase("Crossed"))))	{
				return -1;
			}
			
			if((obj2!=null && obj2.SMAToPriceComparison.equalsIgnoreCase("Crossed")) && (obj1==null || (obj1!=null && !obj1.SMAToPriceComparison.equalsIgnoreCase("Crossed"))))	{
				return 1;
			}
			
			if((obj2!=null && obj2.SMAToPriceComparison.equalsIgnoreCase("Crossed")) && (obj1!=null && obj1.SMAToPriceComparison.equalsIgnoreCase("Crossed")))	{
				
				if((obj1!=null && obj1.MACDStatus.equalsIgnoreCase("Crossed")) && (obj2==null || (obj2!=null && !obj2.MACDStatus.equalsIgnoreCase("Crossed"))))	{
					return -1;
				}
				
				if((obj2!=null && obj2.MACDStatus.equalsIgnoreCase("Crossed")) && (obj1==null || (obj1!=null && !obj1.MACDStatus.equalsIgnoreCase("Crossed"))))	{
					return 1;
				}
				
				if((obj2!=null && obj2.MACDStatus.equalsIgnoreCase("Crossed")) && (obj1!=null && obj1.MACDStatus.equalsIgnoreCase("Crossed")))	{
					
					if((obj1!=null && obj1.BBTrend.equalsIgnoreCase("Contracting")) && (obj2==null || (obj2!=null && !obj2.BBTrend.equalsIgnoreCase("Contracting"))))	{
						return -1;
					}
					
					if((obj2!=null && obj2.BBTrend.equalsIgnoreCase("Contracting")) && (obj1==null || (obj1!=null && !obj1.BBTrend.equalsIgnoreCase("Contracting"))))	{
						return 1;
					}
					
					if((obj1!=null && obj1.BBTrend.equalsIgnoreCase("Contracting")) && (obj2!=null && !obj2.BBTrend.equalsIgnoreCase("Contracting")))	{
						if((obj1!=null && obj1.CurrentPrice > obj1.ChandelierExit) && (obj2==null || (obj2!=null && !(obj2.CurrentPrice > obj2.ChandelierExit))))	{
							return -1;
						}
						
						if((obj2!=null && obj2.CurrentPrice > obj2.ChandelierExit) && (obj1==null || (obj1!=null && !(obj1.CurrentPrice > obj1.ChandelierExit))))	{
							return 1;
						}
						
						if((obj2!=null && obj2.CurrentPrice > obj2.ChandelierExit) && (obj1!=null && (obj1.CurrentPrice > obj1.ChandelierExit)))	{
							return 1;
						}
					}
					
				}
				
			} else {
				return 1;
			}
			
		}
		return 0;
	}
}
