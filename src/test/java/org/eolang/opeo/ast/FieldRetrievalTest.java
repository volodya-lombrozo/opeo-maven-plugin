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
import org.eolang.opeo.compilation.HasInstructions;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;
import org.xembly.Directives;
import org.xembly.ImpossibleModificationException;
import org.xembly.Xembler;

/**
 * Test case for {@link FieldRetrieval}.
 * @since 0.1
 */
final class FieldRetrievalTest {

    @Test
    void convertsToXmir() throws ImpossibleModificationException {
        final String actual = new Xembler(
            new Directives()
                .add("o")
                .attr("name", "method")
                .append(new FieldRetrieval(new This(), "bar").toXmir())
                .up()
        ).xml();
        MatcherAssert.assertThat(
            String.format(
                "Can't convert to a field access construct, actual result is : %n%s%n",
                actual
            ),
            actual,
            XhtmlMatchers.hasXPaths(
                "./o[@name='method']",
                "./o[@name='method']/o[@base='.get-field']/o[@base='.bar']",
                "./o[@name='method']/o[@base='.get-field']/o[@base='.bar']/o[@base='$']"
            )
        );
    }

    @Test
    void transformsToOpcodes() {
        final String descriptor = "S";
        final String owner = "java/lang/Object";
        final String name = "bar";
        MatcherAssert.assertThat(
            "Can't transform to opcodes",
            new OpcodeNodes(
                new FieldRetrieval(
                    new This(),
                    new Attributes("name", name, "descriptor", descriptor, "owner", owner)
                )
            ).opcodes(),
            new HasInstructions(
                new HasInstructions.Instruction(Opcodes.ALOAD, 0),
                new HasInstructions.Instruction(Opcodes.GETFIELD, owner, name, descriptor)
            )
        );
    }
}
