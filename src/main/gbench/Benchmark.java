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
 * An annotation to benchmark methods.
 * <p> 
 * This annotation allows you to benchmark methods without modifying their 
 * existing code. It can be added to  methods or classes.
 * <pre><code>
 * class Klass {
 *     {@code @Benchmark}
 *     def foo() {
 *     }
 *     {@code @Benchmark}
 *     def bar() {
 *     }
 * }
 * 
 * {@code @Benchmark}
 * class Klass {
 *     def foo() {
 *     }
 *     def bar() {
 *     }
 * }
 * </code></pre>
 * 
 * The ouputs of both examples will be:
 * <pre><code>
 * Klass    java.lang.Object foo()    user:xxx system:xxx cpu:xxx real:xxx
 * Klass    java.lang.Object bar()    user:xxx system:xxx cpu:xxx real:xxx
 * </code></pre>
 * 
 * The handling of benchmark results can be customized by using handler classes 
 * that implement BenchmarkHandler interface. Handler classes must have two 
 * methods, handle() and getInstance():
 * <pre><code>
 * class MyHandler implements Benchmark.BenchmarkHandler {
 *     static def instance = new MyHandler()
 *         void handle(klass method, time) {
 *         println("${method} of ${klass}: ${(time.real/1000000) as long} ms")
 * 	   }
 *     static MyHandler getInstance() {
 *         instance
 *     } 
 * }
 * 
 * {@code @Benchmark}(MyHandler.class)
 * def foo() {
 * }
 * </code></pre>
 * 
 * Since Groovy 1.8, closures can be used instead of handler classes. With
 * closures, you just need to assign closures that handle benchmark results:
 * <pre><code>
 * {@code @Benchmark}({println("${method} of ${class}: ${(time.real/1000000) as long} ms")})
 * def foo() {
 * }
 * </code></pre>
 * 
 * also the default handling operation can be replaced with a system property, 
 * "gbench.defaulthandle":
 * <pre><code>
 * groovy -cp gbench-xx.xx.xx.jar -Dgbench.defaulthandle="println(method + ' of ' + klass + ': ' + ((time.real/1000000) as long) + ' ms')" Foo.groovy
 * </code></pre>
 * 
 * Then the ouputs of both examples will be:
 * <pre><code>
 * java.lang.Object foo() of Foo: xxx ms
 * </code></pre>
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
        static final DefaultBenchmarkHandler instance = 
            new DefaultBenchmarkHandler();
        GroovyShell shell;
        String handle;
        DefaultBenchmarkHandler() {
            String handle = 
                System.getProperty("gbench.defaulthandle");
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
                System.out.println(klass + "\t" + method + "\t" + time);
            }
        }
        public static DefaultBenchmarkHandler getInstance() {
            return instance;
        }
    };
    Class<?> value() default DefaultBenchmarkHandler.class;
}

