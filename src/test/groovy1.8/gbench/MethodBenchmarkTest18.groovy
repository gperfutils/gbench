package groovyx.gbench

import groovyx.gbench.BenchmarkTestUtilities as U

import org.junit.Test


class MethodBenchmarkTest18 {
    
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
}
