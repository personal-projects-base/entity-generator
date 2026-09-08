# Handoff — Gonthera CLI

## Objetivo

`gonthera-cli` é uma ferramenta de geração de backends distribuída como Maven Plugin (`com.gonthera:gonthera-cli`) e executável standalone. Ela lê `project.json` no diretório em que o Maven/JAR foi executado, com fallback temporário para `properties.json`, e gera persistência, APIs, contratos, mensageria, scripts PostgreSQL e metadados de permissões para Java, .NET e Node.

Este documento descreve o comportamento consolidado da versão `2.1.4`, fechada em 7 de setembro de 2026. Em caso de divergência com o `README.md`, considere este handoff mais próximo da implementação atual.

As correções do Node com PostgreSQL foram acumuladas em `2.1.3`. A versão `2.1.4` adiciona MongoDB somente ao Node e mantém Java e .NET sem alterações. O desenho e a matriz de validação estão em [MONGODB_2.1.4.md](MONGODB_2.1.4.md).

## Como consumir

Pré-requisitos do gerador:

- JDK 11 ou superior;
- Maven com acesso ao repositório onde o plugin foi publicado;
- um `project.json` na raiz do serviço consumidor, ou o legado `properties.json` durante a transição;
- para .NET, uma pasta `static/` já criada na raiz do serviço.

Exemplo de declaração no `pom.xml` do serviço Java:

```xml
<repositories>
  <repository>
    <id>myMavenRepo.read</id>
    <url>https://mymavenrepo.com/repo/go9Ye7KC7xaSZHqFec9g/</url>
  </repository>
</repositories>
<pluginRepositories>
  <pluginRepository>
    <id>myMavenRepo.read</id>
    <url>https://mymavenrepo.com/repo/go9Ye7KC7xaSZHqFec9g/</url>
  </pluginRepository>
</pluginRepositories>

<build>
  <plugins>
    <plugin>
      <groupId>com.gonthera</groupId>
      <artifactId>gonthera-cli</artifactId>
      <version>2.1.4</version>
    </plugin>
  </plugins>
</build>
```

Execute na mesma pasta do `project.json`:

```bash
mvn gonthera-cli:generate-sources
```

O plugin não está associado automaticamente a uma fase do lifecycle. Se a geração precisar ocorrer em todo build, declare uma `execution` com o goal `generate-sources` e uma fase apropriada no serviço consumidor.

Também existe execução como JAR (`Main`), com o mesmo diretório de trabalho e o mesmo arquivo de configuração.

A configuração pode ser validada sem executar os geradores:

```bash
mvn gonthera-cli:validate
gonthera-cli.exe --validate
java -jar gonthera-cli-2.1.4.jar --validate
```

O modo de validação exige a pasta `.gonthera`, embora a geração continue aceitando temporariamente os arquivos da raiz. O validador verifica sintaxe e estrutura JSON, rejeita propriedades desconhecidas em qualquer nível e, após unificar os arquivos, verifica cabeçalho, coleções, entidades e chaves, campos, endpoints, enums, canais RabbitMQ e flags de autorização. A validação não apaga nem gera arquivos.

### Prioridade e configuração modular

A resolução da configuração segue esta prioridade:

1. `.gonthera/project.json` e arquivos separados, quando a pasta `.gonthera` existir;
2. `project.json` na raiz;
3. `properties.json` na raiz como compatibilidade legada.

Na configuração modular, `.gonthera/project.json` contém o cabeçalho (`mainPackage`, `projectName`, `language`) e as coleções são lidas separadamente:

```text
.gonthera/
├── project.json
├── entities.json
├── endpoints.json
├── enums.json
├── messaging.json
└── authorization.json
```

- `entities.json`, `endpoints.json` e `enums.json` contêm arrays JSON diretamente;
- `messaging.json` contém o objeto equivalente ao valor da propriedade `messaging`;
- `authorization.json` contém o objeto equivalente ao valor da propriedade `authorization`;
- todas as seções ainda podem permanecer dentro de `.gonthera/project.json`;
- cada arquivo separado sobrescreve somente sua seção correspondente;
- quando um arquivo separado não existe, o valor declarado no `project.json` é mantido; se também não estiver declarado, coleções ficam vazias e mensageria/autorização permanecem nulas;
- se `.gonthera` existir, seu `project.json` é obrigatório e a leitura não volta para os arquivos da raiz.

O loader unifica os arquivos no mesmo modelo interno usado pela configuração em arquivo único. Os geradores não distinguem a origem da configuração.

## Configuração mínima segura

No arquivo único da raiz, as três coleções abaixo devem estar presentes, mesmo vazias. Dentro de `.gonthera`, elas podem permanecer no `project.json` ou ser movidas para arquivos separados; se ausentes em ambos, são inicializadas como listas vazias.

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

Valores aceitos em `language`: `JAVA`, `DOTNET` e `NODE` (maiúsculos).

Notas sobre propriedades documentadas anteriormente:

- `defaultTypeId` não existe no modelo atual e é ignorado pelo Gson;
- `events` e `listeners` não existem no modelo atual e não geram código;
- `projectName` é desserializado, mas praticamente não participa da geração; os namespaces/pacotes usam `mainPackage`;
- propriedades desconhecidas são silenciosamente ignoradas.

## Entidades

Exemplo completo:

```json
{
  "comment": "Cadastro de clientes",
  "entityName": "customer",
  "tableName": "customer",
  "classExtends": "",
  "generateDefaultControllers": true,
  "controllerAbstract": false,
  "serviceAbstract": false,
  "onlyDTO": false,
  "entityFields": [
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
  ]
}
```

