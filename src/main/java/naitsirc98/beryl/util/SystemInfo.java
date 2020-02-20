package naitsirc98.beryl.util;

public final class SystemInfo {

	private SystemInfo() {}

	private static final Runtime RUNTIME = Runtime.getRuntime();
	
	public static long getTotalMemory() {
		return RUNTIME.totalMemory();
	}
	
	public static long getFreeMemory() {
		return RUNTIME.freeMemory();
	}
	
	public static long getMaxMemory() {
		return RUNTIME.maxMemory();
	}
	
	public static long getMemoryUsed() {
		return getTotalMemory() - getFreeMemory();
	}
	
	public static int getProcessors() {
		return RUNTIME.availableProcessors();
	}

}
