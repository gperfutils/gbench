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

import java.lang.management.CompilationMXBean
import java.lang.management.GarbageCollectorMXBean
import java.lang.management.ManagementFactory
import java.lang.management.ThreadMXBean

/* $if version >= 2.0.0 $ */
@groovy.transform.TypeChecked
/* $endif$ */
class BenchmarkMeasure {

    static long MEASUREMENT_TIME_INTERVAL = 1L * 1000 * 1000 * 1000 // 1 sec

    static {
        // Cache the method when the class is loaded.
        // Full warm-up is better but it might take too long time for preparing.
        executionTime {}
    }

    static long computeExecutionTimes(Closure task) {
        long times = 0
        long st = BenchmarkMeasure.time()
        while (true) {
            task()
            times++
            if (BenchmarkMeasure.time() - st >= MEASUREMENT_TIME_INTERVAL) {
                break
            }
        }
        times
    }

    static void cleanHeap() {
        Runtime rt = Runtime.runtime
        long bu = rt.totalMemory() - rt.freeMemory()
        while (true) {
            System.runFinalization()
            System.gc()
            long au = rt.totalMemory() - rt.freeMemory()
            if (bu > au) {
                bu = au
                continue
            }
            break
        }
    }

    static long time() {
        System.nanoTime()
    }

    static long[] cpuTime() {
        long[] r = new long[2]
        if (!BenchmarkContext.get().measureCpuTime) {
            r[0] = r[1] = 0L
        } else {
            long s, overhead
            s = time()
            ThreadMXBean bean = ManagementFactory.threadMXBean
            overhead = time() - s
            s = time()
            long cpu = Math.max(0L, bean.currentThreadCpuTime - overhead)
            overhead += (time() - s)
            long user = Math.max(0L, bean.currentThreadUserTime - overhead)
            r[0] = cpu
            r[1] = user
        }
        return r
    }

    static long compilationTime() {
        CompilationMXBean bean = ManagementFactory.compilationMXBean
        bean.isCompilationTimeMonitoringSupported() ?
            bean.totalCompilationTime * 1000 * 1000 /* ms -> ns */ : 0L
    }

    static long garbageCollectionTime() {
        ((long) ManagementFactory.garbageCollectorMXBeans
            .inject(0L) { long total, GarbageCollectorMXBean b ->
                total + b.collectionTime
            }) * 1000 * 1000 // ms -> ns
    }

    static BenchmarkTime _executionTime(Closure task, long execTimes = 1) {
        long[] ct
        ct = cpuTime()
        long bct = ct[0]
        long but = ct[1]
        long bt = time()
        execTimes.times { task() }
        long at = time()
        ct = cpuTime()
        long act = ct[0]
        long aut = ct[1]

        long real, cpu, user, system, compile
        real = (long) ((at - bt) / execTimes)
        cpu = (long) ((act - bct) / execTimes)
        user = (long) ((aut - but) / execTimes)
        return new BenchmarkTime(
            real: real,
            cpu: cpu,
            user: user,
        )

    }

    static BenchmarkTime executionTime(Closure task, long execTimes = 1) {
        BenchmarkTime overhead = _executionTime({}, execTimes)
        BenchmarkTime time = _executionTime(task, execTimes)
        time -= overhead
        // Basically the cpu time would be equal or less than the real time and
        // the user time would be less than the cpu time.
        // But sometimes overhead calculation would fail and the times would be wrong.
        long real = time.real
        long cpu = time.cpu
        long user = time.user
        cpu = Math.min(real, cpu)
        user = Math.min(cpu, user)
        return new BenchmarkTime(real: real, cpu: cpu, user: user)
    }

    static class Result {
        BenchmarkTime benchmarkTime
        long compilationTime
    }

    static Result run(Closure task, long execTimes = 1) {
        cleanHeap()
        long bc = compilationTime()
        BenchmarkTime result = executionTime(task, execTimes)
        long ac = compilationTime()
        long compilationTime = (long) ((ac - bc) / execTimes)
        return new Result(
            benchmarkTime: result,
            compilationTime:  compilationTime)
    }

}