Contrato efetivo:

- `entityName`: nome base das classes e, quando `tableName` é `null`, da tabela em snake_case;
- `tableName`: nome da tabela. Use `null`/omita para derivar o nome; string vazia não ativa a derivação;
- `comment`: usado na descrição do recurso gerado;
- `entityFields`: precisa conter ao menos um campo e, para geração normal, uma chave com `metadata.key: true`;
- `generateDefaultControllers`: padrão `true`; controla o CRUD padrão. O alias legado `generateDefaultHandlers` continua aceito com aviso;
- `controllerAbstract`: gera o controller Java, .NET ou Node como classe abstrata. No Node, a aplicação registra a implementação concreta em `GeneratedControllerFactories`, sem repetir as rotas; o alias legado `handlerAbstract` continua aceito com aviso;
- se o nome novo e o legado forem declarados juntos, o nome novo tem precedência e o validador avisa sobre o conflito;
- `serviceAbstract`: afeta apenas o service Java. `false` gera classe concreta com `@Service`; `true` gera classe abstrata sem `@Service` e exige que o consumidor registre um subtipo concreto como bean Spring;
- `onlyDTO`: implementado somente no fluxo Java; evita Entity, converter, service, repository e controller, mas ainda gera DTO. A entidade continua entrando no SQL gerado;
- `classExtends`: está no modelo, mas não é aplicado pelos geradores atuais.

Contrato dos campos:

- `comment`, `fieldName`, `fieldProperties` e `metadata` devem ser informados;
- `list` envolve o tipo em `List<T>`;
- `fieldProperties.required` e `valueDefault` existem no modelo, mas não controlam de forma consistente o código gerado;
- `metadata.nullable` controla anotações/nullable em partes dos geradores;
- `metadata.key` identifica a PK;
- para tipos que representam outra entidade, `relationShips` deve existir para evitar falhas durante a geração.

Relacionamentos:

```json
"relationShips": {
  "fetchType": "LAZY",
  "relationShip": "ManyToOne",
  "mappedBy": "",
  "bidirectional": false,
  "reference": true
}
```

- `relationShip`: esperado como `OneToOne`, `OneToMany`, `ManyToOne` ou `ManyToMany`;
- `fetchType`: inserido nas anotações Java, normalmente `LAZY` ou `EAGER`;
- `bidirectional: false`: lado dono. Gera `@JoinColumn`, coluna SQL e FK;
- `bidirectional: true`: lado inverso. Gera anotação com `mappedBy`, `cascade = CascadeType.ALL`, `orphanRemoval = true` e não gera coluna SQL;
- `mappedBy`: nome do campo proprietário, especialmente em autorrelacionamentos. Exemplo: `children` usa `mappedBy: "parentCode"`;
- `reference`: usado pelo conversor Java para impedir recursão. Normalmente fica `true` no lado dono/FK;
- em autorrelacionamentos, declare primeiro o campo proprietário/referência e depois o inverso.

Exemplo autorreferenciado `ManyToOne`/`OneToMany`:

```json
{
  "fieldName": "parentCode",
  "list": false,
  "fieldProperties": { "fieldType": "costCenter" },
  "relationShips": {
    "fetchType": "LAZY",
    "relationShip": "ManyToOne",
    "bidirectional": false,
    "reference": true
  }
}
```

```json
{
  "fieldName": "children",
  "list": true,
  "fieldProperties": { "fieldType": "costCenter" },
  "relationShips": {
    "fetchType": "LAZY",
    "relationShip": "OneToMany",
    "mappedBy": "parentCode",
    "bidirectional": true,
    "reference": false
  }
}
```

Saída Java observada:

```java
@JoinColumn(name = "parent_code")
@ManyToOne(fetch = FetchType.LAZY)
private CostCenterEntity parentCode;

@OneToMany(mappedBy = "parentCode", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
private List<CostCenterEntity> children;
```

Saída SQL observada:

```sql
parent_code uuid
ALTER TABLE cost_center ADD CONSTRAINT fk_cost_center_cost_center_parent_code FOREIGN KEY (parent_code) REFERENCES cost_center(id);
```

O converter Java reamarra o lado inverso ao lado dono:

```java
entity.setChildren(childrenDtoConverter.toEntity(dto.children, null));
if (entity.getChildren() != null) entity.getChildren().forEach(e -> e.setParentCode(entity));
```

## Tipos reconhecidos

| Configuração | Java | .NET | PostgreSQL |
|---|---|---|---|
| `uuid` | `UUID` | `Guid?` | `uuid` |
| `string`, `password` | `String` | `string` | `varchar` |
| `datetime` | `LocalDateTime` | `DateTime` | `timestamp` |
| `date` | `LocalDate` | `DateTime` | `date` |
| `int` | `Integer` | `int?` | `integer`/`serial` para PK |
| `integer` | `int` | `int` | `integer`/`serial` para PK |
| `long` | `Long` | `Long` | `integer` |
| `decimal`, `double` | `Double` | `Double` | `numeric` |
| `boolean` | `boolean` | `bool` | `boolean` |
| `byte`, `byte[]`, `inputStream`, `map` | suportados apenas no mapeamento Java | não suportados corretamente | sem mapeamento completo |

Um tipo que não seja primitivo nem enum é tratado como entidade: `FooEntity`/`FooDTO`. Portanto, o nome deve corresponder a um `entityName` configurado. Enums são comparados sem diferenciar maiúsculas/minúsculas.

## Endpoints

