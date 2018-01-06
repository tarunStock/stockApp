package com.amazonaws.tarun.stockApp.FinancialIndicator.Data;

import java.util.Date;

public class StockFinancialData {
	public String stockCode;
	public String statementPeriod;
	public Date periodEnded;
	//Consolidate or Non-consolidated
	public String typeOfReport;
	public float netSales;
	public float otherOperatingIncome;
	public float netProfit;
	public float netProfitAfterTaxes;
	
	@Override
	public String toString() {
		return "StockFinancialData [stockCode=" + stockCode + ", statementPeriod=" + statementPeriod + ", periodEnded="
				+ periodEnded + ", typeOfReport=" + typeOfReport + ", netSales=" + netSales + ", otherOperatingIncome="
				+ otherOperatingIncome + ", netProfit=" + netProfit + ", netProfitAfterTaxes=" + netProfitAfterTaxes
				+ "]";
	}
}
