## >2.1.4 - 07-09-2026

### Node com MongoDB
* Adicionada `database.provider` à configuração Node, aceitando `POSTGRESQL` e `MONGODB`; a ausência da propriedade preserva PostgreSQL como padrão.
* Separadas as regras relacionais e documentais em dialetos e templates próprios, mantendo compartilhada a validação estrutural das relações.
* O schema Mongo usa UUID textual em `_id`, relações explícitas `ManyToMany` com arrays internos de IDs, FKs escalares em `OneToOne`/`ManyToOne`, índices das relações `ManyToOne` e ações `NoAction` nos proprietários para suportar ciclos e autorrelações.
* Repositories Mongo mantêm transações sem enviar o isolamento PostgreSQL `RepeatableRead`. O runtime exige MongoDB configurado como replica set.
* Filtros Mongo preservam a sintaxe pública e tratam `isNull` como nulo ou campo ausente e `notNull` como campo presente não nulo.
* A geração Mongo não cria `postgree.sql` e remove esse artefato ao trocar um projeto Node de PostgreSQL para MongoDB. O fluxo de schema usa `prisma db push` porque Prisma Migrate não suporta MongoDB.
* Projetos Node Mongo exigem uma única chave escalar `uuid`, preservando o mesmo contrato de identificador e evitando o `autoincrement()` indisponível no MongoDB.
* O `node-test-service` foi atualizado para gerar MongoDB com o JAR 2.1.4 e usar `prisma db push`, mantendo o contrato HTTP existente.
* Documentada a URL autenticada `?authSource=admin&replicaSet=rs0`: `authSource` seleciona o banco de autenticação e o replica set habilita as transações exigidas pelos repositories.
* O schema Mongo foi validado com `prisma validate`, `prisma generate` e `prisma db push`; a saída TypeScript passou no `typecheck` e os 14 testes locais foram aprovados.
* A validação funcional em MongoDB replica set confirmou criação e leitura de `Customer` e o ciclo ManyToMany `Customer.tags` / `Tag.customers`: associação, leitura bidirecional, substituição por coleção vazia e preservação da `Tag` compartilhada.
* Confirmado que MongoDB standalone falha com Prisma `P2031`, sem fallback silencioso que pudesse comprometer a atomicidade das escritas.

## >2.1.3 - 07-09-2026

### Relacionamentos Node com Prisma e PostgreSQL
* A geração Node passou a resolver relações bidirecionais com a mesma convenção de lado proprietário, lado inverso e `mappedBy` usada pelo gerador Java.
* `OneToOne` gera uma FK única no proprietário, mantém o tipo nativo da chave referenciada e compartilha o nome da relação com o campo inverso. O inverso é opcional no schema, como exigido pelo Prisma.
* `ManyToOne`/`OneToMany` preserva a FK somente no proprietário, sem unicidade indevida. `ManyToMany` usa relações implícitas distintas, inclusive em autorrelações e quando há mais de um vínculo entre o mesmo par de entidades.
* Pares ausentes, ambíguos, com cardinalidade incompatível, `mappedBy` inválido ou colisão de FK são rejeitados antes da limpeza de `src/generated`.
* Corrigidos autoimports, imports duplicados e resolução de enums em models, DTOs e schema Prisma. A regeneração agora remove recursivamente subdiretórios antigos e interrompe sem deixar uma saída parcial quando a validação falha.

### DTOs, persistência e respostas Node
* POST e PUT recebem relações como objetos que contêm a chave configurada, por exemplo `{"customer":{"id":"UUID"}}`; campos artificiais como `customerId` não fazem parte do contrato HTTP.
* `reference: true` apenas conecta a entidade existente. Relações sem `reference` podem criar ou atualizar objetos aninhados na mesma transação; campos omitidos no PUT são preservados.
* Coleções inversas `OneToMany` enviadas removem os filhos omitidos, enquanto `ManyToMany` substitui somente os vínculos. Não foi criado cascade recursivo geral equivalente ao JPA.
* GET, POST e PUT retornam DTOs com relações expandidas. O conversor corta o campo recíproco imediato com `null`, evitando respostas circulares como `customer.profile.customer`, e limita a profundidade total da expansão.
* Datas usam ISO, bytes usam Base64 e valores `long` fora da faixa segura do JavaScript são serializados como string.

