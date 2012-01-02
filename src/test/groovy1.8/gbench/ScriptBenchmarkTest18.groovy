package gbench

import gbench.BenchmarkTestUtilities as U

import org.junit.Test

import groovy.lang.Singleton

class ScriptBenchmarkTest18 {
    
    def proxy = new ScriptBenchmarkTestProxy18()
    
    @Test void testUseClosureHandler() {
        proxy.testUseClosureHandler()
    }
    
    @Test void testUseCustomHandlerWithDupArgs() {
        proxy.testUseClosureHandlerWithDupArgs()
    }
}