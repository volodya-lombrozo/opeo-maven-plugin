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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Root node.
 * @since 0.1
 */
public final class Root implements AstNode {

    /**
     * Children.
     */
    private final Collection<AstNode> children;

    /**
     * Constructor.
     */
    public Root() {
        this(new ArrayList<>(0));
    }

    /**
     * Constructor.
     * @param children Children.
     */
    public Root(final AstNode... children) {
        this(Arrays.asList(children));
    }

    /**
     * Constructor.
     * @param children Children.
     */
    public Root(final Collection<AstNode> children) {
        this.children = children;
    }

    @Override
    public Iterable<Directive> toXmir() {
        final Iterable<Directive> result;
        if (this.children.isEmpty()) {
            result = Collections.emptyList();
        } else {
            final Directives directives = new Directives();
            directives.add("o").attr("base", "tuple").attr("star", "");
            this.children.stream().map(AstNode::toXmir).forEach(directives::append);
            directives.up();
            result = directives;
        }
        return result;
    }

    @Override
    public List<AstNode> opcodes() {
        return this.children.stream()
            .flatMap(node -> node.opcodes().stream())
            .collect(Collectors.toList());
    }

    /**
     * Append child.
     * @param node Child
     */
    public void append(final AstNode node) {
        this.children.add(node);
    }

}
