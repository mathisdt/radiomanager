package org.zephyrsoft.radiomanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Manages the broadcast program table and supports the output as HTML.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class ProgramManager {
	
	private static final String firstColWidth = "5%";
	private static final String normalColWidth = "10%";
	
	private static final String[] weekdays = {"Mo", "Di", "Mi", "Do", "Fr", "Sa", "So"}; // monday is first!
	private static final String[] hours = {"00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11",
		"12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23"};
	
	/**
	 * create the webradio program table in HTML
	 */
	public static String getProgramTableAsHtml(File... radioBaseDirs) {
		StringBuilder ret = new StringBuilder();
		
		ret.append("<style type=\"text/css\">\n" + "<!--\n" + ".c1 {\n" + "font-weight:bold;\n" + "font-size:100%;\n"
			+ "padding:2px;\n" + "border-bottom:1px solid grey;\n" + "border-right:1px solid grey;\n"
			+ "vertical-align:top;\n" + "}\n" + ".c2 {\n" + "font-weight:normal;\n" + "font-size:90%;\n"
			+ "padding:2px;\n" + "border-bottom:1px solid grey;\n" + "border-right:1px solid grey;\n"
			+ "vertical-align:top;\n" + "}\n" + "-->\n" + "</style>\n");
		
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
	
	public static String getRestOfDayAsHtml(File... radioBaseDirs) {
		StringBuilder ret = new StringBuilder();
		
		ret.append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
		GregorianCalendar calendar = new GregorianCalendar();
		int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
		String currentWeekDay = getWeekdayAsString(calendar.get(Calendar.DAY_OF_WEEK));
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
		}
		ret.append("</table>");
		
		return ret.toString();
	}
	
	private static String getWeekdayAsString(int weekdayFromCalendar) {
		switch (weekdayFromCalendar) {
			case Calendar.MONDAY:
				return weekdays[0];
			case Calendar.TUESDAY:
				return weekdays[1];
			case Calendar.WEDNESDAY:
				return weekdays[2];
			case Calendar.THURSDAY:
				return weekdays[3];
			case Calendar.FRIDAY:
				return weekdays[4];
			case Calendar.SATURDAY:
				return weekdays[5];
			case Calendar.SUNDAY:
				return weekdays[6];
			default:
				throw new IllegalArgumentException();
		}
	}
	
	private static String getInfoFileContent(String path) {
		String ret = "Sendung ohne Titel";
		File infoFile = new File(path + File.separator + "info.txt");
		if (infoFile.exists() && infoFile.canRead()) {
			try {
				BufferedReader in = new BufferedReader(new FileReader(infoFile));
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
	
	public static void main(String[] args) {
		File radioBaseDir = null;
		if (args != null && args.length >= 1) {
			radioBaseDir = new File(args[0]);
		}
		String data = getProgramTableAsHtml(radioBaseDir);
		System.out.println(data);
	}
	
}
