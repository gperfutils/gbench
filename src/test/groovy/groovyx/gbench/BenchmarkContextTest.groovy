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

import java.lang.management.ManagementFactory
import java.util.concurrent.CountDownLatch

class BenchmarkContextTest {

    void doTest(Map expected) {
        CountDownLatch latch = new CountDownLatch(1)
        new Thread().start {
            assert expected == BenchmarkContext.get()
            latch.countDown()
        }
        latch.await()
    }

    @Test void defaultValues() {
        doTest(
            cpuTimeSupported: ManagementFactory.threadMXBean.currentThreadCpuTimeSupported,
            measureCpuTime: ManagementFactory.threadMXBean.currentThreadCpuTimeSupported,
            warmUpTime: BenchmarkConstants.AUTO_WARM_UP,
            maxWarmUpTime: 60,
            verbose: false,
            quiet: false
        )
    }

    @Test void useSystemProperties() {
        System.setProperty("gbench.measureCpuTime",
            String.valueOf(!ManagementFactory.threadMXBean.currentThreadCpuTimeSupported))
        System.setProperty("gbench.warmUpTime", "10")
        System.setProperty("gbench.maxWarmUpTime", "20")
        System.setProperty("gbench.verbose", "true")
        System.setProperty("gbench.quiet", "true")
        doTest(
            cpuTimeSupported: ManagementFactory.threadMXBean.currentThreadCpuTimeSupported,
            measureCpuTime: Boolean.valueOf(System.getProperty("gbench.measureCpuTime")),
            warmUpTime: Integer.valueOf(System.getProperty("gbench.warmUpTime")),
            maxWarmUpTime: Integer.valueOf(System.getProperty("gbench.maxWarmUpTime")),
            verbose: Boolean.valueOf(System.getProperty("gbench.verbose")),
            quiet: Boolean.valueOf(System.getProperty("gbench.quiet"))
        )
    }

}
