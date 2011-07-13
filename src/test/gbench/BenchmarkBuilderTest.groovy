package gbench

import java.lang.management.*

class BenchmarkBuilderTest {
    
    private isCpuTimeSupported() {
        def mxBean = ManagementFactory.getThreadMXBean();
        return ManagementFactory.threadMXBean.isCurrentThreadCpuTimeSupported()
    }
    
    def testStandard() {
        def benchmarker = new BenchmarkBuilder()    
        benchmarker.run {
            with 'foo', {
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
        }.each { bm ->
            assert bm.label == 'foo'   
            assert bm.time.real > 0 
            if (isCpuTimeSupported()) {
                assert bm.time.cpu > 0 
                assert bm.time.user > 0 
                assert bm.time.system > 0 
            } 
        }.prettyPrint()
    }
    
    def testMultiple() {
        def benchmarker = new BenchmarkBuilder()
        benchmarker.run {
            with 'foo', {
                Thread.sleep(100)
            }    
            with 'bar', {
                Thread.sleep(50)
            }
        }
        assert benchmarker.benchmarks*.label == ['foo', 'bar']
        benchmarker.benchmarks.sort()
        assert benchmarker.benchmarks*.label == ['bar', 'foo']
    }
    
    def testUnlabeled() {
        def benchmarker = new BenchmarkBuilder()
        benchmarker.run {
            with '', {
            }
        }
        assert benchmarker.benchmarks[0].label == ''
    }
    
    
    def run() {
        testStandard()
        testMultiple()
        testUnlabeled()
    }
    
    static void main(args) {
        new BenchmarkBuilderTest().run()
    }
}
