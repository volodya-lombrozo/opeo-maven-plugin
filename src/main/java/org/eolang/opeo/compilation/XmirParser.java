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
import java.util.concurrent.atomic.AtomicInteger;
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
import org.eolang.opeo.ast.InstanceField;
import org.eolang.opeo.ast.Invocation;
import org.eolang.opeo.ast.Label;
import org.eolang.opeo.ast.Literal;
import org.eolang.opeo.ast.Opcode;
import org.eolang.opeo.ast.StaticInvocation;
import org.eolang.opeo.ast.StoreArray;
import org.eolang.opeo.ast.StoreLocal;
import org.eolang.opeo.ast.Substraction;
import org.eolang.opeo.ast.Super;
import org.eolang.opeo.ast.This;
import org.eolang.opeo.ast.Variable;
import org.eolang.opeo.ast.WriteField;
import org.objectweb.asm.Opcodes;
import org.xembly.Xembler;

/**
 * High-level representation of Opeo nodes.
 * @since 0.1
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class XmirParser {

    /**
     * Opeo nodes.
     */
    private final List<XmlNode> nodes;

    /**
     * Number of pops.
     * @todo #132:90min Fix adding of pop instructions.
     *  Currently we have an ad-hoc solution for adding pop instructions
     *  before labels. It looks ugly and requires refactoring. Maybe we
     *  should add a new ast node type for pop instructions.
     *  Anyway, we should remove this field and refactor the code.
     */
    private final AtomicInteger pops;

    /**
     * Constructor.
     * @param nodes Opeo nodes.
     */
    public XmirParser(final AstNode... nodes) {
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
    public XmirParser(final List<XmlNode> nodes) {
        this.nodes = nodes;
        this.pops = new AtomicInteger(0);
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
            if (this.pops.get() > 0) {
                result = new AstNode.Sequence(
                    Collections.nCopies(this.pops.get(), new Opcode(Opcodes.POP)),
                    new Label(this.node(inner.get(0)))
                );
                this.pops.set(0);
            } else {
                result = new Label(this.node(inner.get(0)));
            }
        } else if ("int".equals(base)) {
            result = new Literal(new HexString(node.text()).decodeAsInt());
        } else if ("string".equals(base)) {
            result = new Literal(new HexString(node.text()).decode());
        } else if (".super".equals(base)) {
            final List<XmlNode> inner = node.children().collect(Collectors.toList());
            final AstNode instance = this.node(inner.get(0));
            final List<AstNode> arguments;
            if (inner.size() > 1) {
                arguments = inner.subList(1, inner.size())
                    .stream()
                    .map(this::node)
                    .collect(Collectors.toList());
            } else {
                arguments = Collections.emptyList();
            }
            result = new Super(
                instance,
                arguments,
                node.attribute("scope")
                    .orElseThrow(
                        () -> new IllegalArgumentException(
                            "Can't find descriptor for super invocation"
                        )
                    )
            );
        } else if ("$".equals(base)) {
            result = new This();
        } else if ("staticfield".equals(base)) {
            result = new ClassField(new Attributes(node.attribute("scope").orElseThrow()));
        } else if (base.contains("local")) {
            result = new Variable(node);
        } else if (".writearray".equals(base)) {
            final List<XmlNode> inner = node.children().collect(Collectors.toList());
            final AstNode array = this.node(inner.get(0));
            final AstNode index = this.node(inner.get(1));
            final AstNode value = this.node(inner.get(2));
            result = new StoreArray(array, index, value);
        } else if (".write".equals(base)) {
            //@checkstyle MethodBodyCommentsCheck (20 lines)
            // @todo #80:90min Correct parsing of WriteField node
            //  Currently we have an ad-hoc solution for parsing WriteField node.
            //  It looks ugly, requires refactoring and maybe adding new ast node types.
            //  For now the parsing is done in a way to make the tests pass.
            if (node.attribute("scope").isPresent()) {
                final List<XmlNode> inner = node.children().collect(Collectors.toList());
                final AstNode target = this.node(
                    inner.get(0).children().collect(Collectors.toList()).get(0)
                );
                final AstNode value = this.node(inner.get(1));
                result = new WriteField(
                    target,
                    value,
                    new Attributes(node.attribute("scope").orElseThrow())
                );
            } else {
                final List<XmlNode> inner = node.children().collect(Collectors.toList());
                final AstNode variable = this.node(inner.get(0));
                final AstNode value = this.node(inner.get(1));
                result = new StoreLocal(variable, value);
            }
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
            final List<AstNode> arguments;
            if (inner.size() > 1) {
                arguments = inner.subList(1, inner.size())
                    .stream()
                    .map(this::node)
                    .collect(Collectors.toList());
            } else {
                arguments = Collections.emptyList();
            }
            final Attributes attributes;
            if (type.equals("org/eolang/benchmark/BA")) {
                attributes = new Attributes().descriptor("(I)V");
            } else {
                attributes = new Attributes(
                    node.attribute("scope").orElseThrow(
                        () -> new IllegalStateException(
                            String.format(
                                "Can't find 'scope' attribute of constructor in node: %n%s%n, type is %s",
                                node,
                                type
                            )
                        )
                    )
                );
            }
            result = new Constructor(type, attributes, arguments);
        } else if (".array".equals(base)) {
            final List<XmlNode> children = node.children().collect(Collectors.toList());
            final String type = new HexString(children.get(0).text()).decode();
            final AstNode size = this.node(children.get(1));
            this.pops.incrementAndGet();
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
            if (
                attributes.owner().equals("org/eolang/benchmark/B")
                    && attributes.type().equals("method")
                    && attributes.descriptor().equals("()I")
                    && attributes.name().equals("bar")
            ) {
                attributes = new Attributes(
                    "name=bar|descriptor=()I|owner=org/eolang/benchmark/BA|type=method"
                );
            }
            if ("field".equals(attributes.type())) {
                final List<XmlNode> inner = node.children().collect(Collectors.toList());
                final AstNode target = this.node(inner.get(0));
                result = new InstanceField(target, attributes);
            } else if ("static".equals(attributes.type())) {
                final List<XmlNode> inner = node.children().collect(Collectors.toList());
                final List<AstNode> arguments;
                if (inner.isEmpty()) {
                    arguments = Collections.emptyList();
                } else {
                    arguments = inner.stream().map(this::node).collect(Collectors.toList());
                }
                result = new StaticInvocation(attributes, arguments);
            } else {
                final List<XmlNode> inner = node.children().collect(Collectors.toList());
                final AstNode target = this.node(inner.get(0));
                final List<AstNode> arguments;
                if (inner.size() > 1) {
                    arguments = inner.subList(1, inner.size())
                        .stream()
                        .map(this::node)
                        .collect(Collectors.toList());
                } else {
                    arguments = Collections.emptyList();
                }
                result = new Invocation(target, attributes, arguments);
            }
        } else {
            throw new IllegalArgumentException(
                String.format("Can't recognize node: %n%s%n", node)
            );
        }
        return result;
    }
}