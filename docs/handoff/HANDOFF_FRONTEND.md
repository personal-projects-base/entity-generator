# Handoff para o frontend — contratos do Gonthera CLI

## Objetivo

O frontend deve ser capaz de montar um fragmento JSON com entidades, campos, relacionamentos, endpoints e enums. Esse fragmento será colado nas respectivas listas do `project.json` do backend.

O frontend não precisa conhecer DTOs, repositories ou detalhes de persistência. Seu contrato é o JSON descrito abaixo.

## O que o backend disponibiliza

Após a geração, o backend mantém uma cópia normalizada do contrato em:

```text
properties.json
```

No projeto Java ela é gerada em `src/main/resources/properties.json`; no .NET, em `static/properties.json`. A forma como esse arquivo será exposto por HTTP depende do serviço consumidor.

Esse contrato contém:

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

O arquivo exportado pelo frontend não deve conter `mainPackage`, `projectName` ou `language`, pois essas propriedades pertencem ao serviço backend. Use um arquivo por feature, em `kebab-case`, com o envelope completo:

```json
{
  "entities": [],
  "endpoints": [],
  "enums": []
}
```

Mesmo quando uma seção estiver vazia, preserve os três arrays. O backend é responsável por importar e combinar as listas sem criar nomes duplicados.

## Convenções gerais

- Use JSON válido, sem comentários e sem vírgula após o último item.
- Nomes de entidade, campo, endpoint e parâmetro devem usar `lowerCamelCase`: `customerAddress`, `createdAt`, `findCustomer`.
- Nomes de enum e seus valores devem ser consistentes; valores normalmente usam `UPPER_SNAKE_CASE`.
- Tipos e nomes são referências textuais. Se um campo usa `customer`, deve existir uma entidade com `entityName: "customer"`.
- Sempre envie booleanos como `true`/`false`, números como números e listas como arrays.

## Entidade

Formato recomendado para exportação:

```json
{
  "comment": "Cadastro de produtos",
  "entityName": "product",
  "tableName": "product",
  "classExtends": "",
  "generateDefaultHandlers": true,
  "handlerAbstract": false,
  "onlyDTO": false,
  "entityFields": []
}
```

| Propriedade | Tipo | Regra para o frontend |
|---|---|---|
| `comment` | string | Descrição legível da entidade. |
| `entityName` | string | Obrigatório, único e em `lowerCamelCase`. |
| `tableName` | string | Nome em `snake_case`; normalmente igual à entidade convertida. |
| `classExtends` | string | Enviar `""`; herança não é aplicada atualmente. |
| `generateDefaultHandlers` | boolean | `true` para disponibilizar o CRUD padrão. |
| `handlerAbstract` | boolean | Normalmente `false`; opção específica do backend Java. |
| `onlyDTO` | boolean | Normalmente `false`; use `true` apenas para contrato sem persistência no Java. |
| `entityFields` | array | Obrigatório, com pelo menos um campo. |

Uma entidade persistida deve ter exatamente um campo com `metadata.key: true`.

## Campo de entidade

```json
{
  "comment": "Nome do produto",
  "fieldName": "name",
  "list": false,
  "fieldProperties": {
    "fieldType": "string",
    "required": true,
    "valueDefault": ""
  },
  "metadata": {
    "nullable": false,
    "key": false
  },
  "frontendProperties": {
    "label": "Nome",
    "size": 12,
    "hidden": false,
    "order": 2,
    "guidance": "Informe o nome comercial do produto",
    "reference": "",
    "enableFieldsFilter": true
  }
}
```

### Propriedades funcionais

| Propriedade | Tipo | Regra |
|---|---|---|
| `comment` | string | Obrigatório; explica o campo. |
| `fieldName` | string | Obrigatório e em `lowerCamelCase`. |
| `list` | boolean | `true` quando o valor é uma coleção. |
| `fieldProperties.fieldType` | string | Tipo primitivo, enum ou nome de outra entidade. |
| `fieldProperties.required` | boolean | Indica obrigatoriedade no contrato de entrada. |
| `fieldProperties.valueDefault` | string | Valor padrão textual; use `""` quando não houver. |
| `metadata.nullable` | boolean | Indica se banco/modelo aceita `null`. |
| `metadata.key` | boolean | `true` somente para a chave primária. |

