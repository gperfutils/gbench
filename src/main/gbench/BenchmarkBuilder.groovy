/*
 * Copyright 2011 Nagai Masato
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
package gbench;

/**
 * <p>A builder for benchmarking.</p>
 * <p>For example, you can benchmark character concatenation like the following:</p>
 * <pre><code>
 * def chars = ['g', 'r', 'o', 'o', 'v', 'y']
 * def benchmark = new BenchmarkBuilder()
 * benchmark.run times: 1000, {
 *     with 'concat', { 
 *         def s = ''
 *         for (c in chars) {
 *             s.concat c    
 *         }
 *     }
 *     with '+=', { 
 *         def s = ''
 *         for (c in chars) {
 *             s += c    
 *         }
 *     }
 *     with 'string builder', {
 *         def sb = new StringBuilder()    
 *         for (c in chars) {
 *             sb << c    
 *         }
 *         sb.toString() 
 *     }
 *     with 'join', {
 *         chars.join()
 *     }
 *  }
 *  println benchmark.sort()
 * <code></pre>
 * then output will be like:
 * <pre>
 *                     time
 * join             5000000
 * string builder   7500000
 * +=              10000000
 * concat          12500000
 * </pre>
 * 
 * @author Nagai Masato
 *
 */
class BenchmarkBuilder {
    
    private def benchmarks
    private int times
    
    def run(Map args=[:], Closure clos) {
        benchmarks = []
        this.times = args.times ? args.times : 1    
        clos.delegate = this
        clos()
        return this
    }
   
    def with(String label, Closure clos) {
        measure(1, clos) // avoid large overhead of the first closure call
        def benchmark = [label: label, time: measure(times, clos)]
        benchmarks << benchmark
        return this
    }
    
    def each(Closure clos) {
        benchmarks.each(clos)
        return this
    }
    
    def sort(Closure clos = { lhs, rhs -> lhs.time <=> rhs.time }) {
        benchmarks.sort(clos)    
        return this
    }
    
    @Override
    String toString() {
        def wtable = benchmarks.inject([label:1, time: 'time'.length()]) { wtable, bm -> 
            wtable.label = Math.max(wtable.label, bm.label.length()) 
            wtable.time = Math.max(wtable.time, bm.time.toString().length())
            wtable
        }
        def nl = System.getProperty('line.separator')
        def sb = new StringBuilder()
        sb << String.format("%${wtable.label}s\t%${wtable.time}s", ' ', 'time')
        benchmarks.each { bm ->
            sb << nl 
            sb << String.format("%-${wtable.label}s\t%${wtable.time}d", bm.label, bm.time)
        }
        return sb.toString()
    }
    
    private def measure(times, Closure clos) {
        def time = 0
        for (i in 0..<times) {
            def b = System.nanoTime()
            clos()
            def a = System.nanoTime()
            time += a - b
        }
        return time
    }
}

