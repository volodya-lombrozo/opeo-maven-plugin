package org.eolang.opeo.storage;

import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eolang.opeo.compilation.DefaultCompiler;

public final class CompilationStorage implements Storage {

    /**
     * Path to the generated XMIRs by opeo-maven-plugin.
     * In other words, it is the folder of the high-level EO constructs that were decompiled
     * on the previous step.
     */
    private final Path xmirs;


    /**
     * Path to the output directory.
     * The output folder with XMIRs accepted by jeo-maven-plugin.
     */
    private final Path output;

    public CompilationStorage(final Path xmirs, final Path output) {
        this.xmirs = xmirs;
        this.output = output;
    }

    @Override
    public Collection<XmirEntry> all() {
        if (!Files.exists(this.xmirs)) {
            throw new IllegalArgumentException(
                String.format(
                    "The input xmirs folder '%s' doesn't exist",
                    this.xmirs
                )
            );
        }
        Logger.info(this, "Compiling EO sources from %[file]s", this.xmirs);
        Logger.info(this, "Saving new compiled EO sources to %[file]s", this.output);
        try (Stream<Path> decompiled = Files.walk(this.xmirs).filter(CompilationStorage::isXmir)) {
            return decompiled.map(this::read).collect(Collectors.toList());
        } catch (final IOException exception) {
            throw new IllegalStateException(
                String.format(
                    "Some problem with reading XMIRs from the '%s' folder",
                    this.xmirs
                ),
                exception
            );
        }
    }

    private XmirEntry read(final Path path) {
        try {
            return new XmirEntry(new XMLDocument(path), this.xmirs.relativize(path).toString());
        } catch (final FileNotFoundException exception) {
            throw new IllegalStateException(
                String.format("Can't compile '%x'", path),
                exception
            );
        }
    }

    /**
     * Check if the file is XMIR.
     * @param path Path to the file
     * @return True if the file is XMIR
     */
    private static boolean isXmir(final Path path) {
        return Files.isRegularFile(path) && path.toString().endsWith(".xmir");
    }


    @Override
    public void save(final XmirEntry xml) {
        try {
            final Path out = this.output.resolve(Path.of(xml.pckg()));
            Files.createDirectories(out.getParent());
            Files.write(
                out,
                xml.xml().toString().getBytes(StandardCharsets.UTF_8)
            );
            Logger.info(this, "Compiled %[file]s (%[size]s)", this.output, Files.size(this.output));
        } catch (final IOException exception) {
            throw new IllegalStateException(
                String.format("Can't compile '%s'", xml),
                exception
            );
        }
    }
}