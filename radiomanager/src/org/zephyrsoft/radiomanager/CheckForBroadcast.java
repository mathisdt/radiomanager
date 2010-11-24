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
		Utils.exit(data.getResultType().getIntValue());
	}
	
	/**
	 * check if a broadcast is currently available, see {@link CheckForBroadcast} class comment for more information
	 */
	public static BroadcastData doCheckForBroadcast(File radioBaseDir) {
		Calendar currentDate = new GregorianCalendar();
		String dayOfWeek = Utils.getDayOfWeek(currentDate);
		String hourOfDay = Utils.getHourOfDay(currentDate);
		if (radioBaseDir == null || !radioBaseDir.isDirectory() || !radioBaseDir.canRead()) {
			return new BroadcastData(CheckResultEnum.ERROR, "PROBLEM_WITH_RADIO_BASE_DIR");
		} else {
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
					if (Utils.isDayOfWeekIncluded(dayOfWeek, subDir.getName())
						&& Utils.isHourOfDayIncluded(hourOfDay, subDir.getName())) {
						// planned broadcast found
						return new BroadcastData(CheckResultEnum.BROADCAST_FOUND, subDir.getAbsolutePath());
					}
				}
				// obviously no broadcast planned
				return new BroadcastData(CheckResultEnum.NO_BROADCAST_FOUND, null);
			} catch (Exception e) {
				// signal exception
				return new BroadcastData(CheckResultEnum.ERROR, Utils.getStackTrace(e));
			}
		}
	}
	
	/**
	 * check if a broadcast is available for the given day-of-week and hour,
	 * see {@link CheckForBroadcast} class comment for more information
	 */
	public static BroadcastData doCheckForBroadcast(File radioBaseDir, String dayOfWeek, String hourOfDay) {
		if (radioBaseDir == null || !radioBaseDir.isDirectory() || !radioBaseDir.canRead()) {
			return new BroadcastData(CheckResultEnum.ERROR, "PROBLEM_WITH_RADIO_BASE_DIR");
		} else {
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
					if (Utils.isDayOfWeekIncluded(dayOfWeek, subDir.getName())
						&& Utils.isHourOfDayIncluded(hourOfDay, subDir.getName())) {
						// planned broadcast found
						return new BroadcastData(CheckResultEnum.BROADCAST_FOUND, subDir.getAbsolutePath());
					}
				}
				// obviously no broadcast planned
				return new BroadcastData(CheckResultEnum.NO_BROADCAST_FOUND, null);
			} catch (Exception e) {
				// signal exception
				return new BroadcastData(CheckResultEnum.ERROR, Utils.getStackTrace(e));
			}
		}
	}
	
}
