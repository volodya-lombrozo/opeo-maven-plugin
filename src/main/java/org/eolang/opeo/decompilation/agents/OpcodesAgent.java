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

import org.eolang.opeo.decompilation.DecompilerState;

/**
 * Agent that handles opcodes.
 * @since 0.4
 */
public final class OpcodesAgent implements DecompilationAgent {

    /**
     * Original agent that supports some opcodes.
     * If the agent does not support the current opcode, it will be skipped.
     */
    private final DecompilationAgent agent;

    /**
     * Constructor.
     * @param original Original agent that supports some opcodes.
     */
    public OpcodesAgent(final DecompilationAgent original) {
        this.agent = original;
    }

    @Override
    public boolean appropriate(final DecompilerState state) {
        return state.hasInstructions() && this.supported().isSupported(state.current());
    }

    @Override
    public void handle(final DecompilerState state) {
        this.agent.handle(state);
    }

    @Override
    public Supported supported() {
        return this.agent.supported();
    }
}
