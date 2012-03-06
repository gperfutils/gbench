def bm = new gbench.BenchmarkBuilder().run(warmUpTime:3, verbose:true) {
    'StringBuilder' {
        def sb = new StringBuilder()        
        sb.append('foo')
        sb.append('bar')
        sb.append('baz')
        sb.toString()
    }
    'StringBuffer' {
        def sb = new StringBuffer()        
        sb.append('foo')
        sb.append('bar')
        sb.append('baz')
        sb.toString()
    }
}
bm.prettyPrint()