package groovyx.gbench

import groovyx.gbench.BenchmarkTestUtilities as U

import org.junit.Test

import groovy.lang.Singleton

@Benchmark
def useDefaultHandler() {
    U.simulateOperation()
}
void testUseDefaultHandler() {
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
void testUseCustomHandler() {
    useCustomHandler()
    U.customAssert(CustomBenchmarkHandler.instance.result)
}