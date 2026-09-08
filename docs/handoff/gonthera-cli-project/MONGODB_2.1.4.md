# Node + MongoDB — Gonthera CLI 2.1.4

## Estado deste documento

Este documento registra o desenho, a implementação e o fechamento do MongoDB exclusivamente na geração Node. A versão `2.1.4` foi fechada em 7 de setembro de 2026, e os itens descritos como comportamento atual fazem parte do motor de geração.

Os geradores Java e .NET ficam fora deste escopo. O contrato HTTP já consolidado no Node com PostgreSQL deve permanecer igual: rotas, corpos com relações em forma de objeto, DTOs, paginação, filtros, ordenação, projeção, erros e documento OpenAPI não devem expor qual banco está sendo usado.

## Decisões implementadas

### Configuração do banco

O cabeçalho de `.gonthera/project.json` aceita uma configuração Node com padrão retrocompatível:

```json
{
  "mainPackage": "com.example.service",
  "projectName": "service-name",
  "language": "NODE",
  "database": {
    "provider": "POSTGRESQL"
  }
}
```

Valores aceitos: `POSTGRESQL` e `MONGODB`. Se `database` estiver ausente, o Node assume `POSTGRESQL`, preservando todos os projetos existentes. Quando o objeto é declarado, `database.provider` é obrigatório. A propriedade só afeta a geração Node; Java e .NET continuam com o comportamento atual.

O provider é configuração de geração. A conexão continua vindo de `DATABASE_URL` no `.env` do serviço consumidor.

### Versão do Prisma

Manter Prisma `5.22` na 2.1.4 é a opção de menor risco: o código Node existente, a API de `PrismaClient` e as transações permanecem estáveis, sem introduzir uma atualização de dependência que possa alterar PostgreSQL na mesma entrega.

