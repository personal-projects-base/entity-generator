# Contexto de continuidade — Arquiteturas Java

Atualizado em: 2026-07-24  
Branch: `feat/hexagonal`

## Objetivo atual

Adicionar ao Gonthera CLI uma escolha de arquitetura para a geração Java:

- `MVC`;
- `HEXAGONAL`.

A implementação começará pelo Java e deve reaproveitar a infraestrutura Spring existente quando ela também fizer sentido na arquitetura hexagonal.

## Regra de continuidade

Este arquivo deve ser atualizado após cada alteração relevante feita nesta frente de trabalho. Ele deve registrar decisões, mudanças realizadas, validações executadas, pendências e o próximo passo recomendado.

Ao iniciar uma nova conversa ou retomar esta branch, leia primeiro este `CONTEXT.md` e use-o como ponto de partida. O documento deve permanecer autossuficiente: toda decisão nova, alteração relevante, validação executada, limitação descoberta e próximo passo deve ser registrada aqui antes de encerrar o trabalho.

Nesta frente, alterações de contexto solicitadas pelo usuário devem ser feitas somente neste arquivo, salvo quando ele pedir explicitamente a atualização de outros documentos.

Durante a construção incremental, não executar a suíte de testes após toda pequena alteração. O usuário fará revisões locais e informará problemas. Reservar testes automatizados para marcos maiores, correções de erro ou quando forem solicitados explicitamente; sempre registrar neste arquivo quando uma alteração ainda não tiver sido testada.

## Como selecionar a arquitetura

A propriedade do cabeçalho que identifica a arquitetura é `architecture`:

```json
{
  "mainPackage": "com.example.service",
  "projectName": "service-name",
  "language": "JAVA",
  "architecture": "HEXAGONAL",
  "entities": [],
  "endpoints": [],
  "enums": []
}
```

Valores aceitos:

- `"MVC"`;
- `"HEXAGONAL"`.

A propriedade é sensível a maiúsculas e minúsculas. Quando `architecture` for omitida, o gerador usa `MVC`. Por enquanto, `HEXAGONAL` é aceito apenas com `"language": "JAVA"`.

## Decisões tomadas

- `GenerateJava` permanece em `com.gonthera.cli.service.java` como fachada e futuro ponto de seleção da arquitetura.
- Código reutilizável entre arquiteturas Java fica em `com.gonthera.cli.service.java.common`.
- O gerador Spring MVC existente fica em `com.gonthera.cli.service.java.mvc`.
- O novo gerador hexagonal ficará em `com.gonthera.cli.service.java.hexagonal`.
- A reorganização inicial não altera o contrato JSON nem a estrutura dos arquivos gerados nos projetos consumidores.
- O cabeçalho aceita `architecture: "MVC"` ou `architecture: "HEXAGONAL"`.
- `architecture` é opcional e usa `MVC` como padrão compatível.
- `HEXAGONAL` é suportado somente quando `language` é `JAVA`.
- O núcleo hexagonal atual gera domínio puro, portas CRUD de entrada e saída, serviços de aplicação, enums, SQL e metadados. Adapters serão adicionados depois.

## Alterações realizadas

### Estrutura compartilhada

Movidos para `src/main/java/com/gonthera/cli/service/java/common`:

- `GenerateAuthorization`;
- `GenerateDTO`;
- `GenerateEndpoint`;
- `GenerateEnum`;
- `GenerateMessaging`.

### Estrutura MVC

Movidos para `src/main/java/com/gonthera/cli/service/java/mvc`:

- `GenerateController`;
- `GenerateDTOConverter`;
- `GenerateEntity`;
- `GenerateRepositories`;
- `GenerateService`.

Os métodos de entrada desses geradores passaram de `protected` para `public`, pois deixaram de compartilhar o mesmo package com `GenerateJava`.

### Estrutura hexagonal

Criado `src/main/java/com/gonthera/cli/service/java/hexagonal/package-info.java` para reservar e documentar o package da nova implementação.

Também foram adicionados:

- `Architecture`, com os valores `MVC` e `HEXAGONAL`;
- a propriedade `Properties.architecture`, com fallback para `MVC`;
- validação estrutural dos valores de enums;
- validação semântica que restringe `HEXAGONAL` ao Java;
- `GenerateDomain`, responsável por gerar modelos em `<mainPackage>_gen.domain.model`;
- o template `xsd/java/domain.mxsd`.

Os modelos de domínio:

- são derivados das mesmas configurações em `entities`;
- não possuem dependências de Spring, JPA ou Lombok;
- geram campos privados, construtor vazio, construtor completo, getters e setters;
- usam tipos de domínio para relacionamentos, sem sufixo `Entity`;
- continuam usando os enums gerados.

### Portas e serviços de aplicação hexagonais

Para cada entidade que não possui `onlyDTO: true`, agora são gerados:

```text
<mainPackage>_gen/
├── domain/
│   ├── model/<Entity>.java
│   └── ports/
│       ├── in/<Entity>UseCase.java
│       └── out/<Entity>RepositoryPort.java
└── application/
    └── services/<Entity>ApplicationService.java
```

A porta de entrada expõe inicialmente os casos de uso CRUD:

- `save`;
- `update`;
- `delete`;
- `get`;
- `getAll`.

A porta de saída define o contrato de persistência sem depender de Spring Data:

- `save`;
- `deleteById`;
- `findById`;
- `findAll`.

