package gbench

import gbench.BenchmarkTestUtils as U

@Benchmark
class ClassBenchmarkTest {
    
    def useClassBenchmark() {
        U.simulateOperation()
    }
    def testUseClassBenchmark() {
        U.defaultAssert(U.firstLine(U.callAndGetStdout({useClassBenchmark()})))
    }
   
    @Benchmark({System.out.println("${klass} ${method} = ${time}")})
    def useMethodBenchmark() {
        U.simulateOperation()
    }
    def testUseMethodBenchmark() {
        U.customAssert(U.firstLine(U.callAndGetStdout({useMethodBenchmark()})))
    }
    
    def run() {
        testUseClassBenchmark()
        testUseMethodBenchmark()
    }
}