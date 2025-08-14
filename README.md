# entity-generator


Modulo gerador de código fonte

Este modulo faz a geração de models, repositories, DTOS, Classes de eventos e listeners e endpoints 

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
    "events": [],
    "listeners": []

##### DotNet

    "mainPackage": "EntityGenerator",  
    "projectName": "EntityGenerator",
    "language": "DOTNET"
    "defaultTypeId": "UUID"
    "entities": [],
    "endpoints": [],
    "enums": [],
    "events": [],
    "listeners": []


* mainPackage: Nome completo do pacote do projeto
* projectName: Nome do projeto
* language: JAVA ou DOTNET
* defaultTypeId: (string) Padrão do tipo das chaves primarias - Apenas necessário para DotNet
* entities: Objeto de configuração das classes de entidades
* endpoints: configuração para criação dos endpoints
* enums: Criação das enumerations
* events: criação de filas no rabbit
* listeners: registra para ser ouvinte de algum evento do rabbit

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
### Events
Não implementado
### Listeners
Não implementado
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
