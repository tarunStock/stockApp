package com.amazonaws.tarun.stockApp.TechnicalIndicator.Data;

public class ErrorDetails {
	public String className;
	public String methodName;
	public String exceptionTrace;
	
	public ErrorDetails(String className, String methodName, String exceptionTrace) {
		super();
		this.className = className;
		this.methodName = methodName;
		this.exceptionTrace = exceptionTrace;
	}
	
	
}
