package org.zephyrsoft.radiomanager;

import java.io.*;
import java.text.*;
import java.util.*;
import javax.swing.text.*;

/**
 * Checks if a broadcast is available for the current day and time.
 * 
 * All broadcasts have to be inside subdirectories located in the "radio base directory" (first parameter).
 * 
 * The naming scheme for the subdirectories is [day or days][starting hour]. Some examples: 
 * <ul>
 * <li>Mo18</li>
 * <li>MoDiDoFrSaSo20</li>
 * <li>SaSo08</li>
 * </ul>
 * 
 * When calling this class, a return value of 0 means that there is a broadcast (which is printed to stdout).
 * A negative value indicates a problem with parameters or file system access rights. 
 * A positive value means that there is no broadcast planned for the current time.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class CheckForBroadcast {
	
	/** maps days of week to their abbreviations used in dir names */
	private static Map<Integer, String> DAY_OF_WEEK_ABBREVIATED;
	
	/**
	 * main method, see {@link CheckForBroadcast} class comment for more information
	 */
	public static void main(String[] args) {
		if (args==null || args.length==0) {
			// no arguments => radio base directory missing
			System.out.println("ERROR_EXPECTED_RADIO_BASE_DIR_AS_FIRST_ARGUMENT");
			exit(-1);
		} else {
			populateLookupTable();
			
			Calendar currentDate = new GregorianCalendar();
			try {
				File mainDir = new File(args[0]);
				File[] subDirs = mainDir.listFiles(new FileFilter() {
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
						if (o1==null || o2==null) {
							throw new IllegalArgumentException("a file to compare was null");
						}
						// compare the absolute pathnames directly
						return o1.getAbsolutePath().compareTo(o2.getAbsolutePath());
					}
				});
				for (File subDir : subDirs) {
					if (isDayOfWeekIncluded(currentDate, subDir.getName()) && isHourOfDayIncluded(currentDate, subDir.getName())) {
						// planned broadcast found 
						System.out.println(subDir.getAbsolutePath());
						exit(0);
					}
				}
				// obviously no broadcast planned
				exit(1);
			} catch (Exception e) {
				// signal exception
				e.printStackTrace();
				exit(-2);
			}
		}
	}

	/**
	 * fill the static lookup table with the days of week
	 */
	private static void populateLookupTable() {
		DAY_OF_WEEK_ABBREVIATED = new HashMap<Integer, String>();
		DAY_OF_WEEK_ABBREVIATED.put(Calendar.MONDAY, "Mo");
		DAY_OF_WEEK_ABBREVIATED.put(Calendar.TUESDAY, "Di");
		DAY_OF_WEEK_ABBREVIATED.put(Calendar.WEDNESDAY, "Mi");
		DAY_OF_WEEK_ABBREVIATED.put(Calendar.THURSDAY, "Do");
		DAY_OF_WEEK_ABBREVIATED.put(Calendar.FRIDAY, "Fr");
		DAY_OF_WEEK_ABBREVIATED.put(Calendar.SATURDAY, "Sa");
		DAY_OF_WEEK_ABBREVIATED.put(Calendar.SUNDAY, "So");
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
	private static boolean isDayOfWeekIncluded(Calendar date, String subDirName) {
		List<String> subDirNameParts = splitIntoPairs(subDirName);
		String currentDayOfWeekAbbreviated = DAY_OF_WEEK_ABBREVIATED.get(date.get(Calendar.DAY_OF_WEEK));
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
