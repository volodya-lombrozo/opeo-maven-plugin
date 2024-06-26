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
package it;

import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.tools.ToolProvider;
import org.cactoos.io.ResourceOf;
import org.cactoos.text.TextOf;
import org.eolang.jeo.representation.BytecodeRepresentation;
import org.eolang.jeo.representation.bytecode.Bytecode;
import org.eolang.jucs.ClasspathSource;
import org.eolang.opeo.jeo.JeoDecompiler;
import org.eolang.parser.xmir.Xmir;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.condition.EnabledOnJre;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;

/**
 * Integration tests that verify that java code transforms into EO correctly.
 * The test logic is as follows:
 * 1. Compile java code into bytecode
 * 2. Transform bytecode into XMIR
 * 3. Transform XMIR into EO code
 * 4. Compare EO code with expected EO code
 *
 * @since 0.1
 * @todo #316:60min Enable {@link TrasformationPacksTest}.
 *  This test is disabled because it tries to compare results with outdated EO representation.
 *  We need to fix EO representation of decompiled bytecode and fix the tests.
 *  Don't forget to remove @Disabled annotation from all the methods inside the class.
 */
final class TrasformationPacksTest {

    @ParameterizedTest
    @ClasspathSource(value = "packs", glob = "**.yaml")
    @EnabledIf(value = "hasJavaCompiler", disabledReason = "Java compiler is not available")
    @Disabled
    @EnabledOnJre({JRE.JAVA_11, JRE.JAVA_17})
    void checksPack(final String pack, @TempDir final Path where) throws IOException {
        final JavaEoPack jeopack = new JavaEoPack(pack);
        final List<Program> java = jeopack.java();
        final List<String> decompiled = TrasformationPacksTest.compile(where, java)
            .stream()
            .map(BytecodeRepresentation::new)
            .map(BytecodeRepresentation::toEO)
            .map(JeoDecompiler::new)
            .map(JeoDecompiler::decompile)
            .map(Xmir.Default::new)
            .map(Xmir.Default::toEO)
            .collect(Collectors.toList());
        final List<Program> eolang = jeopack.eolang();
        final List<String> expected = eolang
            .stream()
            .map(Program::src)
            .collect(Collectors.toList());
        MatcherAssert.assertThat(
            String.format(
                "Decompiled EO (number of files %d) doesn't match expected EO (number of files %d). Result: %n%s%n",
                decompiled.size(),
                expected.size(),
                String.join("\n", decompiled)
            ),
            decompiled.size(),
            Matchers.equalTo(expected.size())
        );
        for (int index = 0; index < expected.size(); ++index) {
            MatcherAssert.assertThat(
                "Decompiled EO doesn't match with expected",
                decompiled.get(index),
                Matchers.equalTo(expected.get(index))
            );
        }
    }

    /**
     * You can use this method to check how jeo + opeo decompile java bytecode into EO.
     * - "decompiled" variable contains XMIR representation of EO code.
     *
     * @param where Temporary directory where to compile java code.
     * @throws IOException If fails.
     */
    @Test
    @Disabled
    @EnabledOnJre({JRE.JAVA_11, JRE.JAVA_17})
    void decompilesSimpleExample(@TempDir final Path where) throws Exception {
        final XML decompiled = new JeoDecompiler(
            new BytecodeRepresentation(
                TrasformationPacksTest.compile(
                    where,
                    new Program(
                        "Simple.java",
                        "public class Simple { public static void main(String[] args) { new StringBuilder(\"abc\").append(1); } }"
                    )
                ).get(0)
            ).toEO()
        ).decompile();
        Logger.debug(this, "Decompiled XMIR example:%n%s%n", decompiled);
        MatcherAssert.assertThat(
            "Decompiled EO doesn't match expected EO",
            new Xmir.Default(decompiled).toEO(),
            Matchers.equalTo(
                new TextOf(new ResourceOf("simple.eo")).asString()
            )
        );
    }

    /**
     * Compile java classes into bytecode.
     *
     * @param where Where to compile.
     * @param program All java programs to compile.
     * @return Bytecode.
     * @throws IOException If fails.
     */
    private static List<Bytecode> compile(
        final Path where,
        final Program... program
    ) throws IOException {
        return TrasformationPacksTest.compile(where, Arrays.asList(program));
    }

    /**
     * Compile java classes into bytecode.
     *
     * @param where Where to compile.
     * @param programs All java programs to compile.
     * @return Bytecode.
     */
    private static List<Bytecode> compile(
        final Path where,
        final List<? extends Program> programs
    ) throws IOException {
        final Collection<String> saved = new ArrayList<>(0);
        for (final Program program : programs) {
            final Path path = where.resolve(program.name());
            saved.add(path.toString());
            Files.write(
                path,
                program.src().getBytes(StandardCharsets.UTF_8)
            );
        }
        ToolProvider.getSystemJavaCompiler().run(
            System.in,
            System.out,
            System.err,
            Stream.concat(
                Stream.of("-g:none", "-source", "11", "-target", "11"),
                saved.stream()
            ).toArray(String[]::new)
        );
        return Arrays.stream(where.toFile().listFiles())
            .map(File::toPath)
            .sorted()
            .filter(p -> p.getFileName().toString().endsWith(".class"))
            .map(TrasformationPacksTest::readBytectode)
            .collect(Collectors.toList());
    }

    /**
     * Tries to read bytecode of a classfile.
     *
     * @param classfile Classfile with '.class' extension.
     * @return Bytecode.
     */
    private static Bytecode readBytectode(final Path classfile) {
        try {
            return new Bytecode(Files.readAllBytes(classfile));
        } catch (final IOException exception) {
            throw new IllegalStateException(
                String.format("Can't read classfile: %s", classfile),
                exception
            );
        }
    }

    /**
     * Check if java compiler is available.
     * Don't care about PMD warning: this method is used in @EnabledIf annotation.
     *
     * @return True if java compiler is available.
     */
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private static boolean hasJavaCompiler() {
        return ToolProvider.getSystemJavaCompiler() != null;
    }
}