Para evitar contratos contraditórios, use estas combinações:

- obrigatório: `required: true` e `nullable: false`;
- opcional: `required: false` e `nullable: true`;
- chave primária: `key: true`, `required: true`, `nullable: false`, normalmente com tipo `uuid`.

### Metadados exclusivos do frontend

`frontendProperties` é preservado no contrato para orientar telas dinâmicas. Ele não altera diretamente a persistência.

| Propriedade | Tipo | Uso sugerido |
|---|---|---|
| `label` | string | Texto exibido no formulário/tabela. |
| `size` | number | Largura em grid de 1 a 12. Padrão recomendado: `12`. |
| `hidden` | boolean | Oculta o campo da interface. IDs normalmente usam `true`. |
| `order` | number | Ordem crescente de exibição. |
| `guidance` | string | Ajuda, placeholder ou descrição para o usuário. |
| `reference` | string | Referência de UI definida pelo serviço; use `""` quando não utilizada. |
| `enableFieldsFilter` | boolean | Permite oferecer o campo em seleção/filtro de campos. |

O backend apenas armazena esses metadados. A interpretação visual deve ser padronizada pelo frontend.

## Tipos de campo

| `fieldType` | Tipo esperado no frontend |
|---|---|
| `uuid` | string UUID |
| `string`, `password` | string |
| `datetime` | string ISO-8601 com data e hora |
| `date` | string de data, preferencialmente `YYYY-MM-DD` |
| `int`, `integer`, `long` | number inteiro |
| `decimal`, `double` | number decimal |
| `boolean` | boolean |
| nome de enum | string com um dos valores do enum |
| nome de entidade | objeto da entidade; array quando `list: true` |

Evite `byte`, `byte[]`, `inputStream` e `map` em contratos gerados para múltiplas plataformas, pois o suporte não é uniforme.

## Relacionamentos

Inclua `relationShips` somente quando `fieldType` apontar para outra entidade ou para a própria entidade.

Campo proprietário:

```json
{
  "comment": "Categoria do produto",
  "fieldName": "category",
  "list": false,
  "fieldProperties": {
    "fieldType": "category",
    "required": true,
    "valueDefault": ""
  },
  "metadata": {
    "nullable": false,
    "key": false
  },
  "relationShips": {
    "fetchType": "LAZY",
    "relationShip": "ManyToOne",
    "mappedBy": "",
    "bidirectional": false,
    "reference": true
  }
}
```

Lado inverso:

```json
{
  "comment": "Produtos da categoria",
  "fieldName": "products",
  "list": true,
  "fieldProperties": {
    "fieldType": "product",
    "required": false,
    "valueDefault": ""
  },
  "metadata": {
    "nullable": true,
    "key": false
  },
  "relationShips": {
    "fetchType": "LAZY",
    "relationShip": "OneToMany",
    "mappedBy": "category",
    "bidirectional": true,
    "reference": false
  }
}
```

Regras:

- `fetchType`: `LAZY` por padrão; use `EAGER` apenas quando o objeto relacionado sempre precisar acompanhar a resposta;
- `relationShip`: `OneToOne`, `OneToMany`, `ManyToOne` ou `ManyToMany`;
- `mappedBy`: no lado inverso, nome exato do campo proprietário;
- `bidirectional`: `false` no proprietário e `true` no inverso;
- `reference`: `true` no campo que referencia o registro relacionado e `false` no inverso;
- `list`: normalmente `true` em `OneToMany` e `ManyToMany`;
- em autorrelacionamento, coloque o campo proprietário antes do campo inverso no array.

## Endpoints

Formato exportável:

