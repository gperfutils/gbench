package groovyx.gbench

import org.junit.Test
import static org.junit.Assert.*

import groovyx.gbench.BenchmarkTestUtilities as U

class BenchmarkWarmUpTest {

    @Test void timeout() {
        BenchmarkContext.set(warmUpTime: -1, maxWarmUpTime: 1)

        def _stable = BenchmarkWarmUp.metaClass.'static'.stable
        BenchmarkWarmUp.metaClass.'static'.stable = {
            BenchmarkMeasure.Result current, BenchmarkMeasure.Result last ->
            return false
        }

        def error
        // when the benchmark took maxWarmUpTime while stabilizing
        error = U.firstLine(U.callAndGetStderr({
            BenchmarkWarmUp.run("test", { Thread.sleep(100) }, 1)
        }))
        assert "WARNING: Timed out waiting for \"test\" to be stable" == error

        BenchmarkWarmUp.metaClass.'static'.stable = {
            BenchmarkMeasure.Result current, BenchmarkMeasure.Result last ->
            return true
        }

        // when the benchmark took maxWarmUpTime with the first run
        error = U.firstLine(U.callAndGetStderr({
            BenchmarkWarmUp.run("test", { Thread.sleep(1000) }, 1)
        }))
        assert "WARNING: Timed out waiting for \"test\" to be stable" == error

        // when the benchmark is stable before it takes maxWarmUpTime
        error = U.firstLine(U.callAndGetStderr({
            BenchmarkWarmUp.run("test", { Thread.sleep(100) }, 1)
        }))
        assert null == error

        BenchmarkWarmUp.metaClass.'static'.stable = _stable
    }
}
