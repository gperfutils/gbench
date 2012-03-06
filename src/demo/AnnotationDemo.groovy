// System.properties."gbench.measureCpuTime" = 'false'
// System.properties."gbench.defaultHandler" = "println(method + 'of' + class + ': ' + time)"

@gbench.Benchmark
void task() {
    Thread.sleep(1000)
}
task()