/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2023 Objectionary.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.eolang.opeo.decompilation;

import java.util.Map;
import java.util.UUID;
import org.eolang.jeo.representation.xmir.AllLabels;
import org.eolang.opeo.LabelInstruction;
import org.eolang.opeo.OpcodeInstruction;
import org.eolang.opeo.ast.Add;
import org.eolang.opeo.ast.ArrayConstructor;
import org.eolang.opeo.ast.Attributes;
import org.eolang.opeo.ast.ClassField;
import org.eolang.opeo.ast.Invocation;
import org.eolang.opeo.ast.Literal;
import org.eolang.opeo.ast.Opcode;
import org.eolang.opeo.ast.Root;
import org.eolang.opeo.ast.StaticInvocation;
import org.eolang.opeo.ast.StoreArray;
import org.eolang.opeo.ast.This;
import org.eolang.opeo.compilation.Compiler;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.xembly.ImpossibleModificationException;
import org.xembly.Xembler;

/**
 * Test case for {@link DecompilerMachine}.
 * @since 0.1
 */
@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.TooManyMethods"})
final class DecompilerMachineTest {

    /**
     * Test decompilation of instance call instructions.
     * <p>
     *     {@code
     *       new A().bar();
     *     }
     * </p>
     */
    @Test
    void decompilesSimpleInstanceCall() {
        Assertions.assertDoesNotThrow(
            () -> new DecompilerMachine().decompile(
                new OpcodeInstruction(Opcodes.NEW, "A"),
                new OpcodeInstruction(Opcodes.DUP),
                new OpcodeInstruction(Opcodes.INVOKESPECIAL, "A", "<init>", "()V"),
                new OpcodeInstruction(Opcodes.INVOKEVIRTUAL, "A", "bar", "()V")
            ),
            "Can't decompile method call instructions for 'new A().bar();'"
        );
    }

    /**
     * Test decompilation of instance call instructions with arguments.
     * <p>
     *     {@code
     *       new A(28).bar(29);
     *     }
     * </p>
     */
    @Test
    void decompilesInstanceCallWithArguments() {
        Assertions.assertDoesNotThrow(
            () -> new DecompilerMachine().decompile(
                new OpcodeInstruction(Opcodes.NEW, "A"),
                new OpcodeInstruction(Opcodes.DUP),
                new OpcodeInstruction(Opcodes.BIPUSH, 28),
                new OpcodeInstruction(Opcodes.INVOKESPECIAL, "A", "<init>", "(I)V"),
                new OpcodeInstruction(Opcodes.BIPUSH, 29),
                new OpcodeInstruction(Opcodes.INVOKEVIRTUAL, "A", "bar", "(I)V")
            ),
            "Can't decompile method call instructions for 'new A(28).bar(29);'"
        );
    }

    /**
     * Test decompilation of nested instance call instructions with arguments.
     * <p>
     *     {@code
     *        foo(bar()) + 3;
     *     }
     * </p>
     */
    @Test
    void decompilesNestedInstanceCallWithArguments() {
        Assertions.assertDoesNotThrow(
            () -> new DecompilerMachine().decompile(
                new OpcodeInstruction(Opcodes.ALOAD, 0),
                new OpcodeInstruction(Opcodes.ALOAD, 0),
                new OpcodeInstruction(Opcodes.INVOKEVIRTUAL, "App", "bar", "()I"),
                new OpcodeInstruction(Opcodes.INVOKEVIRTUAL, "App", "foo", "(I)I"),
                new OpcodeInstruction(Opcodes.ICONST_3),
                new OpcodeInstruction(Opcodes.IADD)
            ),
            "Can't decompile method call instructions for 'foo(bar()) + 3;'"
        );
    }

