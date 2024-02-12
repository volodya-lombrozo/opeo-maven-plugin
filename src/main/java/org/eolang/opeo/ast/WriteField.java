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

import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.Opcodes;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Put a value into a field.
 * @since 0.1
 */
public final class WriteField implements AstNode {

    /**
     * The target where the value is put.
     */
    private final AstNode target;

    /**
     * The value to put.
     */
    private final AstNode value;

    /**
     * Field attributes.
     */
    private final Attributes attributes;

    /**
     * Constructor.
     * @param target The target where the value is put
     * @param value The value to put
     * @param attributes Field attributes
     */
    public WriteField(final AstNode target, final AstNode value, final Attributes attributes) {
        this.target = target;
        this.value = value;
        this.attributes = attributes;
    }

    @Override
    public Iterable<Directive> toXmir() {
        return new Directives().add("o")
            .attr("base", ".write")
            .attr("scope", this.attributes)
            .add("o")
            .attr("base", String.format(".%s", this.attributes.name()))
            .append(this.target.toXmir())
            .up()
            .append(this.value.toXmir())
            .up();
    }

    @Override
    public List<AstNode> opcodes() {
        final List<AstNode> res = new ArrayList<>(1);
        res.addAll(this.target.opcodes());
        res.addAll(this.value.opcodes());
        res.add(
            new Opcode(
                Opcodes.PUTFIELD,
                this.attributes.owner(),
                this.attributes.name(),
                this.attributes.descriptor()
            )
        );
        return res;
    }
}
