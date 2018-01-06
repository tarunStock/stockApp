package com.amazonaws.tarun.stockApp.Common;

public class StockDetails {
	public String stockCode;
	public String companyName;
	public String isinNo;
	
	@Override
	public String toString() {
		return "StockDetails [stockCode=" + stockCode + ", companyName=" + companyName + ", isinNo=" + isinNo + "]";
	}

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
		StockDetails other = (StockDetails) obj;
		if (stockCode == null) {
			if (other.stockCode != null)
				return false;
		} else if (!stockCode.equals(other.stockCode))
			return false;
		return true;
	}
	
	
	 
}
