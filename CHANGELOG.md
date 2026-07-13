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
