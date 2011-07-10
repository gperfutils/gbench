/*
 * Copyright 2011 Nagai Masato
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
package gbench;

import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import gbench.Benchmark.BenchmarkHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

/**
 * The AST transform to handle {@code @Benchmark} annotation
 * 
 * @author Nagai Masato
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class BenchmarkASTTransformation implements ASTTransformation {
 
    static final String BEFORE_VAR; 
    static final String TIME_VAR;
    static final String METHOD_VAR;
    static final String CLASS_VAR;
    static {
        String pfx = BenchmarkASTTransformation.class.getName().replaceAll("\\.", "_").toLowerCase();
        BEFORE_VAR = pfx + "_b";
        TIME_VAR = pfx + "_t";
        METHOD_VAR = pfx + "_m";
        CLASS_VAR = pfx + "_c";
    }
    
    static final Class<?> MY_CLASS = Benchmark.class;
    static final ClassNode MY_TYPE = ClassHelper.make(MY_CLASS);
    static final ClassNode SYSTEM_TYPE = ClassHelper.make(System.class);
    static final ClassNode DEFAULT_HANDLER_TYPE = 
        ClassHelper.make(Benchmark.DefaultBenchmarkHandler.class);
    static final ClassNode CLOSURE_HANDLER_TYPE = ClassHelper.make(ClosureBenchmarkHandler.class);
    
    static final Token MINUS = Token.newSymbol("-", -1, -1);
    static final Token PLUS = Token.newSymbol("+", -1, -1);
    static final Token ASSIGN = Token.newSymbol(Types.ASSIGN, -1, -1);
    
    /**
     * <pre>
     * def method() {
     *     // body
     * }
     * </pre>
     * will be converted to: 
     * <pre>
     * def method() {
     *     def b
     *     b = System.nanoTime()
     *     try {
     *         // body
     *     } finally {
     *         handler.handle(method, System.nanoTime() - b)
     *     }
     *  }
     *  </pre>
     */
    public void visit(ASTNode[] nodes, SourceUnit source) {
        if (nodes.length != 2 || 
                !(nodes[0] instanceof AnnotationNode) || 
                !(nodes[1] instanceof AnnotatedNode)) {
            throw new RuntimeException(
                "Internal error: expecting [AnnotationNode, AnnotatedNode] but got: " + 
                Arrays.asList(nodes)
            );
        }
        AnnotationNode annotation = (AnnotationNode) nodes[0];
        if (!MY_TYPE.equals(annotation.getClassNode())) {
            return;
        }
        AnnotatedNode parent = (AnnotatedNode) nodes[1]; 
        if (parent instanceof MethodNode) {
            transform((MethodNode) parent, annotation);
        } else if (parent instanceof ClassNode) {
            transform((ClassNode) parent, annotation); 
        }
    }
    
    boolean hasOwnBenchmark(AnnotatedNode node) {
        return !node.getAnnotations(MY_TYPE).isEmpty();
    }
    
    void transform(ClassNode klass, AnnotationNode benchmark) {
        for (MethodNode method : klass.getMethods()) {
            if (hasOwnBenchmark(method)) {
                continue;
            }
            transform(method, benchmark);
        }
    }
    
    void transform(MethodNode method, AnnotationNode benchmark) {
        List<Statement> statements = ((BlockStatement) method.getCode()).getStatements();
        
        BlockStatement tryStatement = new BlockStatement();
        tryStatement.addStatements(statements);
        statements.clear();
        
        BlockStatement finallyStatement = new BlockStatement();
        finallyStatement.addStatement(var(TIME_VAR));
        finallyStatement.addStatement(assign(
            TIME_VAR,
            new BinaryExpression(
                currentTime(),
                MINUS,
                new VariableExpression(BEFORE_VAR)
            )
        ));
        finallyStatement.addStatement(var(CLASS_VAR));
        finallyStatement.addStatement(assign(
            CLASS_VAR, new ConstantExpression(method.getDeclaringClass().getName())
        ));
        finallyStatement.addStatement(var(METHOD_VAR));
        finallyStatement.addStatement(assign(
            METHOD_VAR, new ConstantExpression(method.getTypeDescriptor())
        ));
        finallyStatement.addStatement(handleBenchmark(benchmark));
         
        statements.add(var(BEFORE_VAR));
        statements.add(assign(BEFORE_VAR, currentTime()));
        statements.add(new TryCatchStatement(tryStatement, finallyStatement));
    }

    static class ClosureBenchmarkHandler implements BenchmarkHandler {
        static class Delegate extends GroovyObjectSupport {
            Map<String, Object> propMap;
            Delegate(Object[] props) {
                propMap = new HashMap<String, Object>(props.length / 2);
                for (int i = 0, n = props.length; i < n; i += 2) {
                    propMap.put(String.valueOf(props[i]), props[i + 1]);
                }
            }
            @Override
            public Object getProperty(String property) {
                if (propMap.containsKey(property)) {
                    return propMap.get(property);
                }
                return super.getProperty(property);
            }
        }
        Closure clos;
        public ClosureBenchmarkHandler(Closure clos) {
            this.clos = clos;
        }
        public void handle(Object klass, Object method, Object time) {
            clos.setDelegate(new Delegate(new Object[] { 
                CLASS_VAR, klass, METHOD_VAR, method, TIME_VAR, time }
            ));
            clos.setResolveStrategy(Closure.DELEGATE_FIRST);
            clos.call();
        }
    }
    Statement handleBenchmark(AnnotationNode benchmark) {
        Expression value = benchmark.getMember("value");
        Statement handleStatement;
        if (value == null || value instanceof ClassExpression) {
            ClassNode handler = 
                value != null ? ((ClassExpression) value).getType() : DEFAULT_HANDLER_TYPE;
            handleStatement = new ExpressionStatement(
                new MethodCallExpression(
                    new StaticMethodCallExpression(
                        handler,
                        "getInstance",
                        ArgumentListExpression.EMPTY_ARGUMENTS
                    ),
                    "handle",
                    new ArgumentListExpression(
                        new VariableExpression(CLASS_VAR), 
                        new VariableExpression(METHOD_VAR), 
                        new VariableExpression(TIME_VAR)
                    )
                )
            );
        } else if (value instanceof ClosureExpression) {
            ClosureExpression clos = (ClosureExpression) value;
            List<Statement> statements = new ArrayList<Statement>(); 
            statements.add(var("klass"));
            statements.add(var("method"));
            statements.add(var("time"));
            statements.add(assign("klass", new VariableExpression(CLASS_VAR)));
            statements.add(assign("method", new VariableExpression(METHOD_VAR)));
            statements.add(assign("time", new VariableExpression(TIME_VAR)));
            
            BlockStatement statement = (BlockStatement) clos.getCode();
            List<Statement> originalStatements = statement.getStatements();
            statements.addAll(originalStatements);
            originalStatements.clear();
            statement.addStatements(statements);
            handleStatement = new ExpressionStatement(
                new MethodCallExpression(
                    new ConstructorCallExpression(
                        CLOSURE_HANDLER_TYPE,
                        new ArgumentListExpression((ClosureExpression) value)
                    ),
                    "handle",
                    new ArgumentListExpression(
                        new VariableExpression(CLASS_VAR), 
                        new VariableExpression(METHOD_VAR), 
                        new VariableExpression(TIME_VAR)
                    )
                )
            );
        } else {
            handleStatement = EmptyStatement.INSTANCE;
        }
        return handleStatement;
    }
    
    Statement var(String name) {
        return new ExpressionStatement(
            new DeclarationExpression(
                new VariableExpression(name),
                ASSIGN,
                new ConstantExpression(null)
            )
        );
        
    }
    
    Statement assign(String name, Expression init) {
        return new ExpressionStatement(
            new BinaryExpression(
                new VariableExpression(name),
                ASSIGN,
                init
            )
        );
    }
    
    Expression currentTime() {
        return new MethodCallExpression(
            new ClassExpression(SYSTEM_TYPE),
            "nanoTime",
            ArgumentListExpression.EMPTY_ARGUMENTS
        );
    }
}