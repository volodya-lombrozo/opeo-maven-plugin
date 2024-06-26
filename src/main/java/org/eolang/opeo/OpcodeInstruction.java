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
package org.eolang.opeo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.ToString;
import org.eolang.opeo.ast.OpcodeName;
import org.eolang.opeo.jeo.JeoInstruction;

/**
 * Instruction that was created directly in Java code.
 * This class is useful for testing purposes.
 * In real scenarios we mostly use {@link JeoInstruction}.
 * @since 0.1
 */
@ToString
public final class OpcodeInstruction implements Instruction {
    /**
     * Opcode index.
     */
    private final int code;

    /**
     * Operands.
     */
    private final List<Object> arguments;

    /**
     * Constructor.
     * @param code Opcode index
     * @param args Operands
     */
    public OpcodeInstruction(final int code, final Object... args) {
        this(code, Arrays.asList(args));
    }

    /**
     * Constructor.
     * @param code Opcode index
     * @param args Operands
     */
    private OpcodeInstruction(final int code, final List<Object> args) {
        this.code = code;
        this.arguments = args;
    }

    @Override
    public int opcode() {
        return this.code;
    }

    @Override
    public Object operand(final int index) {
        return this.arguments.get(index);
    }

    @Override
    public List<Object> operands() {
        return Collections.unmodifiableList(this.arguments);
    }

    @ToString.Include(name = "opcode-name")
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private String opname() {
        return new OpcodeName(this.code).simplified();
    }
}
