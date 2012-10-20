package groovyx.gbench

import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.syntax.Token
import org.codehaus.groovy.syntax.Types

/* $if version >= 1.8.0 $ */
@groovy.transform.PackageScope
/* $endif$ */
class BenchmarkASTBuilder {
    
    private static Map AST_CLASSES = [
        "classNode": org.codehaus.groovy.ast.ClassNode,
        "variableScope": org.codehaus.groovy.ast.VariableScope,
        // syntax
        "token": org.codehaus.groovy.syntax.Token,
        // statement
        "block": org.codehaus.groovy.ast.stmt.BlockStatement,
        "expression": org.codehaus.groovy.ast.stmt.ExpressionStatement,
        "ifStatement": org.codehaus.groovy.ast.stmt.IfStatement,
        "tryCatch": org.codehaus.groovy.ast.stmt.TryCatchStatement,
        // expression
        "argumentList": org.codehaus.groovy.ast.expr.ArgumentListExpression,
        "binary": org.codehaus.groovy.ast.expr.BinaryExpression,
        "booleanExpression": org.codehaus.groovy.ast.expr.BooleanExpression,
        "classExpression": org.codehaus.groovy.ast.expr.ClassExpression,
        "closure": org.codehaus.groovy.ast.expr.ClosureExpression,
        "constant": org.codehaus.groovy.ast.expr.ConstantExpression,
        "constructorCall": org.codehaus.groovy.ast.expr.ConstructorCallExpression,
        "declaration": org.codehaus.groovy.ast.expr.DeclarationExpression,
        "empty": org.codehaus.groovy.ast.stmt.EmptyStatement,
        "methodCall": org.codehaus.groovy.ast.expr.MethodCallExpression,
        "staticMethodCall": org.codehaus.groovy.ast.expr.StaticMethodCallExpression,
        "tuple": org.codehaus.groovy.ast.expr.TupleExpression,
        "variable": org.codehaus.groovy.ast.expr.VariableExpression,
    ]
    
    private static Token TOKEN_UNKNOWN = new Token(Types.UNKNOWN, "", -1, -1)
    
    private def methodMissing(String name, args) {
        Class c = AST_CLASSES[name]
        if (c) {
            try {
                return c.newInstance(args)
            } catch (e) {}
        }
        throw new MissingMethodException(name, BenchmarkASTCompiler, args)
    }
    
    private BlockStatement block(Statement...statements) {
        block(statements as List)
    }
    
    private BlockStatement block(List<Statement> statements) {
        block(statements, variableScope())
    }
    
    private Token token(String s) {
        Token t = keyword(s); t != TOKEN_UNKNOWN ? t : symbol(s)
    }
    
    private Token keyword(String s) {
        int type = Types.lookupKeyword(s)
        type == Types.UNKNOWN ? TOKEN_UNKNOWN : token(type, s, -1, -1)
    }

    private Token symbol(String s) {
        int type = Types.lookupSymbol(s)
        type == Types.UNKNOWN ? TOKEN_UNKNOWN : token(type, s, -1, -1)
    }
    
    private ClassExpression classExpression(Class c) {
        classExpression(classNode(c))
    }
    
    def build(Closure c) {
        c.resolveStrategy = Closure.DELEGATE_FIRST
        c.delegate = this
        c()
    }

}
