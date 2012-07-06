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


import groovy.lang.Closure
import groovy.lang.GroovyObjectSupport

import java.lang.management.ManagementFactory
import java.util.Arrays
import java.util.Map

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotatedNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ConstructorCallExpression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
import org.codehaus.groovy.ast.expr.TupleExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

/**
 * The AST transform to handle {@code @Benchmark} annotation
 * 
 * @author Nagai Masato
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
class BenchmarkASTTransformation implements ASTTransformation {
 
    static final ClassNode MY_TYPE = ClassHelper.make(Benchmark.class)
    static final ClassNode DEFAULT_HANDLER_TYPE = 
        ClassHelper.make(Benchmark.DefaultBenchmarkHandler.class)
    static final ClassNode CLOSURE_HANDLER_TYPE = 
        ClassHelper.make(ClosureBenchmarkHandler.class)
    
    void visit(ASTNode[] nodes, SourceUnit source) {
        if (nodes.length != 2 || 
                !(nodes[0] instanceof AnnotationNode) || 
                !(nodes[1] instanceof AnnotatedNode)) {
            throw new RuntimeException(
                "Internal error: expecting [AnnotationNode, AnnotatedNode] " +
                "but got: ${Arrays.asList(nodes)}"
            );
        }
        def annotation = nodes[0]
        if (MY_TYPE != annotation.classNode) {
            return
        }
        if (BenchmarkSystem.isMeasureCpuTime() && 
            !ManagementFactory.threadMXBean.currentThreadCpuTimeSupported) {
            System.err.println("The JVM doesn't support CPU time measurement.")
        }
        def parent = nodes[1] 
        if (parent instanceof MethodNode) {
            transform((MethodNode) parent, annotation)
        } else if (parent instanceof ClassNode) {
            transform((ClassNode) parent, annotation)
        }
    }
    
    boolean hasOwnBenchmark(AnnotatedNode node) {
        return !node.getAnnotations(MY_TYPE).isEmpty();
    }
    
    void transform(ClassNode klass, AnnotationNode benchmark) {
        for (method in klass.methods) {
            if (hasOwnBenchmark(method)) {
                continue;
            }
            transform(method, benchmark);
        }
    }
   
    void transform(MethodNode method, AnnotationNode benchmark) {
        if (!(method.code instanceof BlockStatement)) {
            return
        }
        def statements = new AstBuilder().buildFromSpec {
            expression {
                declaration {
                    owner.expression.add(new VariableExpression(
                            '__gbench_measureCpuTime', ClassHelper.make(boolean.class)
                        ))
                    token '='
                    binary {
                        methodCall {
                            classExpression gbench.BenchmarkSystem
                            constant 'isMeasureCpuTime'
                            argumentList()
                        }
                        token '&&'
                        methodCall {
                            classExpression java.lang.management.ManagementFactory
                            constant 'getThreadMXBean'
                            argumentList()
                        }
                    }
                }
            }
            expression {
                declaration {
                    owner.expression.add(new VariableExpression(
                            '__gbench_bReal', ClassHelper.make(long.class)
                        ))
                    token '='
                    methodCall {
                        classExpression System
                        constant 'nanoTime'
                        argumentList()
                    }
                }
            }
            expression {
                declaration {
                    owner.expression.add(new VariableExpression(
                            '__gbench_mxBean', ClassHelper.make(java.lang.management.ThreadMXBean.class)
                        ))
                    token '='
                    constant()
                }
            }
            expression {
                declaration {
                    owner.expression.add(new VariableExpression(
                            '__gbench_bCpu', ClassHelper.make(long.class)
                        ))
                    token '='
                    constant 0
                }
            }
            expression {
                declaration {
                    owner.expression.add(new VariableExpression(
                            '__gbench_bUser', ClassHelper.make(long.class)
                        ))
                    token '='
                    constant 0
                }
            }
            ifStatement {
                booleanExpression {
                    owner.expression.add(new VariableExpression(
                            '__gbench_measureCpuTime', ClassHelper.make(boolean.class)
                        ))
                }    
                block {
                    expression {
                        binary {
                            owner.expression.add(new VariableExpression(
                                    '__gbench_mxBean', ClassHelper.make(java.lang.management.ThreadMXBean.class)
                                ))
                            token '='
                            methodCall {
                                classExpression java.lang.management.ManagementFactory
                                constant 'getThreadMXBean'
                                argumentList()
                            }
                        }
                    }
                    expression {
                        binary {
                            owner.expression.add(new VariableExpression(
                                    '__gbench_bCpu', ClassHelper.make(long.class)
                                ))
                            token '='
                            methodCall {
                                owner.expression.add(new VariableExpression(
                                        '__gbench_mxBean', ClassHelper.make(java.lang.management.ThreadMXBean.class)
                                    ))
                                constant 'getCurrentThreadCpuTime'
                                argumentList()
                            }
                        }
                    }
                    expression {
                        binary {
                            owner.expression.add(new VariableExpression(
                                    '__gbench_bUser', ClassHelper.make(long.class)
                                ))
                            token '='
                            methodCall {
                                owner.expression.add(new VariableExpression(
                                        '__gbench_mxBean', ClassHelper.make(java.lang.management.ThreadMXBean.class)
                                    ))
                                constant 'getCurrentThreadUserTime'
                                argumentList()
                            }
                        }
                    }
                }
                empty()
            }
            tryCatch {
                block {
                    owner.expression.addAll method.code.statements    
                }
                block {
                    expression {
                        declaration {
                            owner.expression.add(new VariableExpression(
                                    '__gbench_cpu', ClassHelper.make(long.class)
                                ))
                            token '='
                            constant 0
                        }
                    }
                    expression {
                        declaration {
                            owner.expression.add(new VariableExpression(
                                    '__gbench_user', ClassHelper.make(long.class)
                                ))
                            token '='
                            constant 0
                        }
                    }
                    expression {
                        declaration {
                            owner.expression.add(new VariableExpression(
                                    '__gbench_system', ClassHelper.make(long.class)
                                ))
                            token '='
                            constant 0
                        }
                    }
                    ifStatement {
                        booleanExpression {
                            owner.expression.add(new VariableExpression(
                                    '__gbench_measureCpuTime', ClassHelper.make(boolean.class)
                                ))
                        }    
                        block {
                            expression {
                                binary {
                                    owner.expression.add(new VariableExpression(
                                            '__gbench_user', ClassHelper.make(long.class)
                                        ))
                                    token '='
                                    binary {
                                        methodCall {
                                            owner.expression.add(new VariableExpression(
                                                    '__gbench_mxBean', ClassHelper.make(java.lang.management.ThreadMXBean.class)
                                                ))
                                            constant 'getCurrentThreadUserTime'
                                            argumentList()
                                        }
                                        token '-'
                                        owner.expression.add(new VariableExpression(
                                                '__gbench_bUser', ClassHelper.make(long.class)
                                            ))
                                    }
                                }
                            }    
                            expression {
                                binary {
                                    owner.expression.add(new VariableExpression(
                                            '__gbench_cpu', ClassHelper.make(long.class)
                                        ))
                                    token '='
                                    binary {
                                        methodCall {
                                            owner.expression.add(new VariableExpression(
                                                    '__gbench_mxBean', ClassHelper.make(java.lang.management.ThreadMXBean.class)
                                                ))
                                            constant 'getCurrentThreadCpuTime'
                                            argumentList()
                                        }
                                        token '-'
                                        owner.expression.add(new VariableExpression(
                                                '__gbench_bCpu', ClassHelper.make(long.class)
                                            ))
                                    }
                                }
                            }    
                            expression {
                                binary {
                                    owner.expression.add(new VariableExpression(
                                            '__gbench_system', ClassHelper.make(long.class)
                                        ))
                                    token '='
                                    binary {
                                        owner.expression.add(new VariableExpression(
                                                '__gbench_cpu', ClassHelper.make(long.class)
                                            ))
                                        token '-'
                                        owner.expression.add(new VariableExpression(
                                                '__gbench_user', ClassHelper.make(long.class)
                                            ))
                                    }
                                }
                            }    
                        }
                        empty()
                    }
                    expression {
                        declaration {
                            owner.expression.add(new VariableExpression(
                                    '__gbench_real', ClassHelper.make(long.class)
                                ))
                            token '='
                            binary {
                                methodCall {
                                    classExpression System
                                    constant 'nanoTime'
                                    argumentList()
                                }
                                token '-'
                                owner.expression.add(new VariableExpression(
                                        '__gbench_bReal', ClassHelper.make(long.class)
                                    ))
                            }
                        }
                    }    
                    expression {
                        declaration {
                            owner.expression.add(new VariableExpression(
                                    '__gbench_time', ClassHelper.make(BenchmarkTime.class)
                                ))
                            token '='
                            owner.expression.add(new ConstructorCallExpression(
                                ClassHelper.make(BenchmarkTime.class),
                                new TupleExpression(Arrays.asList(
                                    new VariableExpression(
                                        '__gbench_real', ClassHelper.make(long.class)
                                    ),
                                    new VariableExpression(
                                        '__gbench_cpu', ClassHelper.make(long.class)
                                    ),
                                    new VariableExpression(
                                        '__gbench_system', ClassHelper.make(long.class)
                                    ),
                                    new VariableExpression(
                                        '__gbench_user', ClassHelper.make(long.class)
                                    )
                                ))
                            ))
                        }
                    }    
                    expression {
                        declaration {
                            owner.expression.add(new VariableExpression(
                                    '__gbench_class', ClassHelper.make(String.class)
                                ))
                            token '='
                            constant(method.declaringClass.name)
                        }
                    }
                    expression {
                        declaration {
                            owner.expression.add(new VariableExpression(
                                    '__gbench_method', ClassHelper.make(String.class)
                                ))
                            token '='
                            constant(method.typeDescriptor)
                        }
                    }
                    owner.expression.addAll handleBenchmark(benchmark)
                }
            }
        }
        method.code.statements.clear()
        method.code.statements += statements
    }

    Statement handleBenchmark(AnnotationNode benchmark) {
        def handleExpression = benchmark.getMember('value')
        def handleStatement
        if (!handleExpression || handleExpression instanceof ClassExpression) {
            def handleClass = handleExpression ? 
                                handleExpression.type : DEFAULT_HANDLER_TYPE
            handleStatement = new AstBuilder().buildFromSpec({
                expression {
                    methodCall {
                        owner.expression << 
                            new StaticMethodCallExpression(
                                handleClass,
                                'getInstance',
                                ArgumentListExpression.EMPTY_ARGUMENTS
                            )
                        // staticMethodCall (handleClass.typeClass, 'getInstance') {
                        //     argumentList()
                        // }
                        constant 'handle'
                        argumentList {
                            variable '__gbench_class'    
                            variable '__gbench_method'
                            variable '__gbench_time'
                        }
                    }    
                }
            })[0]
        } else if (handleExpression instanceof ClosureExpression) {
            def statements = new AstBuilder().buildFromSpec({
                expression {
                    declaration {
                        variable 'klass'
                        token '='
                        variable '__gbench_class'
                    }
                }
                expression {
                    declaration {
                        variable 'method'
                        token '='
                        variable '__gbench_method'
                    }
                }
                expression {
                    declaration {
                        variable 'time'
                        token '='
                        variable '__gbench_time'
                    }
                }
            }) + handleExpression.code.statements
            handleExpression.code.statements.clear()
            handleExpression.code.statements += statements
            
            handleStatement = new AstBuilder().buildFromSpec({
                expression {
                    methodCall {
                        owner.expression << 
                            new ConstructorCallExpression(
                                CLOSURE_HANDLER_TYPE,
                                new ArgumentListExpression(
                                    (ClosureExpression) handleExpression)
                            )
                        // constructorCall(CLOSURE_HANDLER_TYPE.typeClass) {
                        //     argumentList {
                        //         (ClosureExpression) handleExp
                        //     }
                        // }
                        constant 'handle'
                        argumentList {
                            variable '__gbench_class'    
                            variable '__gbench_method'
                            variable '__gbench_time'
                        }
                    }
                }
            })[0]
        } else {
            handleStatement = EmptyStatement.INSTANCE
        }
        return handleStatement
    }
    
    static class ClosureBenchmarkHandler implements Benchmark.BenchmarkHandler {
        
        Closure clos;
        
        ClosureBenchmarkHandler(Closure clos) {
            this.clos = clos;
        }
        
        void handle(Object klass, Object method, Object time) {
            clos.delegate = new Delegate(
                '__gbench_class', klass, '__gbench_method', method, '__gbench_time', time
            )
            clos.resolveStrategy = Closure.DELEGATE_FIRST
            clos()
        }
        
        static class Delegate extends GroovyObjectSupport {
            Map propMap;
            Delegate(Object[] props) {
                propMap = [:]
                (0..<props.length).step(2) { i ->
                    propMap[props[i]] = props[i + 1]
                }
            }
            @Override
            Object getProperty(String property) {
                if (propMap.containsKey(property)) {
                    return propMap[property];
                }
                return super.getProperty(property);
            }
        }
    }
    
    
}