```json
{
  "comment": "Consulta clientes",
  "methodName": "listCustomer",
  "httpMethod": "GET",
  "grouper": "customer",
  "metadata": {
    "anonymous": false,
    "input": [],
    "output": [
      {
        "parameterName": "customers",
        "parameterType": "customer",
        "list": true
      }
    ]
  },
  "permissions": {
    "description": "",
    "resource": "",
    "premissions": ["VIEW"],
    "permissionDefault": false
  }
}
```

- `metadata.input` e `metadata.output` devem existir, mesmo vazios;
- use `GET` ou `POST`; outras strings podem produzir código inválido;
- Java gera uma interface por endpoint e ignora `grouper`;
- .NET usa `grouper` para reunir métodos em uma classe `*Primitive`; use `""` quando não houver grupo;
- `metadata.anonymous: true` gera a anotação própria `<mainPackage>_gen.authorization.stereotype.Anonymous` no Java ou `[AllowAnonymous]` no .NET;
- um parâmetro de entrada com tipo `requestdata`/`responsedata` ativa os envelopes genéricos e impede a geração da classe Input/Output específica;
- a grafia `premissions` está errada no código, mas é a chave JSON que deve ser usada;
- permissões aceitas: `ALL`, `VIEW`, `CREATE`, `UPDATE`, `DELETE`.

## Enums

```json
{
  "enumName": "Status",
  "values": ["ACTIVE", "INACTIVE"]
}
```

`values` não pode estar vazio, pois o gerador remove a última vírgula assumindo que existe ao menos um item.

## Filtros do CRUD gerado

O `GET /<entityName>` do CRUD Java recebe o filtro no parâmetro de query string `filter` e o entrega a `SpecificationFilter`. A classe converte a expressão em um `Specification<T>` do Spring Data JPA. Isso se parece com uma cláusula de consulta, mas **não aceita JPQL, SQL nem o conjunto completo de operadores do JPA**.

Exemplo de chamada (a biblioteca HTTP deve fazer o URL encoding):

```text
GET /costCenter?size=20&offset=1&filter=description eq matriz and parentCode.id eq 550e8400-e29b-41d4-a716-446655440000
```

Com `URLSearchParams` no frontend:

```javascript
const params = new URLSearchParams({
  size: "20",
  offset: "1",
  filter: "description eq matriz and parentCode.id eq 550e8400-e29b-41d4-a716-446655440000",
  displayFields: "*"
});

fetch(`/costCenter?${params.toString()}`);
```

Sintaxe Java efetivamente implementada:

| Expressão | Efeito observado |
|---|---|
| `field eq value` | Para `String`, `lower(field) like '%value%'`, sem diferenciar maiúsculas/minúsculas. Para `UUID`, igualdade exata. Para enum, tenta igualdade. |
| `relation.field eq value` | Cria joins JPA pelo caminho pontuado; UUID é exato e os demais tipos são tratados como texto parcial. |
| `field isNull` | `field IS NULL`. Também aceita caminho relacionado. |
| `field notNull` | `field IS NOT NULL`. Também aceita caminho relacionado. |
| `field gte value` / `field ge value` | Para `date` e `datetime`, maior ou igual ao valor ISO informado. Também aceita caminho relacionado. |
| `field lte value` / `field le value` | Para `date` e `datetime`, menor ou igual ao valor ISO informado. Também aceita caminho relacionado. |
| `expr and expr` | Combina predicados com `AND`. A palavra precisa estar separada por espaços. |
| `expr or expr` | Combina predicados com `OR`. A palavra precisa estar separada por espaços. |

Exemplos seguros:

```text
description eq matriz
id eq 550e8400-e29b-41d4-a716-446655440000
parentCode.id eq 550e8400-e29b-41d4-a716-446655440000
parentCode isNull
parentCode notNull
description eq matriz and parentCode notNull
description eq matriz or description eq filial
createdAt gte 2026-09-01T00:00:00 and createdAt lte 2026-09-30T23:59:59
birthDate ge 2026-01-01 and birthDate le 2026-12-31
```

Regras e limitações importantes do parser Java:

- use os nomes dos atributos Java/JSON em `lowerCamelCase`, não os nomes das colunas SQL;
- não envolva valores em aspas; tudo depois de `eq` até o próximo operador lógico é o valor textual;
- `eq`, `isNull`, `notNull`, `gte`, `lte`, `ge` e `le` são sensíveis a maiúsculas/minúsculas e devem ser enviados exatamente assim; `and`/`or` são reconhecidos sem diferenciar caixa;
- `gte`/`ge` e `lte`/`le` são exclusivos para campos Java `LocalDate` e `LocalDateTime`; use ISO `yyyy-MM-dd` para `date` e o formato ISO local, como `yyyy-MM-dd'T'HH:mm:ss`, para `datetime`;
- `eq` em `String` significa **contém**, não igualdade exata;
- UUID deve ser válido; filtro inválido resulta em HTTP 400 com `Invalid filter: <expressão>`;
- joins são criados com o tipo padrão do JPA, normalmente `INNER JOIN`, portanto relações ausentes podem excluir o registro;
- números e booleanos não têm conversão implementada de forma segura no Java. Datas devem usar `gte`/`ge` ou `lte`/`le`; o caminho genérico de `eq` aplica `lower`/`like` e pode falhar em tempo de execução para esses tipos;
- não existem atualmente `ne`, `gt`, `lt`, `in`, `between`, `like` explícito ou `not`;
- parênteses e precedência mista não são analisados de forma confiável. Não misture `and` e `or` na mesma expressão e não gere grupos aninhados;
- valores contendo as palavras ` and ` ou ` or ` não podem ser escapados e serão divididos pelo parser;
- filtro ausente ou vazio não restringe os resultados;
- `size` e `offset` devem ser enviados no CRUD Java. `offset` é baseado em 1 na requisição; internamente é convertido para a página baseada em 0;
- `order` aplica ordenação ao `PageRequest` Java no formato `campo,asc` ou `campo,desc`; quando a direção é omitida, usa `asc`. Caminhos relacionados, como `customer.name,desc`, são aceitos; formato ou direção inválidos resultam em HTTP 400 com `Invalid order: <valor>`;
- `displayFields` controla a projeção do DTO e não participa do filtro.

