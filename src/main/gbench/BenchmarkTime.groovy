package gbench

@groovy.transform.ToString(includeNames = true)
class BenchmarkTime {
    long real
    long cpu
    long system
    long user
}
