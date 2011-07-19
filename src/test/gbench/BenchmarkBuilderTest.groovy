package gbench

import java.lang.management.*

class BenchmarkBuilderTest {
    
    private isCpuTimeSupported() {
        def mxBean = ManagementFactory.getThreadMXBean();
        return ManagementFactory.threadMXBean.isCurrentThreadCpuTimeSupported()
    }
    
    def testStandard() {
        def benchmarker = new BenchmarkBuilder()    
        def benchmarks = benchmarker.run {
            foo {
                // gain user-time
                def sum = 0
                (0..100000).each {
                    sum += it    
                }
                
                // gain system-time
                def file = new File('.')
                1000.times {
                    file.list()
                }
            }    
        }
        benchmarks.each { bm ->
            assert bm.label == 'foo'   
            assert bm.time.real > 0 
            if (isCpuTimeSupported()) {
                assert bm.time.cpu > 0 
                assert bm.time.user > 0 
                assert bm.time.system > 0 
            } 
        }
        benchmarks.prettyPrint()
    }
    
    def testMultiple() {
        def benchmarker = new BenchmarkBuilder()
        def benchmarks = benchmarker.run {
            foo {
                Thread.sleep(100)
            }    
            bar {
                Thread.sleep(50)
            }
        }
        assert benchmarks*.label == ['foo', 'bar']
        assert benchmarks.sort()*.label == ['bar', 'foo']
    }
    
    def testPrettyPrint() {
       def benchmarker = new BenchmarkBuilder() 
       benchmarker.benchmarks = []
       benchmarker.benchmarks << [
               label: 'foo',
               time: new BenchmarkTime(user:300000, system:200000, cpu:500000, real:1000000)
           ]
       benchmarker.benchmarks << [
               label: 'bar', 
               time: new BenchmarkTime(user:450000, system:300000, cpu:700000, real:1500000)
           ]
       
       def sw = new StringWriter()
       def pw = new PrintWriter(sw)
       pw.println('   \t  user\tsystem\t   cpu\t   real')
       pw.println()
       pw.println('foo\t300000\t200000\t500000\t1000000')
       pw.println('bar\t450000\t300000\t700000\t1500000')
       pw.flush()
       
       assert benchmarker.toString() == sw.toString()
    }
    
    def run() {
        testStandard()
        testMultiple()
        testPrettyPrint()
    }
    
    static void main(args) {
        new BenchmarkBuilderTest().run()
    }
}