No .NET, `DynamicFilter` é uma implementação separada: suporta apenas `eq`, `and` ou `or`; texto também usa `Contains` sem diferenciar caixa, UUID é exato, e coleção usa um caminho com `*` (por exemplo, `children*.description eq matriz`). Não há `isNull`/`notNull`, o parser só escolhe um operador lógico por expressão e os nomes das propriedades C# são sensíveis à forma gerada. Portanto, o frontend deve selecionar o dialeto conforme `language`; uma expressão Java não é portável por garantia para .NET.

O CRUD Node 2.1.3 aplica filtros, ordenação e projeção; consulte o contrato CRUD Node abaixo.

## Messaging RabbitMQ

Implementado nos fluxos Java, .NET e Node. O contrato atual agrupa RabbitMQ em `messaging.RabbitMq`, com `pub` para publishers e `sub` para subscribers.

```json
{
  "messaging": {
    "RabbitMq": {
      "pub": [
        {
          "name": "notification",
          "queue": "4libert.queue.profile.notification",
          "routingKey": "4libert.routingKey.profile.notification"
        }
      ],
      "sub": [
        {
          "name": "notificationChat",
          "queue": "4libert.queue.chat.notification"
        }
      ]
    }
  }
}
```

Contrato efetivo:

- `name`: nome base da classe gerada. `notification` gera `NotificationPub`; `notificationChat` gera `NotificationChatSub`;
- `className`: opcional. Sobrescreve o nome base quando informado; o sufixo `Pub`/`Sub` é adicionado se faltar;
- `queue`: nome da fila;
- `routingKey`: obrigatório para `pub`; opcional para `sub` no .NET e Node, onde declara também o binding da fila com a exchange;
- a exchange não fica fixa no JSON. O gerador cria uma anotação/atributo de exchange e uma configuração abstrata `RabbitConfig`.
- nenhuma configuração RabbitMQ é gerada se `messaging.RabbitMq` estiver ausente, nulo ou sem canais.

O serviço consumidor deve criar uma configuração concreta fora do diretório `_gen`, por exemplo:

```java
package com.example.service.messaging;

import com.example.service_gen.messaging.RabbitConfig;
import com.example.service_gen.messaging.RabbitExchange;
import org.springframework.context.annotation.Configuration;

@Configuration
@RabbitExchange("4libert.profile")
public class AppRabbitConfig extends RabbitConfig {
}
```

Em .NET, o equivalente usa `RabbitExchangeAttribute`:

```csharp
using ExampleBackend.ExampleBackend_Gen.Messaging;
using Microsoft.Extensions.Configuration;

namespace ExampleBackend.Messaging
{
    [RabbitExchange("4libert.profile")]
    public class AppRabbitConfig : RabbitConfig
    {
        public AppRabbitConfig(IConfiguration configuration) : base(configuration)
        {
        }
    }
}
```

No `Program.cs`, registre a configuração concreta e os publishers gerados:

```csharp
builder.Services.AddSingleton<RabbitConfig, AppRabbitConfig>();
AddRabbitMessaging.AddRabbitMessagingGenerate(builder);
```

Subscribers são gerados como classes abstratas. No Java, o serviço consumidor deve implementar o listener fora de `_gen` e registrá-lo como bean Spring:

```java
package com.example.service.messaging;

import com.example.service_gen.messaging.sub.NotificationChatSub;
import org.springframework.stereotype.Component;

@Component
public class NotificationChatListener extends NotificationChatSub {
    @Override
    protected void onMessage(String message) {
        // tratar mensagem
    }
}
```

No .NET, implemente a classe abstrata e registre o listener concreto como hosted service:

```csharp
builder.Services.AddHostedService<NotificationChatListener>();
```

## Saídas geradas

### Java

O diretório inteiro abaixo é apagado e recriado a cada execução:

```text
src/main/java/<mainPackage convertido em caminho>_gen/
```

São gerados, conforme a configuração:

- `entities/*Entity`;
- `dtos/*DTO`;
- `converters/*DTOConverter`;
- `repositories/*Repository`;
- `controllers/*Controller`;
- `services/*Service` com transações, conversão, filtros, paginação e persistência;
- `endpoints/` com interfaces de endpoint e seus modelos `*Input`/`*Output`;
- `enums/` com os enums configurados;
- `common/` com `CrudController`, `RestConfig`, `SpecificationFilter`, `RequestData` e `ResponseData`;
- `authorization/` com exceções, contratos de permissões, autenticação JWT, anotações e contexto de tenant;
- abstrações RabbitMQ em `messaging/`, `messaging/pub/` e `messaging/sub/` quando `messaging.RabbitMq` é configurado;

Cada subdiretório corresponde a um subpackage Java, por exemplo `com.example.service_gen.entities`. A mudança é incompatível com imports antigos que apontavam diretamente para `com.example.service_gen`.

