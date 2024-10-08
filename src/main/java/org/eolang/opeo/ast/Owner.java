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

import lombok.EqualsAndHashCode;
import org.objectweb.asm.Type;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Owner.
 * Method owner. It might be a class or an interface.
 * Usually, it is the first argument in the static method invocation.
 * @since 0.2
 */
@EqualsAndHashCode
public final class Owner implements Xmir {

    /**
     * Owner type.
     */
    @EqualsAndHashCode.Exclude
    private final Type type;

    /**
     * Constructor.
     * @param type Owner type.
     */
    public Owner(final String type) {
        this(Type.getObjectType(new PrefixedName(type).withoutPrefix()));
    }

    /**
     * Constructor.
     * @param type Type of the owner.
     */
    private Owner(final Type type) {
        this.type = type;
    }

    @Override
    public Iterable<Directive> toXmir() {
        return new Directives()
            .add("o")
            .attr("base", new PrefixedName(this.toString()).withPrefix())
            .up();
    }

    @EqualsAndHashCode.Include
    @Override
    public String toString() {
        return this.type.getClassName();
    }
}
