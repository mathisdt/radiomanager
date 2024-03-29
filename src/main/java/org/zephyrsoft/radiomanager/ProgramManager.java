package org.zephyrsoft.radiomanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Manages the broadcast program table and supports the output as HTML.
 */
public class ProgramManager {

	private static final String firstColWidth = "5%";
	private static final String normalColWidth = "10%";

	private static final String[] weekdays = { "Mo", "Di", "Mi", "Do", "Fr", "Sa", "So" }; // monday is first!
	private static final String[] hours = { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11",
		"12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" };

	/**
	 * create the webradio program table in HTML
	 */
	public static String getProgramTableAsHtml(final File... radioBaseDirs) {
		StringBuilder ret = new StringBuilder();

		ret.append("""
			<style type="text/css">
			<!--
			.c1 {
			    font-weight:bold;
			    font-size:100%;
			    padding:2px;
			    border-bottom:1px solid grey;
			    border-right:1px solid grey;
			    vertical-align:top;
			}
			.c2 {
			    font-weight:normal;
			    font-size:90%;
			    padding:2px;
			    border-bottom:1px solid grey;
			    border-right:1px solid grey;
			    vertical-align:top;
			}
			-->
			</style>
			""");

		ret.append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n<tr><td class=\"c1\" style=\"width:"
			+ firstColWidth + "\">&nbsp;</td>");
		for (String day : weekdays) {
			ret.append("<td class=\"c1\" style=\"width:" + normalColWidth + "\">" + day + "</td>");
		}
		ret.append("</tr>\n");
		for (String hour : hours) {
			ret.append("<tr>");
			ret.append("<td class=\"c1\">" + hour + "</td>");
			for (String day : weekdays) {
				ret.append("<td class=\"c2\">");
				BroadcastData broadcastData = CheckForBroadcast.doCheckForBroadcast(day, hour, radioBaseDirs);
				if (broadcastData.getResultType() == CheckResultEnum.BROADCAST_FOUND) {
					ret.append(getInfoFileContent(broadcastData.getResultText()));
				} else {
					ret.append("&nbsp;");
				}
				ret.append("</td>");
			}
			ret.append("</tr>\n");
		}
		ret.append("</table>");

		return ret.toString();
	}

	public static String getRestOfDayAsHtml(final File... radioBaseDirs) {
		StringBuilder ret = new StringBuilder();

		ret.append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
		GregorianCalendar calendar = new GregorianCalendar();
		int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
		String currentWeekDay = getWeekdayAsString(calendar.get(Calendar.DAY_OF_WEEK));
		calendar.add(Calendar.DATE, 1);
		String nextWeekDay = getWeekdayAsString(calendar.get(Calendar.DAY_OF_WEEK));
		final int minLines = 5;
		int lines = 0;
		for (String hour : hours) {
			int hourAsInt = Integer.parseInt(hour);
			if (hourAsInt < currentHour) {
				continue;
			}
			ret.append("<tr>");
			boolean isCurrentHour = hourAsInt == currentHour;
			ret.append("<td class=\"" + (isCurrentHour ? "c1" : "n1") + "\">" + hour + " Uhr</td>");
			ret.append("<td class=\"" + (isCurrentHour ? "c2" : "n2") + "\">");
			BroadcastData broadcastData = CheckForBroadcast.doCheckForBroadcast(currentWeekDay, hour, radioBaseDirs);
			if (broadcastData.getResultType() == CheckResultEnum.BROADCAST_FOUND) {
				ret.append(getInfoFileContent(broadcastData.getResultText()));
			} else {
				ret.append("&nbsp;");
			}
			ret.append("</td>");
			ret.append("</tr>\n");
			lines++;
		}
		if (lines < minLines) {
			// add hours from next day to meet the defined minimum
			for (String hour : hours) {
				if (lines >= minLines) {
					break;
				}
				int hourAsInt = Integer.parseInt(hour);
				ret.append("<tr>");
				boolean isCurrentHour = hourAsInt == currentHour;
				ret.append("<td class=\"" + (isCurrentHour ? "c1" : "n1") + "\">" + hour + " Uhr</td>");
				ret.append("<td class=\"" + (isCurrentHour ? "c2" : "n2") + "\">");
				BroadcastData broadcastData = CheckForBroadcast.doCheckForBroadcast(nextWeekDay, hour, radioBaseDirs);
				if (broadcastData.getResultType() == CheckResultEnum.BROADCAST_FOUND) {
					ret.append(getInfoFileContent(broadcastData.getResultText()));
				} else {
					ret.append("&nbsp;");
				}
				ret.append("</td>");
				ret.append("</tr>\n");
				lines++;
			}
		}
		ret.append("</table>");

		return ret.toString();
	}

	private static String getWeekdayAsString(final int weekdayFromCalendar) {
		return switch (weekdayFromCalendar) {
			case Calendar.MONDAY -> weekdays[0];
			case Calendar.TUESDAY -> weekdays[1];
			case Calendar.WEDNESDAY -> weekdays[2];
			case Calendar.THURSDAY -> weekdays[3];
			case Calendar.FRIDAY -> weekdays[4];
			case Calendar.SATURDAY -> weekdays[5];
			case Calendar.SUNDAY -> weekdays[6];
			default -> throw new IllegalArgumentException();

		};
	}

	private static String getInfoFileContent(final String path) {
		String ret = "Sendung ohne Titel";
		File infoFile = new File(path + File.separator + "info.txt");
		if (infoFile.exists() && infoFile.canRead()) {
			try (BufferedReader in = new BufferedReader(new FileReader(infoFile))) {
				StringBuilder content = new StringBuilder((int) infoFile.length());
				while (in.ready()) {
					content.append(in.readLine());
					content.append('\n');
				}
				if (content.length() > 0) {
					ret = content.toString();
					// strip last newline
					if (ret.substring(ret.length() - 1, ret.length()).equals("\n")) {
						ret = ret.substring(0, ret.length() - 1);
					}
				}
			} catch (Exception e) {
				System.err.println("error while reading " + infoFile.getAbsolutePath());
				e.printStackTrace();
			}
		}
		return ret;
	}

	public static void main(final String[] args) {
		File[] radioBaseDirs = new File[args.length];
		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				radioBaseDirs[i] = new File(args[i]);
			}
			String data = getRestOfDayAsHtml(radioBaseDirs);
			System.out.println(data == null ? "" : data);
			Utils.exit(data == null ? 1 : 0);
		}
	}

}
