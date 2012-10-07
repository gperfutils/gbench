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

/**
 * The class defines extension methods.
 * 
 * @author Nagai Masato
 *
 */
class BenchmarkExtension {
    
    /**
     * <pre>
     * { ->
     *     // ...
     * }.benchmark()
     * </pre>
     */
    static BenchmarkList benchmark(Closure self, Map args = [:]) {
        new BenchmarkBuilder().run(args, self)    
    }

}
