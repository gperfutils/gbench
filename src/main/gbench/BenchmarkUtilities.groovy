package gbench;

import java.lang.management.ManagementFactory

class BenchmarkUtilities {

    static boolean isCpuTimeEnabled() {
        if ('on' == (System.properties['gbench.cputime']?: 'on')) {
            return ManagementFactory.threadMXBean.isCurrentThreadCpuTimeSupported()
        }
        return false
    }
    
    static boolean isTraceEnabled() {
        return 'on' == (System.properties['gbench.trace']?: 'off')
    }
}
