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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.eolang.jeo.representation.xmir.HexString;
import org.eolang.jeo.representation.xmir.XmlInstruction;
import org.eolang.jeo.representation.xmir.XmlNode;
import org.eolang.opeo.ast.Add;
import org.eolang.opeo.ast.ArrayConstructor;
import org.eolang.opeo.ast.AstNode;
import org.eolang.opeo.ast.Attributes;
import org.eolang.opeo.ast.ClassField;
import org.eolang.opeo.ast.Constructor;
import org.eolang.opeo.ast.ConstructorDescriptor;
import org.eolang.opeo.ast.Field;
import org.eolang.opeo.ast.FieldAssignment;
import org.eolang.opeo.ast.FieldRetrieval;
import org.eolang.opeo.ast.Invocation;
import org.eolang.opeo.ast.Label;
import org.eolang.opeo.ast.Literal;
import org.eolang.opeo.ast.LocalVariable;
import org.eolang.opeo.ast.Opcode;
import org.eolang.opeo.ast.StaticInvocation;
import org.eolang.opeo.ast.StoreArray;
import org.eolang.opeo.ast.Substraction;
import org.eolang.opeo.ast.Super;
import org.eolang.opeo.ast.This;
import org.eolang.opeo.ast.VariableAssignment;
import org.xembly.Xembler;

