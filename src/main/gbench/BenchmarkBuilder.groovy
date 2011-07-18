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
package gbench

import java.lang.management.ManagementFactory

/**
 * <p>A builder for benchmarking.</p>
 * <p>For example, you can benchmark character concatenation like the 
 * following:</p>
 * <pre><code>
 * new BenchmarkBuilder().run(times: 10000, {
 *     def chars = ['g', 'r', 'o', 'o', 'v', 'y']
 *     concat {
 *         def s = ''
 *         for(c in chars){
 *             s.concat c
 *         }
 *     }
 *     '+=' {
 *         def s = ''
 *         for (c in chars) {
 *             s += c
 *         }
 *     }
 *     stringbuilder {
 *         def sb = new StringBuilder()
 *         for(c in chars){
 *             sb << c
 *         }
 *         sb.toString()
 *     }
 *     join {
 *         chars.join()
 *     }
 * }).sort().prettyPrint()
 * <code></pre>
 * then output will be like:
 * <pre>
 *                     user      system         cpu         real
 *
 * join            46800300    15600100    62400400     91680789
 * stringbuilder   62400400    15600100    78000500    101281757
 * +=              62400400    15600100    78000500    121649445
 * concat          46800300    31200200    78000500    129421409
 * </pre>
 *
 * @author Nagai Masato
 *
 */
class BenchmarkBuilder {

    static class Benchmarks extends ArrayList {

        def sort() {
            return sort{ it.time.real }
        }

        /**
         * Pretty-prints the benchmarks.
         * 
         * @param writer a print writer.
         */
        def prettyPrint(PrintWriter writer = new PrintWriter(System.out)) {
            def wids =
                inject([
                    label: 1,
                    user: 'user'.size(),
                    system: 'system'.size(),
                    cpu: 'cpu'.size(),
                    real: 'real'.size()
                ]) { wids, bm ->
                    wids.label = Math.max(wids.label, bm.label.size())
                    wids.user = 
                        Math.max(wids.user, bm.time.user.toString().size())
                    wids.system = 
                        Math.max(wids.system, bm.time.system.toString().size())
                    wids.cpu = Math.max(wids.cpu, bm.time.cpu.toString().size())
                    wids.real = 
                        Math.max(wids.real, bm.time.real.toString().size())
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
    int idles
    boolean average
    boolean trim

    /**
     * Gets benchmarks.
     * 
     * @param options
     * <ul>
     * <li>average: if <code>true</code>, gets average instead of sum. the 
     *              default value is <code>false</code> (gets sum).</li>
     * <li>times:   times to execute each code block. the default value is 
     *              <code>1</code>.</li>
     * <li>idles:   times to execute each code block before starting to 
     *              benchmark. This option is useful to reduce effects of 
     *              overhead. the default value is <code>1</code>.</li>
     * <li>trim:    if <code>true</code>, removes the highest and the lowest 
     *              benchmarks. the default value is <code>false</code>.</li>
     * </ul>
     * @param clos a closure to add code blocks for benchmarking.
     * @return benchmarks
     */
    Benchmarks run(Map options=[:], Closure clos) {
        benchmarks = []
        this.times = options.times ?: 1
        this.idles = options.idles ?: 1
        this.average = options.average ?: false
        if (options.trim) {
            this.trim = true
            if (options.times < 3) {
                // cannot trim in case of lack of times
                this.trim = false
            }
        }
        clos.delegate = this
        clos()
        return benchmarks
    }
   
    /**
     * Gets sum of benchmarks. This method behaves the same as 
     * <code>run() or run(average: false)</code>
     * 
     * @param options
     * <ul>
     * <li>times:   times to execute each code block. the default value is 
     *              <code>1</code>.</li>
     * <li>idles:   times to execute each code block before starting to 
     *              benchmark. This option is useful to reduce effects of 
     *              overhead. the default value is <code>1</code>.</li>
     * <li>trim:    if <code>true</code>, removes the highest and the lowest 
     *              benchmarks. the default value is <code>false</code>.</li>
     * </ul>
     * @param clos a closure to add code blocks for benchmarking
     * @return benchmarks
     */
    Benchmarks sum(Map options=[:], Closure clos) {
        run(options, clos)    
    }

    /**
     * Gets average of benchmarks. This method behaves the same as 
     * <code>run(average: true)</code>
     * 
     * @param options
     * <ul>
     * <li>times:   times to execute each code block. the default value is 
     *              <code>1</code>.</li>
     * <li>idles:   times to execute each code block before starting to 
     *              benchmark. This option is useful to reduce effects of 
     *              overhead. the default value is <code>1</code>.</li>
     * <li>trim:    if <code>true</code>, removes the highest and the lowest 
     *              benchmarks. the default value is <code>false</code>.</li>
     * </ul>
     * @param clos a closure to add code blocks for benchmarking
     * @return benchmarks
     */
    Benchmarks average(Map options=[:], Closure clos) {
        options = new HashMap(options)
        options.average = true
        run(options, clos)    
    }
    
    /**
     * Adds a code block as a benchmark target.
     * 
     * @deprecated Use the following alternate syntax instead: 
     *               <code>label { code }</code>
     *               
     * @param label the label of the code block.
     * @param clos a code block.
     */
    def with(String label, Closure clos) {
        idles.times { clos() }
        def benchmark = [label: label, time: measure(times, clos)]
        benchmarks << benchmark
    }
   
    def invokeMethod(String name, Object args) {
        if (args && args.size() == 1) {
            with(name, args[0])
        }
    }
    
    private BenchmarkTime measure(times, Closure clos) {
        def reals = []
        def cpus = []
        def systems = []
        def users = []
        def mxBean = ManagementFactory.threadMXBean
        def cpuTimeSupported = mxBean.isCurrentThreadCpuTimeSupported()
        times.times {
            def bReal = System.nanoTime()
            def bCpu
            def bUser
            if (cpuTimeSupported) {
                bCpu = mxBean.currentThreadCpuTime
                bUser = mxBean.currentThreadUserTime
            }
            clos()
            if (cpuTimeSupported) {
                def user = mxBean.currentThreadUserTime - bUser
                def cpu = mxBean.currentThreadCpuTime - bCpu
                users << user
                cpus << cpu
                systems << cpu - user
            }
            reals << System.nanoTime() - bReal
        }
        def calc = { list ->
            if (trim) {
                list -= list.max()
                list -= list.min()
            }
            def total = list.sum() ?: 0
            if (average) {
                return total / list.size()
            }
            return total
        }
        return new BenchmarkTime(
            real: calc(reals),
            cpu: calc(cpus),
            system: calc(systems),
            user: calc(users),
        )
    }
}