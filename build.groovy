def project = [
    groupId: "com.google.gbench",
    artifactId: "gbench",
    version: new Date().format("yy.MM.dd"),
]

def srcdir = "src/main"
def testdir = "src/test"
def builddir = "build"
def distdir = "dist"
def verbose = "false"

def ant = new AntBuilder()
ant.mkdir(dir: builddir)
ant.mkdir(dir: distdir)

ant.echo(message: "Cleaning...")
ant.delete(
    verbose: verbose,
    {
        fileset(dir: builddir)
    }
)
ant.delete(
    verbose: verbose,
    {
        fileset(dir: distdir)
    }
)

ant.echo(message: "Generating pom...")
def pom = new File("${distdir}/${project.artifactId}-${project.version}.pom")
pom.createNewFile()
def pomBuilder = new groovy.xml.MarkupBuilder(pom.newPrintWriter("UTF-8"))
pomBuilder.project {
    modelVersion("4.0.0")    
    groupId(project.groupId)
    artifactId(project.artifactId)
    packaging("jar")
    name("GBench")
    version(project.version)
    description("GBench is a benchmarking framework for Groovy")
    url("http://code.google.com/p/gbench")
    licenses {
        license {
            name("The Apache Software License, Version 2.0")
            url("http://www.apache.org/licenses/LICENSE-2.0.txt")
            distribution("repo")
        }    
    }
    parent {
        groupId("org.sonatype.oss")    
        artifactId("oss-parent")
        version("7")
    }
    scm {
        connection("scm:svn:http://gbench.googlecode.com/svn")    
        developerConnection("scm:svn:https://gbench.googlecode.com/svn")
        url("http://code.google.com/p/gbench/source/browse")
    }
}

ant.echo(message: "Compiling...")
def classesdir = "${builddir}/classes"
ant.mkdir(dir: classesdir)
ant.taskdef(name: "groovyc", classname: "org.codehaus.groovy.ant.Groovyc")
ant.groovyc(
    srcdir: srcdir,
    destdir: classesdir, 
    verbose: verbose,
    {
        javac(source: "1.5", target: "1.5", debug: "on")    
    }
)
ant.echo(message: "Packaging classes...")
ant.jar(
    destfile: "${distdir}/${project.artifactId}-${project.version}.jar",
    basedir: classesdir,
    update: "true",
)

ant.echo(message: "Testing...")
ant.taskdef(name: "groovy", classname: "org.codehaus.groovy.ant.Groovy")
ant.groovy(
    src: "${testdir}/gbench/BenchmarkTest.groovy",
    classpath: "${testdir};${distdir}/${project.artifactId}-${project.version}.jar",
)

ant.echo(message: "Packaging sources...")
ant.zip(
    destfile: "${distdir}/${project.artifactId}-${project.version}-sources.jar",
    basedir: ".",
    includes: "${srcdir}/**",
    update: "true",
)   

ant.echo(message: "Generating groovydoc...")
ant.taskdef(name: "groovydoc", classname: "org.codehaus.groovy.ant.Groovydoc")
ant.groovydoc(
        destdir: "${builddir}/doc",
        sourcepath: srcdir,
        packagenames: "**.*",
        use: "true",
        private: "false", 
    {
       link(packages:"java.,org.xml.,javax.,org.xml.",href:"http://download.oracle.com/javase/6/docs/api")
       link(packages:"groovy.,org.codehaus.groovy.",  href:"http://groovy.codehaus.org/api")
       link(packages:"org.apache.tools.ant.",         href:"http://evgeny-goldin.org/javadoc/ant/api")
       link(packages:"org.junit.,junit.framework.",   href:"http://kentbeck.github.com/junit/javadoc/latest")
       link(packages:"org.codehaus.gmaven.",          href:"http://evgeny-goldin.org/javadoc/gmaven")
    }
)
ant.echo(message: "Packaging groovydoc...")
ant.zip(
    destfile: "${distdir}/${project.artifactId}-${project.version}-javadoc.jar",
    basedir: builddir,
    includes: "doc/**",
    update: "true",
)

ant.echo(message: "Done!")