package modules;

import ast.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class ModuleLoaderTest {

    private Path tempModulesDir;

    @BeforeEach
    void setUp() throws IOException {
        tempModulesDir = Files.createTempDirectory("rx-modules-test");

        Files.writeString(tempModulesDir.resolve("UserModule.rx"), """
            import Prelude
            def userRule() = true
            """);
    }

    @Test
    void testLoadAll_LoadsPreludeAndMainAndImports() {
        Pattern pattern = new Pattern("mainRule", List.of());
        Rule mainRule = new Rule(pattern, new IntLiteral(1));
        Import importUser = new Import("UserModule");

        ModuleLoader loader = new ModuleLoader(tempModulesDir);
        Map<String, Namespace> modules = loader.loadAll(List.of(mainRule), List.of(importUser));

        assertThat(modules)
                .containsKeys("Prelude", "Main", "UserModule");

        assertThat(modules.get("Main").rules())
                .extracting(r -> r.pattern().name())
                .containsExactly("mainRule");

        assertThat(modules.get("Main").imports())
                .extracting(Import::module)
                .containsExactly("UserModule");

        assertThat(modules.get("UserModule").imports())
                .extracting(Import::module)
                .containsExactly("Prelude");

        assertThat(modules.get("UserModule").rules())
                .extracting(r -> r.pattern().name())
                .containsExactly("userRule");
    }

    @Test
    void testLoadAll_ThrowsForMissingModule() {
        Import missing = new Import("DoesNotExist");
        ModuleLoader loader = new ModuleLoader(tempModulesDir);

        assertThatThrownBy(() -> loader.loadAll(List.of(), List.of(missing)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Module not found");
    }

    @Test
    void testLoadPreludeIsAlwaysLoaded() {
        ModuleLoader loader = new ModuleLoader(tempModulesDir);
        Map<String, Namespace> modules = loader.loadAll(List.of(), List.of());

        assertThat(modules).containsKey("Prelude");
    }
}
