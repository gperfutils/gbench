package gbench

import gbench.BenchmarkTestUtilities as U

import groovy.lang.Singleton

import org.junit.Test


class MethodBenchmarkTest {
    
    @Benchmark
    def useDefaultHandler() {
        U.simulateOperation()
    }
    @Test void testUseDefaultHandler() {
        U.defaultAssert(U.firstLine(U.callAndGetStdout({useDefaultHandler()})))
    }

    @Singleton
    class CustomBenchmarkHandler implements Benchmark.BenchmarkHandler {
        def result
        void handle(klass, method, time) {
            result = "${klass} of ${method}\t${time}"
        }
    }
    @Benchmark(CustomBenchmarkHandler.class)
    def useCustomHandler() {
        U.simulateOperation()
    }
    @Test void testUseCustomHandler() {
        useCustomHandler()
        U.customAssert(CustomBenchmarkHandler.instance.result)
    }
}
