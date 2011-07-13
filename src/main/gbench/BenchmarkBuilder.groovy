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

import java.lang.management.ManagementFactory

/**
 * <p>A builder for benchmarking.</p>
 * <p>For example, you can benchmark character concatenation like the following:</p>
 * <pre><code>
 * new BenchmarkBuilder().run(times: 1000, {
 *     def chars = ['g', 'r', 'o', 'o', 'v', 'y']
 *     with 'concat', {
 *         def s = ''
 *         for(c in chars){
 *             s.concat c
 *         }
 *     }
 *     with '+=', {
 *         def s= ''
 *         for(c in chars){
 *             s += c
 *         }
 *     }
 *     with 'stringbuilder',{
 *         def sb = new StringBuilder()
 *         for(c in chars){
 *             sb << c
 *         }
 *         sb.toString()
 *     }
 *     with 'join', { 
 *         chars.join() 
 *     }
 * }).sort().prettyPrint()
 * <code></pre>
 * then output will be like:
 * <pre>
 *                  user    system       cpu        real
 *
 * join            15600100         0  15600100    11799977
 * stringbuilder   15600100         0  15600100    16124097
 * +=              15600100         0  15600100    23323655
 * concat          15600100         0  15600100    37513352
 * </pre>
 * 
 * @author Nagai Masato
 *
 */
class BenchmarkBuilder {
    
    static class Benchmarks extends ArrayList {
        
        def sort() {
            return sort {lhs, rhs -> lhs.time.real <=> rhs.time.real }
        }
        
        @Override
        String prettyPrint(PrintWriter writer = new PrintWriter(System.out)) {
            def wids = 
                inject([
                    label: 1, 
                    user: 'user'.length(), 
                    system: 'system'.length(), 
                    cpu: 'cpu'.length(),
                    real: 'real'.length()
                ]) { wids, bm -> 
                    wids.label = Math.max(wids.label, bm.label.length()) 
                    wids.user = Math.max(wids.user, bm.time.user.toString().length())
                    wids.system = Math.max(wids.system, bm.time.system.toString().length())
                    wids.cpu = Math.max(wids.cpu, bm.time.cpu.toString().length())
                    wids.real = Math.max(wids.real, bm.time.real.toString().length())
                    wids
                }
            writer.printf(
                "%${wids.label}s\t%${wids.user}s\t%${wids.system}s\t%${wids.cpu}s\t%${wids.real}s",
                ' ', 'user', 'system', 'cpu', 'real'
            )
            writer.println()
            each { bm ->
                writer.println()
                writer.printf(
                    "%-${wids.label}s\t%${wids.user}d\t%${wids.system}d\t%${wids.cpu}d\t%${wids.real}d",
                    bm.label, bm.time.user, bm.time.system, bm.time.cpu, bm.time.real
                )
            }
            writer.println()
            writer.flush()
        }
    }
    
    Benchmarks benchmarks
    int times
    
    Benchmarks run(Map args=[:], Closure clos) {
        benchmarks = []
        this.times = args.times ? args.times : 1    
        clos.delegate = this
        clos()
        return benchmarks
    }
   
    def with(String label, Closure clos) {
        measure(1, clos) // avoid large overhead of the first closure call
        def benchmark = [label: label, time: measure(times, clos)]
        benchmarks << benchmark
    }
    
    private BenchmarkTime measure(times, Closure clos) {
        def mxBean = ManagementFactory.threadMXBean;
        def cpuTimeSupported = mxBean.isCurrentThreadCpuTimeSupported()
        def real = 0
        def cpu = 0
        def user = 0 
        for (i in 0..<times) {
            def bReal = System.nanoTime()
            def bCpu
            def bUser
            if (cpuTimeSupported) {
                bCpu = mxBean.currentThreadCpuTime
                bUser = mxBean.currentThreadUserTime
            }
            clos()
            if (cpuTimeSupported) {
                user += mxBean.currentThreadUserTime - bUser
                cpu += mxBean.currentThreadCpuTime - bCpu
            }
            real += System.nanoTime() - bReal
        }
        return new BenchmarkTime(
                    real : real, cpu: cpu, system: cpu - user, user: user
                )
    }
}