```json
{
  "comment": "Pesquisa produtos por categoria",
  "methodName": "findProductsByCategory",
  "httpMethod": "GET",
  "grouper": "product",
  "metadata": {
    "anonymous": false,
    "input": [
      {
        "parameterName": "categoryId",
        "parameterType": "uuid",
        "list": false
      }
    ],
    "output": [
      {
        "parameterName": "products",
        "parameterType": "product",
        "list": true
      }
    ]
  },
  "permissions": {
    "description": "Permite pesquisar produtos",
    "resource": "findProductsByCategory",
    "premissions": ["VIEW"],
    "permissionDefault": false
  }
}
```

Regras:

- `methodName`: obrigatório, único e em `lowerCamelCase`;
- `httpMethod`: use `GET` para consulta e `POST` para envio de corpo;
- `grouper`: agrupa endpoints no .NET; use um nome estável ou `""`;
- `metadata.anonymous`: `true` somente quando não exigir autenticação;
- `metadata.input` e `metadata.output`: sempre devem existir, mesmo vazios;
- cada parâmetro contém `parameterName`, `parameterType` e `list`;
- `parameterType` segue a mesma tabela de tipos dos campos;
- a chave correta do contrato atual é `premissions`, apesar da grafia;
- permissões aceitas: `ALL`, `VIEW`, `CREATE`, `UPDATE`, `DELETE`.

O endpoint gerado define a assinatura, mas não sua implementação de negócio.

### Envelope padrão para paginação

Quando um endpoint usar `requestdata`/`responsedata`, o contrato padrão é:

```json
{
  "request": {
    "size": 10,
    "offset": 1,
    "filter": "",
    "order": "",
    "displayFields": ""
  },
  "response": {
    "size": 10,
    "offset": 1,
    "total": 100,
    "contents": []
  }
}
```

### Como enviar filtros no CRUD

No CRUD Java, envie `filter` como parâmetro do `GET /<entityName>`, junto de `size` e `offset`. Não envie o envelope acima como JSON no corpo do GET.

```typescript
const params = new URLSearchParams({
  size: "20",
  offset: "1",
  filter: "name eq café and category.id eq 550e8400-e29b-41d4-a716-446655440000",
  displayFields: "*"
});

const response = await fetch(`/product?${params.toString()}`);
```

Use `URLSearchParams` (ou a opção `params` do cliente HTTP) para codificar espaços, acentos e caracteres especiais. A expressão não deve ser concatenada manualmente à URL.

Operadores Java disponíveis:

| Formato | Uso no frontend |
|---|---|
| `field eq value` | Texto: busca parcial, case-insensitive. UUID: igualdade exata. Enum: igualdade. |
| `relation.field eq value` | Filtra por atributo de uma relação usando caminho pontuado. |
| `field isNull` | Seleciona valores nulos. |
| `field notNull` | Seleciona valores não nulos. |
| `condition and condition` | Exige ambas as condições. |
| `condition or condition` | Aceita qualquer uma das condições. |

Exemplos:

```text
name eq café
id eq 550e8400-e29b-41d4-a716-446655440000
category.id eq 550e8400-e29b-41d4-a716-446655440000
category isNull
status eq ACTIVE
name eq café and category notNull
name eq café or name eq chá
```

Ao montar a expressão:

- use `fieldName` em `lowerCamelCase`, nunca o nome da coluna SQL;
- envie os operadores exatamente como `eq`, `isNull` e `notNull`; coloque espaços ao redor de `and`/`or`;
- não coloque aspas em volta do valor;
- não misture `and` e `or` na mesma expressão e não gere parênteses: a precedência e o agrupamento do parser atual não são confiáveis;
- não permita ` and ` ou ` or ` dentro do valor, pois não existe escape;
- limite filtros Java a campos de texto, UUID, enum, nulidade e caminhos relacionados. Número, booleano e data podem falhar no backend atual;
- não ofereça `ne`, comparações, intervalos, listas ou ordenação: esses operadores não foram implementados;
- filtro vazio ou omitido lista sem restrição; erro de campo, UUID ou sintaxe retorna HTTP 400;
- `offset` começa em 1 para o cliente;
- `order` não é aplicado pelo handler Java atual;
- `displayFields` escolhe campos do DTO, mas não filtra registros.

