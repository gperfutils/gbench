package gbench


class BenchmarkTestUtilities {

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
        assert actual.matches(
                'gbench\\..+\\s\\sjava\\.lang\\.Object\\s[a-zA-Z0-9]+\\(.*\\)\\s\\s' +
                'user:[0-9]+\\ssystem:[0-9]+\\scpu:[0-9]+\\sreal:[0-9]+'
            )
    }
    
    static def customAssert(actual) {
        assert actual.matches(
                'gbench\\..+\\sof\\sjava\\.lang\\.Object\\s[a-zA-Z0-9]+\\(.*\\)\\t' +
                'user:[0-9]+\\ssystem:[0-9]+\\scpu:[0-9]+\\sreal:[0-9]+'
            )    
    }
    
}