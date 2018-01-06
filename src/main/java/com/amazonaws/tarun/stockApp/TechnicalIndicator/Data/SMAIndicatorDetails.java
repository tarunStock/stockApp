package com.amazonaws.tarun.stockApp.TechnicalIndicator.Data;
import java.time.LocalDate;

public class SMAIndicatorDetails {// implements Comparable<SMAIndicatorDetails> {
	public String stockCode;
	public float stockPrice;
	public boolean pricegrowth = false;
	public float percentagePriceChange;
	public String signalPriceToSMA; //"put" for options in case stock dropping down. "buy" in case price going up and crossed middleperiodSMA
	public float priceToSMApercentageDeviation;
	public String signalSMAToSMA; //"put" for options in case stock dropping down. "buy" in case lower SMA going up and crossed higherperiodSMA
	public float SMAToSMApercentageDeviation;
	public LocalDate signalDate;
	public boolean PNSMAcrossover = false;
	public boolean SMNSMcrossover = false;
	public boolean PNSMcontinuousGrowth = false;
	public boolean SMNSMcontinuousGrowth = false;
	public boolean IsSMGrowing = false;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((stockCode == null) ? 0 : stockCode.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SMAIndicatorDetails other = (SMAIndicatorDetails) obj;
		if (stockCode == null) {
			if (other.stockCode != null)
				return false;
		} else if (!stockCode.equals(other.stockCode))
			return false;
		return true;
	}
	
	
	
	
	@Override
	public String toString() {
		return "SMAIndicatorDetails [stockCode=" + stockCode + ", SMNSMcrossover=" + SMNSMcrossover + ", PNSMAcrossover=" + PNSMAcrossover + ", signalSMAToSMA=" + signalSMAToSMA
				+ ", signalPriceToSMA=" + signalPriceToSMA + "]";
	}
	
	
	
}
