package groovyx.gbench

class BenchmarkExtension {
    
    static BenchmarkList benchmark(Closure self, Map args = [:]) {
        new BenchmarkBuilder().run(args, self)    
    }

}
