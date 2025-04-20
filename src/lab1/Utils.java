package lab1;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
	
	// Prints name of caller func
	public static void logFunc() {
		String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
	    System.out.println(methodName);
	}
	
	public static void logFunc(String msg) {
		String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
	    System.out.println(methodName + " " + msg);
	}
	
	public static void logClass(Object c) {
		System.out.println(c.getClass().getSimpleName());
	}
	
	public static void log(String msg) {
		System.out.println(msg);
	}
	
	// ------------
	
	public static boolean isValidIP(String ip) {
        String regex =
            "^((25[0-5]|2[0-4]\\d|[0-1]?\\d\\d?)\\.){3}" +
            "(25[0-5]|2[0-4]\\d|[0-1]?\\d\\d?)$";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(ip);
        return matcher.matches();
    }
	
}
