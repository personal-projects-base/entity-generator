# Gonthera CLI

Ferramenta para geração de backends Java, .NET e Node, incluindo entidades, DTOs, repositories, endpoints, contratos, PostgreSQL e abstrações RabbitMQ para publish/subscribe.

[UI Criação entidades](https://develop.smartverse.com.br/entity/)


## Configuração inicial

Para novos projetos, use a pasta `.gonthera`. O arquivo `.gonthera/project.json` é obrigatório e contém a identificação do serviço:

```text
.gonthera/
├── project.json
├── entities.json
├── endpoints.json
├── enums.json
├── messaging.json
└── authorization.json
```

```json
{
  "mainPackage": "com.example.service",
  "projectName": "service-name",
  "language": "JAVA"
}
```

`language` aceita `JAVA`, `DOTNET` ou `NODE`. Ajuste `mainPackage` para o pacote ou namespace do serviço consumidor; ele não deve usar o pacote interno do Gonthera CLI.

Os arquivos `entities.json`, `endpoints.json` e `enums.json` contêm arrays JSON diretamente. Para iniciar sem definições, use `[]` em cada arquivo. O `messaging.json` contém diretamente o objeto da mensageria:

```json
{
  "RabbitMq": {
    "pub": [],
    "sub": []
  }
}
```

O `authorization.json` controla os pontos de customização da autorização Java:

```json
{
  "authenticateAbstract": false,
  "tenantConfigurationAbstract": false
}
```

Os arquivos separados são opcionais. As seções `entities`, `endpoints`, `enums`, `messaging` e `authorization` também podem permanecer em `.gonthera/project.json`; quando existir, o arquivo separado sobrescreve somente sua seção. Se uma lista não estiver em nenhum dos locais, ela será inicializada vazia.

### Configuração em arquivo único

Durante a transição para o Gonthera CLI 2.0, também é aceito um `project.json` na raiz:

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

O legado `properties.json` permanece como último fallback. A prioridade é `.gonthera`, `project.json` da raiz e, por fim, `properties.json`. Se a pasta `.gonthera` existir, seu `project.json` será obrigatório e não haverá fallback para a raiz.

- `mainPackage`: pacote ou namespace base do serviço gerado.
- `projectName`: nome do serviço.
- `language`: `JAVA`, `DOTNET` ou `NODE`, sempre em maiúsculas.
- `entities`: entidades do projeto.
- `endpoints`: endpoints customizados.
- `enums`: enums do projeto.
- `messaging`: provedores de mensageria; atualmente, `RabbitMq`.
- `authorization`: customização das classes Java `Authenticate` e `TenantConfiguration`.

Para o Maven localizar e baixar o plugin, adicione o repositório de leitura e o plugin ao `pom.xml` do serviço consumidor:

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
      <version>2.1.2</version>
    </plugin>
  </plugins>
</build>
```

Após configurar o projeto, o código pode ser gerado com o seguinte comando a partir da raiz:

  `mvn gonthera-cli:generate-sources`

### Validação

A configuração oficial em `.gonthera` pode ser validada sem gerar ou apagar arquivos:

```bash
mvn gonthera-cli:validate
```

Com o executável ou JAR:

```bash
gonthera-cli.exe --validate
java -jar gonthera-cli-2.1.2.jar --validate
```

A validação isolada exige a pasta `.gonthera`, verifica a sintaxe e os tipos estruturais dos arquivos JSON, valida os campos obrigatórios e rejeita propriedades desconhecidas em qualquer nível. As coleções `entities`, `endpoints` e `enums` devem ser arrays; `messaging` deve ser objeto. A geração continua temporariamente compatível com `project.json` e `properties.json` na raiz e aplica as mesmas validações antes de alterar qualquer saída.

### Entities

Neste objeto deverá ser configurado a criação das entidades.
Todas as entidades mapeadas serão criado automaticamente uma classe de DTO e uma classe de conversão.

O objeto entities deve ser configurado da seguinte forma:

    {
      "entityName": "cpf",
      "tableName": "cpf",
      "classExtends" : "document",
      "generateDefaultControllers": false,
      "controllerAbstract": false,
      "serviceAbstract": false,
      "onlyDTO": false,
      "entityFields": [
        {
          "comment": "Identificador único do cpf",
          "fieldName": "id",
          "fieldProperties": {
            "fieldType": "uuid",
            "required": true,
            "valueDefault": ""
          },
          "metadata": {
            "nullable": true,
            "key": true
          }
        },
        {
          "comment": "Numero do CPF",
          "fieldName": "number",
          "fieldProperties": {
            "fieldType": "string",
            "required": false,
            "valueDefault": ""
          }
        },
        {
          "comment": "Cidade",
          "fieldName": "city",
          "fieldProperties": {
            "fieldType": "city",
            "required": false,
            "valueDefault": ""
          },
          "relationShips": {
            "fetchType": "EAGER",
            "relationShip": "OneToOne",
            "bidirectional": false
          }
        }
      ]
    },

* entityName: nome da entidade
* tableName: nome da tabela
* classExtends: se extende de alguma outra classe
* generateDefaultControllers: define se o CRUD padrão será gerado
* controllerAbstract: gera o Controller Java ou .NET como classe abstrata, permitindo implementação concreta e sobrescritas no projeto consumidor
* serviceAbstract: no Java, gera o `*Service` abstrato e sem `@Service`; o consumidor deve registrar uma implementação concreta. O padrão é `false`.
* onlyDTO: é gerrado apenas a classe DTO, nenhum conversor, ou repository alem de crud é gerado

#### Services Java

O CRUD Java separa transporte HTTP e operações de aplicação:

```text
Controller → Service → Repository
```

O `*Controller` recebe parâmetros HTTP e delega ao `*Service`. O service concentra transações, conversão DTO/entity, filtros, paginação e persistência.

Os nomes antigos `generateDefaultHandlers` e `handlerAbstract` continuam aceitos temporariamente, mas produzem avisos de depreciação. Quando o nome novo e o antigo forem declarados juntos, `generateDefaultControllers` e `controllerAbstract` terão precedência.

Com `serviceAbstract: false`, o service é concreto e recebe `@Service`. Com `serviceAbstract: true`, ele é abstrato, não recebe a anotação e exige um bean concreto no projeto consumidor:

```java
@Service
public class AppCustomerService extends CustomerService {
    @Override
    public CustomerDTO save(CustomerDTO dto) {
        return super.save(dto);
    }
}
```

#### Controllers .NET

A saída .NET também usa a nomenclatura Controller. O par legado `*Handler`/`*HandlerImpl` não é mais gerado. Para cada entidade com `generateDefaultControllers: true`, a CLI cria um único `Controllers/*Controller.cs` com os métodos CRUD `virtual`.

- `controllerAbstract: false`: gera um Controller concreto pronto para descoberta pelo ASP.NET Core;
- `controllerAbstract: true`: gera um Controller abstrato com a implementação CRUD padrão; o consumidor deve criar uma classe concreta fora de `_gen` e pode herdar ou sobrescrever métodos seletivamente;
- `generateDefaultControllers: false`: não gera Controller para a entidade;
- `serviceAbstract` continua específico do Java; o CRUD .NET ainda acessa o repository diretamente pelo Controller.

A saída fica organizada em:

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

Exemplo de customização:

```csharp
public class CustomerApplicationController : CustomerController
{
    public CustomerApplicationController(ICustomerRepository repository) : base(repository)
    {
    }

    public override ActionResult<CustomerDTO> Save(CustomerDTO input)
    {
        // Validação específica do consumidor.
        return base.Save(input);
    }
}
```
* entityFields: Objeto que contém os campos da entidade
  * comment: Comentario do campo, este item é obrigatório
  * fieldName: Nome do campo
  * list: Se é uma lista
  * fieldProperties: Propriedades do campo
    * fieldType: Tipo do campo(caso o tipo seja outra classe basta colocar o nome igual ao inserido no entityName)
    * required: se é obrigatório
    * valueDefault: valor default
  * metadata: Outras configurações
    * key: se é uma chave primaria
    * nullable: se aceita valor nulo
  * relationShips: Configurações de relacionamento
    * fetchType: (string) fetchType do campo: EAGER|LAZY
    * relationShip: (string) relacionamento ex: OneToOne, ManyToOne...
    * reference: (boolean) - deve ser marcado como verdadeiro caso seja referencia de uma propriedade autoreferenciada
    * mappedBy: (string) Em casos de classes autoReferenciada, o item que será bidirecional deve conter valor no mappedBy, referenciado a propriedade de referencia
    * bidirectional: (boolean) se é uma classe que terá um relacionamento bidirecional

OBS: Em caso de classes auto-referenciada, o campo de onde referencia o código pai, deve vir primeiro que a classe referenciada o filho no caso

#### Relacionamentos

Campos cujo `fieldProperties.fieldType` aponta para outra entidade devem declarar `relationShips`.

O lado dono do relacionamento deve usar `bidirectional: false`. Esse lado gera a coluna no SQL, a FK e o `@JoinColumn` no Java.

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

Saída Java:

```java
@JoinColumn(name = "parent_code")
@ManyToOne(fetch = FetchType.LAZY)
private CostCenterEntity parentCode;
```

Saída SQL:

```sql
parent_code uuid
ALTER TABLE cost_center ADD CONSTRAINT fk_cost_center_cost_center_parent_code FOREIGN KEY (parent_code) REFERENCES cost_center(id);
```

O lado inverso deve usar `bidirectional: true` e `mappedBy` apontando para o campo dono. Esse lado não gera coluna no SQL.

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

Saída Java:

```java
@OneToMany(mappedBy = "parentCode", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
private List<CostCenterEntity> children;
```

No converter Java, o lado inverso também é reamarrado ao lado dono ao converter DTO para entidade:

```java
entity.setChildren(childrenDtoConverter.toEntity(dto.children, null));
if (entity.getChildren() != null) entity.getChildren().forEach(e -> e.setParentCode(entity));
```

Para autorrelacionamentos `OneToOne`, o padrão é o mesmo:

```json
{
  "fieldName": "repliesCode",
  "fieldProperties": { "fieldType": "comment" },
  "relationShips": {
    "fetchType": "LAZY",
    "relationShip": "OneToOne",
    "bidirectional": false,
    "reference": true
  }
}
```

```json
{
  "fieldName": "replies",
  "fieldProperties": { "fieldType": "comment" },
  "relationShips": {
    "fetchType": "LAZY",
    "relationShip": "OneToOne",
    "mappedBy": "repliesCode",
    "bidirectional": true,
    "reference": false
  }
}
```

Gerando:

```java
@JoinColumn(name = "replies_code")
@OneToOne(fetch = FetchType.LAZY)
private CommentEntity repliesCode;

@OneToOne(mappedBy = "repliesCode", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
private CommentEntity replies;
```

Regras práticas:

* `relationShip`: `OneToOne`, `OneToMany`, `ManyToOne` ou `ManyToMany`.
* `fetchType`: usado nas anotações Java, normalmente `LAZY` ou `EAGER`.
* `bidirectional: false`: lado dono, gera coluna/FK.
* `bidirectional: true`: lado inverso, usa `mappedBy`.
* `mappedBy`: deve apontar para o campo dono.
* `reference: true`: use no campo de referência/FK, especialmente em autorrelacionamentos, para ajudar os converters a evitar recursão.
* `list: true`: use quando o relacionamento representa uma coleção.

#### DTO Converters

Para cada entidade, o fluxo Java gera DTO e converter. Exemplo para `costCenter`:

```txt
entities/CostCenterEntity.java
dtos/CostCenterDTO.java
converters/CostCenterDTOConverter.java
```

Use o converter para transformar dados entre a camada de API e a camada de persistência:

```java
import com.example.backend_gen.converters.CostCenterDTOConverter;
import com.example.backend_gen.dtos.CostCenterDTO;
import com.example.backend_gen.entities.CostCenterEntity;
import org.springframework.stereotype.Service;

@Service
public class CostCenterService {
    private final CostCenterDTOConverter converter;

    public CostCenterService(CostCenterDTOConverter converter) {
        this.converter = converter;
    }

    public CostCenterEntity toEntity(CostCenterDTO dto) {
        return converter.toEntity(dto, null);
    }

    public CostCenterDTO toDTO(CostCenterEntity entity) {
        return converter.toDTO(entity, "*");
    }
}
```

O segundo parâmetro é `displayFields`.

```java
converter.toDTO(entity, "*");
converter.toDTO(entity, "id,description");
converter.toDTO(entity, "id,description,children.id,children.description");
```

Use `*` para retornar todos os campos permitidos pelo converter. Em listagens e relacionamentos, prefira informar campos explicitamente para evitar respostas grandes.

Em relacionamentos bidirecionais, o converter reamarra o lado inverso ao lado dono ao converter DTO para entidade:

```java
entity.setChildren(childrenDtoConverter.toEntity(dto.children, null));
if (entity.getChildren() != null) entity.getChildren().forEach(e -> e.setParentCode(entity));
```

Quando um campo tem `reference: true`, ele representa o lado de referência/FK. Esse lado é evitado na conversão para DTO em alguns fluxos para impedir recursão infinita, como `pai -> filhos -> pai -> filhos`.

### Endpoints

Neste objeto deverá ser implementado os endpoints que deseja ser gerado

abaixo um exemplo da sintaxe:

    {
      "methodName": "listCity",
      "httpMethod": "POST",
      "grouper": "POST",
      "metadata": {
        "anonymous": true,
        "input": [
          {
            "parameterName": "id",
            "parameterType": "uuid",
            "list": false
          }
        ],
        "output": [
          {
            "parameterName": "city",
            "parameterType": "city",
            "list": true
          }
        ]
      }
    }

* methodName: nome do endpoint
* grouper: Agrupador de primitivas(a mesma interface será implementado os metodos agrupados)
* httpMethod: metodo do endpoint GET,POST
* metadata:
  * input: parametros de entrada
    * parameterName: nome do parametro
    * parameterType: tipo do parametro
    * list: se o objeto é do tipo lista
  * output: parametros de saida
    * parameterName: nome do parametro
    * parameterType: tipo do parametro
    * list: se o objeto é do tipo lista
  * anonymous: se o endpoint é anonimo

#### Autorização gerada no Java

Projetos Java não precisam mais adicionar o antigo `authorization-backend`. O Gonthera gera os componentes necessários em:

```text
src/main/java/<mainPackage convertido em caminho>_gen/authorization/
├── exception/ServiceException.java
├── permission/PermissionType.java
├── permission/Permissions.java
├── security/Authenticate.java
├── security/Roles.java
├── security/UserSupplier.java
├── stereotype/Anonymous.java
├── stereotype/SecureResource.java
├── tenant/TenantConfiguration.java
└── tenant/TenantContext.java
```

Endpoints com `metadata.anonymous: true` importam automaticamente a anotação gerada `authorization.stereotype.Anonymous`. Interceptors customizados devem usar também o `TenantConfiguration` gerado; a classe reconhece a anotação local e evita dependência do pacote `com.potatotech.authorization`.

Por padrão, `Authenticate` recebe `@Service` e `TenantConfiguration` recebe `@Component`, ficando prontas para uso. Para fornecer uma implementação Spring própria, use `.gonthera/authorization.json`:

```json
{
  "authenticateAbstract": true,
  "tenantConfigurationAbstract": true
}
```

Com um flag `true`, a classe correspondente é gerada como abstrata, sem o stereotype Spring. Seus métodos continuam com implementação funcional e podem ser herdados, chamados com `super` ou sobrescritos seletivamente. O consumidor deve criar os beans concretos fora de `_gen`:

```java
@Service
public class ApplicationAuthenticate extends Authenticate {

    @Override
    protected String resolveSecret() {
        return System.getenv("CUSTOM_SECRET_JWT");
    }
}

@Component
public class ApplicationTenantConfiguration extends TenantConfiguration {

    @Override
    public boolean validAnonymous(Object handler) {
        return super.validAnonymous(handler);
    }
}
```

`Authenticate` oferece hooks protegidos para `resolveSecret`, `extractToken`, `parseClaims`, `createUser`, `validateUser` e `createToken`. Normalmente sobrescreva apenas o ponto que realmente precisa mudar.

As classes `Authenticate` e `UserSupplier` usam JJWT `0.11.5`. Inclua no projeto consumidor:

```xml
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-api</artifactId>
  <version>0.11.5</version>
</dependency>
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-impl</artifactId>
  <version>0.11.5</version>
  <scope>runtime</scope>
</dependency>
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-jackson</artifactId>
  <version>0.11.5</version>
  <scope>runtime</scope>
</dependency>
```

Configure `SECRET_JWT` com uma chave HMAC adequada antes de autenticar ou gerar tokens. Ao usar `TenantContext`, chame `TenantContext.clear()` ao final de cada requisição para impedir que valores de `ThreadLocal` sejam reutilizados pela thread seguinte.

### Enums
Gera as enums do projeto

ex:

    {
      "enumName": "Status",
      "values": [
        "ACTIVE",
        "INACTIVE"
      ]
    }

### Messaging RabbitMQ

Disponível para projetos Java, .NET e Node.

A propriedade `messaging` agrupa os provedores de mensageria. Hoje o provedor suportado é `RabbitMq`. Em Java, o código gerado usa Spring AMQP. Em .NET, o código gerado usa RabbitMQ.Client e abstrações de hosting/configuração do ASP.NET Core. Em Node, o código gerado usa amqplib.

O gerador só cria arquivos RabbitMQ quando `messaging.RabbitMq` existe e possui ao menos um item em `pub` ou `sub`. Se `messaging` estiver vazio, ou se `RabbitMq` estiver ausente/nulo/vazio, nenhuma configuração RabbitMQ será gerada e o projeto consumidor não precisa carregar dependências de RabbitMQ.

Ela substitui o modelo antigo documentado como `events` e `listeners`, que não é implementado pelo gerador atual.

Use:

* `messaging.RabbitMq.pub`: canais RabbitMQ que o serviço publica.
* `messaging.RabbitMq.sub`: canais RabbitMQ que o serviço ouve.

Exemplo:

```json
{
  "mainPackage": "com.example.profilebackend",
  "projectName": "profile-backend",
  "language": "JAVA",
  "entities": [],
  "endpoints": [],
  "enums": [],
  "messaging": {
    "RabbitMq": {
      "pub": [
        {
          "name": "notification",
          "queue": "4libert.queue.profile.notification",
          "routingKey": "4libert.routingKey.profile.notification"
        },
        {
          "name": "followers",
          "queue": "4libert.queue.profile.followers",
          "routingKey": "4libert.routingKey.profile.followers"
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

#### Propriedades

* name: nome base da classe gerada. Exemplo: `notification` gera `NotificationPub`; `notificationChat` gera `NotificationChatSub`.
* className: opcional. Permite informar diretamente o nome da classe. Se o sufixo `Pub` ou `Sub` não existir, o gerador adiciona automaticamente.
* queue: nome da fila RabbitMQ.
* routingKey: chave de roteamento usada pelos publishers. Obrigatória em itens de `messaging.RabbitMq.pub`. Em .NET, também pode ser usada em `messaging.RabbitMq.sub` para o subscriber declarar o binding da fila com a exchange.

#### Arquivos gerados

##### Java

Quando `messaging.RabbitMq` é configurado em projeto Java, o gerador cria arquivos dentro de:

```text
src/main/java/<mainPackage convertido em caminho>_gen/messaging/
```

Arquivos comuns:

* `messaging/RabbitExchange.java`: anotação usada para informar a exchange no projeto consumidor.
* `messaging/RabbitConfig.java`: configuração abstrata com `TopicExchange`, `RabbitAdmin` e conversor JSON.

Arquivos de publisher:

* `messaging/pub/RabbitPublisher.java`: classe base com `RabbitTemplate` e método protegido `publishMessage`.
* `messaging/pub/<Name>Pub.java`: classe gerada para cada item de `messaging.RabbitMq.pub`, contendo `Queue`, `Binding`, `routingKey` e método público `publish(Object message)`.

Arquivos de subscriber:

* `messaging/sub/<Name>Sub.java`: classe abstrata gerada para cada item de `messaging.RabbitMq.sub`, contendo `@RabbitListener` e método abstrato `onMessage(String message)`.

##### .NET

Quando `messaging.RabbitMq` é configurado em projeto .NET, o gerador cria arquivos dentro de:

```text
<mainPackage com pontos convertidos em barras>_gen/Messaging/
```

Arquivos comuns:

* `Messaging/RabbitExchangeAttribute.cs`: atributo usado para informar a exchange no projeto consumidor.
* `Messaging/RabbitConfig.cs`: configuração abstrata que lê `RabbitMQ:*` de `IConfiguration`, cria conexão RabbitMQ e declara a exchange.
* `Messaging/AddRabbitMessaging.cs`: helper para registrar os publishers gerados no DI.

Arquivos de publisher:

* `Messaging/Pub/RabbitPublisher.cs`: classe base com conexão/canal RabbitMQ e método protegido `PublishMessage`.
* `Messaging/Pub/<Name>Pub.cs`: classe gerada para cada item de `messaging.RabbitMq.pub`, contendo fila, routing key e método público `Publish(object message)`.

Arquivos de subscriber:

* `Messaging/Sub/<Name>Sub.cs`: classe abstrata baseada em `BackgroundService`, contendo consumo da fila, ack/nack e método abstrato `OnMessage(string message)`.

#### Configuração da exchange Java

A exchange não é definida dentro do `project.json`. O serviço consumidor deve criar uma classe concreta fora do diretório `_gen` e informar a exchange via `@RabbitExchange`.

Exemplo:

```java
package com.example.profilebackend.messaging;

import com.example.profilebackend_gen.messaging.RabbitConfig;
import com.example.profilebackend_gen.messaging.RabbitExchange;
import org.springframework.context.annotation.Configuration;

@Configuration
@RabbitExchange("4libert.profile")
public class AppRabbitConfig extends RabbitConfig {
}
```

Essa decisão evita deixar a exchange fixa no código gerado e permite que cada serviço escolha sua própria configuração.

#### Configuração da exchange .NET

No .NET, a exchange também não é definida dentro do `project.json`. O serviço consumidor deve criar uma classe concreta fora do diretório `_gen` e informar a exchange via `RabbitExchange`.

Exemplo:

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

Registre a configuração concreta e os publishers gerados no `Program.cs`:

```csharp
using ExampleBackend.ExampleBackend_Gen.Messaging;
using ExampleBackend.Messaging;

builder.Services.AddSingleton<RabbitConfig, AppRabbitConfig>();
AddRabbitMessaging.AddRabbitMessagingGenerate(builder);
```

#### Publicando mensagens em Java

Com o exemplo acima, o gerador cria `NotificationPub`.

Uso em uma classe do serviço consumidor:

```java
package com.example.profilebackend.services;

import com.example.profilebackend_gen.messaging.pub.NotificationPub;
import org.springframework.stereotype.Service;

@Service
public class NotificationPublisherService {

    private final NotificationPub notificationPub;

    public NotificationPublisherService(NotificationPub notificationPub) {
        this.notificationPub = notificationPub;
    }

    public void send(Object payload) {
        notificationPub.publish(payload);
    }
}
```

O método `publish` envia a mensagem para a exchange configurada em `@RabbitExchange`, usando a `routingKey` informada no `project.json`.

#### Publicando mensagens em .NET

Com o exemplo acima, o gerador cria `NotificationPub`.

Uso em uma classe do serviço consumidor:

```csharp
using ExampleBackend.ExampleBackend_Gen.Messaging.Pub;

namespace ExampleBackend.Services
{
    public class NotificationPublisherService
    {
        private readonly NotificationPub _notificationPub;

        public NotificationPublisherService(NotificationPub notificationPub)
        {
            _notificationPub = notificationPub;
        }

        public void Send(object payload)
        {
            _notificationPub.Publish(payload);
        }
    }
}
```

O método `Publish` envia a mensagem para a exchange configurada no atributo `RabbitExchange`, usando a `routingKey` informada no `project.json`.

#### Ouvindo mensagens em Java

Subscribers são gerados como classes abstratas para manter a lógica de negócio fora de `_gen`.

Com o exemplo acima, o gerador cria `NotificationChatSub`. O serviço consumidor deve criar uma implementação concreta:

```java
package com.example.profilebackend.messaging;

import com.example.profilebackend_gen.messaging.sub.NotificationChatSub;
import org.springframework.stereotype.Component;

@Component
public class NotificationChatListener extends NotificationChatSub {

    @Override
    protected void onMessage(String message) {
        // Converter e processar a mensagem aqui.
    }
}
```

O método gerado com `@RabbitListener` recebe a mensagem como `String`, trata erros com log e chama `onMessage`.

#### Ouvindo mensagens em .NET

Subscribers .NET também são gerados como classes abstratas para manter a lógica de negócio fora de `_gen`.

Com o exemplo acima, o gerador cria `NotificationChatSub`. O serviço consumidor deve criar uma implementação concreta:

```csharp
using ExampleBackend.ExampleBackend_Gen.Messaging;
using ExampleBackend.ExampleBackend_Gen.Messaging.Sub;
using Microsoft.Extensions.Logging;

namespace ExampleBackend.Messaging
{
    public class NotificationChatListener : NotificationChatSub
    {
        public NotificationChatListener(RabbitConfig rabbitConfig, ILoggerFactory loggerFactory)
            : base(rabbitConfig, loggerFactory)
        {
        }

        protected override void OnMessage(string message)
        {
            // Converter e processar a mensagem aqui.
        }
    }
}
```

Registre o subscriber concreto como hosted service:

```csharp
builder.Services.AddHostedService<NotificationChatListener>();
```

O serviço gerado abre a conexão RabbitMQ, declara a exchange e a fila, consome mensagens como `string`, chama `OnMessage`, confirma com `BasicAck` em caso de sucesso e usa `BasicNack` com requeue em caso de erro.

Se o item de `messaging.RabbitMq.sub` tiver `routingKey`, o gerador também declara o binding entre a fila e a exchange:

```json
{
  "name": "notificationChat",
  "queue": "4libert.queue.chat.notification",
  "routingKey": "4libert.routingKey.chat.notification"
}
```

#### Dependências necessárias

##### Java

O projeto consumidor Java precisa ter Spring AMQP/RabbitMQ no classpath. Em projetos Spring Boot, normalmente:

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

Também é necessário configurar a conexão RabbitMQ do serviço consumidor, por exemplo via `application.properties` ou `application.yml`, conforme o padrão do Spring Boot:

```properties
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
```

##### .NET

O projeto consumidor .NET precisa ter o pacote RabbitMQ.Client:

```xml
<PackageReference Include="RabbitMQ.Client" Version="6.8.1" />
```

As abstrações `Microsoft.Extensions.Hosting`, `Microsoft.Extensions.Configuration` e `Microsoft.Extensions.Logging` normalmente já existem em projetos ASP.NET Core. Se o projeto não as tiver, adicione os pacotes correspondentes.

Configure a conexão RabbitMQ no `appsettings.json`:

```json
{
  "RabbitMQ": {
    "HostName": "localhost",
    "Port": "5672",
    "UserName": "guest",
    "Password": "guest",
    "VirtualHost": "/"
  }
}
```

#### Cuidados

* Não edite os arquivos gerados em `_gen`; implemente configurações e controllers concretos fora desse diretório.
* `routingKey` deve ser informado nos publishers.
* Em Java, a classe concreta que estende `RabbitConfig` deve ter `@Configuration` e `@RabbitExchange`.
* Em .NET, a classe concreta que estende `RabbitConfig` deve ter `[RabbitExchange("...")]` e ser registrada no DI como `RabbitConfig`.
* Em .NET, subscribers concretos devem ser registrados como hosted services.
* O nome das filas e routing keys é escrito diretamente no código gerado a partir do `project.json`.

### Node

O suporte Node gera código TypeScript em `src/generated`.

O objetivo inicial é fornecer uma base equivalente para integração em projetos Node modernos, sem tentar inferir toda a estrutura de aplicação do serviço consumidor. O código gerado assume:

* Express para controllers e rotas.
* Prisma Client para repositories.
* amqplib quando `messaging.RabbitMq` estiver configurado.

O Node é gerado pelo mesmo Maven Plugin/JAR usado para Java e .NET. Não existe um gerador npm separado; basta executar o plugin ou o JAR na raiz do projeto consumidor com `language: "NODE"` no `project.json`.

Paridade atual:

* Gera models/DTOs, enums, repositories, controllers, rotas CRUD, contratos de endpoints, arquivos estáticos, SQL e RabbitMQ.
* Respeita `generateDefaultControllers` — e o alias legado `generateDefaultHandlers` — e `onlyDTO` para decidir se gera controllers/rotas/repositories.
* Gera `prisma/schema.prisma` com datasource PostgreSQL, generator Prisma Client, enums, models, campos escalares e suporte inicial a relacionamentos.
* Ainda não gera `package.json`, `tsconfig.json` ou migrations.
* Relacionamentos complexos podem exigir revisão manual do `schema.prisma`, especialmente Many-to-Many e relações bidirecionais customizadas.

Exemplo mínimo:

```json
{
  "mainPackage": "example-node",
  "projectName": "example-node",
  "language": "NODE",
  "entities": [],
  "endpoints": [],
  "enums": [],
  "messaging": {
    "RabbitMq": {
      "pub": [],
      "sub": []
    }
  }
}
```

#### Arquivos gerados

```text
src/generated/models/*.model.ts
src/generated/enums/*.enum.ts
src/generated/repositories/*.repository.ts
src/generated/controllers/*.controller.ts
src/generated/routes/*.routes.ts
src/generated/routes/index.ts
src/generated/endpoints/*.endpoint.ts
src/generated/static/properties.json
src/generated/static/resources.json
src/generated/static/postgree.sql
prisma/schema.prisma
```

Quando `messaging.RabbitMq` possui canais, também são gerados:

```text
src/generated/messaging/rabbitmq/rabbit-config.ts
src/generated/messaging/rabbitmq/rabbit-publisher.ts
src/generated/messaging/rabbitmq/pub/*.pub.ts
src/generated/messaging/rabbitmq/sub/*.sub.ts
```

#### Uso das rotas geradas

```ts
import express from 'express';
import { PrismaClient } from '@prisma/client';
import { createGeneratedRoutes } from './generated/routes';

const app = express();
const prisma = new PrismaClient();

app.use(express.json());
app.use(createGeneratedRoutes(prisma));
```

#### RabbitMQ em Node

Crie uma configuração concreta fora de `src/generated`:

```ts
import { RabbitConfig } from './generated/messaging/rabbitmq/rabbit-config';

export class AppRabbitConfig extends RabbitConfig {
  constructor() {
    super({
      exchange: '4libert.profile',
      url: process.env.RABBITMQ_URL,
    });
  }
}
```

Publisher gerado:

```ts
import { CustomerChangedPub } from './generated/messaging/rabbitmq/pub/customerChanged.pub';
import { AppRabbitConfig } from './messaging/app-rabbit-config';

const publisher = new CustomerChangedPub(new AppRabbitConfig());
await publisher.publish({ id: 'customer-id' });
```

Subscriber gerado:

```ts
import { CustomerImportedSub } from './generated/messaging/rabbitmq/sub/customerImported.sub';
import { AppRabbitConfig } from './messaging/app-rabbit-config';

class CustomerImportedListener extends CustomerImportedSub {
  protected onMessage(message: string) {
    // tratar mensagem
  }
}

await new CustomerImportedListener(new AppRabbitConfig()).start();
```

#### Dependências Node esperadas

Exemplo de `package.json` para um serviço consumidor:

```json
{
  "name": "example-node-service",
  "version": "1.0.0",
  "type": "module",
  "scripts": {
    "build": "tsc",
    "start": "node dist/index.js",
    "dev": "tsx src/index.ts",
    "prisma:generate": "prisma generate"
  },
  "dependencies": {
    "@prisma/client": "^5.22.0",
    "amqplib": "^0.10.5",
    "express": "^4.21.2"
  },
  "devDependencies": {
    "@types/amqplib": "^0.10.6",
    "@types/express": "^4.17.21",
    "@types/node": "^22.10.2",
    "prisma": "^5.22.0",
    "tsx": "^4.19.2",
    "typescript": "^5.7.2"
  }
}
```

Se o projeto não usar RabbitMQ, remova `amqplib` e `@types/amqplib`. O gerador só cria arquivos RabbitMQ quando `messaging.RabbitMq` possui canais.

O gerador cria `prisma/schema.prisma`, mas não cria migrations. Depois de revisar o schema gerado, execute o fluxo Prisma usado pelo serviço consumidor, por exemplo `npx prisma generate` e `npx prisma migrate dev`.

O gerador não cria `package.json` ou `tsconfig.json`. Esses arquivos continuam sob responsabilidade do serviço consumidor.

### OBS:
  * Para geração correta dos arquivos estaticos para DotNet deve possuir a pasta "static"
### Tipos de dados

| fieldType | Campo gerado                        |
|-----------|-------------------------------------|
| uuid      | UUID ou Guid                        |
| string    | String                              |
| password  | String                              |
| datetime  | LocalDateTime(Java) ou DateTime(C#) |
| date      | LocalDate(Java) ou DateTime(C#)     |
| int       | Integer(Java) ou int(C#)            |
| integer   | int                                 |
| long      | Long                                |
| decimal   | Double                              |
| double    | Double                              |
| boolean   | boolean(Java) ou bool(C#)           |
| byte      | byte(apenas java)                   |
| byte[]    | InputStream(apenas Java)            |
| map       | Map<String, Object>(apenas Java)    |