### Contrato CRUD e consultas Node
* A listagem agora retorna `{size, offset, total, contents}`. O tamanho padrão é 20, a primeira página é informada como `offset=1` e retornada como `offset: 0`.
* Implementados `filter`, `order` e `displayFields` no repository Prisma. O filtro aceita caminhos relacionados, coleções com caminho pontuado ou `*`, `eq`, `isNull`, `notNull`, `gte`/`ge`, `lte`/`le` e uma única família lógica por expressão (`and` ou `or`).
* `eq` textual faz busca parcial sem diferenciar maiúsculas; UUID usa igualdade; enums aceitam nome ou ordinal; números e booleanos são convertidos para o tipo Prisma.
* `order=campo,asc|desc` controla a ordenação e `displayFields=id;customer.name` controla a projeção, inclusive no GET individual. Entradas inválidas retornam 400 e uma chave inexistente retorna 404.
* A rota pública continua `/:id`, mas repositories e relações usam o nome e o tipo reais da única chave primária escalar configurada. O Node recusa entidades sem exatamente uma chave escalar antes de alterar a saída gerada.

### Configuração, Prisma e serviço base
* Adicionada a geração de `configuration/database/database.config.ts`. A classe abstrata `DatabaseConfig` carrega `.env`, reutiliza um `PrismaClient`, oferece `connect()`/`disconnect()` e hooks para URL, opções e criação do cliente; a implementação concreta fica fora de `src/generated`.
* `prisma/schema.prisma` é gerado com datasource PostgreSQL, Prisma Client, enums, models, tipos nativos e relacionamentos. Migrations continuam sendo responsabilidade do projeto consumidor.
* `controllerAbstract: true` agora gera no Node uma base abstrata com CRUD funcional, repository protegido e métodos sobrescrevíveis. `GeneratedControllerFactories` exige uma implementação concreta para cada controller abstrato e aceita overrides opcionais para controllers concretos; todas as rotas permanecem geradas e validam factories ausentes em runtime.
* Item 8.2: o Node agora gera `src/generated/documentation/openapi.ts` a partir de entidades, enums e endpoints. O documento inclui CRUD, tipos reais de chave, schemas de criação/atualização/resposta/referência, relações, paginação, filtros, projeção, erros, permissões e endpoints customizados.
* Criado `node-test-service` como serviço base reutilizável com Express 5, configuração concreta do banco, tratamento uniforme de erros, health check, Swagger UI em `/docs`, extensão externa do OpenAPI gerado e encerramento ordenado do Prisma.
* O serviço inclui `.env.example`, migration PostgreSQL inicial e scripts `dev`, `build`, `start`, `typecheck`, `gonthera-cli`, `gonthera-validate` e migrations Prisma. O comando npm executa o JAR local para manter a geração independente do ecossistema da aplicação.
* `package.json`, `tsconfig.json`, `.env`, migrations, servidor Express, montagem do Swagger UI, extensões OpenAPI, middleware de erros e configurações concretas continuam sendo arquivos do consumidor e não são sobrescritos pelo gerador.

### RabbitMQ no Node
* A geração passou a usar `amqp-connection-manager` 5.0.0 sobre `amqplib` 2.0.1, compartilhando uma conexão entre publishers e subscribers.
* Publishers usam canais confirmáveis e mantêm publicações durante reconexões. Subscribers recuperam setup e consumo, aplicam `prefetch`, confirmam mensagens somente após sucesso e usam `nack` sem requeue por padrão, com política configurável.
* Conexão, canais e consumidores oferecem encerramento explícito. A implementação concreta define URL, exchange e hooks fora de `src/generated`; o serviço base habilita o bootstrap com `RABBITMQ_ENABLED=true`.

### Validação desta entrega
* O JAR 2.1.3 foi compilado, a saída Node foi regenerada e o projeto TypeScript passou por `typecheck` e `build`.
* O responsável validou manualmente a API Node com PostgreSQL, incluindo CRUD, relações bidirecionais, expansão de DTOs, filtros e paginação, e aprovou o comportamento como base de projeto.
* O portal estático foi reorganizado por linguagem. Um seletor na introdução mantém toda a navegação e todos os exemplos seguintes restritos a Java, Node.js ou .NET; as trilhas Java e Node incluem instalação, geração, código, relações, CRUD, filtros, RabbitMQ, segurança e fronteiras dos arquivos gerados.
* A ampliação dos testes automatizados permanece para uma etapa posterior. O suporte MongoDB ainda não foi implementado e foi planejado separadamente para a versão 2.1.4.

## >2.1.2 - 02-09-2026

