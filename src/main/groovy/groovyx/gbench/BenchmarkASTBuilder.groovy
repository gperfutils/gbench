/*
 * Copyright 2012 Masato Nagai
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovyx.gbench

import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.VariableScope
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.syntax.Token
import org.codehaus.groovy.syntax.Types

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
        "returnStatement": org.codehaus.groovy.ast.stmt.ReturnStatement,
        // expression
        "argumentList": org.codehaus.groovy.ast.expr.ArgumentListExpression,
        "binary": org.codehaus.groovy.ast.expr.BinaryExpression,
        "booleanExpression": org.codehaus.groovy.ast.expr.BooleanExpression,
        "classExpression": org.codehaus.groovy.ast.expr.ClassExpression,
        // "closure": org.codehaus.groovy.ast.expr.ClosureExpression,
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
            } catch (e) {
                e.printStackTrace()
            }
        }
        throw new MissingMethodException(name, BenchmarkASTBuilder, args)
    }

    private ClosureExpression closure(Parameter[] parameters, Statement statement) {
        ClosureExpression e = new ClosureExpression(parameters, statement)
        // The variableScope is required by groovyc.
        // Why ClosureExpression doesn't have it in its constructor parameters?
        e.variableScope = statement.variableScope.copy()
        return e
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

    private Parameter[] parameters() {
        Parameter.EMPTY_ARRAY
    }

    def build(Closure c) {
        c.resolveStrategy = Closure.DELEGATE_FIRST
        c.delegate = this
        c()
    }

}
