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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.objectweb.asm.Opcodes;

/**
 * Opcode name.
 * The name of bytecode instruction. The name combined with unique number in order to
 * avoid name collisions.
 * @since 0.1.0
 * @todo #65:30min Code duplication with jeo-maven-plugin.
 *  This class is a copy of org.eolang.jeo.representation.directives.OpcodeName.
 *  It should be moved to jeo-maven-plugin and used from there.
 */
public final class OpcodeName {

    /**
     * Opcode names.
     */
    private static final Map<Integer, String> NAMES = OpcodeName.init();

    /**
     * Unknown opcode name.
     */
    private static final String UNKNOWN = "unknown";

    /**
     * Bytecode operation code.
     */
    private final int opcode;

    /**
     * Constructor.
     * @param name Opcode name.
     */
    public OpcodeName(final String name) {
        this(
            OpcodeName.NAMES.entrySet()
                .stream().filter(e -> e.getValue().equals(name.toLowerCase(Locale.ROOT)))
                .findFirst()
                .orElseThrow(
                    () -> new IllegalArgumentException(
                        String.format("Opcode name '%s' not found", name)
                    )
                )
                .getKey()
        );
    }

    /**
     * Constructor.
     * @param opcode Bytecode operation code.
     */
    public OpcodeName(final int opcode) {
        this.opcode = opcode;
    }

    /**
     * Get simplified opcode name without counter.
     * @return Simplified opcode name.
     */
    public String simplified() {
        return OpcodeName.NAMES.getOrDefault(this.opcode, OpcodeName.UNKNOWN);
    }

    /**
     * Get opcode.
     * @return Opcode.
     */
    public int code() {
        return this.opcode;
    }

    /**
     * Initialize opcode names.
     * @return Opcode names.
     */
    private static Map<Integer, String> init() {
        try {
            final Map<Integer, String> res = new HashMap<>();
            for (final Field field : Opcodes.class.getFields()) {
                if (field.getType() == int.class) {
                    res.put(field.getInt(Opcodes.class), field.getName().toLowerCase(Locale.ROOT));
                }
            }
            return res;
        } catch (final IllegalAccessException exception) {
            throw new IllegalStateException(
                String.format("Can't retrieve opcode names from '%s'", Opcodes.class),
                exception
            );
        }
    }
}

