/*
 * Copyright 2011 Masato Nagai
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

import java.util.concurrent.Callable

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
 * @author Masato Nagai
 *
 */
class BenchmarkBuilder {

    static int AUTO = BenchmarkConstants.AUTO_WARM_UP;

    BenchmarkResultList results = new BenchmarkResultList()

    private Map blocks = [:]

    /**
     * Creates an instance of the class.
     *
     * @return the new instance
     */
    static BenchmarkBuilder create() {
        return new BenchmarkBuilder()
    }

    BenchmarkBuilder setWarmUpTime(int warmUpTime) {
        return setOption('warmUpTime', warmUpTime)
    }

    BenchmarkBuilder setMaxWarmUpTime(int maxWarmUpTime) {
        return setOption('maxWarmUpTime', maxWarmUpTime)
    }

    BenchmarkBuilder setMeasureCpuTime(boolean measureCpuTime) {
        return setOption('measureCpuTime', measureCpuTime)
    }

    BenchmarkBuilder setQuiet(boolean quiet) {
        return setOption('quiet', quiet)
    }

    BenchmarkBuilder setVerbose(boolean verbose) {
        return setOption('verbose', verbose)
    }

    private BenchmarkBuilder setOption(name, value) {
        Map context = BenchmarkContext.get()
        if (name == "measureCpuTime" && value && !context.cpuTimeSupported) {
            BenchmarkLogger.error("The JVM doesn't support CPU time measurement.")
        } else {
            context.put(name, value)
        }
        return this
    }

    private void setOptions(options = [:]) {
        options.each { name, value ->
            setOption(name, value)
        }
    }

    /**
     * Adds a code block to the list to be measured.
     *
     * @param label a label for the code block
     * @param code a code block to be measured
     * @return the instance of the builder
     */
    BenchmarkBuilder add(String label, Callable code) {
        blocks[label] = code
        return this
    }

    /**
     * Benchmarks using DSL and returns the results.
     *
     * @param options
     * <ul>
     * <li>warmUpTime: the length of time (in seconds) to warm up Groovy and JVM.
     *              the default value is {@link #AUTO}.</li>
     * <li>maxWarmUpTime: the maximum length of time (in seconds) that is used when warmUpTime is {@link #AUTO}.
     * <li>measureCpuTime: measure CPU time. the default value depends on JVM.</li>
     * <li>quiet: suppress output. the default value is <code>false</code>.
     * <li>verbose: enable verbose output. the default value is <code>false</code>.
     * </ul>
     * @param dsl DSL to setup
     * @return the results of the benchmarking
     */
    BenchmarkResultList run(Map options = [:], Closure dsl) {
        dsl.resolveStrategy = Closure.DELEGATE_FIRST
        dsl.delegate = this
        dsl()
        return run(options)
    }

    /**
     * Benchmarks and returns the results.
     *
     * @param options
     * <ul>
     * <li>warmUpTime: the length of time (in seconds) to warm up Groovy and JVM.
     *              the default value is {@link #AUTO}.</li>
     * <li>maxWarmUpTime: the maximum length of time (in seconds) that is used when warmUpTime is {@link #AUTO}.
     * <li>measureCpuTime: measure CPU time. the default value depends on JVM.</li>
     * <li>quiet: suppress output. the default value is <code>false</code>.
     * <li>verbose: enable verbose output. the default value is <code>false</code>.
     * </ul>
     * @return the results of the benchmarking
     */
    BenchmarkResultList run(Map options = [:]) {
        setOptions(options);
        return doRun()
    }

    private doRun() {
        printEnv();
        printOption()
        results.clear()
        blocks.each { label, code -> results << benchmark(label, code) }
        return results
    }

    private BenchmarkResult benchmark(String label, Callable block) {
        new BenchmarkResult(label: label, time: Benchmarker.run(label, block))
    }

    private def printOption() {
        def options = BenchmarkContext.get()
        BenchmarkLogger.info("""\
            Options
            =======
            * Warm Up: ${
            AUTO == options.warmUpTime ? 'Auto (- ' + options.maxWarmUpTime + ' sec)' : options.warmUpTime + ' sec'
        }
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

    String toString() {
        def writer = new StringWriter()
        results.prettyPrint(new PrintWriter(writer))
        return writer.toString()
    }

    Object invokeMethod(String name, Object args) {
        if (args && args.size() == 1) {
            add(name, args[0])
        }
    }
}
