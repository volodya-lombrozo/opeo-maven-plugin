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
package org.eolang.opeo.compilation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eolang.jeo.representation.xmir.XmlNode;
import org.eolang.opeo.ast.Addition;
import org.eolang.opeo.ast.ArrayConstructor;
import org.eolang.opeo.ast.AstNode;
import org.eolang.opeo.ast.Attributes;
import org.eolang.opeo.ast.Cast;
import org.eolang.opeo.ast.CheckCast;
import org.eolang.opeo.ast.ClassField;
import org.eolang.opeo.ast.ClassName;
import org.eolang.opeo.ast.Constant;
import org.eolang.opeo.ast.Constructor;
import org.eolang.opeo.ast.Duplicate;
import org.eolang.opeo.ast.DynamicInvocation;
import org.eolang.opeo.ast.FieldAssignment;
import org.eolang.opeo.ast.FieldRetrieval;
import org.eolang.opeo.ast.If;
import org.eolang.opeo.ast.InterfaceInvocation;
import org.eolang.opeo.ast.Invocation;
import org.eolang.opeo.ast.Label;
import org.eolang.opeo.ast.Labeled;
import org.eolang.opeo.ast.Literal;
import org.eolang.opeo.ast.LocalVariable;
import org.eolang.opeo.ast.Multiplication;
import org.eolang.opeo.ast.NewAddress;
import org.eolang.opeo.ast.Opcode;
import org.eolang.opeo.ast.Popped;
import org.eolang.opeo.ast.RawXml;
import org.eolang.opeo.ast.Return;
import org.eolang.opeo.ast.StaticInvocation;
import org.eolang.opeo.ast.StoreArray;
import org.eolang.opeo.ast.Substraction;
import org.eolang.opeo.ast.Super;
import org.eolang.opeo.ast.This;
import org.eolang.opeo.ast.VariableAssignment;
import org.xembly.Xembler;

/**
 * High-level representation of Opeo nodes.
 *
 * @since 0.1
 * @checkstyle ClassFanOutComplexityCheck (500 lines)
 */
final class XmirParser implements Parser {

    /**
     * Opeo nodes.
     */
    private final List<XmlNode> nodes;

    /**
     * References.
     */
    private final Map<String, Duplicate> references;

    /**
     * Constructor.
     *
     * @param nodes Opeo nodes.
     */
    XmirParser(final AstNode... nodes) {
        this(
            Arrays.stream(nodes)
                .map(AstNode::toXmir)
                .map(Xembler::new)
                .map(Xembler::xmlQuietly)
                .map(XmlNode::new)
                .collect(Collectors.toList())
        );
    }

    /**
     * Constructor.
     *
     * @param nodes Opeo nodes.
     */
    XmirParser(final List<XmlNode> nodes) {
        this.nodes = nodes;
        this.references = new HashMap<>(0);
    }

