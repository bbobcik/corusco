package cz.auderis.corusco.processor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

class IncrementalConsumerTest {
    @TempDir
    Path fixture;

    @Test
    void recompilesAReferencedRowAndRemovesAnUnrequestedCompanion() throws IOException {
        Path artifacts = Files.createDirectory(fixture.resolve("artifacts"));
        for (String module : List.of("annotations", "core", "swing", "processor")) {
            Files.copy(Path.of(System.getProperty("consumerJar.corusco-" + module)),
                    artifacts.resolve(module + ".jar"));
        }
        write("settings.gradle", "rootProject.name = 'incremental-consumer'\n");
        write("build.gradle", """
                plugins { id 'java' }
                dependencies {
                    implementation files('artifacts/annotations.jar', 'artifacts/core.jar', 'artifacts/swing.jar')
                    annotationProcessor files('artifacts/processor.jar', 'artifacts/annotations.jar')
                }
                """);
        write("src/main/java/ui/package-info.java", """
                @cz.auderis.corusco.annotations.SwingCompanionPackage(tables = model.CustomerRow.class)
                package ui;
                """);
        Path companion = fixture.resolve("build/classes/java/main/ui/CustomerRowTableBindings.class");
        Path columns = fixture.resolve(
                "build/generated/sources/annotationProcessor/java/main/model/CustomerRowColumns.java");

        writeRow(210);
        compile();
        assertThat(companion).exists();
        assertThat(columns).content().contains("new ColumnDefaults(210,");

        writeRow(220);
        compile();
        assertThat(companion).exists();
        assertThat(columns).content().contains("new ColumnDefaults(220,");

        write("src/main/java/ui/package-info.java", "package ui;\n");
        compile();
        assertThat(companion).doesNotExist();
        assertThat(columns).exists();
    }

    private void compile() {
        GradleRunner.create()
                .withProjectDir(fixture.toFile())
                .withArguments("compileJava", "--offline", "--max-workers=2", "--stacktrace")
                .build();
    }

    private void writeRow(int width) throws IOException {
        write("src/main/java/model/CustomerRow.java", """
                package model;
                import cz.auderis.corusco.annotations.table.Column;
                import cz.auderis.corusco.annotations.table.CoruscoTable;
                @CoruscoTable(id = "customers")
                public record CustomerRow(@Column(width = %d) String name) {}
                """.formatted(width));
    }

    private void write(String name, String contents) throws IOException {
        Path file = fixture.resolve(name);
        Files.createDirectories(file.getParent());
        Files.writeString(file, contents);
    }
}
