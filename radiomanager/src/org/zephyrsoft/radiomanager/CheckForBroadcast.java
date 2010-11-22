package org.zephyrsoft.radiomanager;

import java.io.*;
import java.text.*;
import java.util.*;
import javax.swing.text.*;

/**
 * Checks if a broadcast is available for the current day and time.
 * All broadcasts have to be inside subdirectories located in the "radio base directory" (first parameter).
 * The naming scheme for the subdirectories is [day or days][starting hour]. Some examples:
 * <ul>
 * <li>Mo18</li>
 * <li>MoDiDoFrSaSo20</li>
 * <li>SaSo08</li>
 * </ul>
 * When calling this class, a return value of 0 means that there is a broadcast (which is printed to stdout).
 * A negative value indicates a problem with parameters or file system access rights (error is printed to stdout).
 * A positive value means that there is no broadcast planned for the current time.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class CheckForBroadcast {
	
	/**
	 * main method, see {@link CheckForBroadcast} class comment for more information
	 */
	public static void main(String[] args) {
		File radioBaseDir = null;
		if (args != null && args.length >= 1) {
			radioBaseDir = new File(args[0]);
		}
		BroadcastData data = doCheckForBroadcast(radioBaseDir);
		System.out.println((data.getResultText() == null ? "" : data.getResultText()));
		exit(data.getResultType().getIntValue());
	}
	
	/**
	 * check if a broadcast is currently available, see {@link CheckForBroadcast} class comment for more information
	 */
	public static BroadcastData doCheckForBroadcast(File radioBaseDir) {
		if (radioBaseDir == null || !radioBaseDir.isDirectory() || !radioBaseDir.canRead()) {
			return new BroadcastData(CheckResultEnum.ERROR, "PROBLEM_WITH_RADIO_BASE_DIR");
		} else {
			Map<Integer, String> daysOfWeekAbbeviated = populateLookupTable();
			Calendar currentDate = new GregorianCalendar();
			try {
				File[] subDirs = radioBaseDir.listFiles(new FileFilter() {
					@Override
					public boolean accept(File pathname) {
						// accept only directories
						return pathname.isDirectory();
					}
				});
				Arrays.sort(subDirs, new Comparator<File>() {
					@Override
					public int compare(File o1, File o2) {
						// null is illegal
						if (o1 == null || o2 == null) {
							throw new IllegalArgumentException("a file to compare was null");
						}
						// compare the absolute pathnames directly
						return o1.getAbsolutePath().compareTo(o2.getAbsolutePath());
					}
				});
				for (File subDir : subDirs) {
					if (isDayOfWeekIncluded(daysOfWeekAbbeviated, currentDate, subDir.getName())
						&& isHourOfDayIncluded(currentDate, subDir.getName())) {
						// planned broadcast found
						return new BroadcastData(CheckResultEnum.BROADCAST_FOUND, subDir.getAbsolutePath());
					}
				}
				// obviously no broadcast planned
				return new BroadcastData(CheckResultEnum.NO_BROADCAST_FOUND, null);
			} catch (Exception e) {
				// signal exception
				return new BroadcastData(CheckResultEnum.ERROR, getStackTrace(e));
			}
		}
	}
	
	private static String getStackTrace(Throwable aThrowable) {
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		aThrowable.printStackTrace(printWriter);
		return result.toString();
	}
	
	/**
	 * fill the static lookup table with the days of week
	 */
	private static Map<Integer, String> populateLookupTable() {
		Map<Integer, String> daysOfWeekAbbeviated = new HashMap<Integer, String>();
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
	private static void exit(int returnValue) {
		System.exit(returnValue);
	}
	
	/**
	 * splits the given subDirName into pairs of characters (odd length results in exception)
	 */
	private static List<String> splitIntoPairs(String subDirName) {
		if (subDirName.length() % 2 == 1) {
			throw new IllegalArgumentException("subDirName contains an odd number of characters");
		} else {
			List<String> ret = new ArrayList<String>();
			int i = 0;
			while (i < subDirName.length()) {
				ret.add(subDirName.substring(i, i + 2));
				i = i + 2;
			}
			return ret;
		}
	}
	
	/**
	 * tests if the day-of-week of the given date is mentioned in the subDirName
	 */
	private static boolean isDayOfWeekIncluded(Map<Integer, String> daysOfWeekAbbeviated, Calendar date,
		String subDirName) {
		List<String> subDirNameParts = splitIntoPairs(subDirName);
		String currentDayOfWeekAbbreviated = daysOfWeekAbbeviated.get(date.get(Calendar.DAY_OF_WEEK));
		boolean ret = subDirNameParts.contains(currentDayOfWeekAbbreviated);
		return ret;
	}
	
	/**
	 * tests if the hour-of-day of the given date is mentioned in the subDirName
	 */
	private static boolean isHourOfDayIncluded(Calendar date, String subDirName) {
		List<String> subDirNameParts = splitIntoPairs(subDirName);
		DecimalFormat formatter = new DecimalFormat("00");
		String currentHourOfDay = formatter.format(date.get(Calendar.HOUR_OF_DAY));
		String subDirHourOfDay = subDirNameParts.get(subDirNameParts.size() - 1);
		boolean ret = subDirHourOfDay.equals(currentHourOfDay);
		return ret;
	}
	
}
