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
package org.eolang.opeo.ast;

import com.jcabi.matchers.XhtmlMatchers;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eolang.jeo.representation.HexData;
import org.eolang.jeo.representation.xmir.XmlNode;
import org.eolang.opeo.compilation.HasInstructions;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.xembly.ImpossibleModificationException;
import org.xembly.Xembler;

/**
 * Test for {@link LocalVariable}.
 * @since 0.2
 */
final class LocalVariableTest {

    @Test
    void convertsToXmir() throws ImpossibleModificationException {
        MatcherAssert.assertThat(
            "We expect the xmir variable to be equal to <o base='local1'/>, but it wasn't",
            new Xembler(new LocalVariable(1, Type.INT_TYPE).toXmir()).xml(),
            XhtmlMatchers.hasXPath("./o[@base='local-1']")
        );
    }

    @ParameterizedTest(name = "[{index}] {0} => {1}")
    @MethodSource("typesArguments")
    void convertsType(
        final Type type, final String expected
    ) throws ImpossibleModificationException {
        final String xml = new Xembler(new LocalVariable(1, type).toXmir()).xml();
        MatcherAssert.assertThat(
            String.format(
                "We expect the xmir variable type to be equal to <o base='local1' scope='%s'/>, but it wasn't, current value is '%s'",
                expected,
                xml
            ),
            xml,
            Matchers.containsString(new HexData(expected).value())
        );
    }

    @Test
    void createsVariableFromXmir() throws ImpossibleModificationException {
        final LocalVariable original = new LocalVariable(2, Type.FLOAT_TYPE);
        MatcherAssert.assertThat(
            "Can't correctly create variable from XMIR. We expect the variable to be equal to the original, but it wasn't",
            new LocalVariable(new XmlNode(new Xembler(original.toXmir()).xml())),
            Matchers.equalTo(original)
        );
    }

    @ParameterizedTest(name = "[{index}] {0} => {1}")
    @MethodSource("loads")
    void transformsToBytecodeInstructions(final Type type, final int expected) {
        MatcherAssert.assertThat(
            "Can't correctly transform variable to bytecode instructions. It should be exactly 1 instruction",
            new LocalVariable(0, type).opcodes().stream().map(AstNode::toXmir)
                .map(Xembler::new)
                .map(Xembler::xmlQuietly)
                .map(XmlNode::new)
                .collect(Collectors.toList()),
            new HasInstructions(expected)
        );
    }

    /**
     * Types arguments.
     * Don't remove this method, it's used by {@link #convertsType(Type, String)}.
     * @return Arguments.
     * @checkstyle UnusedPrivateMethod (10 lines)
     */
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private static Stream<Arguments> typesArguments() {
        return Stream.of(
            Arguments.of(Type.INT_TYPE, "I"),
            Arguments.of(Type.BOOLEAN_TYPE, "Z"),
            Arguments.of(Type.BYTE_TYPE, "B"),
            Arguments.of(Type.CHAR_TYPE, "C"),
            Arguments.of(Type.DOUBLE_TYPE, "D"),
            Arguments.of(Type.FLOAT_TYPE, "F"),
            Arguments.of(Type.LONG_TYPE, "J"),
            Arguments.of(Type.SHORT_TYPE, "S"),
            Arguments.of(Type.VOID_TYPE, "V"),
            Arguments.of(Type.getType("Ljava/lang/String;"), "Ljava/lang/String;")
        );
    }

    /**
     * Arguments for {@link #transformsToBytecodeInstructions(Type, int)} ()}.
     * Don't remove this method, it's used by {@link #transformsToBytecodeInstructions(Type, int)}}.
     * @return Arguments.
     */
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private static Stream<Arguments> loads() {
        return Stream.of(
            Arguments.of(Type.INT_TYPE, Opcodes.ILOAD),
            Arguments.of(Type.BOOLEAN_TYPE, Opcodes.ILOAD),
            Arguments.of(Type.BYTE_TYPE, Opcodes.ILOAD),
            Arguments.of(Type.CHAR_TYPE, Opcodes.ILOAD),
            Arguments.of(Type.DOUBLE_TYPE, Opcodes.DLOAD),
            Arguments.of(Type.FLOAT_TYPE, Opcodes.FLOAD),
            Arguments.of(Type.LONG_TYPE, Opcodes.LLOAD),
            Arguments.of(Type.SHORT_TYPE, Opcodes.ILOAD),
            Arguments.of(Type.getType("Ljava/lang/String;"), Opcodes.ALOAD)
        );
    }
}
