package tests;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

public class TestMetrics {
    private static OperatingSystemMXBean osBean;

    public TestMetrics() {
        osBean = ManagementFactory.getOperatingSystemMXBean();
    }

    public static double getCpuLoad() {
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            return ((com.sun.management.OperatingSystemMXBean) osBean).getSystemCpuLoad() * 100;
        } else {
            throw new UnsupportedOperationException("CPU load monitoring not supported on this platform.");
        }
    }

    public static long getFreeMemory() {
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            return ((com.sun.management.OperatingSystemMXBean) osBean).getFreePhysicalMemorySize();
        } else {
            throw new UnsupportedOperationException("Memory monitoring not supported on this platform.");
        }
    }

    /**
    public static void main(String[] args) {
        TestMetrics test = new TestMetrics();

        System.out.printf("CPU Load: %.2f%%\n", test.getCpuLoad());
        System.out.printf("Free Physical Memory: %.2f MB\n", test.getFreeMemory() / (1024.0 * 1024.0));
    } */
}
