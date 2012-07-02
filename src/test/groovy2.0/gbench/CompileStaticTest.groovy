package gbench;

import groovy.transform.CompileStatic;

import org.junit.Test;

public class CompileStaticTest {

    @CompileStatic
    @Benchmark
    int compiledMethod() {
        return 0;
    }

    @Test public void testStaticCompiledMethod() throws Exception {
        compiledMethod();
    }
}
