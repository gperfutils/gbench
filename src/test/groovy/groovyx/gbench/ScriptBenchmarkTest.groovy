package groovyx.gbench

import groovyx.gbench.BenchmarkTestUtilities as U

import org.junit.Test

import groovy.lang.Singleton

class ScriptBenchmarkTest {
    
    def proxy = new ScriptBenchmarkTestProxy()
    
    @Test void testUseDefaultHandler() {
        proxy.testUseDefaultHandler()
    }
    
    @Test void testUseCustomHandler() {
        proxy.testUseCustomHandler()
    }
}