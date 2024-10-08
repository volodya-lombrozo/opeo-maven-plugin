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
package org.eolang.opeo.decompilation.agents;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.eolang.opeo.ast.Opcode;
import org.eolang.opeo.ast.OpcodeName;

/**
 * Supported opcodes.
 * Used to check if the instruction is supported.
 * @since 0.4
 */
final class Supported {

    /**
     * Supported opcodes.
     */
    private final Set<Integer> all;

    /**
     * Constructor.
     * @param supported Supported opcodes.
     */
    Supported(final int... supported) {
        this(Arrays.stream(supported).boxed().collect(Collectors.toSet()));
    }

    /**
     * Constructor.
     * @param supported Supported opcodes.
     */
    Supported(final Set<Integer> supported) {
        this.all = supported;
    }

    /**
     * Check if the instruction is supported.
     * @param opcode Instruction to check.
     * @return True if the instruction is supported, false otherwise.
     */
    boolean isSupported(final Opcode opcode) {
        return this.all.contains(opcode.opcode());
    }

    /**
     * Merge two supported sets.
     * @param supported Supported to merge.
     * @return Merged supported set.
     */
    Supported merge(final Supported supported) {
        final Set<Integer> merged = new HashSet<>(this.all);
        merged.addAll(supported.all);
        return new Supported(merged);
    }

    /**
     * Simplified names of supported opcodes.
     * @return Names of supported opcodes.
     */
    String[] names() {
        return this.all.stream()
            .map(OpcodeName::new)
            .map(OpcodeName::simplified)
            .toArray(String[]::new);
    }

}
