package gbench

class BenchmarkBuilderTest {
    
    def run() {
        def strings = ['GBench', ' = ', '@Benchmark Annotation', ' + ', 'BenchmarkBuilder']
        def benchmark = new BenchmarkBuilder()
        benchmark.run times: 1000, {
            with '+=', { 
                def s = ''
                for (string in strings) {
                    s += string    
                }
            }
            with 'concat', { 
                def s = ''
                for (string in strings) {
                    s.concat string    
                }
            }
            with 'string builder', {
                def sb = new StringBuilder()    
                for (string in strings) {
                    sb << string    
                }
                sb.toString() 
            }
            with 'join', {
                strings.join()
            }
        }
        
        new File('benchmark.csv').withWriter { file ->
            file.writeLine 'label,time(ms)'
            benchmark.sort().each { bm ->
                file.writeLine "${bm.label},${bm.time / 1000000}"
            }
        }
        println new File('benchmark.csv').text
    }
    
    static void main(args) {
        new BenchmarkBuilderTest().run()
    }
}
