package com.gonthera.cli;

import com.google.gson.Gson;
import com.gonthera.cli.enuns.Language;
import com.gonthera.cli.model.Entities;
import com.gonthera.cli.model.Properties;
import com.gonthera.cli.service.common.Common;
import com.gonthera.cli.service.node.GenerateNode;
import com.gonthera.cli.service.node.GeneratePrisma;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class NodeCollectionRelationsTest {
    private List<Entities> fixture() throws Exception {
        return Arrays.asList(new Gson().fromJson(Files.readString(
                Path.of("src/test/resources/node-relations/collections.json")), Entities[].class));
    }

    @Test
    public void generatesDistinctPairsNativeForeignKeysAndSelfRelationsIndependentOfOrder() throws Exception {
        List<Entities> entities = fixture();
        String schema = generate(entities, Path.of("target/node-relations/collections").toAbsolutePath());
        assertTrue(schema.contains("orders Purchase[] @relation(\"Purchase_Customer_customer\")"));
        assertTrue(schema.contains("reviewed Purchase[] @relation(\"Purchase_Customer_reviewer\")"));
        assertTrue(schema.contains("customerId String @db.Uuid @map(\"customer\")"));
        assertTrue(schema.contains("reviewerId String? @db.Uuid @map(\"reviewer\")"));
        assertTrue(schema.contains("parentId Int? @map(\"parent\")"));
        assertTrue(schema.contains("references: [code]"));
        assertFalse(schema.contains("@unique"));
        assertTrue(schema.contains("courses Course[] @relation(\"School_Course_courses\")"));
        assertTrue(schema.contains("schools School[] @relation(\"School_Course_courses\")"));
        assertTrue(schema.contains("featuredAt School[] @relation(\"School_Course_featured\")"));
        assertTrue(schema.contains("following Member[] @relation(\"Member_Member_following\")"));
        assertTrue(schema.contains("followers Member[] @relation(\"Member_Member_following\")"));
        Collections.reverse(entities);
        entities.forEach(entity -> Collections.reverse(entity.getEntityFields()));
        String reversed = generate(entities, Path.of("target/node-relations/reversed").toAbsolutePath());
        assertEquals(schema.lines().map(String::trim).filter(s -> !s.isEmpty()).sorted().collect(java.util.stream.Collectors.toList()),
                reversed.lines().map(String::trim).filter(s -> !s.isEmpty()).sorted().collect(java.util.stream.Collectors.toList()));
    }

    @Test
    public void rejectsMissingInverse() throws Exception {
        List<Entities> entities = fixture();
        entities.get(0).getEntityFields().remove(1);
        rejects(entities, "purchase.customer");
    }

    @Test
    public void rejectsAmbiguousInverse() throws Exception {
        List<Entities> entities = fixture();
        entities.get(0).getEntityFields().get(2).getRelationShips().setMappedBy("customer");
        rejects(entities, "found 2");
    }

    @Test
    public void rejectsInvalidMappedBy() throws Exception {
        List<Entities> entities = fixture();
        entities.get(0).getEntityFields().get(1).getRelationShips().setMappedBy("missing");
        rejects(entities, "purchase.missing");
    }

    @Test
    public void rejectsCollectionWithScalarShape() throws Exception {
        List<Entities> entities = fixture();
        entities.get(2).getEntityFields().get(1).setList(false);
        rejects(entities, "must have list=true");
    }

    @Test
    public void rejectsManyToOneWithInverseOwnership() throws Exception {
        List<Entities> entities = fixture();
        entities.get(1).getEntityFields().get(1).getRelationShips().setBidirectional(true);
        rejects(entities, "expected one ManyToOne owner");
    }

    @Test
    public void rejectsTwoManyToManyOwners() throws Exception {
        List<Entities> entities = fixture();
        entities.get(3).getEntityFields().get(1).getRelationShips().setBidirectional(false);
        rejects(entities, "expected exactly one ManyToMany inverse");
    }

    @Test
    public void invalidPairsDoNotEraseExistingGeneratedFiles() throws Exception {
        List<Entities> entities = fixture();
        entities.get(0).getEntityFields().remove(1);
        Path root = Files.createTempDirectory("gonthera-invalid-node-");
        Path marker = root.resolve("src/generated/existing.ts");
        Files.createDirectories(marker.getParent());
        Files.writeString(marker, "preserve");
        String previous = System.getProperty("user.dir");
        try {
            System.setProperty("user.dir", root.toString());
            Properties project = new Properties();
            project.setEntities(entities);
            try { GenerateNode.generateSource(project); fail("Expected rejection"); }
            catch (IllegalArgumentException expected) { assertEquals("preserve", Files.readString(marker)); }
        } finally { System.setProperty("user.dir", previous); }
    }

    private void rejects(List<Entities> entities, String message) {
        try { GeneratePrisma.validateCollectionRelations(entities); fail("Expected rejection"); }
        catch (IllegalArgumentException ex) { assertTrue(ex.getMessage(), ex.getMessage().contains(message)); }
    }

    private String generate(List<Entities> entities, Path root) throws Exception {
        String previous = System.getProperty("user.dir");
        Properties previousProperties = Common.properties;
        try {
            Properties project = new Properties();
            project.setLanguage(Language.NODE);
            project.setEnums(Collections.emptyList());
            Common.properties = project;
            System.setProperty("user.dir", root.toString());
            GeneratePrisma.generatePrismaSchema(entities, project.getEnums());
            return Files.readString(root.resolve("prisma/schema.prisma"));
        } finally {
            System.setProperty("user.dir", previous);
            Common.properties = previousProperties;
        }
    }
}
