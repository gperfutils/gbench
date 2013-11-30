/*
 * Copyright 2013 Masato Nagai
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

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.concurrent.Callable;

public class BenchmarkBuilderInJavaTest {

    @Test public void run() {
        BenchmarkResultList results =
            BenchmarkBuilder
                .create()
                .add("take 100", new Callable() {
                    public Object call() throws Exception {
                        Thread.sleep(100);
                        return null;
                    }
                })
                .add("take 200", new Callable() {
                    public Object call() throws Exception {
                        Thread.sleep(200);
                        return null;
                    }
                })
                .setQuiet(true)
                .run();

        BenchmarkResult result;

        // groovy 1.8 requires casting the elements of a list because it ignores generic types
        result = (BenchmarkResult) results.get(0);
        assertEquals("take 100", result.getLabel());
        assertEquals(1, (int) (result.getTime().getReal() / 1000 / 1000 / 100));

        result = (BenchmarkResult) results.get(1);
        assertEquals("take 200", result.getLabel());
        assertEquals(2, (int) (result.getTime().getReal() / 1000 / 1000 / 100));
    }
}
