package gbench;

import groovy.transform.CompileStatic;
import groovy.transform.TypeChecked;

import org.junit.Test;

public class CompileStaticTest {

    @CompileStatic
    @Benchmark
    void compiledMethod() {
    }
    
    @TypeChecked
    @Benchmark
    void typeCheckedMethod() {
    }

    @Test public void testStaticCompiledMethod() throws Exception {
        compiledMethod();
    }
    
    @Test public void testTypeCheckedMethod() throws Exception {
        typeCheckedMethod();
    }
}
