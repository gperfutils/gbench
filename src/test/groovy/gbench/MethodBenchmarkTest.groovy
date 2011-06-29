package gbench

import gbench.BenchmarkTestUtils as U

import groovy.lang.Singleton;

class MethodBenchmarkTest {
    
    @Benchmark
    def useDefaultHandler() {
        U.simulateOperation()
    }
    def testUseDefaultHandler() {
        U.defaultAssert(U.firstLine(U.callAndGetStdout({useDefaultHandler()})))
    }

    @Singleton
    class CustomBenchmarkHandler implements Benchmark.BenchmarkHandler {
        def result
        void handle(klass, method, time) {
            result = "${klass} ${method} = ${time}"
        }
    }
    @Benchmark(CustomBenchmarkHandler.class)
    def useCustomHandler() {
        U.simulateOperation()
    }
    def testUseCustomHandler() {
        useCustomHandler()
        U.customAssert(CustomBenchmarkHandler.instance.result)
    }

    @Benchmark({System.out.println("${klass} ${method} = ${time}")})
    def useClosureHandler() {
        U.simulateOperation()
    }
    def testUseClosureHandler() {
        U.customAssert(U.firstLine(U.callAndGetStdout({useClosureHandler()})))
    }

    @Benchmark({System.out.println("${klass} ${method} = ${time}")})
    def useClosureHandlerWithDupArgs(method, time) {
        U.simulateOperation()
    }
    def testUseClosureHandlerWithDupArgs() {
        U.customAssert(U.firstLine(U.callAndGetStdout({useClosureHandlerWithDupArgs('foo', 'bar')})))
    }

    void run() {
        testUseDefaultHandler()
        testUseCustomHandler()
        testUseClosureHandler()
        testUseClosureHandlerWithDupArgs()
    }
}
