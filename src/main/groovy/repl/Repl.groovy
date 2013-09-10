package repl

import org.codehaus.groovy.tools.shell.Groovysh
import org.codehaus.groovy.tools.shell.IO


class Repl{


    static def repl(def args){
        def out =  new PrintStream(new FileOutputStream(FileDescriptor.out))
        def inStream = new BufferedInputStream(new FileInputStream(FileDescriptor.in))
        def io = new IO(inStream, out, out)
        Groovysh shell = new Groovysh(new Binding(args), io)
		def interpreter = shell.interp.shell.config.setTargetDirectory("/tmp")
        shell.run()
        println("\nshell exited")
    }
}
