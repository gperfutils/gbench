package groovyx.gbench

class BenchmarkStaticExtension {
    
    static BenchmarkList benchmark(Object selfType, Map args = [:], Closure c) {
        new BenchmarkBuilder().run(args, c)
    }
    
}