O salto para Prisma 8 deve ser uma tarefa separada. O conector MongoDB do Prisma 8 usa outra geração de cliente e outro contrato de runtime; sua documentação de migração também exige Node 24, TypeScript 5.9 e MongoDB 8. Misturar essa migração com o primeiro suporte Mongo ampliaria muito o escopo da 2.1.4. Referência: [guia oficial de atualização do Prisma MongoDB](https://docs.prisma.io/docs/guides/upgrade-prisma-orm/mongodb).

### Identificadores

Para entidades com chave `uuid`, manter UUID textual também no MongoDB:

```prisma
id String @id @default(uuid()) @map("_id")
```

Isso preserva os mesmos valores e schemas OpenAPI usados no PostgreSQL. O MongoDB não precisa usar `ObjectId`; o Prisma permite UUID como `_id` quando não se aplica `@db.ObjectId`. Referência: [Prisma Schema Reference](https://docs.prisma.io/docs/orm/reference/prisma-schema-reference).

O MongoDB não suporta `autoincrement()`. Na primeira entrega, uma chave inteira é rejeitada pela validação Node para projetos Mongo, evitando que o mesmo POST passe a exigir um ID fornecido pelo cliente. Chaves escalares textuais fornecidas explicitamente podem ser avaliadas depois, mas UUID é a estratégia suportada e documentada na 2.1.4.

Chaves compostas também ficam fora do escopo; o Node já exige uma única chave escalar.

### Migrações e execução

PostgreSQL continua com schema Prisma e SQL gerado. MongoDB não executa `GenerateSQL.generateSql()`, remove um `postgree.sql` antigo ao trocar o provider e não possui migration SQL.

O Prisma Migrate não suporta MongoDB. O fluxo documentado do consumidor será:

```bash
npx prisma generate
npx prisma db push
```

Referência: [visão geral oficial do Prisma Migrate](https://docs.prisma.io/docs/orm/v6/prisma-migrate/understanding-prisma-migrate/overview).

O MongoDB usado pela aplicação precisa operar como replica set. O Prisma usa transações para escritas aninhadas, e o repository gerado também depende delas para manter operações compostas atômicas. Instâncias standalone não serão uma configuração suportada. Referência: [conector MongoDB do Prisma](https://www.prisma.io/docs/orm/v7/core-concepts/supported-databases/mongodb).

## Impacto por componente

### Modelo e validação da configuração

Arquivos centrais:

- `src/main/java/com/gonthera/cli/model/Properties.java`;
- loader e validador de `project.json` e `.gonthera/project.json`;
- schema/validações de propriedades desconhecidas;
- metadado `src/generated/static/properties.json`.

Criar um enum de provider e um objeto de configuração de banco. A validação deve aceitar a nova seção somente para `language: "NODE"`, aplicar PostgreSQL como padrão e recusar combinações incompatíveis antes de limpar `src/generated`.

Validações Mongo iniciais:

- exatamente uma chave escalar por entidade, como já ocorre no Node;
- chave Mongo suportada como `uuid` nesta primeira entrega;
- relações completas e sem ambiguidades;
- campos e relações que exigirem recursos não suportados devem falhar com mensagem que informe entidade e campo.

### Geração do schema Prisma

Arquivos centrais:

- `GeneratePrisma.java`;
- `src/main/resources/xsd/node/prismaschema.mxsd`.

Separar as regras comuns das regras do provider. A geração PostgreSQL deve produzir exatamente o schema atual. Para MongoDB:

- datasource com `provider = "mongodb"`;
- chave com `@map("_id")`;
- remover `@db.Uuid` e `@db.Date`, que hoje estão embutidos na geração PostgreSQL;
- nunca emitir `autoincrement()`;
- armazenar referências como UUID textual;
- declarar `onDelete: NoAction` e `onUpdate: NoAction` nas relações proprietárias para aceitar ciclos e autorrelações;
- emitir índices explicitamente, pois `relationMode = "prisma"` não cria índices de foreign key no banco;
- mapear nomes de campos internos sem alterar DTOs ou OpenAPI.

O `Decimal` clássico não é suportado pelo conector Mongo. Como o gerador atualmente converte `decimal` e `double` para Prisma `Float`, a 2.1.4 deve documentar que `decimal` não oferece precisão decimal exata no Node. Uma futura estratégia com string/Decimal128 exigiria conversão explícita e está fora desta primeira entrega.

### Relacionamentos

`OneToOne`, `ManyToOne` e `OneToMany` podem manter a mesma interpretação de proprietário, inverso, `mappedBy`, `bidirectional` e `reference`. O schema Mongo precisa gerar o campo escalar da relação sem tipos nativos PostgreSQL e criar `@unique` no lado proprietário de `OneToOne`.

MongoDB não suporta a relação muitos-para-muitos implícita usada hoje no PostgreSQL. O schema precisa adicionar arrays internos de IDs nos dois lados:

```prisma
model Customer {
  id     String   @id @default(uuid()) @map("_id")
  tagIds String[] @map("tag_ids")
  tags   Tag[]    @relation("CustomerTags", fields: [tagIds], references: [id])
}

model Tag {
  id          String     @id @default(uuid()) @map("_id")
  customerIds String[]   @map("customer_ids")
  customers  Customer[] @relation("CustomerTags", fields: [customerIds], references: [id])
}
```

Os arrays de IDs são detalhes do Prisma e nunca entram nos models HTTP, DTOs, filtros públicos, metadados ou OpenAPI. Seus nomes devem ser derivados do nome do campo da relação para evitar colisões em múltiplas relações e autorrelacionamentos. Referência: [relações muitos-para-muitos no MongoDB](https://docs.prisma.io/docs/orm/v6/prisma-schema/data-model/relations/many-to-many-relations).

### Conversores e escrita de relações

Arquivo central: `src/main/resources/xsd/node/entityconverter.mxsd`.

O contrato de entrada permanece:

```json
{
  "customer": { "id": "uuid" },
  "tags": [{ "id": "uuid" }]
}
```

As operações Prisma `connect`, `set`, `create`, `update` e `upsert` podem continuar sendo a base. A implementação precisa validar em Mongo real que o Prisma sincroniza corretamente os arrays de IDs explícitos do muitos-para-muitos, inclusive ao substituir vínculos e ao remover órfãos de coleções inversas. `Prisma.DbNull` em campos `map` também precisa de validação específica, pois Mongo diferencia valor nulo de campo inexistente.

A supressão de ciclos dos DTOs permanece independente do banco e não deve mudar.

### Repositories e transações

Arquivo central: `src/main/resources/xsd/node/repository.mxsd`.

POST e PUT podem conservar a transação interativa. A listagem hoje envia `isolationLevel: RepeatableRead`; essa opção é específica do fluxo relacional e não deve ser emitida para MongoDB. A geração precisa selecionar apenas a configuração da transação conforme o provider, mantendo o retorno `{size, offset, total, contents}`.

Não deve existir fallback silencioso sem transação. Se o Mongo não for replica set, a aplicação deve falhar com erro claro, pois continuar poderia deixar escritas aninhadas incompletas.

### Filtros, ordenação e projeção

Arquivo central: `src/main/resources/xsd/node/crudquery.mxsd`.

O texto recebido pelo endpoint não muda. Busca textual parcial e `mode: "insensitive"`, caminhos relacionados, ordenação e seleção de campos têm equivalentes no conector Mongo. Referência: [case sensitivity no Prisma Client](https://docs.prisma.io/docs/orm/v6/prisma-client/queries/case-sensitivity).

Mongo diferencia um campo ausente de um campo armazenado com `null`. Para manter a intenção do contrato Gonthera:

- `isNull` deve encontrar valor `null` ou campo ausente;
- `notNull` deve encontrar apenas campo presente com valor diferente de `null`;
- a implementação Mongo deve compor o predicado com `isSet`, sem alterar a sintaxe pública do filtro;
- PostgreSQL conserva os predicados atuais.

Filtros JSON avançados ficam fora do escopo inicial. Campos `map` continuam retornáveis e graváveis, mas apenas operações já expostas pelo contrato comum devem ser documentadas.

### Orquestração, configuração e API

Arquivo central: `GenerateNode.java`.

O provider deve ser resolvido uma vez e repassado para Prisma, repository e consulta. O fluxo Mongo pula somente a geração SQL. Controllers, factories, rotas, models, OpenAPI, RabbitMQ, resources e a classe abstrata `DatabaseConfig` permanecem compartilhados.

`DatabaseConfig` já recebe `DATABASE_URL` e cria `PrismaClient`, portanto sua interface pode continuar igual. A documentação deve mostrar URLs distintas:

```dotenv
# PostgreSQL
DATABASE_URL=postgresql://user:password@localhost:5432/service

# MongoDB autenticado e configurado como replica set
DATABASE_URL=mongodb://user:password@localhost:27017/service?authSource=admin&replicaSet=rs0
```

`authSource` define o banco de autenticação e `replicaSet` identifica o conjunto de
réplicas; são parâmetros diferentes e podem ser usados juntos. Informar apenas
`authSource=admin` não habilita transações em uma instância standalone.

Erros Prisma de chave duplicada, registro ausente e falha de transação devem ser conferidos no Mongo para manter os mesmos status HTTP usados no PostgreSQL.

## Estrutura implementada

1. `DatabaseProvider` e `Database` representam a configuração, e `NodeDatabaseDialects` aplica PostgreSQL como fallback.
2. `PostgreSqlDatabaseDialect` mantém tipos nativos, transação com `RepeatableRead`, relações implícitas e SQL.
3. `MongoDbDatabaseDialect` gera atributos documentais, IDs internos do `ManyToMany`, transação Mongo, filtros de nulo e limpeza do SQL relacional.
4. `prismaschema-postgresql.mxsd` e `prismaschema-mongodb.mxsd` mantêm os datasources separados.
5. `GeneratePrisma`, repositories e suporte CRUD usam o dialeto resolvido sem expor o provider no contrato HTTP.

## Matriz de aceitação e continuidade

Não foram adicionados testes unitários específicos do dialeto nesta etapa. O schema
gerado foi aceito pelo `prisma validate` 5.22, os fontes Mongo passaram no compilador
TypeScript e a suíte existente do `node-test-service` passou. A 2.1.4 foi fechada com
a validação funcional descrita abaixo. Para ampliar a regressão entre providers,
continue comparando serviços equivalentes PostgreSQL e MongoDB nos seguintes pontos:

- validação e geração repetida sem resíduos;
- criação, atualização, busca, listagem e exclusão;
- chave UUID gerada e recebida da mesma forma;
- `OneToOne`, `ManyToOne`, `OneToMany` e `ManyToMany`;
- relações unidirecionais, bidirecionais, autorrelações e dois vínculos entre o mesmo par;
- relações por referência e objetos aninhados;
- substituição de coleções e remoção de órfãos;
- ausência de recursão nos DTOs;
- paginação, total, ordenação e `displayFields`;
- todos os operadores de filtro, com atenção a nulo versus campo ausente;
- rollback de uma escrita aninhada que falha no meio da operação;
- erros 400, 404 e conflito de unicidade;
- OpenAPI e formato JSON idênticos entre os providers.

## Validação funcional concluída

Em 7 de setembro de 2026, o `node-test-service` foi regenerado com
`database.provider: "MONGODB"` e validado contra um MongoDB replica set. O fechamento
incluiu:

- `prisma validate`, `prisma generate` e `prisma db push` com Prisma 5.22;
- compilação TypeScript sem erros;
- 14 testes locais de HTTP, configuração, DTOs, consultas e conversores aprovados;
- criação e consulta reais de um `Customer` com UUID textual em `_id`;
- confirmação do erro Prisma `P2031` quando o servidor estava em modo standalone,
  comprovando que não existe fallback silencioso sem transação;
- repetição bem-sucedida do CRUD após ativar o replica set;
- validação da relação MongoDB `Customer.tags` / `Tag.customers`:

  - criação independente de `Customer` e `Tag`;
  - associação pelo contrato público `{"tags":[{"id":"UUID"}]}`;
  - leitura do vínculo nos dois lados da relação;
  - substituição da coleção por `tags: []`;
  - remoção dos IDs internos nos dois documentos;
  - preservação da `Tag` compartilhada após a remoção do vínculo.

Todas as operações retornaram sucesso e os registros temporários foram removidos ao
fim do teste. O suporte MongoDB e o ponto de sincronização ManyToMany
`Customer` / `Tag` estão fechados para a 2.1.4. Os demais itens da matriz permanecem
como cobertura adicional de regressão, sem bloquear esta versão.

## Limites da 2.1.4

- suporte somente para geração Node;
- Prisma 5.22 mantido;
- MongoDB obrigatoriamente em replica set;
- UUID textual como chave Mongo suportada;
- sem Prisma Migrate para Mongo;
- sem `ObjectId` no contrato HTTP;
- sem atualização para Prisma 8;
- sem precisão decimal nativa;
- sem mudança nos geradores Java e .NET.