O `ApplicationService`:

- implementa a porta de entrada;
- recebe a porta de saída por construtor;
- não possui anotações ou imports do Spring;
- força a chave recebida no caminho durante `update`;
- oferece os hooks protegidos `validateBeforeSave`, `validateBeforeUpdate` e `validateBeforeDelete`;
- é concreto normalmente e se torna abstrato quando a entidade usa `serviceAbstract: true`;
- ainda não possui paginação, filtros ou transações, pois essas decisões serão integradas junto aos adapters.

### Infraestrutura transversal

No modo hexagonal, a infraestrutura de autorização existente passou a ser gerada em:

```text
<mainPackage>_gen/infrastructure/authorization/
├── exception/
├── permission/
├── security/
├── stereotype/
└── tenant/
```

Isso inclui:

- `Authenticate`;
- `UserSupplier`;
- `Roles`;
- `Anonymous`;
- `SecureResource`;
- `TenantConfiguration`;
- `TenantContext`;
- permissões e exceções relacionadas.

O MVC continua gerando essas classes no package legado `<mainPackage>_gen.authorization`. A implementação foi compartilhada pelo mesmo gerador e pelos mesmos templates; apenas o package e o destino são escolhidos conforme a arquitetura.

Esses componentes ficam em `infrastructure` porque lidam com JWT, Spring Web, contexto de execução, autorização e tenant. Nenhum deles é importado pelo domínio ou pelos serviços de aplicação.

Correção posterior: o método interno compartilhado foi nomeado `generateAuthorizationFiles`. Inicialmente ele tinha a mesma assinatura de `generateAuthorization`, causando erro de método duplicado em `GenerateAuthorization`.

### Modelo de persistência

O modo hexagonal também gera a primeira parte do adapter de persistência:

```text
infrastructure/adapters/out/persistence/
├── entities/<Entity>JpaEntity.java
└── mappers/<Entity>PersistenceMapper.java
```

- `<Entity>JpaEntity` concentra `@Entity`, `@Table`, colunas, chaves e relacionamentos JPA;
- o modelo do domínio permanece livre de JPA;
- `<Entity>PersistenceMapper` converte domínio para persistência e persistência para domínio;
- campos que referenciam outras entidades são convertidos pelos respectivos persistence mappers;
- listas de relacionamentos são convertidas com streams;
- os mappers são componentes Spring, mas pertencem exclusivamente à infraestrutura.

O caminho de persistência foi completado com:

```text
infrastructure/
├── adapters/out/persistence/
│   ├── <Entity>PersistenceAdapter.java
│   ├── entities/<Entity>JpaEntity.java
│   ├── mappers/<Entity>PersistenceMapper.java
│   └── repositories/<Entity>JpaRepository.java
└── configuration/
    └── <Entity>ApplicationConfiguration.java
```

- `<Entity>JpaRepository` estende `JpaRepository`;
- `<Entity>PersistenceAdapter` implementa a porta pura `<Entity>RepositoryPort`;
- escrita e exclusão usam `@Transactional`;
- leituras usam `@Transactional(readOnly = true)`;
- `<Entity>ApplicationConfiguration` cria o bean da porta de entrada e instancia o application service;
- a configuração só é gerada quando `serviceAbstract` é `false`;
- com `serviceAbstract: true`, o consumidor deve fornecer uma subclasse concreta do application service e registrá-la como bean.

Spring Data, JPA, transações e composição de beans permanecem inteiramente em `infrastructure`.

### Referências e documentação

- `GenerateJava` passou a importar os geradores dos novos packages.
- O gerador .NET passou a importar `GenerateEnum` de `java.common`.
- `RabbitExchangeGenerationTest` foi ajustado para o novo package de `GenerateMessaging`.
- O handoff e a skill do projeto documentam a nova organização interna.

## Validação executada

```bash
mvn -q -Dtest=ProjectValidatorTest,ConfigurationFileValidatorTest,JavaOutputLayoutTest,RabbitExchangeGenerationTest test
```

Resultado: testes aprovados.

O modelo e o enum hexagonais produzidos pelo teste também foram compilados diretamente com `javac`, com sucesso.

As portas CRUD, os serviços de aplicação, a autorização hexagonal e toda a infraestrutura de persistência adicionados depois dessa validação ainda não tiveram testes automatizados executados, conforme o acordo de trabalho atual. Eles passaram apenas por revisão estática.

O teste completo não foi executado porque `GenerateSourceTest` trabalha sobre arquivos reais e pode gerar alterações destrutivas ou ruído no workspace.

## Estado importante do workspace

Antes desta frente foram observadas alterações não relacionadas:

- `docs/handoff/entity-generator-project/HANDOFF.md`, atualmente adicionada no índice e ausente da árvore de trabalho;
- `GONTHERA_2_1_0_DOCUSAURUS_HANDOFF.md`, observado no início e ausente na verificação final.

Essas alterações não foram modificadas, adicionadas ao índice nem revertidas por este trabalho.

## Próximo passo recomendado

Validar o núcleo gerado em um projeto consumidor e então avançar para:

1. revisar a saída de persistência em um projeto consumidor, principalmente relacionamentos;
2. criar o adapter de entrada Spring Web;
3. definir DTOs HTTP e seus mappers sem acoplar o domínio ao contrato web;
4. mover ou adaptar RabbitMQ para `infrastructure`;
5. integrar paginação e filtros sem acoplar o núcleo ao Spring.