O frontend deve considerar `language` antes de montar o filtro. No .NET, o dialeto separado aceita apenas `eq` e uma única espécie de operador lógico (`and` ou `or`) por expressão; relações comuns usam caminho pontuado e coleções usam `*`, como `children*.description eq matriz`. `isNull` e `notNull` não existem no .NET. No Node, o CRUD gerado atualmente ignora `filter` e usa somente `size`/`offset`.

## Enums

```json
{
  "enumName": "productStatus",
  "values": [
    "ACTIVE",
    "INACTIVE"
  ]
}
```

- `enumName` deve ser único;
- `values` deve ter pelo menos um item;
- o valor enviado pelo frontend deve coincidir exatamente com um item da lista.

## Exemplo completo pronto para colar no backend

O objeto abaixo é uma entidade independente e pode ser adicionado diretamente ao array `entities`:

```json
{
  "comment": "Cadastro de produtos",
  "entityName": "product",
  "tableName": "product",
  "classExtends": "",
  "generateDefaultHandlers": true,
  "handlerAbstract": false,
  "onlyDTO": false,
  "entityFields": [
    {
      "comment": "Identificador do produto",
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
      },
      "frontendProperties": {
        "label": "Código",
        "size": 12,
        "hidden": true,
        "order": 1,
        "guidance": "",
        "reference": "",
        "enableFieldsFilter": false
      }
    },
    {
      "comment": "Nome do produto",
      "fieldName": "name",
      "list": false,
      "fieldProperties": {
        "fieldType": "string",
        "required": true,
        "valueDefault": ""
      },
      "metadata": {
        "nullable": false,
        "key": false
      },
      "frontendProperties": {
        "label": "Nome",
        "size": 8,
        "hidden": false,
        "order": 2,
        "guidance": "Informe o nome comercial",
        "reference": "",
        "enableFieldsFilter": true
      }
    },
    {
      "comment": "Preço de venda",
      "fieldName": "price",
      "list": false,
      "fieldProperties": {
        "fieldType": "decimal",
        "required": true,
        "valueDefault": "0"
      },
      "metadata": {
        "nullable": false,
        "key": false
      },
      "frontendProperties": {
        "label": "Preço",
        "size": 4,
        "hidden": false,
        "order": 3,
        "guidance": "Valor de venda",
        "reference": "",
        "enableFieldsFilter": true
      }
    },
    {
      "comment": "Indica se o produto está ativo",
      "fieldName": "active",
      "list": false,
      "fieldProperties": {
        "fieldType": "boolean",
        "required": true,
        "valueDefault": "true"
      },
      "metadata": {
        "nullable": false,
        "key": false
      },
      "frontendProperties": {
        "label": "Ativo",
        "size": 4,
        "hidden": false,
        "order": 4,
        "guidance": "",
        "reference": "",
        "enableFieldsFilter": true
      }
    }
  ]
}
```

## Validações mínimas da tela geradora

Antes de permitir copiar/exportar:

1. Exigir `entityName`, `tableName`, `comment` e pelo menos um campo.
2. Impedir nomes duplicados de campos.
3. Exigir exatamente uma chave primária para entidades persistidas.
4. Validar `required` contra `nullable`.
5. Validar tipos contra primitivos, enums e entidades conhecidos.
6. Exigir `relationShips` para campos cujo tipo seja uma entidade.
7. Exigir `mappedBy` no lado bidirecional/inverso.
8. Exigir arrays `input` e `output` em endpoints.
9. Impedir enums sem valores ou com valores duplicados.
10. Exportar somente JSON, sem texto explicativo ao redor, para permitir colagem direta.
