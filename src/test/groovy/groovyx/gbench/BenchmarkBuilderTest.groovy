package groovyx.gbench


import java.lang.management.*

import org.junit.Test

import java.util.concurrent.CountDownLatch

import static org.junit.Assert.*

class BenchmarkBuilderTest {

    @Test void testDefaultOptions() {
        def expected = [
            warmUpTime: BenchmarkSystem.warmUpTime,
            maxWarmUpTime: BenchmarkSystem.maxWarmUpTime,
            measureCpuTime: BenchmarkSystem.measureCpuTime,
            quiet: BenchmarkSystem.quiet,
            verbose: BenchmarkSystem.verbose ]
        def actual = [:]
        new Thread().start {
            def bb = new BenchmarkBuilder()
            bb.options = [:]
            actual.putAll(BenchmarkContext.get().findAll { expected.containsKey(it.key) })
        }.join()
        assert expected == actual
    }

    @Test void testOptions() {
        def expected = [ 
            warmUpTime: 1,
            maxWarmUpTime: 2,
            measureCpuTime: false,
            quiet: true,
            verbose: true ]
        def actual = [:]
        new Thread().start {
            def bb = new BenchmarkBuilder()
            bb.options = [
                warmUpTime: 1,
                maxWarmUpTime: 2,
                measureCpuTime: false,
                quiet: true,
                verbose: true ]
            actual.putAll(BenchmarkContext.get().findAll { expected.containsKey(it.key) })
        }.join()
        assert expected == actual
    }
    
    @Test void testSingle() {
        def benchmarks = new BenchmarkBuilder().run(quiet: true) {
            'foo' {
                Thread.sleep(10)
            }
        }
        assert benchmarks.size() == 1
        assert benchmarks[0].label == 'foo'
        assert benchmarks[0].time.real >= 10 * 1000 * 1000
    }

    @Test void testSingleUnlabeled() {
        def benchmarks = new BenchmarkBuilder().run(quiet: true) {
            Thread.sleep(10)
        }
        assert benchmarks.size() == 1
        assert benchmarks[0].label == ''
        assert benchmarks[0].time.real >= 10 * 1000 * 1000
    }
    
    @Test void testMultiple() {
        def benchmarks = new BenchmarkBuilder().run(quiet: true) {
            foo {
                Thread.sleep(20)
            }
            bar {
                Thread.sleep(10)
            }
        }
        assert benchmarks.size() == 2
        assert benchmarks*.label == ['foo', 'bar']
        assert benchmarks.sort()*.label == ['bar', 'foo']
    }

    @Test void testPrettyPrint() {
       def benchmarker = new BenchmarkBuilder()
       benchmarker.options = [measureCpuTime: true]
       benchmarker.benchmarks = new BenchmarkList()
       benchmarker.benchmarks << [
               label: 'foo',
               time: new BenchmarkTime(user:300, cpu:500, real:501)
           ]
       benchmarker.benchmarks << [
               label: 'bar',
               time: new BenchmarkTime(user:450, cpu:700, real:701)
           ]

       def sw = new StringWriter()
       def pw = new PrintWriter(sw)
       pw.println('     user  system  cpu  real')
       pw.println()
       pw.println('foo   300     200  500   501')
       pw.println('bar   450     250  700   701')
       pw.flush()

       assert sw.toString() == benchmarker.toString()
    }
}