Controllers Java delegam o CRUD ao service correspondente. Quando `serviceAbstract: true`, a validação emite um aviso porque a CLI não consegue confirmar se o projeto consumidor fornece a implementação concreta necessária.

Também são sobrescritos:

```text
src/main/resources/properties.json
src/main/resources/resources.json
src/main/resources/postgree.sql
```

O nome `postgree.sql` é o nome efetivamente usado (incluindo a grafia).

### .NET

O diretório inteiro abaixo é apagado e recriado:

```text
<mainPackage com pontos convertidos em barras>_gen/
```

A saída principal é organizada desta forma:

```text
<mainPackage>_gen/
├── Common/
├── Controllers/
├── Converters/
├── Data/
├── Dtos/
├── Endpoints/
├── Entities/
├── Enums/
├── Messaging/
└── Repositories/
```

Os arquivos permanecem atualmente no namespace C# raiz `<mainPackage>.<mainPackage>_Gen`; a separação desta versão é física, por diretórios. O diretório inteiro é removido recursivamente antes da regeneração.

O par legado `*Handler`/`*HandlerImpl` deixou de ser gerado. Com `generateDefaultControllers: true`, cada entidade gera um único `Controllers/*Controller.cs`, contendo a implementação CRUD e métodos `virtual`. `controllerAbstract: false` gera classe concreta; `controllerAbstract: true` gera classe abstrata para implementação e sobrescrita fora de `_gen`. Com `generateDefaultControllers: false`, nenhum controller CRUD é criado.

Nesta etapa não foi introduzida uma camada Service no .NET: o Controller ainda injeta `I*Repository` e executa conversão e persistência diretamente. `serviceAbstract` permanece exclusivo do Java até que o registro explícito de DI do .NET seja redesenhado.

Também são gerados primitives de endpoint, `CustomDbContext`, registro de DI e classes auxiliares. A pasta `static/` não é criada automaticamente; nela são sobrescritos `properties.json`, `resources.json` e `postgree.sql`.

Quando `messaging.RabbitMq` é configurado, também são gerados:

- `Messaging/RabbitExchangeAttribute.cs`;
- `Messaging/RabbitConfig.cs`;
- `Messaging/AddRabbitMessaging.cs`;
- `Messaging/Pub/RabbitPublisher.cs` e `Messaging/Pub/*Pub.cs`;
- `Messaging/Sub/*Sub.cs`.

### Node

O alvo Node da 2.1.4 gera APIs Express com Prisma para PostgreSQL ou MongoDB. O
provider é escolhido por configuração sem alterar rotas, DTOs, respostas, filtros,
ordenação, projeção, OpenAPI ou RabbitMQ. Esta rodada não alterou os geradores Java
e .NET.

#### Saída gerada e fronteira de customização

O diretório `src/generated/` é apagado e recriado por completo. Ele contém:

```text
src/generated/
├── common/                         # contratos, erros, metadados e query parser
├── configuration/database/         # DatabaseConfig abstrata
├── controllers/                    # fluxo CRUD
├── converters/                     # DTO, Prisma input e resposta
├── documentation/                  # documento OpenAPI 3.0.3
├── endpoints/                      # contratos customizados
├── enums/
├── messaging/rabbitmq/             # somente quando RabbitMq possui canais
├── models/                         # DTOs TypeScript
├── repositories/                   # consultas e transações Prisma
└── routes/                          # routers Express
prisma/schema.prisma
```

Também são gerados os arquivos estáticos `properties.json` e `resources.json`.
`postgree.sql` é gerado somente para PostgreSQL. Os fontes vêm de
`src/main/resources/xsd/node/`.

O consumidor deve manter fora de `src/generated`: `package.json`, `tsconfig.json`,
`.env`, migrations Prisma, implementação concreta de banco e RabbitMQ, `app.ts`,
`server.ts`, middlewares, montagem do Swagger UI, extensões OpenAPI e autenticação.
O Gonthera não sobrescreve esses arquivos. O `node-test-service` é a referência atual.

O item 8.2 gera `documentation/openapi.ts` a partir das entidades, enums e endpoints.
O documento inclui paths CRUD, schemas de resposta/POST/PUT/referência/paginação,
relações, tipos reais das chaves, filtros, ordenação, projeção, erros e contratos de
endpoints customizados. A aplicação pode importar `generatedOpenApiDocument` e
mesclar título, health check, segurança, servidores e paths manuais fora da geração.

`generateDefaultControllers: false` impede a geração do controller e da rota CRUD
da entidade, permitindo que o consumidor forneça ambos manualmente. Com
`controllerAbstract: true`, o Node gera uma base abstrata com todo o CRUD implementado,
repository protegido e métodos normais sobrescrevíveis. A aplicação cria uma classe
concreta fora de `src/generated` e a registra em `GeneratedControllerFactories`.
A factory da entidade abstrata é obrigatória no TypeScript e também validada em
runtime; factories de controllers concretos são opcionais e permitem substituí-los.
As rotas continuam geradas, portanto a aplicação não repete os bindings HTTP.

```ts
export class AppCustomerController extends CustomerController {
  override async save(request: Request, response: Response): Promise<void> {
    // regra específica
    await super.save(request, response);
  }
}

export const controllerFactories = {
  customer: repository => new AppCustomerController(repository)
} satisfies GeneratedControllerFactories;

app.use(createGeneratedRoutes(prisma, controllerFactories));
```

