// System.properties."gbench.measureCpuTime" = 'false'
// System.properties."gbench.defaultHandler" = "println(method + 'of' + class + ': ' + time)"

@groovyx.gbench.Benchmark
void task() {
    Thread.sleep(1000)
}
task()