package com.gonthera.cli;

import com.gonthera.cli.enuns.Language;
import com.gonthera.cli.model.*;
import com.gonthera.cli.service.common.Common;
import com.gonthera.cli.service.node.GeneratePrisma;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class NodeOneToOneTest {
    @Test
    public void pairsExplicitMappedByAndPreservesUuidForeignKeyType() throws Exception {
        Entities customer = entity("customer", "code", "uuid", relation("profile", "profile", true, "customerRef", false));
        Entities profile = entity("profile", "id", "uuid", relation("customerRef", "customer", false, null, false));
        String schema = generate(customer, profile);
        assertTrue(schema.contains("profile Profile? @relation(\"Profile_Customer_customerRef\")"));
        assertTrue(schema.contains("customerRef Customer @relation(\"Profile_Customer_customerRef\", fields: [customerRefId], references: [code])"));
        assertTrue(schema.contains("customerRefId String @unique @db.Uuid @map(\"customer_ref\")"));
        assertFalse(schema.contains("profileId"));
    }

    @Test
    public void defaultsMappedByToInverseEntityNameAndSupportsOptionalIntegerForeignKey() throws Exception {
        Entities customer = entity("customer", "code", "int", relation("profile", "profile", true, null, true));
        Entities profile = entity("profile", "id", "int", relation("customer", "customer", false, null, true));
        String schema = generate(profile, customer);
        assertTrue(schema.contains("profile Profile? @relation(\"Profile_Customer_customer\")"));
        assertTrue(schema.contains("customerId Int? @unique @map(\"customer\")"));
        assertTrue(schema.contains("customer Customer? @relation(\"Profile_Customer_customer\", fields: [customerId], references: [code])"));
    }

    @Test
    public void disambiguatesTwoRelationsBetweenTheSameModels() throws Exception {
        Entities customer = entity("customer", "id", "uuid",
                relation("mainProfile", "profile", true, "mainCustomer", true),
                relation("backupProfile", "profile", true, "backupCustomer", true));
        Entities profile = entity("profile", "id", "uuid",
                relation("mainCustomer", "customer", false, null, true),
                relation("backupCustomer", "customer", false, null, true));
        String schema = generate(customer, profile);
        assertTrue(schema.contains("mainProfile Profile? @relation(\"Profile_Customer_mainCustomer\")"));
        assertTrue(schema.contains("backupProfile Profile? @relation(\"Profile_Customer_backupCustomer\")"));
        assertTrue(schema.contains("mainCustomerId String? @unique @db.Uuid"));
        assertTrue(schema.contains("backupCustomerId String? @unique @db.Uuid"));
    }

    @Test
    public void rejectsMappedByThatDoesNotIdentifyTheOwner() throws Exception {
        Entities customer = entity("customer", "id", "uuid", relation("profile", "profile", true, "missing", true));
        Entities profile = entity("profile", "id", "uuid", relation("customer", "customer", false, null, false));
        try {
            generate(customer, profile);
            fail("Expected invalid mappedBy to be rejected");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("customer.profile -> profile.missing"));
        }
    }

    private String generate(Entities... entities) throws Exception {
        Path root = Files.createTempDirectory("gonthera-node-one-to-one-");
        String previousDirectory = System.getProperty("user.dir");
        Properties previousProperties = Common.properties;
        Properties project = new Properties();
        project.setLanguage(Language.NODE);
        project.setEnums(Collections.emptyList());
        try {
            System.setProperty("user.dir", root.toString());
            Common.properties = project;
            GeneratePrisma.generatePrismaSchema(Arrays.asList(entities), project.getEnums());
            return Files.readString(root.resolve("prisma/schema.prisma"));
        } finally {
            Common.properties = previousProperties;
            System.setProperty("user.dir", previousDirectory);
        }
    }

    private Entities entity(String name, String keyName, String keyType, EntityFields... relations) {
        Entities entity = new Entities();
        entity.setEntityName(name);
        EntityFields key = field(keyName, keyType, false);
        key.getMetadata().setKey(true);
        java.util.List<EntityFields> fields = new java.util.ArrayList<>();
        fields.add(key);
        fields.addAll(Arrays.asList(relations));
        entity.setEntityFields(fields);
        return entity;
    }

    private EntityFields relation(String name, String type, boolean inverse, String mappedBy, boolean nullable) {
        EntityFields field = field(name, type, nullable);
        RelationsShips relation = new RelationsShips();
        relation.setRelationShip("OneToOne");
        relation.setBidirectional(inverse);
        relation.setMappedBy(mappedBy);
        field.setRelationShips(relation);
        return field;
    }

    private EntityFields field(String name, String type, boolean nullable) {
        EntityFields field = new EntityFields();
        field.setFieldName(name);
        FieldProperties properties = new FieldProperties();
        properties.setFieldType(type);
        field.setFieldProperties(properties);
        FieldMetadata metadata = new FieldMetadata();
        metadata.setNullable(nullable);
        field.setMetadata(metadata);
        return field;
    }
}
