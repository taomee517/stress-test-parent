/**
 * 
 */
package com.fzk.stress.util;


import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author yu.hou
 *
 */
public class DateTimeUtil {

	public static final String NOSECONDS = "yyyy-MM-dd HH:mm";
	public static final String DATETIME = "yyyy-MM-dd HH:mm:ss";
	public static final String DATE = "yyyy-MM-dd";

	public static String getDatetime(){
		return DateFormatUtils.format(System.currentTimeMillis(), DATETIME);
	}


	/**  
	 * 计算两个日期之间相差的天数  
	 * @param smdate 较小的时间 
	 * @param bdate  较大的时间 
	 * @return 相差天数 
	 * @throws ParseException  
	 */
	public static int daysBetween(Date smdate, Date bdate) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		smdate = sdf.parse(sdf.format(smdate));
		bdate = sdf.parse(sdf.format(bdate));
		Calendar cal = Calendar.getInstance();
		cal.setTime(smdate);
		long time1 = cal.getTimeInMillis();
		cal.setTime(bdate);
		long time2 = cal.getTimeInMillis();
		long between_days = (time2 - time1) / (1000 * 3600 * 24);

		return Integer.parseInt(String.valueOf(between_days));
	}

	/**
	 * 
	* @Description: 增加天数
	* @param  time
	* @param  incDayNum
	* @return Date
	* @throws
	 */
	public static Date addDay(Date time, int incDayNum) {
		Calendar result = Calendar.getInstance();
		result.setTime(time);
		result.add(Calendar.DATE, incDayNum);
		return result.getTime();
	}

	/**
	 * 增加月份
	 * @param time
	 * @param incMonthNum
	 * @return
	 */
	public static Date addMonth(Date time, int incMonthNum) {
		Calendar result = Calendar.getInstance();
		result.setTime(time);
		result.add(Calendar.MONTH, incMonthNum);
		return result.getTime();
	}

	/**
	 * 获取两个时间的时间差（秒）
	 * @param smdate
	 * @param bdate
	 * @return
	 * @throws ParseException
	 */
	public static long secondBetween(Date smdate, Date bdate) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		smdate = sdf.parse(sdf.format(smdate));
		bdate = sdf.parse(sdf.format(bdate));

		Calendar cal = Calendar.getInstance();

		cal.setTime(smdate);
		long time1 = cal.getTimeInMillis();

		cal.setTime(bdate);
		long time2 = cal.getTimeInMillis();

		long between_second = (time2 - time1) / (1000);

		return Long.parseLong(String.valueOf(between_second));
	}

	/*
	 * 时间差分钟
	 */
	public static long minuteBetween(Date smdate, Date bdate) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

		smdate = sdf.parse(sdf.format(smdate));
		bdate = sdf.parse(sdf.format(bdate));

		Calendar cal = Calendar.getInstance();

		cal.setTime(smdate);
		long time1 = cal.getTimeInMillis();

		cal.setTime(bdate);
		long time2 = cal.getTimeInMillis();

		long between_second = (time2 - time1) / (1000 * 60);

		return Long.parseLong(String.valueOf(between_second));
	}

	/**
	 * 两个日期是否相交
	 * @param start1
	 * @param end1
	 * @param start2
	 * @param end2
	 * @return
	 */
	public static boolean isCrossed(Date start1, Date end1, Date start2, Date end2) {
		long s1 = 0;
		long e1 = 0;
		long s2 = 0;
		long e2 = 0;

		if (start1.getTime() < start2.getTime()) {
			s1 = start1.getTime();
			e1 = end1.getTime();
			s2 = start2.getTime();
			e2 = end2.getTime();
		} else {
			s1 = start2.getTime();
			e1 = end2.getTime();
			s2 = start1.getTime();
			e2 = end1.getTime();
		}
		return s2 <= e1;
	}


	/**
	 * 增加小时数
	 * @param time
	 * @param hourNum
	 * @return
	 */
	public static Date addHour(Date time, int hourNum) {
		Calendar result = Calendar.getInstance();
		result.setTime(time);
		result.add(Calendar.HOUR, hourNum);
		return result.getTime();
	}

	/**
	 * 增加分钟数
	 * @param time
	 * @param
	 * @return
	 */
	public static Date addMinutes(Date time, int minuteNum) {
		Calendar result = Calendar.getInstance();
		result.setTime(time);
		result.add(Calendar.MINUTE, minuteNum);
		return result.getTime();
	}

	public static int getLastDayOfMonth(int year, int month) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month);
		// 某年某月的最后一天  
		return cal.getActualMaximum(Calendar.DATE);
	}

	/**
	 * 增加年份
	 * @param time
	 * @param incYearNum
	 * @return
	 */
	public static Date addYear(Date time, int incYearNum) {
		Calendar result = Calendar.getInstance();
		result.setTime(time);
		result.add(Calendar.YEAR, incYearNum);
		return result.getTime();
	}

	public static Date UTCTimeParseBJTime(Date UTCDate) {
		return addHour(UTCDate, 8);
	}

	public static long getUTCTime() {
		// 1、取得本地时间：
		final Calendar cal = Calendar.getInstance();
		//System.out.println(cal.getTime());
		// 2、取得时间偏移量：
		final int zoneOffset = cal.get(Calendar.ZONE_OFFSET);
		//	System.out.println(zoneOffset);
		// 3、取得夏令时差：
		final int dstOffset = cal.get(Calendar.DST_OFFSET);
		//System.out.println(dstOffset);
		// 4、从本地时间里扣除这些差量，即可以取得UTC时间：
		cal.add(Calendar.MILLISECOND, -(zoneOffset + dstOffset));
		return cal.getTime().getTime();
	}

	public static Date getUTCTime(Date d) {
		return new Date(getUTCTime(d.getTime()));
	}

	public static long getUTCTime(long localTime) {
		return localTime - 8 * 60 * 60 * 1000;
	}

	public static Date getUTCToBJ(Date date) {
		return new Date(date.getTime() + 8 * 60 * 60 * 1000);
	}
	
	@SuppressWarnings("null")
	public static String HexStringToTime(String hex) {
		String time = "";	
		int year = Integer.parseInt(hex.substring(0, 2),16) + 2000;
		int month = Integer.parseInt(hex.substring(2, 4),16);
		int day = Integer.parseInt(hex.substring(4, 6),16);
		int hour = Integer.parseInt(hex.substring(6, 8),16);
		int minute = Integer.parseInt(hex.substring(8, 10),16);
		int second = Integer.parseInt(hex.substring(10, 12),16);
		time = time + year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second;
		return time.toString();
		
	}
	public static String TimeToHexString(String time) {
		String hex = "";
		String yyyy = time.split(" ")[0].split("-")[0];
		String mmm = time.split(" ")[0].split("-")[1];
		String dd = time.split(" ")[0].split("-")[2];
		String year = Integer.toHexString(Integer.parseInt(yyyy)-2000);
		String month = Integer.toHexString(Integer.parseInt(mmm));
		String day = Integer.toHexString(Integer.parseInt(dd));
		String hh = time.split(" ")[1].split(":")[0];
		String mm = time.split(" ")[1].split(":")[1];
		String ss = time.split(" ")[1].split(":")[2];
		String hour = Integer.toHexString(Integer.parseInt(hh));
		String minute = Integer.toHexString(Integer.parseInt(mm));
		String second = Integer.toHexString(Integer.parseInt(ss));
		hex = hex + year + month + day + hour + minute + second;
		return hex;
		
	}
}
