package com.amazonaws.tarun.stockApp.TechnicalIndicator.Data;

public class DailyStockData {
	
	public String stockName;
	public String tradedDate;
	public float openPrice;
	public float closePrice;
	public float highPrice;
	public float lowPrice;
	public float changePercentage;
	public float volume;
	public float yearlyHigh;
	public float yearlyLow;
	public float changeInPrice;
	
	 @Override
	  public boolean equals(Object comparingObject) {
	        boolean retVal = false;

	        if (comparingObject instanceof DailyStockData){
	        	DailyStockData ptr = (DailyStockData) comparingObject;
	            retVal = (ptr.stockName.equalsIgnoreCase(this.stockName));
	        } else if (comparingObject instanceof String) {
	        	retVal = (this.stockName.equalsIgnoreCase((String)comparingObject));
	        }

	     return retVal;
	  }

	    /*@Override
	    public int hashCode() {
	        int hash = 7;
	        hash = 17 * hash + (this.stockName != null ? this.stockName.hashCode() : 0);
	        return hash;
	    }*/
}