    /**
     * Test decompilation of instance field access.
     * <p>
     *     {@code
     *       this.a + this.b;
     *     }
     * </p>
     */
    @Test
    void decompilesFieldAccess() {
        Assertions.assertDoesNotThrow(
            () -> new DecompilerMachine().decompile(
                new OpcodeInstruction(Opcodes.ALOAD, 0),
                new OpcodeInstruction(Opcodes.GETFIELD, "App", "a", "I"),
                new OpcodeInstruction(Opcodes.ALOAD, 0),
                new OpcodeInstruction(Opcodes.GETFIELD, "App", "b", "I"),
                new OpcodeInstruction(Opcodes.IADD)
            ),
            "Can't decompile field access instructions for 'this.a + this.b;'"
        );
    }

    /**
     * Test decompilation of instance field access and method invocation.
     * <p>
     *     {@code
     *       this.a.intValue() + 1;
     *     }
     * </p>
     */
    @Test
    void decompilesFieldAccessAndMethodInvocation() {
        Assertions.assertDoesNotThrow(
            () -> new DecompilerMachine().decompile(
                new OpcodeInstruction(Opcodes.ALOAD, 0),
                new OpcodeInstruction(Opcodes.GETFIELD, "App", "a", "Ljava/lang/Integer;"),
                new OpcodeInstruction(Opcodes.INVOKEVIRTUAL, "App", "intValue", "()I"),
                new OpcodeInstruction(Opcodes.ICONST_1),
                new OpcodeInstruction(Opcodes.IADD)
            ),
            "Can't decompile field access and method invocation instructions for 'this.a.intValue() + 1;'"
        );
    }

    /**
     * Decimpilation test of a simple method with "if" clause.
     * <p>
     *     {@code
     *         public int get() {
     *         if (d <= 0) {
     *             return d;
     *         }
     *         return new A(d - 1).get();
     *         }
     *     }
     * </p>
     */
    @Test
    void decompilesIfStatement() {
        final AllLabels labels = new AllLabels();
        final String uid = UUID.randomUUID().toString();
        final Label label = labels.label(uid);
        Assertions.assertDoesNotThrow(
            () -> {
                new DecompilerMachine().decompile(
                    new OpcodeInstruction(Opcodes.ALOAD, 0),
                    new OpcodeInstruction(Opcodes.GETFIELD, "org/eolang/other/A", "d", "I"),
                    new OpcodeInstruction(Opcodes.IFGT, label),
                    new OpcodeInstruction(Opcodes.ALOAD, 0),
                    new OpcodeInstruction(Opcodes.GETFIELD, "org/eolang/other/A", "d", "I"),
                    new OpcodeInstruction(Opcodes.IRETURN),
                    new LabelInstruction(label),
                    new OpcodeInstruction(Opcodes.NEW, "org/eolang/other/A"),
                    new OpcodeInstruction(Opcodes.DUP),
                    new OpcodeInstruction(Opcodes.ALOAD, 0),
                    new OpcodeInstruction(Opcodes.GETFIELD, "org/eolang/other/A", "d", "I"),
                    new OpcodeInstruction(Opcodes.ICONST_1),
                    new OpcodeInstruction(Opcodes.ISUB),
                    new OpcodeInstruction(
                        Opcodes.INVOKESPECIAL, "org/eolang/other/A", "<init>", "(I)V"
                    ),
                    new OpcodeInstruction(
                        Opcodes.INVOKEVIRTUAL, "org/eolang/other/A", "get", "()I"
                    ),
                    new OpcodeInstruction(Opcodes.IRETURN)
                );
            },
            "Compiles without exceptions"
        );
    }

    @Test
    void decompilesInvokeVirtual() {
        Assertions.assertDoesNotThrow(
            () ->
                new Xembler(
                    new DecompilerMachine()
                        .decompile(
                            new OpcodeInstruction(Opcodes.LLOAD, 4),
                            new OpcodeInstruction(Opcodes.ALOAD, 1),
                            new OpcodeInstruction(Opcodes.INVOKEVIRTUAL, "App", "run", "()I"),
                            new OpcodeInstruction(Opcodes.I2L),
                            new OpcodeInstruction(Opcodes.LADD),
                            new OpcodeInstruction(Opcodes.LSTORE, 4)
                        )
                ).xml(),
            "Can't decompile invoke virtual"
        );
    }

