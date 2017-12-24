package com.amazonaws.tarun.stockApp.Utils;

import java.util.ArrayList;

import com.amazonaws.tarun.stockApp.TechnicalIndicator.Calculation.SendSuggestedStockInMail;
import com.amazonaws.tarun.stockApp.TechnicalIndicator.Data.ErrorDetails;

public class HandleErrorDetails {
	static ArrayList<ErrorDetails> objErrorDetailsList = new ArrayList<ErrorDetails>();
	
	public static void addError(String className, String methodName, String error) {
		//Skip adding duplicate primary key error
		if(!error.contains("Duplicate")) {
			ErrorDetails obj = new ErrorDetails(className, methodName, error);
			objErrorDetailsList.add(obj);
		}
	}
	
	public static void sendErrorsInMail(String flow) {
		
		StringBuilder mailBody = new StringBuilder();
		mailBody.append("<html><body><table border='1'><tr><th>Sr. No.</th><th>Class</th><th>Method</th><th>Error</th></tr>");
		for (int counter = 0; counter <objErrorDetailsList.size(); counter++) {
			mailBody.append("<tr><td>" + (counter+1) + "</td>");
			mailBody.append("<td>" + objErrorDetailsList.get(counter).className + "</td>");
			mailBody.append("<td>"+ objErrorDetailsList.get(counter).methodName + "</td>");
			mailBody.append("<td>" + objErrorDetailsList.get(counter).exceptionTrace + "</td>");
			mailBody.append("<td>" +  "</td></tr>");
		}
		 if(objErrorDetailsList.size() > 0) {
	        	new SendSuggestedStockInMail("tarunstockcomm@gmail.com","Error List for flow -> "+flow,mailBody.toString());
	        	System.out.println("Mail Sent");
	        }
	}
}
