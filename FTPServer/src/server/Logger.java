package server;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

	private static DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:sss");

	public static void out(String msg) {
		System.out.println(String.format("%s - %s", fmt.format(new Date()), msg));
	}

	public static void err(String msg) {
		System.err.println(String.format("%s - %s", fmt.format(new Date()), msg));
	}

	private Logger() {

	}
}