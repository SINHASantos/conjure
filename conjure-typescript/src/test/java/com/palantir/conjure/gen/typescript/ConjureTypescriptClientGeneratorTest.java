/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.gen.typescript.services.DefaultServiceGenerator;
import com.palantir.conjure.gen.typescript.types.DefaultTypeGenerator;
import com.palantir.conjure.gen.typescript.utils.GenerationUtils;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class ConjureTypescriptClientGeneratorTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private File src;
    private ConjureTypescriptClientGenerator generator;

    @Before
    public void before() throws IOException {
        src = folder.newFolder("src");
        generator = new ConjureTypescriptClientGenerator(
                new DefaultServiceGenerator(),
                new DefaultTypeGenerator());
    }

    @Test
    public void nativeTypesTest() throws IOException {
        ConjureDefinition conjure = Conjure.parse(new File("src/test/resources/native-types.conjure"));
        generator.emit(ImmutableList.of(conjure), src);
        String xfile = "package/foo.ts";
        assertThat(compiledFileContent(src, xfile))
                .contains("interface IFoo");
        assertThat(compiledFileContent(src, xfile))
                .contains("fdouble: number");
        assertThat(compiledFileContent(src, xfile))
                .contains("finteger: number");
        assertThat(compiledFileContent(src, xfile))
                .contains("fmap: { [key: string]: string }");
        assertThat(compiledFileContent(src, xfile))
                .contains("fstring: string");
        assertThat(compiledFileContent(src, xfile))
                .contains("foptional?: string");
    }

    @Test
    public void referenceTypesTest() throws IOException {
        ConjureDefinition conjure = Conjure.parse(new File("src/test/resources/reference-types.conjure"));
        generator.emit(ImmutableList.of(conjure), src);
        String fooFile = "package1/foo.ts";
        String barFile = "package1/folder/bar.ts";
        String boomFile = "package2/folder/boom.ts";

        // Assert all files are generated
        assertThat(compiledFileContent(src, fooFile))
                .contains("interface IFoo");
        assertThat(compiledFileContent(src, barFile))
                .contains("interface IBar");
        assertThat(compiledFileContent(src, boomFile))
                .contains("interface IBoom");

        // Assert expected references to Bar/Boom, and EnumObject from Foo
        assertThat(compiledFileContent(src, fooFile))
                .contains("import { IBar } from \"./folder/bar\"");
        assertThat(compiledFileContent(src, fooFile))
                .contains("import { IBoom } from \"../package2/folder/boom\"");
        assertThat(compiledFileContent(src, fooFile))
                .contains("import { EnumObject } from \"./enumObject\"");
    }

    @Test
    public void indexFileTest() throws IOException {
        ConjureDefinition conjure = Conjure.parse(new File("src/test/resources/services/test-service.yml"));
        generator.emit(ImmutableList.of(conjure), src);

        assertThat(compiledFileContent(src, "index.ts")).isEqualTo(new String(Files.readAllBytes(
                new File("src/test/resources/services/test-service-index.ts").toPath()), StandardCharsets.UTF_8));
    }

    @Test
    public void indexFileTest_duplicate() throws IOException {
        ConjureDefinition conjure = Conjure.parse(new File("src/test/resources/services-types-duplicates.yml"));
        generator.emit(ImmutableList.of(conjure), src);

        assertThat(compiledFileContent(src, "index.ts")).isEqualTo(new String(Files.readAllBytes(
                new File("src/test/resources/services-types-duplicates-index.ts").toPath()), StandardCharsets.UTF_8));
    }

    @Test
    public void indexFileTest_multipleConjureDefinitions() throws IOException {
        ConjureDefinition conjure1 = Conjure.parse(new File("src/test/resources/multiple-conjure-files-1.yml"));
        ConjureDefinition conjure2 = Conjure.parse(new File("src/test/resources/multiple-conjure-files-2.yml"));
        List<ConjureDefinition> definitions = ImmutableList.of(conjure1, conjure2);
        generator.emit(definitions, src);

        assertThat(compiledFileContent(src, "index.ts")).isEqualTo(new String(Files.readAllBytes(
                new File("src/test/resources/multiple-conjure-files-index.ts").toPath()), StandardCharsets.UTF_8));
    }

    @Test
    public void testConjureImports() throws IOException {
        ConjureDefinition conjure = Conjure.parse(new File("src/test/resources/example-conjure-imports.yml"));
        generator.emit(ImmutableList.of(conjure), src);

        // Generated files contain imports
        assertThat(compiledFileContent(src, "with/imports/complexObjectWithImports.ts"))
                .contains("import { IStringExample } from \"../../test/api/stringExample\"");
        assertThat(compiledFileContent(src, "with/imports/testService.ts"))
                .contains("import { IBackingFileSystem } from \"../../foundry/catalog/api/datasets/backingFileSystem\"")
                .contains("import { IStringExample } from \"../../test/api/stringExample\"");

        // Imported files are not generated
        assertThat(compiledFile(src, "foundry/catalog/api/datasets/backingFileSystem.ts")).doesNotExist();
        assertThat(compiledFile(src, "test/api/stringExample.ts")).doesNotExist();
    }

    private static String compiledFileContent(File srcDir, String clazz) throws IOException {
        return GenerationUtils.getCharSource(compiledFile(srcDir, clazz));
    }

    private static File compiledFile(File srcDir, String clazz) throws IOException {
        return new File(srcDir, clazz);
    }
}
