---
name: build-frontend-entity-contracts
description: Orientar o desenvolvimento frontend integrado ao Gonthera CLI, incluindo definição de entidades, campos, relacionamentos, endpoints e enums, consumo do CRUD gerado, filtros e exportação de um arquivo JSON por feature para importação no backend. Usar ao criar ou alterar telas, formulários, clientes HTTP ou contratos de domínio a partir do frontend.
---

# Construir contratos de entidades no frontend

Desenvolver o frontend a partir de contratos explícitos. Tratar o JSON exportado como a entrega do frontend para o backend, e não como configuração interna da interface.

Antes de implementar, ler `../../handoff/HANDOFF_FRONTEND.md` para consultar todos os campos, tipos, relacionamentos, filtros e limitações vigentes.

## Trabalhar por feature

Organizar o domínio por feature, por exemplo `products`, `cost-centers` ou `user-access`. Manter juntas as entidades, os endpoints e os enums necessários à mesma capacidade de negócio.

Gerar um arquivo por feature, com o nome em `kebab-case`:

```text
contracts/products.json
contracts/cost-centers.json
contracts/user-access.json
```

Não criar nem sobrescrever um único `properties.json` no frontend. Não incluir `mainPackage`, `projectName` ou `language` no arquivo da feature; esses valores pertencem ao projeto backend.

Cada arquivo deve ser JSON válido e usar obrigatoriamente este envelope, inclusive quando uma lista estiver vazia:

```json
{
  "entities": [],
  "endpoints": [],
  "enums": []
}
```

O arquivo deve poder ser exportado ou baixado sem comentários, texto adicional ou vírgulas finais. O backend deve importar o arquivo e combinar suas três listas com a configuração do projeto. Evitar nomes duplicados entre arquivos de features.

## Modelar uma entidade

Usar `lowerCamelCase` em `entityName` e `fieldName`, e `snake_case` em `tableName`. Para uma entidade persistida, definir exatamente uma chave primária.

`generateDefaultControllers` controla a geração do CRUD em Java, .NET e Node. Em Java e .NET, `controllerAbstract: true` gera o controller como classe abstrata para implementação no projeto consumidor. Os nomes `generateDefaultHandlers` e `handlerAbstract` são apenas aliases legados e não devem ser emitidos por interfaces novas.

```json
{
  "comment": "Cadastro de produtos",
  "entityName": "product",
  "tableName": "product",
  "classExtends": "",
  "generateDefaultControllers": true,
  "controllerAbstract": false,
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
    }
  ]
}
```

Manter os campos funcionais na estrutura real: tipo em `fieldProperties.fieldType` e nulidade em `metadata.nullable`. Não promover essas propriedades para o nível do campo.

Usar as combinações:

- obrigatório: `required: true` e `nullable: false`;
- opcional: `required: false` e `nullable: true`;
- chave primária: `key: true`, `required: true` e `nullable: false`.

Usar `frontendProperties` para renderização e comportamento visual. Esses dados são preservados no contrato, mas não alteram a persistência.

## Definir relacionamentos

Quando `fieldType` for o nome de outra entidade, incluir `relationShips`. Usar `bidirectional: false` e `reference: true` no lado proprietário. No lado inverso, usar `bidirectional: true`, `reference: false` e apontar `mappedBy` para o nome exato do campo proprietário.

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

Usar `list: true` normalmente em `OneToMany` e `ManyToMany`. Em autorrelacionamentos, declarar o lado proprietário antes do lado inverso.

## Definir endpoints e enums

Definir endpoints customizados no array `endpoints`. Manter `metadata.input` e `metadata.output` mesmo quando vazios. Preservar a chave `premissions`, com essa grafia, pois esse é o contrato implementado.

```json
{
  "comment": "Pesquisa produtos por categoria",
  "methodName": "findProductsByCategory",
  "httpMethod": "GET",
  "grouper": "product",
  "metadata": {
    "anonymous": false,
    "input": [
      { "parameterName": "categoryId", "parameterType": "uuid", "list": false }
    ],
    "output": [
      { "parameterName": "products", "parameterType": "product", "list": true }
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

Definir enums no array `enums`, com nome único e valores sem repetição:

```json
{
  "enumName": "productStatus",
  "values": ["ACTIVE", "INACTIVE"]
}
```

Usar o nome do enum em `fieldProperties.fieldType`. Enviar valores exatamente como declarados.

## Fazer chamadas ao CRUD gerado

Quando `generateDefaultControllers` for `true`, criar um cliente HTTP por entidade com estas operações. `generateDefaultHandlers` permanece apenas como alias legado:

```text
POST   /<entityName>       cria; body = DTO
PUT    /<entityName>/<id>  atualiza; body = DTO
DELETE /<entityName>/<id>  remove
GET    /<entityName>/<id>  busca por id
GET    /<entityName>       lista
```

Centralizar a URL base e autenticação no cliente HTTP da aplicação. Enviar `Content-Type: application/json` em `POST` e `PUT`. Tratar respostas não `2xx` antes de desserializar o corpo.

Exemplo de listagem:

```typescript
const params = new URLSearchParams({
  size: "20",
  offset: "1",
  filter: "name eq café and category.id eq 550e8400-e29b-41d4-a716-446655440000",
  displayFields: "id,name,category.id,category.name"
});

const response = await fetch(`${apiBaseUrl}/product?${params.toString()}`, {
  headers: { Authorization: `Bearer ${token}` }
});

if (!response.ok) throw new Error(`Falha ao listar produtos: ${response.status}`);
const page = await response.json();
```

Usar `URLSearchParams` ou o recurso `params` do cliente HTTP; não concatenar filtros manualmente. Considerar `offset` baseado em 1 na requisição.

No Java, permitir `eq`, `isNull`, `notNull`, `gte`/`ge` e `lte`/`le` para datas ISO, `and`, `or` e caminhos relacionados com ponto. Não misturar `and` e `or`, não gerar parênteses e não aceitar ` and ` ou ` or ` dentro de valores. Limitar `eq` a texto, UUID e enum; usar os operadores inclusivos para `date` e `datetime`. No .NET, usar apenas `eq` e uma única espécie de operador lógico por expressão; coleções usam `*`. No Node, enviar apenas paginação, pois o filtro atual é ignorado.

Para ordenação Java, enviar `order=campo,asc` ou `order=campo,desc`; direção ausente assume `asc` e caminhos relacionados com ponto são aceitos. Não oferecer ordenação dinâmica para .NET ou Node enquanto os respectivos geradores não a implementarem.

## Validar antes de exportar

Bloquear a exportação quando houver:

- arquivo sem os três arrays do envelope;
- entidade, campo, endpoint ou enum duplicado;
- entidade persistida sem exatamente uma chave primária;
- contradição entre `required` e `nullable`;
- tipo que não seja primitivo, enum ou entidade conhecida;
- campo relacional sem `relationShips` ou lado inverso sem `mappedBy`;
- endpoint sem `metadata.input` ou `metadata.output`;
- enum vazio ou com valores repetidos;
- referência a entidade ou enum inexistente na feature ou no catálogo importado das demais features.

Antes de entregar, validar com `JSON.parse`, confirmar o nome `<feature>.json` e retornar o arquivo completo da feature, não fragmentos isolados.