Os handlers gerados usam wrappers como `(request, response) =>
controller.save(request, response)`. Isso preserva `this`, respeita overrides e
permite que a subclasse chame `super`, ao contrário das propriedades arrow usadas
anteriormente. A validação emite warning lembrando que a factory concreta é exigida.

#### Banco e ciclo de vida do Prisma

`configuration/database/database.config.ts` declara `DatabaseConfig` abstrata. Ela
carrega `.env` por `dotenv/config`, cria um único `PrismaClient` de forma preguiçosa
e oferece `client`, `connect()` e `disconnect()`. Os hooks protegidos `resolveUrl`,
`createOptions` e `createClient` permitem customização na subclasse. URL ausente gera
erro sem expor credenciais. O processo deve reutilizar uma instância concreta,
conectar antes de abrir a porta HTTP e desconectar em `SIGINT`/`SIGTERM`.

O provider é configurado no cabeçalho de `.gonthera/project.json`:

```json
"database": {
  "provider": "MONGODB"
}
```

Os valores aceitos são `POSTGRESQL` e `MONGODB`. O objeto `database` ausente mantém
PostgreSQL como padrão; quando declarado, `provider` é obrigatório. `database` é
aceito somente com `language: "NODE"`.

O `prisma/schema.prisma` inclui o datasource escolhido, Prisma Client, enums,
models e relações. A CLI Prisma lê `DATABASE_URL` diretamente do ambiente;
overrides de `resolveUrl` valem para a aplicação em execução. PostgreSQL usa
`prisma migrate`; MongoDB usa `prisma db push` e precisa operar como replica set
para suportar as transações das escritas relacionadas.

Exemplo MongoDB autenticado:

```dotenv
DATABASE_URL="mongodb://user:password@localhost:27017/service?authSource=admin&replicaSet=rs0"
```

`authSource=admin` seleciona o banco de autenticação, enquanto `replicaSet=rs0`
identifica o conjunto de réplicas. Um parâmetro não substitui o outro; sem replica
set, as escritas transacionais dos repositories falham com o erro Prisma `P2031`.

As regras ficam separadas em `PostgreSqlDatabaseDialect` e
`MongoDbDatabaseDialect`, com templates de datasource próprios. PostgreSQL mantém
tipos nativos, relações ManyToMany implícitas, `RepeatableRead` na listagem e
`postgree.sql`. MongoDB usa UUID textual mapeado para `_id`, relações ManyToMany
com arrays internos de IDs e transações sem isolation level. A geração Mongo remove
um `postgree.sql` antigo para que a troca de provider não deixe artefatos relacionais.

#### Relacionamentos PostgreSQL

O gerador usa a convenção madura do Java: `bidirectional: false` identifica o lado
proprietário e `bidirectional: true` identifica o inverso, que aponta ao proprietário
com `mappedBy`. O fallback pelo nome da entidade continua disponível quando não há
`mappedBy`, mas o nome explícito é preferível em modelos com mais de uma relação.

- `OneToOne`: o proprietário recebe FK única com o tipo da chave referenciada; o
  inverso Prisma é opcional, como o ORM exige.
- `ManyToOne`/`OneToMany`: somente o ManyToOne guarda FK, sem `@unique`; o outro lado
  é uma coleção inversa.
- `ManyToMany`: os dois lados são listas e o Prisma cria uma tabela implícita por
  relação. Os nomes permanecem distintos em múltiplos vínculos e autorrelações.
- Autorrelacionamentos são suportados nos três formatos. Autoimports são omitidos e
  imports repetidos são consolidados.

A validação Node rejeita pares ausentes ou ambíguos, cardinalidade incorreta,
`mappedBy` inválido e colisão de nome da FK antes de limpar a saída. Execute também
`prisma validate` após gerar. Renomear campos usados no nome de uma relação pode
exigir uma migration de dados.

#### Relacionamentos MongoDB

MongoDB conserva a mesma interpretação de proprietário, inverso e `mappedBy`.
`OneToOne` mantém o ID único no proprietário; `ManyToOne` armazena o ID relacionado
e recebe índice explícito; `OneToMany` permanece inverso. Em `ManyToMany`, ambos os
lados recebem arrays escalares internos, como `tagsIds`, usados pelo Prisma em
`fields` e `references`. Esses campos não aparecem nos DTOs, metadados ou OpenAPI.
Relações proprietárias declaram `onDelete: NoAction` e `onUpdate: NoAction`, como o
Prisma Mongo exige para ciclos e autorrelações.

A chave de todas as entidades Mongo deve ser `uuid`. Ela é gerada como
`String @id @default(uuid()) @map("_id")`, preservando o identificador público do
PostgreSQL e evitando `ObjectId` no endpoint. Chaves inteiras são recusadas porque
MongoDB não oferece `autoincrement()`.

Nos filtros Mongo, `isNull` abrange valor nulo ou campo ausente. `notNull` exige
campo presente e diferente de nulo; o gerador usa `isSet` internamente mantendo a
mesma expressão recebida pelo endpoint.

#### Contrato de entrada, persistência e saída

As relações chegam como objetos DTO com a chave real da entidade:

```json
{
  "bio": "Perfil de teste",
  "customer": { "id": "4a1a00c8-78c1-42e5-8e74-4ae6efe1a9ae" }
}
```

Não envie FKs sintéticas como `customerId`. Com `reference: true`, o converter apenas
conecta a chave existente e ignora os demais campos do objeto relacionado. Sem
`reference`, ele pode criar ou atualizar o objeto aninhado na mesma transação. PUT
preserva campos omitidos. Ao receber uma coleção inversa OneToMany, remove filhos
omitidos; no ManyToMany, substitui vínculos sem excluir entidades compartilhadas.
Não existe cascade recursivo geral equivalente ao JPA.

