/*
 * Copyright 2013 Masato Nagai
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovyx.gbench

import org.junit.Test

class BenchmarkerTest {

    @Test void uncountOverhead() {
        BenchmarkContext.set(warmUpTime: -1, maxWarmUpTime: 30, measureCpuTime: true, quite: true)
        def benchmarker = new Benchmarker()
        def r = benchmarker.run("Uncount Overhead Test", {})
        // Best effort. Zero is the best but it is impossible.
        def acceptableRange = 0L..<50L
        assert acceptableRange.contains(r.cpu)
        assert acceptableRange.contains(r.real)
        println "Error Range: ${r}"
    }
}