* Adicionados os operadores de data `gte`/`ge` e `lte`/`le` ao `SpecificationFilter` Java.
* Comparações de intervalo agora suportam campos `LocalDate` e `LocalDateTime`, inclusive em caminhos relacionados.
* Adicionada ordenação ao CRUD Java pelo parâmetro `order`, no formato `campo,asc|desc`.
* Corrigida a tipagem das expressões Criteria usadas nas comparações de data.
* Atualizados o portal de documentação, a configuração do repositório Maven e os downloads JAR/EXE da versão `2.1.2`.

## >2.1.0 - 23-07-2026

### Quebras de compatibilidade
* O código Java gerado não importa mais `com.potatotech.authorization`. Projetos consumidores que referenciam diretamente essa biblioteca devem migrar imports manuais para `<mainPackage>_gen.authorization`.
* `@Anonymous`, `ServiceException` e `TenantContext` agora pertencem ao código `_gen`; interceptors e handlers do consumidor devem usar os tipos gerados para preservar o mesmo comportamento.

### Novas funcionalidades
* A saída Java agora gera os componentes próprios de autorização `ServiceException`, `PermissionType`, `Permissions`, `Roles`, `UserSupplier`, `Authenticate`, `Anonymous`, `SecureResource`, `TenantConfiguration` e `TenantContext`.
* Endpoints com `metadata.anonymous: true` passam a usar a anotação `@Anonymous` gerada pelo próprio Gonthera.
* Adicionado `authorization.json` à configuração modular, com `authenticateAbstract` e `tenantConfigurationAbstract` para habilitar implementações customizadas no projeto consumidor.

### Melhorias
* Removida a dependência do código Java gerado em relação ao projeto `authorization-backend`.
* A autenticação gerada deixou de usar `StringUtils` interno do Maven Surefire e valida diretamente cabeçalhos e a variável `SECRET_JWT`.
* `TenantContext` agora fornece `clear()` para remover os valores dos `ThreadLocal` ao final da requisição.
* `Authenticate` agora expõe hooks protegidos para customizar segredo, bearer token, parsing de claims, criação e validação do usuário e geração do JWT sem substituir o fluxo inteiro.


## >2.0.2 - 23-07-2026

### Correções
* A anotação Java gerada `@RabbitExchange` agora possui `@Inherited`, permitindo que `RabbitConfig.resolveExchangeName()` encontre a exchange configurada em uma superclasse da implementação concreta.


## >2.0.1 - 19-07-2026

### Correções
* Campos dos DTOs Java agora são gerados com visibilidade `public`, permitindo que os converters em `converters` continuem acessando diretamente propriedades como `dto.id` e `dto.name` após a separação da saída em subpackages.

### Melhorias
* Centralizadas em `docs/rules/` as regras obrigatórias de versionamento, changelog e atualização dos handoffs.


## >2.0.0 - 17-07-2026

### Quebras de compatibilidade
* Renomeado o produto de `entity-generator` para `gonthera-cli`, refletindo a evolução do projeto para uma ferramenta de geração de backends para múltiplas linguagens e bancos de dados.
* Alterado o artifactId Maven de `entity-generator` para `gonthera-cli`.
* Alterados os artefatos executáveis para `gonthera-cli-x.x.x.jar` e `gonthera-cli.exe`.
* Alterado o pacote interno Java de `com.potatotech.entitygenerator` para `com.gonthera.cli`.
* Reorganizada a saída Java em subpackages `entities`, `dtos`, `converters`, `repositories`, `controllers`, `endpoints`, `enums` e `common`; imports antigos diretamente no package `_gen` precisam ser atualizados.
* Renomeados os CRUDs Java de `*Handler` para `*Controller`, o subpackage `handlers` para `controllers` e `HandlerBase` para `CrudController`.
* O CRUD Java agora gera `services/*Service`, e controllers delegam transações, conversão, filtros, paginação e persistência para essa camada.
* Reorganizada a saída .NET em `Entities`, `Dtos`, `Converters`, `Repositories`, `Controllers`, `Endpoints`, `Enums`, `Common`, `Data` e `Messaging`.
* Removido o par .NET `*Handler`/`*HandlerImpl`; agora é gerado um único `Controllers/*Controller`, concreto ou abstrato conforme `controllerAbstract`, com métodos virtuais para sobrescrita.

