package gbench;

import java.lang.management.ManagementFactory

class BenchmarkUtilities {

    static boolean isCpuTimeEnabled() {
        if (Boolean.valueOf(System.properties['gbench.cputime'])) {
            return ManagementFactory.threadMXBean.isCurrentThreadCpuTimeSupported()
        }
        return false
    }
    
    static boolean isTraceEnabled() {
        return Boolean.valueOf(System.properties['gbench.trace'])
    }
}
