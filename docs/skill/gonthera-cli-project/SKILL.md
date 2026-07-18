---
name: gonthera-cli-project
description: Use this skill when working on the Gonthera CLI Maven plugin, especially when changing Java, .NET, Node.js, SQL generation, project.json schema, RabbitMQ messaging, relationships, DTO converters, Prisma generation, README, Docusaurus docs, or handoff documentation.
---

# Gonthera CLI Project

Use this skill for code or documentation changes in the `gonthera-cli` project.

## Project Purpose

`gonthera-cli` is a Maven plugin/JAR that reads `project.json` from the current working directory and generates code for service projects. It temporarily falls back to the legacy `properties.json` when `project.json` is absent.

Configuration lookup order:

1. `.gonthera/project.json` with optional separated files;
2. root `project.json`;
3. root legacy `properties.json`.

When `.gonthera` exists, its `project.json` is required. `entities.json`, `endpoints.json`, and `enums.json` contain arrays directly; `messaging.json` contains the messaging object. All sections may still remain inside `.gonthera/project.json`; each separated file overrides only its corresponding section. The loader merges them into the same `Properties` model used by single-file configuration.

Validation is available without generation through `mvn gonthera-cli:validate` or `gonthera-cli.exe --validate`. Validation requires `.gonthera`, checks JSON syntax and shape, rejects unknown properties at any nesting level, and then applies the shared semantic `ProjectValidator`. It must not create or delete generated output. Root configuration remains temporarily supported for generation only.

Supported targets:

- `JAVA`: Spring/JPA style generation under `src/main/java/<mainPackage>_gen` and resources under `src/main/resources`.
- `DOTNET`: C# generation under `<mainPackage>_gen` and static files under `static`.
- `NODE`: TypeScript generation under `src/generated` plus `prisma/schema.prisma`.
- SQL: PostgreSQL script generation as `postgree.sql`.
- Messaging: RabbitMQ generation under `messaging.RabbitMq`.

## Must Read First

Before making non-trivial changes, inspect:

- `docs/handoff/gonthera-cli-project/HANDOFF.md`: implementation-oriented behavior and known limitations.
- `docs/handoff/HANDOFF_FRONTEND.md`: frontend-facing JSON and query contracts.
- `README.md`: user-facing instructions.
- `CHANGELOG.md`: current release notes and future improvements.
- Relevant generator package:
  - Java: `src/main/java/com/gonthera/cli/service/java`
  - .NET: `src/main/java/com/gonthera/cli/service/dotNet`
  - Node: `src/main/java/com/gonthera/cli/service/node`
  - Shared: `src/main/java/com/gonthera/cli/service/common`
  - Models: `src/main/java/com/gonthera/cli/model`

For docs-only changes in the Docusaurus project, inspect `docs-docusaurus/docs` and `docs-docusaurus/sidebars.js`.

## project.json Contract

The minimum valid shape must include arrays even when empty:

```json
{
  "mainPackage": "com.example.service",
  "projectName": "service-name",
  "language": "JAVA",
  "entities": [],
  "endpoints": [],
  "enums": []
}
```

Accepted `language` values are uppercase:

- `JAVA`
- `DOTNET`
- `NODE`

The structural validator rejects unknown JSON properties before generation. Do not document unsupported properties as active behavior, even though Gson itself would otherwise ignore them.

## Entity Field Shape

Use the real field shape:

```json
{
  "comment": "Identificador",
  "fieldName": "id",
  "list": false,
  "fieldProperties": {
    "fieldType": "uuid",
    "required": true,
    "valueDefault": ""
  },
  "metadata": {
    "nullable": false,
    "key": true
  }
}
```

Do not use simplified field examples like top-level `fieldType` or top-level `nullable`; the implemented model uses `fieldProperties.fieldType` and `metadata.nullable`.

## Relationships

Java and C# relationship generation are considered mature. Node relationship generation is initial and may require manual Prisma review.

Relationship fields use `relationShips`:

```json
{
  "fieldName": "parentCode",
  "list": false,
  "fieldProperties": {
    "fieldType": "costCenter",
    "required": false,
    "valueDefault": ""
  },
  "metadata": {
    "nullable": true,
    "key": false
  },
  "relationShips": {
    "fetchType": "LAZY",
    "relationShip": "ManyToOne",
    "bidirectional": false,
    "reference": true
  }
}
```

Rules:

- `bidirectional: false`: owner side. Generates FK/column and `@JoinColumn` in Java.
- `bidirectional: true`: inverse side. Uses `mappedBy` and does not generate SQL column.
- `mappedBy`: must point to the owner field, for example `children` maps by `parentCode`.
- `reference: true`: normally set on the FK/reference side; Java converters use it to avoid recursion.
- `list: true`: collection side, usually `OneToMany` or `ManyToMany`.
- For self-relations, declare the owner/reference field before the inverse field.

Example inverse side:

```json
{
  "fieldName": "children",
  "list": true,
  "fieldProperties": {
    "fieldType": "costCenter",
    "required": false,
    "valueDefault": ""
  },
  "metadata": {
    "nullable": false,
    "key": false
  },
  "relationShips": {
    "fetchType": "LAZY",
    "relationShip": "OneToMany",
    "mappedBy": "parentCode",
    "bidirectional": true,
    "reference": false
  }
}
```

