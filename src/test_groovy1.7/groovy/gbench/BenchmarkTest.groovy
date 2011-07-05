package gbench

import groovy.lang.Singleton;
import gbench.BenchmarkTestUtils as U

@Benchmark
def useDefaultHandler() {
    U.simulateOperation()
}
def testUseDefaultHandler() {
    U.defaultAssert(U.firstLine(U.callAndGetStdout({useDefaultHandler()})))
}

@Singleton
class CustomBenchmarkHandler implements Benchmark.BenchmarkHandler {
    String result
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

testUseDefaultHandler()
testUseCustomHandler()

new ClassBenchmarkTest().run()
new MethodBenchmarkTest().run()
new BenchmarkBuilderTest().run()