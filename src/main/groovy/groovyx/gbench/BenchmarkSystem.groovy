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
package groovyx.gbench;

class BenchmarkSystem {

    static String PREFIX = "gbench."

    static def systemProperty(String name, defaultValue) {
        def value = System.getProperty(name)
        return value == null ? defaultValue : value
    }

    static boolean isMeasureCpuTime() {
        Boolean.valueOf(systemProperty("${PREFIX}measureCpuTime", true))
    }

    static boolean isVerbose() {
        Boolean.valueOf(systemProperty("${PREFIX}verbose", false))
    }

    static boolean isQuiet() {
        Boolean.valueOf(systemProperty("${PREFIX}quiet", false))
    }

    static int getWarmUpTime() {
        Integer.valueOf(systemProperty("${PREFIX}warmUpTime", BenchmarkConstants.AUTO_WARM_UP))
    }

    static int getMaxWarmUpTime() {
        Integer.valueOf(systemProperty("${PREFIX}maxWarmUpTime", 60))
    }

}
