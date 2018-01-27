package com.amazonaws.tarun.stockApp.FinancialIndicator.Data;

public class CompanyQuarterFinancialData {
	public String nseCode;
	public String resultMonth;
	public int resultYear;
	public double netSales;
	public double otherIncome;
	public double pbdit;
	public double interest;
	public double pbdt;
	public double profitBeforeTax;
	public double netProfit;
	public float basicEPS;
	
	@Override
	public String toString() {
		return "CompanyQuarterFinancialData [nseCode=" + nseCode + ", resultMonth=" + resultMonth + ", resultYear="
				+ resultYear + ", netSales=" + netSales + ", otherIncome=" + otherIncome + ", pbdit=" + pbdit
				+ ", interest=" + interest + ", pbdt=" + pbdt + ", profitBeforeTax=" + profitBeforeTax + ", netProfit="
				+ netProfit + ", basicEPS=" + basicEPS + "]";
	}
}
