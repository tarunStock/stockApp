package com.amazonaws.tarun.stockApp.FinancialIndicator.Data;

import java.util.ArrayList;

public class CompanyFinancialData {
	public String nseCode;
	public ArrayList<CompanyAnnualFinancialData> objCompanyAnnualFinancialDataList;
	public ArrayList<CompanyQuarterFinancialData> objCompanyQuarterFinancialDataList;
	public ArrayList<CompanyHalfYearlyFinancialData> objCompanyHalfYearlyFinancialDataList;
}
