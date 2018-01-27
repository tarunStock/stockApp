package com.amazonaws.tarun.stockApp.FinancialIndicator.Data;

public class CompanyAnnualFinancialData {
	public String resultMonth;
	public int resultYear;
	public double netSales;
	public double otherIncome;
	public double totalExpenses;
	public double operatingProfit;
	public double pbdit;
	public double interest;
	public double pbdt;
	public double profitBeforeTax;
	public double netProfit;
	public double bookValue;
	public float equitiDividendRatio;
	public float basicEPS;
	public float netCashFlow;
	@Override
	public String toString() {
		return "CompanyAnnualFinancialData [resultMonth=" + resultMonth + ", resultYear=" + resultYear + ", netSales="
				+ netSales + ", otherIncome=" + otherIncome + ", totalExpenses=" + totalExpenses + ", operatingProfit="
				+ operatingProfit + ", pbdit=" + pbdit + ", interest=" + interest + ", pbdt=" + pbdt
				+ ", profitBeforeTax=" + profitBeforeTax + ", netProfit=" + netProfit + ", bookValue=" + bookValue
				+ ", equitiDividendRatio=" + equitiDividendRatio + ", basicEPS=" + basicEPS + ", netCashFlow="
				+ netCashFlow + "]";
	}	
}
