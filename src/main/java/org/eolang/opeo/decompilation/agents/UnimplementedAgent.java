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

import org.eolang.opeo.ast.Opcode;
import org.eolang.opeo.decompilation.DecompilerState;

/**
 * Unimplemented instruction handler.
 * @since 0.1
 */
final class UnimplementedAgent implements DecompilationAgent {

    /**
     * Do we put numbers to opcodes?
     */
    private final boolean counting;

    /**
     * Constructor.
     * @param counting Flag which decides if we need to count opcodes.
     */
    UnimplementedAgent(final boolean counting) {
        this.counting = counting;
    }

    @Override
    public boolean appropriate(final DecompilerState state) {
        return state.hasInstructions() && !new AllAgents().supported().isSupported(state.current());
    }

    @Override
    public Supported supported() {
        return new Supported();
    }

    @Override
    public void handle(final DecompilerState state) {
        if (this.appropriate(state)) {
            state.stack().push(
                new Opcode(
                    state.current().opcode(),
                    state.current().params(),
                    this.counting
                )
            );
            state.popInstruction();
        } else {
            throw new IllegalAgentException(this, state);
        }
    }
}