    /**
     * Convert XmlNode to AstNode.
     *
     * @param node XmlNode
     * @return Ast node
     * @checkstyle CyclomaticComplexityCheck (500 lines)
     * @checkstyle JavaNCSSCheck (500 lines)
     * @checkstyle NoJavadocForOverriddenMethodsCheck (500 lines)
     * @checkstyle ExecutableStatementCountCheck (500 lines)
     */
    @Override
    @SuppressWarnings({"PMD.NcssCount", "PMD.ExcessiveMethodLength"})
    public AstNode parse(final XmlNode node) {
        final AstNode result;
        final String base = node.attribute("base").orElseThrow(
            () -> new IllegalArgumentException(
                String.format(
                    "Can't recognize node: %n%s%n'base' attribute should be present",
                    node
                )
            )
        );
        if (".ignore-result".equals(base)) {
            result = new Popped(this.parse(node.firstChild()));
        } else if ("labeled".equals(base)) {
            result = new Labeled(node, this::parse);
        } else if ("times".equals(base)) {
            result = new Multiplication(node, this::parse);
        } else if (".if".equals(base)) {
            result = new If(node, this::parse);
        } else if ("load-constant".equals(base)) {
            result = new Constant(node);
        } else if (".new-type".equals(base)) {
            result = new NewAddress(node);
        } else if (base.startsWith("ref-")) {
            if (this.references.containsKey(base)) {
                result = this.references.get(base);
            } else {
                throw new IllegalStateException(String.format("Reference not found '%s'", base));
            }
        } else if ("duplicated".equals(base)) {
            final String name = node.attribute("name")
                .orElseThrow(
                    () -> new IllegalStateException(
                        String.format("Name attribute is missing '%s'", node)
                    )
                );
            final Duplicate duplicate = new Duplicate(this.parse(node.firstChild()));
            this.references.put(name, duplicate);
            result = duplicate;
        } else if (".plus".equals(base)) {
            result = new Addition(node, this::parse);
        } else if (".minus".equals(base)) {
            result = new Substraction(node, this::parse);
        } else if ("cast".equals(base)) {
            result = new Cast(node, this::parse);
        } else if ("frame".equals(base)) {
            result = new RawXml(node);
        } else if ("opcode".equals(base)) {
            result = new Opcode(node);
        } else if ("label".equals(base)) {
            result = new Label(node);
        } else if ("float".equals(base)) {
            result = new Literal(node);
        } else if ("int".equals(base)) {
            result = new Literal(node);
        } else if ("string".equals(base)) {
            result = new Literal(node);
        } else if ("double".equals(base)) {
            result = new Literal(node);
        } else if ("long".equals(base)) {
            result = new Literal(node);
        } else if ("type".equals(base)) {
            result = new ClassName(node);
        } else if (".super".equals(base)) {
            result = new Super(node, this);
        } else if ("$".equals(base)) {
            result = new This(node);
        } else if ("static-field".equals(base)) {
            result = new ClassField(node);
        } else if (".write-array".equals(base)) {
            result = new StoreArray(node, this);
        } else if (".write-local-var".equals(base)) {
            result = new VariableAssignment(node, this);
        } else if (".get-field".equals(base)) {
            result = new FieldRetrieval(node, this);
        } else if (".write-field".equals(base)) {
            result = new FieldAssignment(node, this);
        } else if (base.startsWith("local-")) {
            result = new LocalVariable(node);
        } else if (".new".equals(base)) {
            result = new Constructor(node, this);
        } else if (".array-node".equals(base)) {
            result = new ArrayConstructor(node, this);
        } else if ("return".equals(base)) {
            result = new Return(node, this);
        } else if ("checkcast".equals(base)) {
            result = new CheckCast(node, this);
        } else if (!base.isEmpty() && base.charAt(0) == '.') {
            final Attributes attributes = new Attributes(
                node.children().collect(Collectors.toList()).get(1)
            );
            if ("static".equals(attributes.type())) {
                result = new StaticInvocation(node, this);
            } else if ("interface".equals(attributes.type())) {
                result = new InterfaceInvocation(node, this);
            } else if ("dynamic".equals(attributes.type())) {
                result = new DynamicInvocation(node, this);
            } else {
                result = new Invocation(node, this);
            }
        } else {
            throw new IllegalArgumentException(
                String.format("Can't recognize node: %n%s%n", node)
            );
        }
        return result;
    }

    /**
     * Convert to XML nodes.
     *
     * @return XML nodes.
     */
    List<XmlNode> toJeoNodes() {
        return this.nodes.stream()
            .map(this::opcodes)
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }

    /**
     * Convert XmlNode into a list of opcodes.
     *
     * @param node XmlNode
     * @return List of opcodes
     */
    private List<XmlNode> opcodes(final XmlNode node) {
        return this.parse(node).opcodes()
            .stream()
            .map(AstNode::toXmir)
            .map(Xembler::new)
            .map(Xembler::xmlQuietly)
            .map(XmlNode::new)
            .collect(Collectors.toList());
    }
}
