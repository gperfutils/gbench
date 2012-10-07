benchmark(measureCpuTime:false, verbose:true, warmUpTime:0) {
    'foo' { Thread.sleep(1000) }
    'bar' { Thread.sleep(2000) }
}.prettyPrint()

;

{ ->
    'baz' { Thread.sleep(1000) }
    'qux' { Thread.sleep(2000) }
}
.benchmark(measureCpuTime:false, verbose:true, warmUpTime:0)
.prettyPrint()
