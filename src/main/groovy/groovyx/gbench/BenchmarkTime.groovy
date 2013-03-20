/*
 * Copyright 2011 Nagai Masato
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
 */package groovyx.gbench

/* $if version >= 2.0.0 $ */
@groovy.transform.TypeChecked
/* $endif$ */
class BenchmarkTime {
    long real
    long cpu
    long system
    long user

    // I don't know why but named argument constructor does not work with
    // static compilation.
    BenchmarkTime(long real, long cpu, long system, long user) {
        this.real = real
        this.cpu = cpu
        this.system = system
        this.user = user
    }

    BenchmarkTime(Map args) {
        this((long) args.real, (long) args.cpu, (long) args.system, (long) args.user)
    }

    String toString() {
        "user:${user} system:${system} cpu:${cpu} real:${real}"
    }

    BenchmarkTime minus(BenchmarkTime another) {
        return new BenchmarkTime(
            real: Math.max(0L, real - another.real),
            cpu: Math.max(0L, cpu - another.cpu),
            system: Math.max(0L, system - another.system),
            user: Math.max(0L, user - another.user)
        )
    }
}
