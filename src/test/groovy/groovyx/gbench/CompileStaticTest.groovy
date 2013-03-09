package groovyx.gbench;

import org.junit.Test;

/* $if version < 2.0.0 $ */
@org.junit.Ignore
/* $endif$ */
class CompileStaticTest {
/* $if version >= 2.0.0 $ */

    @groovy.transform.CompileStatic
    @Benchmark
    void compiledMethod() {
    }

    @groovy.transform.TypeChecked
    @Benchmark
    void typeCheckedMethod() {
    }

    @Test public void testStaticCompiledMethod() throws Exception {
        compiledMethod();
    }

    @Test public void testTypeCheckedMethod() throws Exception {
        typeCheckedMethod();
    }

/* $endif$ */
}