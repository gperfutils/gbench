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

/* $if version >= 2.0.0 $ */
@groovy.transform.TypeChecked
/* $endif$ */
class BenchmarkMeasure {
    
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
    
    static long cpuTime() {
        BenchmarkContext.get().measureCpuTime ?
            ManagementFactory.threadMXBean.currentThreadCpuTime : 0L
    }
    
    static long userTime() {
        BenchmarkContext.get().measureCpuTime ?
            ManagementFactory.threadMXBean.currentThreadUserTime : 0L
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
    
    static Map run(Closure task, long execTimes) {
        cleanHeap()
        long bc = compilationTime()
        long bct = cpuTime()
        long but = userTime()
        long bt = time()
        execTimes.times { task() }
        long act = cpuTime()
        long aut = userTime()
        long at = time()
        long ac = compilationTime()
        [
            executionTime: at - bt,
            cpuTime: act - bct,
            userTime: aut - but,
            systemTime: act - bct - (aut - but),
            compilationTime: ac - bc,
        ]
    }

}
