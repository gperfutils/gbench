package groovyx.gbench


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

    static def callAndGetStderr(expression) {
        def stderr = System.err;
        def baos = new ByteArrayOutputStream()
        System.err = new PrintStream(baos)
        expression()
        def output = baos.toString()
        System.err = stderr
        output
    }

    static def firstLine(s) {
        s.readLines()[0]
    }

    static def simulateOperation() {
        "foo"
    }

    static def defaultAssert(actual) {
        // TODO system time is often negative number
        assert actual.matches(
                'groovyx.gbench\\..+\\s\\sjava\\.lang\\.Object\\s[a-zA-Z0-9]+\\(.*\\)\\s\\s' + (
                    BenchmarkSystem.isMeasureCpuTime() && java.lang.management.ManagementFactory.getThreadMXBean().isCurrentThreadCpuTimeSupported() ?
                        'user:[0-9]+\\ssystem:[-0-9]+\\scpu:[0-9]+\\sreal:[0-9]+' : '[0-9]+'
                 )
            )
    }

    static def customAssert(actual) {
        // TODO system time is often negative number
        assert actual.matches(
                'groovyx.gbench\\..+\\sof\\sjava\\.lang\\.Object\\s[a-zA-Z0-9]+\\(.*\\)\\t' +
                'user:[0-9]+\\ssystem:[-0-9]+\\scpu:[0-9]+\\sreal:[0-9]+'
            )
    }

}
