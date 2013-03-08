package groovyx.gbench

import org.junit.Test
import static org.junit.Assert.*

import groovyx.gbench.BenchmarkTestUtilities as U

class BenchmarkWarmUpTest {

    @Test void timeout() {
        BenchmarkContext.set(warmUpTime: -1, maxWarmUpTime: 1)

        def _stable = BenchmarkWarmUp.metaClass.'static'.stable
        BenchmarkWarmUp.metaClass.'static'.stable = { Map current, Map last ->
              return false
        }

        def error
        // when the benchmark took maxWarmUpTime while stabilizing
        error = U.firstLine(U.callAndGetStderr({
            BenchmarkWarmUp.run({ Thread.sleep(100) }, 1)
        }))
        assert "[WARN] Timed out waiting for benchmark to be stable" == error

        BenchmarkWarmUp.metaClass.'static'.stable = { Map current, Map last ->
              return true
        }

        // when the benchmark took maxWarmUpTime with the first run
        error = U.firstLine(U.callAndGetStderr({
            BenchmarkWarmUp.run({ Thread.sleep(1000) }, 1)
        }))
        assert "[WARN] Timed out waiting for benchmark to be stable" == error

        // when the benchmark is stable before it takes maxWarmUpTime
        error = U.firstLine(U.callAndGetStderr({
            BenchmarkWarmUp.run({ Thread.sleep(100) }, 1)
        }))
        assert null == error

        BenchmarkWarmUp.metaClass.'static'.stable = _stable
    }
}
