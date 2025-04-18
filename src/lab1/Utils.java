package lab1;

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
	
}
