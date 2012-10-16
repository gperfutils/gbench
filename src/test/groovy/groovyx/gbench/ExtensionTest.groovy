package groovyx.gbench

import org.junit.Test

/* $if version < 2.0.0 $ */
@org.junit.Ignore
/* $endif$ */
class ExtensionTest {
/* $if version >= 2.0.0 $ */
    
    @Test void testClosureInstanceTest() {
        def bmList = { -> 
            'foo' {}    
            'bar' {}
        }.benchmark(measureCpuTime:0, warmUpTime:0)        
    }
    
    @Test void testObjectStaticTest() {
        def bmList = benchmark(measureCpuTime:0, warmUpTime:0) {
            'foo' {}    
            'bar' {}
        }    
    }
    
/* $endif$ */
} 