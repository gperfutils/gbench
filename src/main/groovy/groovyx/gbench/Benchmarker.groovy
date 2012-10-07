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

import groovy.transform.PackageScope

import java.lang.management.ManagementFactory

@PackageScope
class Benchmarker {
    
    def options
    
    private void cleanHeap() {
        def rt = Runtime.runtime
        def bu = rt.totalMemory() - rt.freeMemory()
        while (true) {
            System.runFinalization()
            System.gc()
            def au = rt.totalMemory() - rt.freeMemory()
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
        def bean = ManagementFactory.compilationMXBean
        bean.isCompilationTimeMonitoringSupported() ?
            bean.totalCompilationTime * 1000 * 1000 /* ms -> ns */ : 0L
    }
    
    private long garbageCollectionTime() {
        ManagementFactory.garbageCollectorMXBeans
            .inject(0L) { total, b ->
                total + b.collectionTime
            } * 1000 * 1000 // ms -> ns
    }
    
    private long measurementTimeInterval() {
        1L * 1000 * 1000 * 1000 // 1 sec
    }
    
    private long computeExecutionTimes(Closure task) {
        def times = 0
        def ti = measurementTimeInterval()
        def st = time()
        while (true) {
            task()
            times++
            if (time() - st >= ti) {
                break
            }
        }
        times
    }
    
    private def measure(long repeat, Closure task) {
        cleanHeap()
        def bc = compilationTime()
        def bct = cpuTime()
        def but = userTime()
        def bt = time()
        repeat.times { task() }
        def act = cpuTime()
        def aut = userTime()
        def at = time()
        def ac = compilationTime()
        [ 
            executionTime: at - bt, 
            cpuTime: act - bct,
            userTime: aut - but,
            systemTime: act - bct - (aut - but),
            compilationTime: ac - bc,
        ]
    }
    
    def run(label, Closure task) {
        def log = {
            if (!options.quiet && options.verbose) {
                println(it); println()
            }
        }
        def times = computeExecutionTimes(task)
        def result
        if (0 <= options.warmUpTime) {
            def warmUpTime = ((long) options.warmUpTime) * 1000 * 1000 * 1000 // s -> ns
            def st = time()
            def warmedUp = {
                def b = time() - st >= warmUpTime
            }
            log("Warming up \"$label\"...")
            while (!warmedUp()) {
                measure(times, task)
            }
            log("Measuring \"$label\"...")
            result = measure(times, task)
        } else {
            measure(1, task)
            def warmedUp = { current, last ->
                if (!(current && last)) {
                    return false
                }
                !current.compilationTime && 
                    BenchmarkMath.rmdev(
                        [current.executionTime, last.executionTime]) <= 0.005 /* 0.5% */
            }
            def _result
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
                      real: result.executionTime / times,
                      cpu: result.cpuTime / times,
                      system: result.systemTime / times,
                      user: result.userTime / times,
                  )
        ]
    } 

}
