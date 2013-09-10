package repl

import org.codehaus.groovy.transform.GroovyASTTransformation  
import org.codehaus.groovy.ast.ASTNode  
import org.codehaus.groovy.control.SourceUnit  
import org.codehaus.groovy.transform.ASTTransformation  
import org.codehaus.groovy.control.CompilePhase  
import org.codehaus.groovy.ast.MethodNode  
import org.codehaus.groovy.ast.ClassNode  
import org.codehaus.groovy.ast.Parameter  
import org.codehaus.groovy.ast.stmt.*  
import org.codehaus.groovy.ast.expr.*  
import org.codehaus.groovy.ast.*
import java.util.logging.*




@GroovyASTTransformation(phase=CompilePhase.CONVERSION)  
public class ReplASTTransformation implements ASTTransformation   
{  
    def log = Logger.getLogger(this.class.simpleName)
    public ReplASTTransformation(){
        log.level = Level.OFF
    }
    
    public class DeclarationFinder extends CodeVisitorSupport 
    {
        
        def scopesPerGdb = new LinkedList([ [scopes:[[decs:[]]]]  ]) //list of declaration expressions that preceed each gdb call expression
        def gdbCalls = [] //list of gdb methodCallExpressions 
        /*
          keeps track of decs in the in the current expr tree so we can exclude them from the context in the shell
          for example below, var a cannot go  in the context:
          def a = { com.gdb.GdbShell.gdb() } 
         */
        def mostRecentDec = []
        def mostRecentParameterList = []

        public DeclarationFinder(topLevelVars = []){
            topLevelVars.each {
                scopesPerGdb.last().scopes.last().decs << new DeclarationExpression(new VariableExpression(it.name), null, null)
            }
        }

        public void visitDeclarationExpression(DeclarationExpression expr)
        {
            log.info("dec expression")
            scopesPerGdb.last().scopes.last().decs << expr
            mostRecentDec << expr
            super.visitDeclarationExpression(expr)
            mostRecentDec.pop()

        }
        public void visitClosureListExpression(ClosureListExpression expr){
            log.info("closurelistexp")

        }

        public void visitBlockStatement(BlockStatement stmt){
            scopesPerGdb.last().scopes << [decs:mostRecentParameterList]
            mostRecentParameterList = []
            super.visitBlockStatement(stmt)
            scopesPerGdb.last().scopes.pop()
            
        }
        public void visitClosureExpression(ClosureExpression expr){
            log.info("closureexp")

            expr.getParameters().each{
                mostRecentParameterList << new DeclarationExpression(new VariableExpression(it.name), null, null)
            }
            super.visitClosureExpression(expr)

        }

        public void visitMethodCallExpression(MethodCallExpression expr){
            log.info("methodcalexpression")
            if(expr.getMethodAsString() == "repl"){

                //todo: fix, removes dec from scopes too aggressively 
                if(mostRecentDec.size() > 0)
                    scopesPerGdb.last().scopes[-2].decs.removeAll{ it.is(mostRecentDec.last()) }

                gdbCalls << expr
                scopesPerGdb << deepCopyCollectionsAndMapsOnly(scopesPerGdb.last())
                return
            }
            super.visitMethodCallExpression(expr)
        }

        private def deepCopyCollectionsAndMapsOnly(def original){

            if(original instanceof Collection){
                def copy = original.getClass().newInstance()
                original.each{ copy << deepCopyCollectionsAndMapsOnly(it)}
                return copy
            }

            if(original instanceof Map){
                def copy = original.getClass().newInstance()
                original.each{ copy[deepCopyCollectionsAndMapsOnly(it.key)] = deepCopyCollectionsAndMapsOnly(it.value)}
                return copy
            }

            return original

        }
    }








    //entry point
    public void visit(ASTNode[] nodes, SourceUnit source)  
    {  
        def classNodes = source.getAST().getClasses()

        log.info(classNodes.toString())
        classNodes.each{classNode ->
            def methods = classNode.getMethods()
            methods.each{def method ->   
                def (scopesPerGdbCall, gdbCalls) = findAllDeclarationsPerGdbCall(method)
                replaceGdbCallArguments(scopesPerGdbCall, gdbCalls)
            }

           ////////////////////////////properties
            log.info("properties")
            def properties = classNode.getFields()
            properties.each{def prop -> 
                log.info(prop.toString())
                def (scopesPerGdbCall, gdbCalls) = findAllDeclarationsPerGdbCall(prop.getInitialExpression())
                replaceGdbCallArguments(scopesPerGdbCall, gdbCalls)
            }

        }
    }


                                         //methodNode or propertyNode ie: def a = { ... }
    private def findAllDeclarationsPerGdbCall(def method){

        DeclarationFinder dcF = null;
        if(method instanceof MethodNode){ //standard method
            dcF = new DeclarationFinder(method.getParameters());
            def statements = null
            try{
                statements = method.getCode()?.getStatements()
            }catch(MissingMethodException e){
                //not all methodNodes have a getStatments() function??
                //really do not uderstand this
            }
            statements.each{
                it.visit(dcF)
            }
        }else{ //propertyNode, closure style
            dcF = new DeclarationFinder();
            if(method)
                method.visit(dcF)
        } 
        return [dcF.scopesPerGdb, dcF.gdbCalls]
    }

    private void replaceGdbCallArguments(def scopesPerGdbCall, def gdbCallExpressions){
        gdbCallExpressions.each{
            def vars = []
            def decs = scopesPerGdbCall.poll().scopes.flatten().decs.flatten()
            decs.each{ dec->
                    if(dec.multipleAssignmentDeclaration){
                    dec.leftExpression.expressions.each{vars << it}
                }else {
                    vars << dec.leftExpression
                }
            }            
            log.info((vars*.name).toString())
            it.setArguments(createGdbArgList(vars))
        }
    }
    
    private def createGdbArgList(def vars){  
        def mapEntryOfVars = []
        vars.each{var ->
                mapEntryOfVars << new MapEntryExpression(new ConstantExpression(var.name), new VariableExpression(var.name))
        }

        //add this refence to context, but need to name it self
        mapEntryOfVars << new MapEntryExpression(new ConstantExpression("self"), new VariableExpression("this"))
        return  new NamedArgumentListExpression(mapEntryOfVars)
    }
}
