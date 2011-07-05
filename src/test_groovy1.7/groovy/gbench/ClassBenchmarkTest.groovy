package gbench

import groovy.lang.Singleton;
import gbench.BenchmarkTestUtils as U

@Benchmark
class ClassBenchmarkTest {
    
    def useClassBenchmark() {
        U.simulateOperation()
    }
    def testUseClassBenchmark() {
        U.defaultAssert(U.firstLine(U.callAndGetStdout({useClassBenchmark()})))
    }
  
    @Singleton
    class CustomBenchmarkHandler implements Benchmark.BenchmarkHandler {
        String result
        void handle(klass, method, time) {
            result = "${klass} ${method} = ${time}"
        }
    }
    @Benchmark(CustomBenchmarkHandler.class)
    def useMethodBenchmark() {
        U.simulateOperation()
    }
    def testUseMethodBenchmark() {
        useMethodBenchmark()
        U.customAssert(CustomBenchmarkHandler.instance.result)
    }
    
    def run() {
        testUseClassBenchmark()
        testUseMethodBenchmark()
    }
}