### Novas funcionalidades
* Adicionada leitura de `project.json` na raiz como configuração principal do projeto.
* Mantido suporte temporário a `properties.json` como fallback quando `project.json` não existir.
* Adicionado suporte à configuração modular em `.gonthera`, separando `project.json`, `entities.json`, `endpoints.json`, `enums.json` e `messaging.json`.
* Definida prioridade da pasta `.gonthera` sobre os arquivos de configuração presentes na raiz.
* Mantido suporte a todas as seções dentro de `.gonthera/project.json`, com sobrescrita opcional por arquivo separado.
* Adicionada validação sem geração pelos comandos `gonthera-cli.exe --validate`, `java -jar gonthera-cli-x.x.x.jar --validate` e `mvn gonthera-cli:validate`.
* O modo de validação exige `.gonthera`, verifica sintaxe e estrutura JSON, campos obrigatórios e propriedades desconhecidas possivelmente digitadas incorretamente.
* Integrada a validação estrutural e semântica ao fluxo normal de geração, antes da leitura pelo Gson e antes de alterações nas saídas.
* A limpeza das saídas Java e .NET agora remove recursivamente os subdiretórios gerados antes de recriar a nova estrutura.
* Adicionada a propriedade Java `serviceAbstract`: services concretos recebem `@Service`; services abstratos não recebem a anotação e geram aviso para implementação no consumidor.
* Adicionadas `generateDefaultControllers` e `controllerAbstract`; `generateDefaultHandlers` e `handlerAbstract` permanecem como aliases temporários com avisos de depreciação e precedência dos nomes novos.
* O validador estrutural agora diferencia tipos escalares JSON e rejeita strings no lugar de booleanos, incluindo as configurações de controller.


## >1.0.2 - 15-07-2026

### Correções
* Corrigido o filtro `eq` de campos enum para aceitar tanto ordinal quanto nome, inclusive em propriedades acessadas por join.
* Preservada a causa original ao reportar filtros inválidos.


## >1.0.1 - 13-07-2026

### Correções
* Removido comentario do @CrossOrigin(origins="*") dos handlers java


## >1.0.0 - 13-07-2026

### Novas Funcionalidades
* Adicionado suporte a geração Java e .NET de abstrações RabbitMQ via propriedade `messaging.RabbitMq`.
* Criada configuração abstrata `RabbitConfig` com exchange definida por anotação/atributo no serviço consumidor.
* Criada geração de publishers em `messaging/pub` ou `Messaging/Pub` com filas, bindings e método de publicação.
* Criada geração de subscribers abstratos em `messaging/sub` ou `Messaging/Sub` para implementação fora do diretório `_gen`.
* Adicionado suporte inicial a `language: "NODE"` com geração TypeScript para models, enums, repositories, controllers, rotas, endpoints e RabbitMQ.
* Adicionada geração de `prisma/schema.prisma` para projetos Node.

### Melhorias
* Documentado o contrato de `messaging.RabbitMq.pub` e `messaging.RabbitMq.sub` no handoff backend e no README.
* Documentado o fluxo Node no README e no handoff backend.
* Documentado o uso de relacionamentos e DTO converters na documentação Docusaurus.

### Melhorias futuras
* Adicionar suporte Node a MongoDB preservando o mesmo contrato HTTP já usado com PostgreSQL.
* Reforçar testes automatizados para relacionamentos em Java, C#, SQL e Node, cobrindo `OneToOne`, `OneToMany`, `ManyToOne`, `ManyToMany` e autorrelacionamentos.

### Quebras de compatibilidades
* Versão promovida para `1.0.0`.

### Correções
* N/A

### Melhorias
* N/A

### Novas Funcionalidades
* N/A

### Quebras de compatibilidades
* N/A

### Atualização de dependencia
* N/A

## >0.0.16 - 22-08-2024
### Novas Funcionalidades
* Adicionado suporte a geração de codigo DotNet

## >0.0.15 - 23-09-2023
### Novas Funcionalidades
* Criado configuraçção para relacionamento ManyToMany

## >0.0.14 - 03-09-2023
### Correções
* Implementado exclusão de arquivos metadata

## >0.0.13 - 03-09-2023
### Novas Funcionalidades
* Criado parametro para tornar opcional a geração dos handler base

### Correções
* Ajustado método dos endpoints quando o objeto output era vazio retornar void

## >0.0.11 - 30-08-2023
### Novas Funcionalidades
* Criado propriedade frontendProperties para armazenar propriedades para o front

## >0.0.10 - 30-08-2023
### Melhorias
* Ajustado geração das tabelas para respeitar o valor que está no tableName


## >0.0.9 - 27-08-2023
### Novas Funcionalidades
* Criado gerador de endpoints
* Criado gerador das entidades
* Criado gerador de arquivo sql para o banco