/**
 * High-level representation of Opeo nodes.
 * @since 0.1
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
final class XmirParser {

    /**
     * Opeo nodes.
     */
    private final List<XmlNode> nodes;

    /**
     * Constructor.
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
     * @param nodes Opeo nodes.
     */
    XmirParser(final List<XmlNode> nodes) {
        this.nodes = nodes;
    }

    /**
     * Convert to XML nodes.
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
     * @param node XmlNode
     * @return List of opcodes
     */
    private List<XmlNode> opcodes(final XmlNode node) {
        return this.node(node).opcodes()
            .stream()
            .map(AstNode::toXmir)
            .map(Xembler::new)
            .map(Xembler::xmlQuietly)
            .map(XmlNode::new)
            .collect(Collectors.toList());
    }

    /**
     * Convert XmlNode to AstNode.
     * @param node XmlNode
     * @return Ast node
     * @checkstyle CyclomaticComplexityCheck (200 lines)
     * @checkstyle ExecutableStatementCountCheck (200 lines)
     * @checkstyle JavaNCSSCheck (200 lines)
     * @checkstyle NestedIfDepthCheck (200 lines)
     * @checkstyle MethodLengthCheck (200 lines)
     * @todo #77:90min Refactor this.node() method.
     *  The parsing method this.node() looks overcomplicated and violates many
     *  code quality standards. We should refactor the method and remove all
     *  the checkstyle and PMD "suppressions" from the method.
     * @todo #110:90min Remove ad-hoc solution for replacing descriptors and owners.
     *  Currently we have an ad-hoc solution for replacing descriptors and owners.
     *  It looks ugly and requires refactoring. To remove ad-hoc solution we need
     *  to remove all the if statements that use concerete class names as:
     *  - "org/eolang/benchmark/B"
     *  - "org/eolang/benchmark/BA"
     */
    @SuppressWarnings({"PMD.NcssCount", "PMD.ExcessiveMethodLength"})
    private AstNode node(final XmlNode node) {
        final AstNode result;
        final String base = node.attribute("base").orElseThrow(
            () -> new IllegalArgumentException(
                String.format(
                    "Can't recognize node: %n%s%n'base' attribute should be present",
                    node
                )
            )
        );
        if (".plus".equals(base)) {
            final Attributes attrs = new Attributes(node.attribute("scope").orElseThrow());
            final List<XmlNode> inner = node.children().collect(Collectors.toList());
            final AstNode left = this.node(inner.get(0));
            final AstNode right = this.node(inner.get(1));
            result = new Add(left, right, attrs);
        } else if (".minus".equals(base)) {
            final Attributes attrs = new Attributes(node.attribute("scope").orElseThrow());
            final List<XmlNode> inner = node.children().collect(Collectors.toList());
            final AstNode left = this.node(inner.get(0));
            final AstNode right = this.node(inner.get(1));
            result = new Substraction(left, right, attrs);
        } else if ("opcode".equals(base)) {
            final XmlInstruction instruction = new XmlInstruction(node.node());
            final int opcode = instruction.opcode();
            result = new Opcode(opcode, instruction.operands());
        } else if ("label".equals(base)) {
            final List<XmlNode> inner = node.children().collect(Collectors.toList());
            result = new Label(this.node(inner.get(0)));
        } else if ("int".equals(base)) {
            result = new Literal(new HexString(node.text()).decodeAsInt());
        } else if ("string".equals(base)) {
            result = new Literal(new HexString(node.text()).decode());
        } else if (".super".equals(base)) {
            final List<XmlNode> inner = node.children().collect(Collectors.toList());
            final AstNode instance = this.node(inner.get(0));
            result = new Super(
                instance,
                this.args(inner),
                node.attribute("scope")
                    .orElseThrow(
                        () -> new IllegalArgumentException(
                            "Can't find descriptor for super invocation"
                        )
                    )
            );
        } else if ("$".equals(base)) {
            result = new This(node);
        } else if ("staticfield".equals(base)) {
            result = new ClassField(new Attributes(node.attribute("scope").orElseThrow()));
        } else if (".writearray".equals(base)) {
            final List<XmlNode> inner = node.children().collect(Collectors.toList());
            final AstNode array = this.node(inner.get(0));
            final AstNode index = this.node(inner.get(1));
            final AstNode value = this.node(inner.get(2));
            result = new StoreArray(array, index, value);
        } else if (".write".equals(base)) {
            final List<XmlNode> inner = node.children().collect(Collectors.toList());
            final AstNode target = this.node(inner.get(0));
            final AstNode value = this.node(inner.get(1));
            result = new VariableAssignment((LocalVariable) target, value);
        } else if (".getfield".equals(base)) {
            final List<XmlNode> inner = node.children().collect(Collectors.toList());
            final XmlNode field = inner.get(0);
            result = new FieldRetrieval(
                new Field(
                    this.node(field.children().collect(Collectors.toList()).get(0)),
                    new Attributes(field.attribute("scope").orElseThrow())
                )
            );
        } else if (".writefield".equals(base)) {
            final List<XmlNode> inner = node.children().collect(Collectors.toList());
            final XmlNode field = inner.get(0);
            final AstNode value = this.node(inner.get(1));
            result = new FieldAssignment(
                new Field(
                    this.node(field.children().collect(Collectors.toList()).get(0)),
                    new Attributes(field.attribute("scope").orElseThrow())
                ),
                value
            );
        } else if (base.contains("local")) {
            result = new LocalVariable(node);
        } else if (".new".equals(base)) {
            final List<XmlNode> inner = node.children().collect(Collectors.toList());
            final String type = inner.get(0).attribute("base").orElseThrow(
                () -> new IllegalArgumentException(
                    String.format(
                        "Can't find type of '%s'",
                        base
                    )
                )
            );
            final List<AstNode> args = this.args(inner);
            final Attributes attributes = new Attributes()
                .descriptor(new ConstructorDescriptor(args).toString());
            result = new Constructor(type, attributes, args);
        } else if (".array".equals(base)) {
            final List<XmlNode> children = node.children().collect(Collectors.toList());
            final String type = new HexString(children.get(0).text()).decode();
            final AstNode size = this.node(children.get(1));
            result = new ArrayConstructor(size, type);
        } else if (!base.isEmpty() && base.charAt(0) == '.') {
            Attributes attributes = new Attributes(
                node.attribute("scope")
                    .orElseThrow(
                        () -> new IllegalArgumentException(
                            String.format(
                                "Can't find attributes of '%s'",
                                base
                            )
                        )
                    )
            );
            if ("static".equals(attributes.type())) {
                result = new StaticInvocation(
                    node, this.args(node.children().collect(Collectors.toList()))
                );
            } else {
                final List<XmlNode> inner = node.children().collect(Collectors.toList());
                final AstNode target = this.node(inner.get(0));
                result = new Invocation(target, attributes, this.args(inner));
            }
        } else {
            throw new IllegalArgumentException(
                String.format("Can't recognize node: %n%s%n", node)
            );
        }
        return result;
    }

    /**
     * Convert XML nodes into a list of arguments.
     * @param all XML nodes.
     * @return List of arguments.
     */
    private List<AstNode> args(final List<XmlNode> all) {
        final List<AstNode> arguments;
        if (all.size() > 1) {
            arguments = all.subList(1, all.size())
                .stream()
                .map(this::node)
                .collect(Collectors.toList());
        } else {
            arguments = Collections.emptyList();
        }
        return arguments;
    }
}
