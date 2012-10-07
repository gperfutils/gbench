package groovyx.gbench

import org.junit.Test

class ExtensionTest {
    
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

}
