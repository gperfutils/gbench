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
 */
package gbench;

import groovy.lang.GroovyShell;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;

/**
 * An annotation to measure execution time of methods. You can add it to methods or classes.
 * 
 * <pre>
 * package foo
 * 
 * class Foo {
 *     {@code @Benchmark}
 *     def foo() {
 *     }
 * }
 * </pre>
 * <pre>
 * package foo
 * 
 * {@code Benchmark}
 * class Foo {
 *     def foo() {
 *     }
 * }
 * </pre>
 * 
 * Then the ouputs of both will be:
 * <pre> 
 * foo.Foo java.lang.Object foo(): xxx ns
 * </pre>
 * 
 * You can customize handling of benchmark results by using handler classes that implement
 * BenchmarkHandler interface. Handler classes must have two methods, handle() and getInstance():
 * <pre>
 * class MyHandler implements Benchmark.BenchmarkHandler {
 *     static def instance = new MyHandler()
 *         void handle(klass method, time) {
 *         println("${method} of ${klass}: ${(time/1000000) as long} ms")
 * 	   }
 *     static MyHandler getInstance() {
 *         instance
 *     } 
 * }
 * </pre>
 * <pre>
 * {@code @Benchmark(MyHandler.class)}
 * def foo() {
 * }
 * </pre>
 * 
 * Since Groovy 1.8, you can also use closures instead of handler classes. With closures, you just need to assign closures that handle benchmark results:
 * <pre>
 * {@code @Benchmark({println("${method} of ${class}: ${(time/1000000) as long} ms")})}
 * def foo() {
 * }
 * </pre>
 * 
 * And you can replace the default handling operation with a system property, "groovybenchmark.sf.net.defaulthandle":
 * <pre> 
 * groovy -cp groovybenchmark-xx.xx.xx.jar -Dgroovybenchmark.sf.net.defaulthandle="println(method + ' of ' + klass + ': ' + ((time/1000000) as long) + ' ms')" foo\Foo.groovy
 * </pre>
 * 
 * Then the ouputs of them will be:
 * <pre>
 * java.lang.Object foo() of foo.Foo: xxx ms
 * </pre>
 * 
 * @author Nagai Masato
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.TYPE})
@GroovyASTTransformationClass("gbench.BenchmarkASTTransformation")
public @interface Benchmark {
    public static interface BenchmarkHandler {
        public void handle(Object klass, Object method, Object time);
    }
    static class DefaultBenchmarkHandler implements BenchmarkHandler {
        static final DefaultBenchmarkHandler instance = new DefaultBenchmarkHandler();
        GroovyShell shell;
        String handle;
        DefaultBenchmarkHandler() {
            String handle = System.getProperty("groovybenchmark.sf.net.defaulthandle");
            if (handle != null) {
                this.handle = handle;
                shell = new GroovyShell();
            }
        }
        public void handle(Object klass, Object method, Object time) {
            if (handle != null) {
                shell.setVariable("klass", klass);
                shell.setVariable("method", method);
                shell.setVariable("time", time);
                shell.evaluate(handle);
            } else {
                System.out.println(klass + " " + method + ": " + time + " ns");
            }
        }
        public static DefaultBenchmarkHandler getInstance() {
            return instance;
        }
    };
    Class<?> value() default DefaultBenchmarkHandler.class;
}

