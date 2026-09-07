# Gonthera CLI

Ferramenta para geração de backends Java, .NET e Node, incluindo entidades, DTOs, repositories, endpoints, contratos, PostgreSQL, MongoDB no alvo Node e abstrações RabbitMQ para publish/subscribe.

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
      <version>2.1.4</version>
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
java -jar gonthera-cli-2.1.4.jar --validate
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
* controllerAbstract: gera o Controller Java, .NET ou Node como classe abstrata, permitindo implementação concreta e sobrescritas no projeto consumidor; no Node, a implementação é registrada em `GeneratedControllerFactories`
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

A propriedade `messaging` agrupa os provedores de mensageria. Hoje o provedor suportado é `RabbitMq`. Em Java, o código gerado usa Spring AMQP. Em .NET, o código gerado usa RabbitMQ.Client e abstrações de hosting/configuração do ASP.NET Core. Em Node, o código gerado usa `amqp-connection-manager` sobre `amqplib`.

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
* routingKey: chave de roteamento usada pelos publishers. Obrigatória em itens de `messaging.RabbitMq.pub`. Em .NET e Node, também pode ser usada em `messaging.RabbitMq.sub` para o subscriber declarar o binding da fila com a exchange.

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

O suporte Node gera código TypeScript em `src/generated` para PostgreSQL ou MongoDB.

Escolha o banco no cabeçalho de `.gonthera/project.json`:

```json
{
  "mainPackage": "com.example.service",
  "projectName": "service-name",
  "language": "NODE",
  "database": {
    "provider": "MONGODB"
  }
}
```

Os providers aceitos são `POSTGRESQL` e `MONGODB`. Quando `database` não é
informado, PostgreSQL continua sendo usado para preservar os projetos existentes.
Quando o objeto é declarado, `provider` é obrigatório. Essa configuração é
exclusiva do alvo Node.

A partir da 2.1.3, também gera `configuration/database/database.config.ts` com
`DatabaseConfig` abstrata. Instale `dotenv` e `@prisma/client`, configure
`DATABASE_URL` no `.env` da raiz do serviço e execute `prisma generate` depois da
geração do schema. O gerador não cria nem sobrescreve o `.env` do consumidor.
Crie a implementação concreta fora de `src/generated`:

```ts
import { DatabaseConfig } from './generated/configuration/database/database.config';

export class AppDatabaseConfig extends DatabaseConfig {}
export const database = new AppDatabaseConfig();
```

Use `await database.connect()` na inicialização, passe `database.client` para
`createGeneratedRoutes` e chame `await database.disconnect()` no encerramento.
Reutilize a mesma instância. Sobrescreva `resolveUrl`, `createOptions` ou
`createClient` para customizar o comportamento. Esses hooks afetam a aplicação;
os comandos Prisma CLI continuam lendo `DATABASE_URL` do ambiente/`.env`.
O serviço `node-test-service` contém uma implementação concreta e `.env.example`.

Nos schemas Prisma, `OneToOne` bidirecional usa `bidirectional: false`
no proprietário da FK e `bidirectional: true` no inverso. Declare no inverso
`mappedBy` com o nome do campo proprietário; se omitido ou vazio, o gerador usa
o nome da entidade inversa, seguindo a convenção Java. A FK é única e ambos os
lados compartilham o nome da relação. O campo inverso é sempre opcional no Prisma,
mesmo com `metadata.nullable: false`. A conversão do CRUD segue as regras descritas abaixo.

Os pares `ManyToOne`/`OneToMany` também compartilham o nome da relação. Declare
`ManyToOne` com `list: false`, `bidirectional: false`, e o inverso `OneToMany`
com `list: true`, `bidirectional: true` e `mappedBy` apontando ao campo proprietário.
A FK segue o tipo e a nulabilidade configurados, sem unicidade.

Para `ManyToMany`, declare `list: true` nos dois lados, um proprietário
(`bidirectional: false`) e um inverso (`bidirectional: true`, com `mappedBy`).
No PostgreSQL, Prisma cria a tabela de ligação implícita a partir do nome da relação,
que inclui o campo proprietário para distinguir múltiplos vínculos entre os mesmos
models. Isso difere das tabelas do SQL legado `postgree.sql`: use migrations Prisma
para os serviços Prisma, sem misturar os dois scripts de criação.

No MongoDB, o Prisma exige arrays escalares de IDs nos dois lados de `ManyToMany`.
O Gonthera gera esses campos internamente, como `tagsIds`, mas eles não aparecem
nos DTOs, requests, responses ou OpenAPI. `OneToOne`, `ManyToOne` e `OneToMany`
mantêm o mesmo contrato; campos `ManyToOne` recebem índice explícito.
Relações proprietárias Mongo usam `onDelete: NoAction` e `onUpdate: NoAction` para
permitir ciclos e autorrelações validados pelo Prisma.

