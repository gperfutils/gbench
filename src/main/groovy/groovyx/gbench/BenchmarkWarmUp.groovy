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

/* $if version >= 2.0.0 $ */
@groovy.transform.TypeChecked
/* $endif$ */
class BenchmarkWarmUp {

    private static boolean timeUp(long st, long dt) {
        BenchmarkMeasure.time() - st >= dt
    }

    private static boolean stable(Map current, Map last) {
        if (!(current && last)) {
            false
        } else {
            !current.compilationTime && 
                BenchmarkMath.rmdev(
                    (long) current.executionTime, (long) last.executionTime) <= 0.005 /* 0.5% */
        }
    }

    static void run(Closure task, long execTimes) {
        if (0 <= (int) BenchmarkContext.get().warmUpTime) {
            long dt = ((int) BenchmarkContext.get().warmUpTime) * 1000L * 1000 * 1000 // s -> ns
            long st = BenchmarkMeasure.time()
            while (!timeUp(st, dt)) {
                BenchmarkMeasure.run(task, execTimes)
            }
        } else {
            BenchmarkMeasure.run(task, 1)
            Map bm, lbm
            while (!stable(bm, lbm)) {
                lbm = bm
                bm = BenchmarkMeasure.run(task, execTimes)
            }
        }
    }

}
