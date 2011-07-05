package gbench

class BenchmarkTestUtils {
    
    static def callAndGetStdout(expression) {
        def stdout = System.out;
        def baos = new ByteArrayOutputStream()
        System.out = new PrintStream(baos)
        expression()
        def output = baos.toString()
        System.out = stdout
        output
    }
    
    static def firstLine(s) {
        s.readLines()[0]    
    }
    
    static def simulateOperation() {
        "foo"
    }
    
    static def defaultAssert(actual) {
        if (System.getProperty("gbench.da") == null) {
            assert actual.matches('gbench\\..+ java\\.lang\\.Object [a-zA-Z0-9]+\\(.*\\): [0-9]+ ns')    
        }
        System.out.println(actual)
    }
    
    static def customAssert(actual) {
        if (System.getProperty("gbench.da") == null) {
            assert actual.matches('gbench\\..+ java\\.lang\\.Object [a-zA-Z0-9]+\\(.*\\) = [0-9]+')    
        }
        System.out.println(actual)
    }
}