## Messaging

Messaging is grouped by provider. Current provider:

```json
{
  "messaging": {
    "RabbitMq": {
      "pub": [],
      "sub": []
    }
  }
}
```

Rules:

- Generate RabbitMQ code only when `messaging.RabbitMq` exists and has at least one `pub` or `sub` channel.
- Do not generate RabbitMQ dependencies/configuration when `RabbitMq` is absent, null, or empty.
- Keep the exchange outside `project.json`; Java/.NET use annotation/attribute in consumer code, Node uses a concrete config class.
- The old `pup` typo must not be reintroduced. Use `pub`.
- The old `events`/`listeners` model is not implemented; do not document it as current behavior.

## Node.js Scope

Node generation currently includes:

- models/DTO contracts;
- enums;
- Prisma-friendly repositories;
- Express controllers and routes;
- endpoint contracts;
- RabbitMQ when configured;
- static files;
- SQL;
- `prisma/schema.prisma`.

Node does not generate:

- `package.json`;
- `tsconfig.json`;
- migrations;
- Java-style DTO converters.

Relationship caveat:

- Node/Prisma relation generation is a starting point.
- Self-relations, bidirectional relations, and `ManyToMany` should be documented as requiring manual review before migrations.
- Avoid claiming Node relationship parity with Java/C# until `GeneratePrisma` and generated TypeScript models are improved.

## Templates

The generator source uses `.mxsd` templates under `src/main/resources/xsd`.

Use templates for generated code instead of large inline string literals.

Template folders:

- `xsd/java`
- `xsd/dotnet`
- `xsd/node`
- `xsd/sql`

User-facing documentation should not explain internal templates unless the user explicitly asks about generator development.

## CRUD Filters

Treat filtering as target-specific behavior, not as a portable JPA/SQL query language.

- Java `SpecificationFilter` supports `eq`, `isNull`, `notNull`, `and`, `or`, and dotted relationship paths.
- Java string `eq` is a case-insensitive contains operation; UUID uses exact equality.
- Java numeric, boolean, and date equality is not safely converted by the current template.
- Do not claim reliable mixed `and`/`or` precedence, nested parentheses, escaping, comparison operators, or `in` support.
- .NET `DynamicFilter` is a separate dialect with `eq` and one logical operator kind per expression; collection paths use `*`.
- Node repositories currently ignore `filter` and use only pagination parameters.
- When filter behavior changes, update both entity handoffs and the relevant user documentation.

## Documentation Standards

Update docs when behavior changes:

- `README.md`: user-facing usage.
- `docs/handoff/gonthera-cli-project/HANDOFF.md`: implementation details and caveats.
- `docs/handoff/HANDOFF_FRONTEND.md`: frontend request construction and limitations.
- `CHANGELOG.md`: release notes and future improvements.
- `docs-docusaurus/docs`: detailed user documentation.

Docusaurus docs should focus on how to use the generator, not how the generator is implemented.

For .NET and Node docs, mention that consumers can download `gonthera-cli.exe` or `gonthera-cli-x.x.x.jar` and execute it in the project root, in the same folder as `project.json`.

## Validation

Prefer focused validation over full `mvn test` when generated `_gen` sources or local dependency state make full tests noisy.

Useful compile check for generator sources:

```bash
find src/main/java/com/gonthera/cli -name '*.java' ! -path '*cli_gen*' > /tmp/gonthera-cli-sources.txt
rm -rf /tmp/gonthera-cli-compile
mkdir -p /tmp/gonthera-cli-compile
javac -cp /home/geovane/.m2/repository/org/projectlombok/lombok/1.18.28/lombok-1.18.28.jar:/home/geovane/.m2/repository/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar:/home/geovane/.m2/repository/org/apache/logging/log4j/log4j-api/2.23.1/log4j-api-2.23.1.jar:/home/geovane/.m2/repository/org/apache/logging/log4j/log4j-core/2.23.1/log4j-core-2.23.1.jar:/home/geovane/.m2/repository/org/apache/maven/plugin-tools/maven-plugin-annotations/3.9.0/maven-plugin-annotations-3.9.0.jar:/home/geovane/.m2/repository/org/apache/maven/maven-plugin-api/3.9.3/maven-plugin-api-3.9.3.jar -d /tmp/gonthera-cli-compile @/tmp/gonthera-cli-sources.txt
```

Docusaurus validation:

```bash
cd docs-docusaurus
npm run build
```

When validating generation, create a temporary project under `/tmp`, copy or create `project.json`, execute `Main` or the compiled classes from that folder, then inspect generated output.

## Editing Rules

- Use existing package patterns; do not invent a parallel architecture.
- Keep manual generated-code templates in `.mxsd` files.
- Do not change generated output paths unless explicitly requested.
- Do not revert unrelated dirty files.
- Do not edit generated `_gen` output as source of truth.
- Keep Node relationship limitations honest in docs until implementation is complete.
