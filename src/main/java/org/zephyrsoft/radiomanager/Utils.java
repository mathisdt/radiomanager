/**
 *
 */
package org.zephyrsoft.radiomanager;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utilities for webradio management.
 */
public class Utils {

	private static Map<Integer, String> DAYS_OF_WEEK_ABBREVIATED = populateLookupTable();
	private static DecimalFormat HOUR_FORMATTER = new DecimalFormat("00");

	/**
	 * get the given stack trace as string
	 */
	public static String getStackTrace(Throwable aThrowable) {
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		aThrowable.printStackTrace(printWriter);
		return result.toString();
	}

	/**
	 * fill the static lookup table with the days of week
	 */
	public static Map<Integer, String> populateLookupTable() {
		Map<Integer, String> daysOfWeekAbbeviated = new HashMap<>();
		daysOfWeekAbbeviated.put(Calendar.MONDAY, "Mo");
		daysOfWeekAbbeviated.put(Calendar.TUESDAY, "Di");
		daysOfWeekAbbeviated.put(Calendar.WEDNESDAY, "Mi");
		daysOfWeekAbbeviated.put(Calendar.THURSDAY, "Do");
		daysOfWeekAbbeviated.put(Calendar.FRIDAY, "Fr");
		daysOfWeekAbbeviated.put(Calendar.SATURDAY, "Sa");
		daysOfWeekAbbeviated.put(Calendar.SUNDAY, "So");
		return daysOfWeekAbbeviated;
	}

	/**
	 * exit the application with a defined return value
	 */
	public static void exit(int returnValue) {
		System.exit(returnValue);
	}

	/**
	 * splits the given subDirName into pairs of characters (odd length results in exception)
	 */
	private static List<String> splitIntoPairs(String subDirName) {
		if (subDirName.length() % 2 == 1) {
			throw new IllegalArgumentException("subDirName contains an odd number of characters");
		} else {
			List<String> ret = new ArrayList<>();
			int i = 0;
			while (i < subDirName.length()) {
				ret.add(subDirName.substring(i, i + 2));
				i = i + 2;
			}
			return ret;
		}
	}

	/**
	 * tests if the given day-of-week abbreviation is mentioned in the subDirName
	 */
	public static boolean isDayOfWeekIncluded(String dayAbbreviation, String subDirName) {
		List<String> subDirNameParts = splitIntoPairs(subDirName);
		boolean ret = subDirNameParts.contains(dayAbbreviation);
		return ret;
	}

	/**
	 * gets the day-of-week abbreviation of the given date
	 */
	public static String getDayOfWeek(Calendar date) {
		String dayOfWeekAbbreviated = DAYS_OF_WEEK_ABBREVIATED.get(date.get(Calendar.DAY_OF_WEEK));
		return dayOfWeekAbbreviated;
	}

	/**
	 * tests if the given hour-of-day is mentioned in the subDirName
	 */
	public static boolean isHourOfDayIncluded(String hour, String subDirName) {
		List<String> subDirNameParts = splitIntoPairs(subDirName);
		String subDirHourOfDay = subDirNameParts.get(subDirNameParts.size() - 1);
		boolean ret = subDirHourOfDay.equals(hour);
		return ret;
	}

	/**
	 * gets the hour-of-day of the given date
	 */
	public static String getHourOfDay(Calendar date) {
		String hourOfDay = HOUR_FORMATTER.format(date.get(Calendar.HOUR_OF_DAY));
		return hourOfDay;
	}
}
