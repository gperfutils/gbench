package gbench

class BenchmarkTime {
    long real
    long cpu
    long system
    long user
    
    String toString() {
        "user:${user} system:${system} cpu:${cpu} real:${real}"
    } 
}
