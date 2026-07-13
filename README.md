# entity-generator


Modulo gerador de código fonte

Este modulo faz a geração de models, repositories, DTOS, endpoints e abstrações RabbitMQ para publish/subscribe.

[UI Criação entidades](https://develop.smartverse.com.br/entity/)


### Configurações

Deve ser criado um arquivo com nome properties.json na raiz do projeto

após criar o arquivo deve ser inserido as seguintes propriedades:

##### Java

    "mainPackage": "com.potatotech.entitygenerator",  
    "projectName": "entity-generator",
    "language": "JAVA"
    "entities": [],
    "endpoints": [],
    "enums": [],
    "messaging": {
      "RabbitMq": {
        "pub": [],
        "sub": []
      }
    }

##### DotNet

    "mainPackage": "EntityGenerator",  
    "projectName": "EntityGenerator",
    "language": "DOTNET"
    "defaultTypeId": "UUID"
    "entities": [],
    "endpoints": [],
    "enums": [],
    "messaging": {
      "RabbitMq": {
        "pub": [],
        "sub": []
      }
    }


##### Node

    "mainPackage": "example-node",  
    "projectName": "example-node",
    "language": "NODE"
    "entities": [],
    "endpoints": [],
    "enums": [],
    "messaging": {
      "RabbitMq": {
        "pub": [],
        "sub": []
      }
    }


* mainPackage: Nome completo do pacote do projeto
* projectName: Nome do projeto
* language: JAVA, DOTNET ou NODE
* defaultTypeId: (string) Padrão do tipo das chaves primarias - Apenas necessário para DotNet
* entities: Objeto de configuração das classes de entidades
* endpoints: configuração para criação dos endpoints
* enums: Criação das enumerations
* messaging: configuração de provedores de mensageria. Hoje o provedor suportado é `RabbitMq`

Apos configurado o arquivo properties.json pode se gerar o código gerando o seguinte comando a partir da raiz do projeto

  `mvn entity-generator:generate-sources`

### Entities

Neste objeto deverá ser configurado a criação das entidades.
Todas as entidades mapeadas serão criado automaticamente uma classe de DTO e uma classe de conversão.

O objeto entities deve ser configurado da seguinte forma:

    {
      "entityName": "cpf",
      "tableName": "cpf",
      "classExtends" : "document",
      "generateDefaultHandlers": false,
      "handlerAbstract": false,
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
* generateDefaultHandlers: se gera as interfaces de crud padrões
* handlerAbstract: permite que o Handler seja sobrescrito (disponivel apenas para JAVA)
* onlyDTO: é gerrado apenas a classe DTO, nenhum conversor, ou repository alem de crud é gerado
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
### Endpoints

Neste objeto deverá ser implementado os endpoints que deseja ser gerado

abaixo um exemplo da sintaxe:

    {
      "methodName": "listCity",
      "httpMethod": "POST",
      "grouper": "POST",
      "anonymous": true,
      "metadata": {
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

A exchange não é definida dentro do `properties.json`. O serviço consumidor deve criar uma classe concreta fora do diretório `_gen` e informar a exchange via `@RabbitExchange`.

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

No .NET, a exchange também não é definida dentro do `properties.json`. O serviço consumidor deve criar uma classe concreta fora do diretório `_gen` e informar a exchange via `RabbitExchange`.

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

O método `publish` envia a mensagem para a exchange configurada em `@RabbitExchange`, usando a `routingKey` informada no `properties.json`.

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

O método `Publish` envia a mensagem para a exchange configurada no atributo `RabbitExchange`, usando a `routingKey` informada no `properties.json`.

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

* Não edite os arquivos gerados em `_gen`; implemente configurações e handlers concretos fora desse diretório.
* `routingKey` deve ser informado nos publishers.
* Em Java, a classe concreta que estende `RabbitConfig` deve ter `@Configuration` e `@RabbitExchange`.
* Em .NET, a classe concreta que estende `RabbitConfig` deve ter `[RabbitExchange("...")]` e ser registrada no DI como `RabbitConfig`.
* Em .NET, subscribers concretos devem ser registrados como hosted services.
* O nome das filas e routing keys é escrito diretamente no código gerado a partir do `properties.json`.

### Node

O suporte Node gera código TypeScript em `src/generated`.

O objetivo inicial é fornecer uma base equivalente para integração em projetos Node modernos, sem tentar inferir toda a estrutura de aplicação do serviço consumidor. O código gerado assume:

* Express para controllers e rotas.
* Prisma Client para repositories.
* amqplib quando `messaging.RabbitMq` estiver configurado.

Assim como Java e .NET, a geração Node usa templates do projeto, em `src/main/resources/xsd/node/`.

O Node é gerado pelo mesmo Maven Plugin/JAR usado para Java e .NET. Não existe um gerador npm separado; basta executar o plugin ou o JAR na raiz do projeto consumidor com `language: "NODE"` no `properties.json`.

Paridade atual:

* Gera models/DTOs, enums, repositories, controllers, rotas CRUD, contratos de endpoints, arquivos estáticos, SQL e RabbitMQ.
* Respeita `generateDefaultHandlers` e `onlyDTO` para decidir se gera controllers/rotas/repositories.
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
