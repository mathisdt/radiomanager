package org.zephyrsoft.radiomanager;

import java.io.*;
import java.util.*;

/**
 * Manages the broadcast program table and supports the output as HTML.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class ProgramManager {
	
	private static String[] weekdays = { "Mo", "Di", "Mi", "Do", "Fr", "Sa", "So" };
	private static String[] hours = { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12",
		"13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" };
	
	/**
	 * create the webradio program table in HTML
	 */
	public static String getProgramTableAsHtml(File radioBaseDir) {
		StringBuilder ret = new StringBuilder();
		
		ret.append("<table>\n<tr><td>&nbsp;</td>");
		for (String day : weekdays) {
			ret.append("<td>" + day + "</td>");
		}
		ret.append("</tr>\n");
		for (String hour : hours) {
			ret.append("<tr>");
			ret.append("<td>" + hour + "</td>");
			for (String day : weekdays) {
				ret.append("<td>");
				BroadcastData broadcastData = CheckForBroadcast.doCheckForBroadcast(radioBaseDir, day, hour);
				if (broadcastData.getResultType() == CheckResultEnum.BROADCAST_FOUND) {
					ret.append("<small>" + getInfoFileContent(broadcastData.getResultText()) + "</small>");
				} else {
					ret.append("<small>&nbsp;</small>");
				}
				ret.append("</td>");
			}
			ret.append("</tr>\n");
		}
		ret.append("</table>");
		
		return ret.toString();
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
					content.append("\n");
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
