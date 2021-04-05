package org.zephyrsoft.radiomanager;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;

/**
 * Checks if a broadcast is available for the current day and time.
 * All broadcasts have to be inside subdirectories located in one of the radio base directories
 * (which are given via parameters). All directories are searched one after the other (order preserved
 * from parameters), and the first broadcast which is found "wins". With this mechanism, you can prioritize:
 * put the important broadcasts into one directory which is writable only by few people and thus prevent
 * them from being overwritten by other broadcasts that are uploaded into another directory. The naming
 * scheme for the subdirectories is [day or days][starting hour]. Some examples:
 * <ul>
 * <li>Mo18</li>
 * <li>MoDiDoFrSaSo20</li>
 * <li>SaSo08</li>
 * </ul>
 * When calling this class, a return value of 0 means that there is a broadcast (which is printed to stdout).
 * A negative value indicates a problem with parameters or file system access rights (error is printed to stdout).
 * A positive value means that there is no broadcast planned for the current time.
 */
public class CheckForBroadcast {

	/**
	 * main method, see {@link CheckForBroadcast} class comment for more information
	 */
	public static void main(String[] args) {
		File[] radioBaseDirs = new File[args.length];
		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				radioBaseDirs[i] = new File(args[i]);
			}
			BroadcastData data = doCheckForBroadcast(radioBaseDirs);
			System.out.println((data.getResultText() == null ? "" : data.getResultText()));
			Utils.exit(data.getResultType().getIntValue());
		}
	}

	/**
	 * check if a broadcast is currently available, see {@link CheckForBroadcast} class comment for more information
	 */
	public static BroadcastData doCheckForBroadcast(File... radioBaseDirs) {
		Calendar currentDate = new GregorianCalendar();
		String dayOfWeek = Utils.getDayOfWeek(currentDate);
		String hourOfDay = Utils.getHourOfDay(currentDate);
		return doCheckForBroadcast(dayOfWeek, hourOfDay, radioBaseDirs);
	}

	/**
	 * check if a broadcast is available for the given day-of-week and hour,
	 * see {@link CheckForBroadcast} class comment for more information
	 */
	public static BroadcastData doCheckForBroadcast(String dayOfWeek, String hourOfDay, File... radioBaseDirs) {
		FileFilter acceptOnlyDirectories = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				// accept only directories that don't start with "_" or "."
				return pathname.isDirectory() && !pathname.getName().startsWith("_")
					&& !pathname.getName().startsWith(".");
			}
		};
		Comparator<File> compareAbsolutePathNames = new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				// null is illegal
				if (o1 == null || o2 == null) {
					throw new IllegalArgumentException("a file to compare was null");
				}
				// compare the absolute pathnames directly
				return o1.getAbsolutePath().compareTo(o2.getAbsolutePath());
			}
		};
		for (File radioBaseDir : radioBaseDirs) {
			if (radioBaseDir == null || !radioBaseDir.isDirectory() || !radioBaseDir.canRead()) {
				return new BroadcastData(CheckResultEnum.ERROR, "PROBLEM_WITH_RADIO_BASE_DIR");
			} else {
				try {
					File[] subDirs = radioBaseDir.listFiles(acceptOnlyDirectories);
					Arrays.sort(subDirs, compareAbsolutePathNames);
					for (File subDir : subDirs) {
						if (Utils.isDayOfWeekIncluded(dayOfWeek, subDir.getName())
							&& Utils.isHourOfDayIncluded(hourOfDay, subDir.getName())) {
							// planned broadcast found
							return new BroadcastData(CheckResultEnum.BROADCAST_FOUND, subDir.getAbsolutePath());
						}
					}
				} catch (Exception e) {
					// signal exception
					return new BroadcastData(CheckResultEnum.ERROR, Utils.getStackTrace(e));
				}
			}
		}
		// obviously no broadcast planned
		return new BroadcastData(CheckResultEnum.NO_BROADCAST_FOUND, null);
	}

}
