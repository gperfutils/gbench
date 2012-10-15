package groovyx.gbench

import org.junit.Test

import static BenchmarkMath.*

class BenchmarkMathTest {
    
    @Test
    void testLongMean() {
        assert 10D == mean(11L, 9L)    
    }
    
    @Test
    void testLongMdev() {
        assert 1D == mdev(11L, 9L)   
        assert 1D == mdev(6L, 4L)
        assert 2D == mdev(12L, 8L)
        assert 2D == mdev(7L, 3L)
    }
    
    @Test
    void testLongRmdev() { 
        assert 0.1D == rmdev(11L, 9L)   
        assert 0.2D == rmdev(6L, 4L)
        assert 0.2D == rmdev(12L, 8L)
        assert 0.4D == rmdev(7L, 3L)
    }
    
    @Test
    void testLongStdev() {
        assert 1D == stdev(11L, 9L)   
        assert 1D == stdev(6L, 4L)
        assert 2D == stdev(12L, 8L)
        assert 2D == stdev(7L, 3L)
    }

}
