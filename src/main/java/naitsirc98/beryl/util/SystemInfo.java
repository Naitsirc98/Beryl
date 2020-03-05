package naitsirc98.beryl.util;

public final class SystemInfo {

	private SystemInfo() {}

	private static final Runtime RUNTIME = Runtime.getRuntime();
	
	public static long totalMemory() {
		return RUNTIME.totalMemory();
	}
	
	public static long freeMemory() {
		return RUNTIME.freeMemory();
	}
	
	public static long maxMemory() {
		return RUNTIME.maxMemory();
	}
	
	public static long memoryUsed() {
		return totalMemory() - freeMemory();
	}
	
	public static int processorCount() {
		return RUNTIME.availableProcessors();
	}
}
