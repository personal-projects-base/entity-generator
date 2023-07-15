# entity-generator


Modulo gerador de código fonte

Este modulo faz a geração de models, repositories, DTOS, Classes de eventos e listeners e endpoints 


### Configurações

Deve ser criado um arquivo com nome properties.json na raiz do projeto

após criar o arquivo deve ser inserido as seguintes propriedades:


    "mainPackage": "com.potatotech.entitygenerator",  
    "projectName": "entity-generator",
    "entities": [],
    "endpoints": [],
    "enums": [],
    "events": [],
    "listeners": []

* mainPackage: Nome completo do pacote do projeto
* projectName: Nome do projeto
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
            "relationShip": "OneToOne"
          }
        }
      ]
    },

* entityName: nome da entidade
* tableName: nome da tabela
* classExtends: se extende de alguma outra classe
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
    * fetchType: fetchType do campo: EAGER|LAZY
    * relationShip: relacionamento ex: OneToOne, ManyToOne...

### Endpoints


### Enums


### Events


### Listeners