GET individual, POST e PUT expandem os objetos relacionados. Para impedir repetição
e ciclos, o campo recíproco imediato fica `null`: `customer.profile.customer` não
repete o customer. A profundidade máxima é seis relações. Campos não selecionados
também ficam `null` e FKs internas não aparecem no JSON. Datas são ISO, bytes são
Base64 e `long` fora da faixa segura do JavaScript é string.

#### Listagem, filtros e chaves

A listagem retorna:

```json
{"size":20,"offset":0,"total":1,"contents":[]}
```

Na entrada, `offset=1` representa a primeira página; na saída ela é `0`. `size` usa
20 por padrão e `total` conta os registros depois do filtro. São aceitos:

- `filter`: `eq`, `isNull`, `notNull`, datas com `gte`/`ge` e `lte`/`le`, caminhos
  relacionados e coleções com caminho pontuado ou `*`;
- `order=campo,asc|desc`;
- `displayFields=id;customer.name`, também no GET individual.

Uma expressão usa `and` ou `or`, sem misturar os dois; não há parênteses ou escape.
`eq` textual contém sem diferenciar caixa, UUID compara exatamente, enum aceita nome
ou ordinal e números/booleanos são convertidos. Filtros, projeções, ordenações ou IDs
inválidos retornam 400; GET inexistente retorna 404.

Cada entidade Node precisa de exatamente uma chave primária escalar. A URL continua
`/:id`, enquanto repository, converter e relações resolvem o nome e tipo configurado.
Por exemplo, uma relação com chave numérica `code` recebe `{"category":{"code":12}}`.
UUID e inteiro recebem default gerado; as demais chaves devem ser informadas no POST.

#### RabbitMQ

Quando `messaging.RabbitMq` possui `pub` ou `sub`, o Node gera `RabbitConfig`, uma
base publisher e classes de canal sobre `amqp-connection-manager` 5.0.0 e
`amqplib` 2.0.1. Uma única configuração concreta deve ser compartilhada pelo
processo. O manager recupera conexão, canais, declarations, bindings e consumidores.

Publishers usam confirmação e acumulam mensagens durante reconexão. Subscribers
aplicam `prefetch`, executam `ack` depois do handler e `nack` sem requeue por padrão;
`requeueOnError` permite mudar a política. Todos os recursos expõem fechamento
explícito. A base de exemplo ativa a mensageria somente com
`RABBITMQ_ENABLED=true`, o que permite executar a API sem broker no desenvolvimento.

#### Referência executável

`node-test-service` fornece Express 5, Swagger em `/docs`, OpenAPI gerado em
`/openapi.json`, health check, middleware `{error:{code,message}}`, configuração
concreta do Prisma, bootstrap opcional RabbitMQ e encerramento ordenado. Seus scripts
executam desenvolvimento, build, typecheck, Prisma e o JAR local do Gonthera. A API
e o gerador 2.1.4 foram compilados, o schema Mongo passou por `prisma validate`,
`prisma generate` e `prisma db push`, e os 14 testes locais foram aprovados. Em banco
real, foram confirmados criação e leitura de `Customer` e o ciclo ManyToMany completo
de `Customer.tags` / `Tag.customers`: associação, leitura nos dois lados, remoção do
vínculo e preservação da `Tag` compartilhada. Os registros temporários foram removidos.
Essa validação fecha o suporte MongoDB da 2.1.4; a matriz ampliada de regressão entre
providers permanece documentada em `MONGODB_2.1.4.md`.

## Portal estático de documentação

`docs/index.html` apresenta um seletor global de linguagem na introdução. A escolha
alimenta `targetDocs` em `docs/app.js`, portanto toda a página passa a mostrar uma
única trilha: instalação, configuração, saída gerada, relações, CRUD, endpoints,
filtros, RabbitMQ, segurança, bootstrap e limitações somente de Java, Node.js ou
.NET. Contratos JSON realmente compartilhados permanecem em `snippets`; exemplos
de código e observações específicas pertencem ao objeto da linguagem. Não volte a
misturar cards comparativos dos três alvos no corpo da documentação.

## Dependências exigidas pelo código gerado

Java pressupõe, no mínimo:

- Spring Web, Spring Data JPA e Spring Transactions;
- quando `messaging.RabbitMq` for usado: Spring AMQP/RabbitMQ;
- Jakarta Persistence;
- Lombok;
- JJWT `0.11.5` (`jjwt-api`, `jjwt-impl` e `jjwt-jackson`) para as classes de autenticação geradas;
- a variável de ambiente `SECRET_JWT`, com uma chave compatível com HMAC, quando `Authenticate` for usado.

O Java gerado não depende mais do artefato `authorization-backend`. Os componentes equivalentes são escritos em `<mainPackage>_gen.authorization`, com os subpackages `exception`, `permission`, `security`, `stereotype` e `tenant`. `ValidatePermission` não é gerado porque a implementação da biblioteca anterior estava incompleta.

Ao migrar um serviço existente, substitua imports manuais de `com.potatotech.authorization` pelos tipos equivalentes em `<mainPackage>_gen.authorization`. Isso inclui interceptors que chamam `TenantConfiguration.validAnonymous`: eles precisam usar a classe gerada para reconhecer a nova anotação `@Anonymous`. Como todo conteúdo `_gen`, essas classes não devem ser editadas manualmente.

### Customização da autorização Java

`authorization` é opcional e aceita:

```json
{
  "authenticateAbstract": false,
  "tenantConfigurationAbstract": false
}
```

