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

@PackageScope
class BenchmarkMath {
    
    static double mean(List ns) {
        ns.sum() / ns.size()
    }
    
    static double mdev(List ns) {
        if (ns.empty) {
            return
        }
        def m = mean(ns)
        def sum = ns.inject(0d) { sum, n ->
            sum += Math.abs(n - m)
        }
        sum / ns.size()
    }
    
    static double rmdev(List ns) {
        def m = mean(ns)
        mdev(ns) / m
    }
    
    static double stdev(List ns) {
        if (ns.empty) {
            return
        }
        def m = mean(ns)
        def sum = ns.inject(0d) { sum, n ->
            def diff = n - m
            sum += diff * diff
        }
        Math.sqrt(sum / ns.size())
    }

}
