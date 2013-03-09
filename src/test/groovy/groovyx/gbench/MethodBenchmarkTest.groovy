package groovyx.gbench

import groovyx.gbench.BenchmarkTestUtilities as U

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

/* $if version >= 1.8.0 $ */
    @Benchmark({System.out.println("${klass} of ${method}\t${time}")})
    def useClosureHandler() {
        U.simulateOperation()
    }
    @Test void testUseClosureHandler() {
        U.customAssert(U.firstLine(U.callAndGetStdout({useClosureHandler()})))
    }

    @Benchmark({System.out.println("${klass} of ${method}\t${time}")})
    def useClosureHandlerWithDupArgs(method, time) {
        U.simulateOperation()
    }
    @Test void testUseClosureHandlerWithDupArgs() {
        U.customAssert(U.firstLine(U.callAndGetStdout({useClosureHandlerWithDupArgs('foo', 'bar')})))
    }
/* $endif$ */

}
