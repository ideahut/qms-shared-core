package com.github.ideahut.qms.shared.core.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public abstract class TimeUtil {

	public static Long getGMTCurrentTimeMillis() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		String source = sdf.format(new Date());
		sdf.setTimeZone(TimeZone.getDefault());
		try {
			return sdf.parse(source).getTime();
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}
	
	/*
	public static String getMonthName(int month) {
		String name = ""; 
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] months = dfs.getMonths();
        if (month >= 0 && month <= 11 ) {
        	name = months[month];
        }
		return name;		
	}
	*/
}
