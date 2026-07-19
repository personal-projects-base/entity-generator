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
* Evoluir a geração de relacionamentos no Node/Prisma. A geração atual serve como base inicial, mas relacionamentos bidirecionais, autorrelacionamentos e `ManyToMany` ainda devem ser revisados manualmente antes de executar migrations.
* Evitar auto-imports e imports duplicados nos models TypeScript gerados para entidades autorreferenciadas.
* Avaliar uma abstração de conversão DTO/entity para Node, equivalente ao papel dos converters em Java e C#.
* Reforçar testes automatizados para relacionamentos em Java, C#, SQL e Node, cobrindo `OneToOne`, `OneToMany`, `ManyToOne`, `ManyToMany` e autorrelacionamentos.
* Considerar que a geração de relacionamentos tende a funcionar melhor e com menos ambiguidade em linguagens fortemente tipadas, como Java e C#, onde os converters, entidades e anotações/atributos conseguem expressar melhor o contrato de domínio.

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
