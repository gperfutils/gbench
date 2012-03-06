package gbench


import java.lang.management.*

import org.junit.Test

import static org.junit.Assert.*

class BenchmarkBuilderTest {
    
    @Test void testDefaultOptions() {
        def bb = new BenchmarkBuilder()
        bb.options = [:]
        assert BenchmarkBuilder.AUTO == bb.options.warmUpTime
        assert true == bb.options.measureCpuTime
        assert false == bb.options.quiet
        assert false == bb.options.verbose
    }
    
    @Test void testOptions() {
        def bb = new BenchmarkBuilder()
        bb.options = [ warmUpTime: 1, measureCpuTime: false, 
            quiet: true, verbose: true ]
        assert 1 == bb.options.warmUpTime
        assert false == bb.options.measureCpuTime
        assert true == bb.options.quiet
        assert true == bb.options.verbose
    }
    
    @Test void testStandard() {
        def benchmarks = new BenchmarkBuilder().run {
            Thread.sleep(1000)
        }
        benchmarks.each { bm ->
            assert bm.label == 'foo'   
            assert bm.time.real > 0 
            if (ManagementFactory.threadMXBean.isCurrentThreadCpuTimeSupported()) {
                assert bm.time.cpu > 0 
                assert bm.time.user > 0 
                assert bm.time.system > 0 
            } 
        }
        benchmarks.prettyPrint()
    }
    
    @Test void testMultiple() {
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
    
    @Test void testPrettyPrint() {
       def benchmarker = new BenchmarkBuilder() 
       benchmarker.benchmarks = new BenchmarkList(options: [measureCpuTime: true])
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
       pw.println('       user  system     cpu     real')
       pw.println()
       pw.println('foo  300000  200000  500000  1000000')
       pw.println('bar  450000  300000  700000  1500000')
       pw.flush()
       
       assertEquals(sw.toString(), benchmarker.toString())
    }
}
