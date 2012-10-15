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

/* $if version >= 2.0.0 $ */
@groovy.transform.TypeChecked
/* $endif$ */
/* $if version >= 1.8.0 $ */
@groovy.transform.PackageScope
/* $endif$ */
class BenchmarkMath {
    
    /**
     * Calculates arithmetic mean.
     */
    static double mean(long a, long b) {
        (a + b) / 2
    }
    
    /**
     * Calculates mean deviation.
     */
    static double mdev(long a, long b) {
        mdev(a, b, mean(a, b))
    }
    
    /**
     * Calculates mean deviation.
     */
    static double mdev(long a, long b, double m) {
        (Math.abs(a - m) + Math.abs(b - m)) / 2
    }
    
    /**
     * Calculates relative mean deviation.
     */
    static double rmdev(long a, long b) {
        double m = mean(a, b)
        mdev(a, b, m) / m
    }
    
    /**
     * Calculates standard deviation.
     */
    static double stdev(long a, long b) {
        stdev(a, b, mean(a, b))
    }
    
    /**
     * Calculates standard deviation.
     */
    static double stdev(long a, long b, double m) {
        Math.sqrt(((a - m) * (a - m) + (b - m) * (b - m)) / 2)
    }

}