Na configuração modular, o mesmo objeto pode ficar em `.gonthera/authorization.json`; quando o arquivo existe, sobrescreve a seção completa de `.gonthera/project.json`.

- `authenticateAbstract: false`: gera `Authenticate` concreto com `@Service`;
- `authenticateAbstract: true`: gera `Authenticate` abstrato, sem `@Service`, e exige um `@Service` concreto no consumidor;
- `tenantConfigurationAbstract: false`: gera `TenantConfiguration` concreto com `@Component`;
- `tenantConfigurationAbstract: true`: gera `TenantConfiguration` abstrato, sem `@Component`, e exige um `@Component` concreto no consumidor.

As classes abstratas mantêm as implementações padrão. `Authenticate` expõe como `protected` os hooks `resolveSecret`, `extractToken`, `parseClaims`, `createUser`, `validateUser` e `createToken`; seus métodos públicos também podem ser sobrescritos. A validação emite warnings para os modos abstratos porque não consegue confirmar a existência dos beans concretos no serviço consumidor.

.NET pressupõe, no mínimo:

- ASP.NET Core MVC/Authorization;
- Entity Framework Core;
- quando `messaging.RabbitMq` for usado: RabbitMQ.Client, Microsoft.Extensions.Hosting, Microsoft.Extensions.Configuration e Microsoft.Extensions.Logging;
- infraestrutura específica referenciada pelos templates de contexto (`<Projeto>.Config.Database` e `<Projeto>.Config.DatabaseMigration`).

Node pressupõe, no mínimo:

- Node.js 20.19 ou superior e TypeScript;
- Express 5;
- Prisma CLI e Prisma Client 5.22 (`prisma` e `@prisma/client`);
- `dotenv` 16 para a configuração de ambiente;
- quando `messaging.RabbitMq` for usado: `amqplib` 2 e `amqp-connection-manager` 5.

Os namespaces fixos do template `CustomDbContext` atualmente usam `DataOnBackend.Config.*`, independentemente de `mainPackage`; serviços com outro nome provavelmente precisarão ajustar o arquivo gerado ou o template.

## SQL e permissões

O SQL é direcionado a PostgreSQL. Ele gera tabelas, PKs, FKs e tabelas de junção Many-to-Many. Para Node com `database.provider: "MONGODB"`, ele não é gerado e um `postgree.sql` anterior é removido. Não há migrations incrementais: no PostgreSQL o arquivo é recriado por completo, enquanto MongoDB usa `prisma db push`.

`resources.json` contém:

- CRUD (`CREATE`, `VIEW`, `UPDATE`, `DELETE`) para cada entidade;
- uma entrada para cada endpoint, usando `permissions` quando configurado.

## Cuidados operacionais

- A geração é destrutiva nos diretórios `_gen`; não coloque código manual neles.
- Execute sempre na raiz correta: a resolução usa `System.getProperty("user.dir")`.
- Mantenha nomes em lower camel case. Vários trechos apenas alteram o primeiro caractere e não sanitizam identificadores.
- Garanta uma única PK por entidade. O Node rejeita uma entidade sem exatamente uma chave escalar antes de limpar a saída; no MongoDB 2.1.4 essa chave deve usar `fieldType: "uuid"`. Os demais alvos ainda possuem caminhos legados menos claros para contratos inválidos.
- Listas nulas, metadata ausente e relacionamentos incompletos normalmente causam falhas sem mensagem de validação útil.
- O teste existente executa geração sobre arquivos reais, captura exceções e não possui assertions; ele não garante a validade/compilação do resultado.
- O plugin compila em Java 11, mas o código Java gerado usa Jakarta/Spring 6, o que normalmente implica runtime Java 17 no serviço consumidor.

## Limitações e defeitos conhecidos

- README anterior menciona eventos/listeners; o fluxo atual implementa a abstração RabbitMQ via `messaging.RabbitMq`, não o modelo antigo de `events`/`listeners`.
- `onlyDTO` não é respeitado pelo gerador .NET.
- a validação identifica erros estruturais e semânticos do contrato, mas não compila nem executa o código gerado;
- o mapeamento de nullable .NET contém condições frágeis e pode produzir tipos inesperados;
- o gerador de endpoint .NET possui verificações inconsistentes entre método e HTTP method;
- alguns templates possuem imports/namespaces específicos da infraestrutura PotatoTech/DataOn;
- arquivos são escritos com `CREATE`, sem `TRUNCATE_EXISTING`; a limpeza do diretório reduz esse risco para fontes, mas arquivos de recursos dependem do `dropFile` anterior;
- a geração SQL assume campos e metadados válidos e pode falhar para entidade vazia ou relacionamento incompleto.

## Checklist para integrar um serviço

1. Fixar uma versão publicada do plugin no `pom.xml` ou pipeline.
2. Criar `project.json` com `entities`, `endpoints` e `enums`, mesmo quando vazios. O legado `properties.json` permanece aceito temporariamente.
3. Criar `static/` antes de gerar para .NET.
4. Confirmar uma PK e metadata completa em cada entidade.
5. Confirmar ambos os lados e a ordem dos autorrelacionamentos.
6. Executar o gerador em uma branch limpa e revisar todos os arquivos `_gen`, SQL e permissões.
7. Compilar o serviço consumidor para validar dependências, templates e integrações que não podem ser verificadas apenas pelo contrato JSON.
8. Nunca editar fontes `_gen` manualmente; customizações devem ficar fora deles ou ser incorporadas aos templates desta biblioteca.
