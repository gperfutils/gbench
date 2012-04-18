package groovyx.gbench;

public class BenchmarkSystem {
    
    public static boolean isMeasureCpuTime() {
        Boolean.valueOf(System.properties.'gbench.measureCpuTime'?: true)
    }

}
