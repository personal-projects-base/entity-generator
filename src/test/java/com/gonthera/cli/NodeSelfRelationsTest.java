package com.gonthera.cli;

import com.google.gson.Gson;
import com.gonthera.cli.enuns.Language;
import com.gonthera.cli.model.*;
import com.gonthera.cli.service.common.Common;
import com.gonthera.cli.service.node.GenerateEnum;
import com.gonthera.cli.service.node.GenerateModel;
import com.gonthera.cli.service.node.GeneratePrisma;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.Assert.*;

public class NodeSelfRelationsTest {
    @Test
    public void generatesSelfRelationsWithoutSelfImportsAndDeduplicatesReferences() throws Exception {
        java.util.List<Entities> entities = fixture();
        Entities member = entities.get(4);
        // Inverse intentionally comes before owner: resolution must not depend on field order.
        member.getEntityFields().add(oneToOne("previous", true, "next"));
        member.getEntityFields().add(oneToOne("next", false, null));
        member.getEntityFields().add(field("status", "memberStatus"));
        member.getEntityFields().add(field("previousStatus", "MEMBERSTATUS"));
        Enums status = new Enums();
        status.setEnumName("memberStatus");
        status.setValues(Arrays.asList("ACTIVE", "INACTIVE"));
        Path root = Path.of("target/node-self-relations").toAbsolutePath();
        String previous = System.getProperty("user.dir");
        com.gonthera.cli.model.Properties old = Common.properties;
        try {
            com.gonthera.cli.model.Properties project = new com.gonthera.cli.model.Properties();
            project.setLanguage(Language.NODE);
            project.setEnums(Collections.singletonList(status));
            Common.properties = project;
            System.setProperty("user.dir", root.toString());
            GeneratePrisma.generatePrismaSchema(entities, project.getEnums());
            GenerateModel.generateModels(entities, root.resolve("src/generated"));
            GenerateEnum.generateEnums(project.getEnums(), root.resolve("src/generated"));
            Files.createDirectories(root.resolve("src/generated/common"));
            Files.writeString(root.resolve("src/generated/common/contracts.ts"), Common.loadWxsd("crudcontracts"));
        } finally {
            Common.properties = old;
            System.setProperty("user.dir", previous);
        }
        String dto = Files.readString(root.resolve("src/generated/models/member.model.ts"));
        assertFalse(dto.contains("from './member.model'"));
        assertTrue(dto.contains("next?: MemberDTO"));
        assertTrue(dto.contains("following: MemberDTO[]"));
        assertEquals(1, dto.lines().filter(line -> line.contains("../enums/")).count());
        assertTrue(dto.contains("previousStatus?: MemberStatus"));
        String purchases = Files.readString(root.resolve("src/generated/models/purchase.model.ts"));
        assertEquals(1, purchases.lines().filter(line -> line.contains("from './customer.model'")).count());
        String schema = Files.readString(root.resolve("prisma/schema.prisma"));
        assertTrue(schema.contains("previous Member? @relation(\"Member_Member_next\")"));
        assertTrue(schema.contains("nextId String? @unique @db.Uuid"));
        assertFalse(schema.contains("MEMBERSTATUS"));
    }

    @Test
    public void rejectsSelfOneToOneWithoutInverseBeforeGeneration() throws Exception {
        List<Entities> entities = fixture();
        entities.get(4).getEntityFields().add(oneToOne("next", false, null));
        try {
            GeneratePrisma.validateCollectionRelations(entities);
            fail("Expected missing inverse error");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("member.next"));
            assertTrue(ex.getMessage().contains("found 0"));
        }
    }

    @Test
    public void rejectsAmbiguousSelfOneToOne() throws Exception {
        List<Entities> entities = fixture();
        entities.get(4).getEntityFields().add(oneToOne("next", false, null));
        entities.get(4).getEntityFields().add(oneToOne("previous", true, "next"));
        entities.get(4).getEntityFields().add(oneToOne("otherPrevious", true, "next"));
        try {
            GeneratePrisma.validateCollectionRelations(entities);
            fail("Expected ambiguous inverse error");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("found 2"));
        }
    }

    private List<Entities> fixture() throws Exception {
        return Arrays.asList(new Gson().fromJson(Files.readString(
                Path.of("src/test/resources/node-relations/collections.json")), Entities[].class));
    }

    private EntityFields oneToOne(String name, boolean inverse, String mappedBy) {
        EntityFields result = field(name, "member");
        RelationsShips relation = new RelationsShips();
        relation.setRelationShip("OneToOne");
        relation.setBidirectional(inverse);
        relation.setMappedBy(mappedBy);
        result.setRelationShips(relation);
        return result;
    }

    private EntityFields field(String name, String type) {
        EntityFields result = new EntityFields();
        result.setFieldName(name);
        FieldProperties properties = new FieldProperties();
        properties.setFieldType(type);
        result.setFieldProperties(properties);
        FieldMetadata metadata = new FieldMetadata();
        metadata.setNullable(true);
        result.setMetadata(metadata);
        return result;
    }
}
