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
package groovyx.gbench

import java.lang.management.ManagementFactory

/**
 * A builder for micro-benchmarking.
 * <p>
 * This builder allows accurate and easy benchmark.
 * For example, you can benchmark character concatenation using
 * <code>StringBuilder</code> and <code>StringBuffer</code> and compare their
 * performances like the following:
 * </p>
 * <pre><code>
 * def bm = new BenchmarkBuilder().run {
 *     'StringBuilder' {
 *         def sb = new StringBuilder()
 *         sb.append('foo')
 *         sb.append('bar')
 *         sb.append('baz')
 *         sb.toString()
 *     }
 *     'StringBuffer' {
 *         def sb = new StringBuffer()
 *         sb.append('foo')
 *         sb.append('bar')
 *         sb.append('baz')
 *         sb.toString()
 *     }
 * }
 * bm.prettyPrint()
 * <code></pre>
 * then means (in nanoseconds) of each execution time will be printed out:
 * <pre>
 *                user  system  cpu  real
 *
 * StringBuilder   283       0  284   286
 * StringBuffer    319       0  319   321
 * </pre>
 *
 * <p/>
 *
 * @author Nagai Masato
 *
 */
class BenchmarkBuilder {

    public static int AUTO = -1;

    BenchmarkList benchmarks

    /**
     * Gets benchmarks.
     *
     * @param options
     * <ul>
     * <li>warmUpTime: the length of time (in seconds) to warm up Groovy and JVM.
     *              the default value is {@link AUTO}.</li>
     * <li>measureCpuTime: measure CPU time. the default value depends on JVM.</li>
     * <li>quiet: suppress output. the default value is <code>false</code>.
     * <li>verbose: enable verbose output. the default value is <code>false</code>.
     * </ul>
     * @param clos a closure to add code blocks for benchmarking.
     * @return a list of benchmarks
     */
    def run(Map options = [:], Closure clos) {
        setOptions(options); printEnv(); printOption()
        return doRun(clos)
    }

    private doRun(Closure clos) {
        benchmarks = new BenchmarkList()
        clos.resolveStrategy = Closure.DELEGATE_FIRST
        clos.delegate = this
        clos()
        return benchmarks
    }

    private void setOptions(options) {
        def cpuTimeSupported =
            ManagementFactory.threadMXBean.currentThreadCpuTimeSupported
        Map context = [
            measureCpuTime : cpuTimeSupported,
            warmUpTime : AUTO,
            maxWarmUpTime: 60,
            quiet : false,
            verbose : false]
        context += options
        if (options.measureCpuTime && !cpuTimeSupported) {
            BenchmarkLogger.error("The JVM doesn't support CPU time measurement.")
            context.measureCpuTime = false
        }
        BenchmarkContext.set(context)
    }

    private def printOption() {
        def options = BenchmarkContext.get()
        BenchmarkLogger.info("""\
            Options
            =======
            * Warm Up: ${AUTO == options.warmUpTime ?
                'Auto (- ' + options.maxWarmUpTime + ' sec)'
                : options.warmUpTime + ' sec'}
            * CPU Time Measurement: ${options.measureCpuTime ? 'On' : 'Off' }
        """.stripIndent())
    }

    private def printEnv() {
        def jvm = System.&getProperty
        def rt = Runtime.runtime
        BenchmarkLogger.info("""\
            Environment
            ===========
            * Groovy: ${GroovySystem.version}
            * JVM: ${jvm('java.vm.name')} (${jvm('java.vm.version')}, ${jvm('java.vm.vendor')})
                * JRE: ${jvm('java.version')}
                * Total Memory: ${rt.totalMemory() / 1024 / 1024 + ' MB'}
                * Maximum Memory: ${rt.maxMemory() / 1024 / 1024 + ' MB'}
            * OS: ${jvm('os.name')} (${jvm('os.version')}, ${jvm('os.arch')})
        """.stripIndent())
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
        benchmarks << [ label: label, time: Benchmarker.run(label, clos) ]
    }

    String toString() {
        def writer = new StringWriter()
        benchmarks.prettyPrint(new PrintWriter(writer))
        return writer.toString()
    }

    def invokeMethod(String name, Object args) {
        if (args && args.size() == 1) {
            with(name, args[0])
        }
    }
}
