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

class BenchmarkList extends ArrayList {
    
    def options

    /**
     * Sorts by real time.
     * 
     * @return this
     */
    def sort() {
        return sort {it.time.real}
    }

    /**
     * Pretty-prints.
     * 
     * @param writer a print writer.
     */
    def prettyPrint(PrintWriter writer = new PrintWriter(System.out)) {
        if (!options.measureCpuTime) {
            def w = inject([ label: 1, real: 1 ]) { w, bm ->
                [ label: Math.max(w.label, bm.label.size()),
                    real: Math.max(w.real, bm.time.real.toString().size()) ]    
            } 
            each { bm ->
                writer.printf(
                    "%-${w.label}s  %${w.real}s", bm.label, bm.time.real
                ); writer.println()
            }
        } else {
            def w = inject([
                label: 1, user: 'user'.size(), system: 'system'.size(),
                cpu: 'cpu'.size(), real: 'real'.size()
            ]) { w, bm ->
                [ label: Math.max(w.label, bm.label.size()),
                    user: Math.max(w.user, bm.time.user.toString().size()),
                    system: Math.max(w.system, bm.time.system.toString().size()),
                    cpu: Math.max(w.cpu, bm.time.cpu.toString().size()),
                    real: Math.max(w.real, bm.time.real.toString().size()) ]
            }
            writer.printf(
                "%${w.label}s  %${w.user}s  %${w.system}s  %${w.cpu}s  %${w.real}s",
                ' ', 'user', 'system', 'cpu', 'real'
            ); writer.println()
            writer.println()
            each { bm ->
                writer.printf(
                    "%-${w.label}s  %${w.user}d  %${w.system}d  %${w.cpu}d  %${w.real}d",
                    bm.label, bm.time.user, bm.time.system, bm.time.cpu, bm.time.real
                ); writer.println()
            }
        }
        writer.flush()
    }
}