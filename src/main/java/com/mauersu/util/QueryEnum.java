package com.mauersu.util;

public enum QueryEnum {

	middle("middle" , "like*"), 
	head("middle" , "head^"), 
	tail("middle" , "tail$"), 
	
	
	;
	
	private String queryMode;
	private String queryModeCh;
	
	QueryEnum(String queryMode, String queryModeCh) {
		this.queryMode = queryMode;
		this.queryModeCh = queryModeCh;
	}
	
	
	public String getQueryMode() {
		return queryMode;
	}
	public void setQueryMode(String queryMode) {
		this.queryMode = queryMode;
	}
	public String getQueryModeCh() {
		return queryModeCh;
	}
	public void setQueryModeCh(String queryModeCh) {
		this.queryModeCh = queryModeCh;
	}
	
}