Nesta etapa, ambos os campos de cada par devem estar declarados; não são inventados
campos inversos. O fallback de `mappedBy` é o nome da entidade inversa. Configurações
ausentes, ambíguas ou incompatíveis são rejeitadas antes de apagar `src/generated`.
Em autorrelações ManyToMany implícitas do PostgreSQL, renomear campos mudando a ordem alfabética
pode inverter a interpretação dos lados pelo Prisma; revise a migração dos dados.
DTOs autorreferenciados usam diretamente seu próprio tipo, sem auto-imports;
referências externas e enums repetidos geram um único import de tipo por arquivo.
Enums nos models e no schema usam a grafia configurada, mesmo quando o campo usa
outra capitalização. OneToOne autorreferenciado exige exatamente um inverso com
`mappedBy` válido e não depende da ordem de declaração. A conversão de relações nas requisições/respostas é gerada na etapa 5.


### Contrato CRUD Node — itens 5, 6 e 7 (2.1.3)

O gerador agora produz `common/contracts.ts`, metadados, consultas e
`converters/entity-converter.ts`. POST/PUT recebem relações como objetos DTO:
`{"customer":{"id":"UUID"}}`, sem `customerId` ou operações internas Prisma.
`reference: true` conecta registros existentes pelo ID; os demais campos desse
objeto não atualizam o registro referenciado. `reference: false` permite criar ou
atualizar objetos aninhados, na mesma transação. Campos omitidos no PUT são preservados.
Uma coleção inversa OneToMany enviada substitui seus filhos e remove os omitidos;
ManyToMany substitui os vínculos sem excluir as entidades compartilhadas.
Exclusões continuam sujeitas às FKs: não há cascade recursivo geral equivalente ao JPA.

GET individual, POST e PUT retornam DTOs com relações expandidas. Ao entrar em uma
relação, a resposta deixa o campo recíproco como `null`, evitando repetir o objeto
pai (`customer.profile.customer`, por exemplo), e limita a profundidade a seis relações.
Campos não selecionados retornam `null`; FKs sintéticas não aparecem no JSON.
GET inexistente retorna 404. A listagem retorna
`{"size":20,"offset":0,"total":0,"contents":[]}`: entrada `offset=1` indica a
primeira página, saída usa índice zero; `total` conta os registros filtrados.

Parâmetros da listagem: `size` (padrão 20), `offset`, `filter`, `order` e
`displayFields`. `filter` aceita caminhos pontuados, `eq`, `isNull`, `notNull`,
`gte`/`ge` e `lte`/`le` para datas, unidos por `and` ou por `or`, sem misturá-los.
Texto com `eq` usa busca parcial sem distinguir caixa; UUID usa igualdade e enum
aceita nome ou ordinal. Node também converte igualdade numérica/booleana; não se
presume essa extensão nos demais backends. Coleções aceitam caminho pontuado ou
`*` após o nome. Não há parênteses nem mecanismo de escape na expressão.
`order=name,asc` ordena por campo; listas não são ordenáveis por esse parâmetro.
`displayFields=id;customer.name` projeta campos, também no GET individual;
o padrão é `*`. Entradas inválidas geram `CrudError` com status 400, tratado pelo
middleware da aplicação base. Datas usam ISO local, bytes usam base64 e long fora
da faixa segura do JavaScript retorna string.

No MongoDB, `isNull` encontra valor nulo ou campo ausente e `notNull` exige campo
presente e diferente de nulo. A expressão HTTP permanece igual; a consulta gerada
usa `isSet` somente no dialeto Mongo.

O CRUD Node resolve a chave primária pelos metadados da entidade. A rota permanece
`/:id`, mas o `where` usa o nome configurado e converte o parâmetro para UUID,
string, inteiro, long, decimal, booleano, data ou bytes conforme o campo. Objetos
relacionados também usam a chave real, como `{"category":{"code":12}}`. Cada
entidade Node deve declarar exatamente uma chave escalar. PostgreSQL mantém defaults
para UUID e inteiro. MongoDB exige `fieldType: "uuid"` e gera
`String @id @default(uuid()) @map("_id")`, preservando UUID no endpoint.

O objetivo inicial é fornecer uma base equivalente para integração em projetos Node modernos, sem tentar inferir toda a estrutura de aplicação do serviço consumidor. O código gerado assume:

* Express para controllers e rotas.
* Prisma Client para repositories.
* `amqplib` e `amqp-connection-manager` quando `messaging.RabbitMq` estiver configurado.

O Node é gerado pelo mesmo Maven Plugin/JAR usado para Java e .NET. Não existe um gerador npm separado; basta executar o plugin ou o JAR na raiz do projeto consumidor com `language: "NODE"` no `project.json`.

Paridade atual:

* Gera models/DTOs, enums, repositories, controllers, rotas CRUD, contratos de endpoints, OpenAPI 3.0.3, arquivos estáticos e RabbitMQ. O SQL é gerado somente para PostgreSQL.
* Respeita `generateDefaultControllers` — e o alias legado `generateDefaultHandlers` — e `onlyDTO` para decidir se gera controllers/rotas/repositories.
* `controllerAbstract: true` gera a base CRUD abstrata e exige uma factory concreta; controllers concretos também aceitam factory opcional para override.
* Gera `prisma/schema.prisma` com datasource PostgreSQL ou MongoDB, generator Prisma Client, enums, models, campos escalares e relacionamentos bidirecionais, coleções e autorrelações.
* Ainda não gera `package.json`, `tsconfig.json` ou migrations.

