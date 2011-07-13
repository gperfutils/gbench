package gbench

class BenchmarkBuilderTest {
    
    def run() {
        def chars = ['g', 'r', 'o', 'o', 'v', 'y']
        def benchmark = new BenchmarkBuilder()
        benchmark.run times: 1000, {
            with 'concat', { 
                def s = ''
                for (c in chars) {
                    s.concat c    
                }
            }
            with '+=', { 
                def s = ''
                for (c in chars) {
                    s += c    
                }
            }
            with 'string builder', {
                def sb = new StringBuilder()    
                for (c in chars) {
                    sb << c    
                }
                sb.toString() 
            }
            with 'join', {
                chars.join()
            }
        }
        benchmark.sort().println()
    }
    
    static void main(args) {
        new BenchmarkBuilderTest().run()
    }
}
