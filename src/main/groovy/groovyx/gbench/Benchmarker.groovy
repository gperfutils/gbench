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
 */package groovyx.gbench

import java.lang.management.CompilationMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory

/* $if version >= 2.0.0 $ */
@groovy.transform.TypeChecked
/* $endif$ */
/* $if version >= 1.8.0 $ */
@groovy.transform.PackageScope
/* $endif$ */
class Benchmarker {
    
    Map options
    
    private void cleanHeap() {
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
    
    private long time() {
        System.nanoTime()
    }
    
    private long cpuTime() {
        options.measureCpuTime ? 
            ManagementFactory.threadMXBean.currentThreadCpuTime : 0L
    }
    
    private long userTime() {
        options.measureCpuTime ?
            ManagementFactory.threadMXBean.currentThreadUserTime : 0L
    }
    
    private long compilationTime() {
        CompilationMXBean bean = ManagementFactory.compilationMXBean
        bean.isCompilationTimeMonitoringSupported() ?
            bean.totalCompilationTime * 1000 * 1000 /* ms -> ns */ : 0L
    }
    
    private long garbageCollectionTime() {
        ((long) ManagementFactory.garbageCollectorMXBeans
            .inject(0L) { long total, GarbageCollectorMXBean b ->
                total + b.collectionTime
            }) * 1000 * 1000 // ms -> ns
    }
    
    private long measurementTimeInterval() {
        1L * 1000 * 1000 * 1000 // 1 sec
    }
    
    private long computeExecutionTimes(Closure task) {
        long times = 0
        long ti = measurementTimeInterval()
        long st = time()
        while (true) {
            task()
            times++
            if (time() - st >= ti) {
                break
            }
        }
        times
    }
    
    private Map measure(long repeat, Closure task) {
        cleanHeap()
        long bc = compilationTime()
        long bct = cpuTime()
        long but = userTime()
        long bt = time()
        repeat.times { task() }
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
    
    private boolean warmedUp(long st, long dt) {
        time() - st >= dt
    }
    
    private boolean warmedUp(Map current, Map last) {
        if (!(current && last)) {
            false
        } else {
            !current.compilationTime && 
                BenchmarkMath.rmdev(
                    (long) current.executionTime, (long) last.executionTime) <= 0.005 /* 0.5% */
        }
    }
    
    private void log(String s) {
        if (!options.quiet && options.verbose) {
            println(s); println()
        }
    }
    
    Map run(label, Closure task) {
        long times = computeExecutionTimes(task)
        Map result
        if (0 <= options.warmUpTime) {
            long dt = ((long) options.warmUpTime) * 1000 * 1000 * 1000 // s -> ns
            long st = time()
            log("Warming up \"$label\"...")
            while (!warmedUp(st, dt)) {
                measure(times, task)
            }
            log("Measuring \"$label\"...")
            result = measure(times, task)
        } else {
            measure(1, task)
            Map _result
            log("Warming up \"$label\"...")
            while (!warmedUp(_result, result)) {
                result = _result
                _result = measure(times, task)
            }
            log("Measuring \"$label\"...")
            result = measure(times, task)
        }
        return [
            label: label,
            time: new BenchmarkTime(
                      real: ((long) result.executionTime) / times,
                      cpu: ((long) result.cpuTime) / times,
                      system: ((long) result.systemTime) / times,
                      user: ((long) result.userTime) / times,
                  )
        ]
    } 

}