Exemplo mínimo:

```json
{
  "mainPackage": "example-node",
  "projectName": "example-node",
  "language": "NODE",
  "database": { "provider": "POSTGRESQL" },
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
src/generated/documentation/openapi.ts
src/generated/static/properties.json
src/generated/static/resources.json
src/generated/static/postgree.sql       # somente PostgreSQL
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

O documento OpenAPI gerado contém CRUD, schemas de criação, atualização, resposta,
referência e paginação, parâmetros `filter`, `order`, `displayFields`, `size` e
`offset`, além dos endpoints declarados em `endpoints.json`. A aplicação pode
estendê-lo sem editar a saída gerada:

```ts
import { generatedOpenApiDocument } from './generated/documentation/openapi';

export const openApiDocument = {
  ...generatedOpenApiDocument,
  info: { ...generatedOpenApiDocument.info, title: 'Minha API' },
  paths: { ...generatedOpenApiDocument.paths, ...customPaths }
};
```

Se uma entidade usa `controllerAbstract: true`, crie uma implementação fora de
`src/generated` e informe somente sua factory. As rotas continuam geradas:

```ts
import { CustomerController } from './generated/controllers/customer.controller';
import { createGeneratedRoutes, type GeneratedControllerFactories } from './generated/routes';

class AppCustomerController extends CustomerController {
  override async save(request: Request, response: Response): Promise<void> {
    // regra específica
    await super.save(request, response);
  }
}

const controllerFactories = {
  customer: repository => new AppCustomerController(repository)
} satisfies GeneratedControllerFactories;

app.use(createGeneratedRoutes(prisma, controllerFactories));
```

Cada controller abstrato cria uma propriedade obrigatória em
`GeneratedControllerFactories`. Controllers concretos criam propriedades opcionais,
permitindo substituição sem alterar as rotas. Os handlers gerados chamam métodos
normais por wrappers, preservando `this`, overrides e chamadas a `super`.

#### RabbitMQ em Node

O Node usa `amqp-connection-manager` sobre `amqplib`. Reutilize uma única
configuração concreta: ela compartilha a conexão, refaz exchange, filas, bindings e
consumidores após reconexão e mantém publicações pendentes até o canal voltar.
Publishers usam canal de confirmação. Subscribers executam `ack` somente depois do
handler terminar; falhas usam `nack` sem requeue por padrão. Configure
`requeueOnError: true` apenas quando o processamento for idempotente e houver uma
estratégia para mensagens inválidas.

Crie uma configuração concreta fora de `src/generated`:

```ts
import { RabbitConfig } from './generated/messaging/rabbitmq/rabbit-config';

export class AppRabbitConfig extends RabbitConfig {
  constructor() {
    super({
      exchange: '4libert.profile',
      url: process.env.RABBITMQ_URL,
      requeueOnError: false,
    });
  }
}

export const rabbit = new AppRabbitConfig();
```

Publisher gerado:

```ts
import { CustomerChangedPub } from './generated/messaging/rabbitmq/pub/customerChanged.pub';
import { rabbit } from './messaging/app-rabbit-config';

const publisher = new CustomerChangedPub(rabbit);
await publisher.publish({ id: 'customer-id' });
```

Subscriber gerado:

```ts
import { CustomerImportedSub } from './generated/messaging/rabbitmq/sub/customerImported.sub';
import { rabbit } from './messaging/app-rabbit-config';

class CustomerImportedListener extends CustomerImportedSub {
  protected onMessage(message: string) {
    // tratar mensagem
  }
}

const listener = new CustomerImportedListener(rabbit);
await rabbit.connect();
await listener.start();

// No encerramento da aplicação:
await listener.close();
await rabbit.close();
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
    "prisma:generate": "prisma generate",
    "prisma:migrate": "prisma migrate dev",
    "prisma:push": "prisma db push"
  },
  "dependencies": {
    "@prisma/client": "^5.22.0",
    "amqplib": "^2.0.1",
    "amqp-connection-manager": "^5.0.0",
    "express": "^4.21.2"
  },
  "devDependencies": {
    "@types/express": "^4.17.21",
    "@types/node": "^22.10.2",
    "prisma": "^5.22.0",
    "tsx": "^4.19.2",
    "typescript": "^5.7.2"
  }
}
```

Se o projeto não usar RabbitMQ, remova `amqplib` e `amqp-connection-manager`. O gerador só cria arquivos RabbitMQ quando `messaging.RabbitMq` possui canais. O `amqplib` 2 já inclui suas declarações TypeScript.

O gerador cria `prisma/schema.prisma`, mas não cria migrations. Depois de revisar o schema gerado, execute `npx prisma generate`. Para PostgreSQL, aplique o fluxo de migrations do serviço, como `npx prisma migrate dev`. Para MongoDB, use `npx prisma db push`; Prisma Migrate não suporta esse provider. O MongoDB precisa ser um replica set para as transações usadas pelo CRUD e pelas escritas relacionadas.

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
