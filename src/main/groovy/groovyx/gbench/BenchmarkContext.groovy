/*
 * Copyright 2012 Nagai Masato
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
import java.lang.management.ThreadMXBean

/* $if version >= 2.0.0 $ */
@groovy.transform.TypeChecked
/* $endif$ */
class BenchmarkContext {

    static final ThreadLocal context = new ThreadLocal() {
        @Override
        protected Object initialValue() {
            ThreadMXBean thread = ManagementFactory.threadMXBean
            boolean cpuTimeSupported = thread.currentThreadCpuTimeSupported
            [
                cpuTimeSupported: cpuTimeSupported,
                measureCpuTime: cpuTimeSupported && BenchmarkSystem.measureCpuTime,
                warmUpTime: BenchmarkConstants.AUTO_WARM_UP,
                maxWarmUpTime: 60,
                verbose: BenchmarkSystem.verbose,
                quiet: BenchmarkSystem.quiet,
            ]
        }
    }

    static Map get() {
        (Map) context.get()
    }

    static void set(Map another) {
        context.set(another)
    }

}