    @Test
    void decompilesArrayCreation() throws ImpossibleModificationException {
        final String type = "java/lang/Object";
        MatcherAssert.assertThat(
            "Can't decompile array creation",
            new Xembler(
                new DecompilerMachine()
                    .decompile(
                        new OpcodeInstruction(Opcodes.ICONST_2),
                        new OpcodeInstruction(Opcodes.ICONST_3),
                        new OpcodeInstruction(Opcodes.IADD),
                        new OpcodeInstruction(Opcodes.ANEWARRAY, type)
                    )
            ).xml(),
            Matchers.equalTo(
                new Xembler(
                    new Root(
                        new ArrayConstructor(
                            new Add(new Literal(2), new Literal(3)),
                            type
                        )
                    ).toXmir()
                ).xml()
            )
        );
    }

    @Test
    void decompilesArrayInsertion() throws ImpossibleModificationException {
        final String type = "java/lang/Object";
        MatcherAssert.assertThat(
            "Can't decompile array insertion",
            new Xembler(
                new DecompilerMachine()
                    .decompile(
                        new OpcodeInstruction(Opcodes.ICONST_2),
                        new OpcodeInstruction(Opcodes.ANEWARRAY, type),
                        new OpcodeInstruction(Opcodes.DUP),
                        new OpcodeInstruction(Opcodes.ICONST_0),
                        new OpcodeInstruction(Opcodes.ALOAD, 0),
                        new OpcodeInstruction(Opcodes.AASTORE)
                    )
            ).xml(),
            Matchers.equalTo(
                new Xembler(
                    new Root(
                        new StoreArray(
                            new ArrayConstructor(new Literal(2), type),
                            new Literal(0),
                            new This()
                        )
                    ).toXmir()
                ).xml()
            )
        );
    }

    @Test
    void decompilesVarargInvocation() throws ImpossibleModificationException {
        final String type = "java/lang/Object";
        MatcherAssert.assertThat(
            "Can't decompile vararg invocation",
            new Xembler(
                new DecompilerMachine(Map.of("counting", "false"))
                    .decompile(
                        new OpcodeInstruction(
                            Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"
                        ),
                        new OpcodeInstruction(Opcodes.LDC, "Number is %s"),
                        new OpcodeInstruction(Opcodes.ICONST_1),
                        new OpcodeInstruction(Opcodes.ANEWARRAY, type),
                        new OpcodeInstruction(Opcodes.DUP),
                        new OpcodeInstruction(Opcodes.ICONST_0),
                        new OpcodeInstruction(Opcodes.ICONST_2),
                        new OpcodeInstruction(
                            Opcodes.INVOKESTATIC,
                            "java/lang/Integer",
                            "valueOf",
                            "(I)Ljava/lang/Integer;"
                        ),
                        new OpcodeInstruction(Opcodes.AASTORE),
                        new OpcodeInstruction(
                            Opcodes.INVOKEVIRTUAL,
                            "java/io/PrintStream",
                            "printf",
                            "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/io/PrintStream;"
                        ),
                        new OpcodeInstruction(Opcodes.POP),
                        new OpcodeInstruction(Opcodes.RETURN)
                    )
            ).xml(),
            Matchers.equalTo(
                new Xembler(
                    new Root(
                        new Invocation(
                            new ClassField(
                                "java/lang/System",
                                "out",
                                "Ljava/io/PrintStream;"
                            ),
                            new Attributes()
                                .name("printf")
                                .descriptor(
                                    "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/io/PrintStream;"
                                )
                                .owner("java/io/PrintStream"),
                            new Literal("Number is %s"),
                            new StoreArray(
                                new ArrayConstructor(
                                    new Literal(1),
                                    type
                                ),
                                new Literal(0),
                                new StaticInvocation(
                                    "java/lang/Integer",
                                    "valueOf",
                                    "(I)Ljava/lang/Integer;",
                                    new Literal(2)
                                )
                            )
                        ),
                        new Opcode(Opcodes.RETURN, false)
                    ).toXmir()
                ).xml()
            )
        );
    }
}
