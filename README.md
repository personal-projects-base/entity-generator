# entity-generator


Modulo gerador de código fonte

Este modulo faz a geração de models, repositories, DTOS, Classes de eventos e listeners e endpoints 

[UI Criação entidades](https://personal-projects-base.github.io/entity-generator/)


### Configurações

Deve ser criado um arquivo com nome properties.json na raiz do projeto

após criar o arquivo deve ser inserido as seguintes propriedades:

##### Java

    "mainPackage": "com.potatotech.entitygenerator",  
    "projectName": "entity-generator",
    "entities": [],
    "endpoints": [],
    "enums": [],
    "events": [],
    "listeners": []

##### DotNet

    "mainPackage": "EntityGenerator",  
    "projectName": "EntityGenerator",
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
      "generateDefaultHandlers": false,
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
          },
	        "frontendProperties": {
            "label": "",
            "size": 0,
            "hidden": true,
            "order": 0,
            "guidance": "",
            "reference": "",
            "enableFieldsFilter": false
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
  * frontendProperties: configurações do campo para o frontend
    * label: (string) - Texto da label default (fieldName)
    * size: (int) tamanho do campo
    * hidden: (boolean) - Se é pra ser oculto ou não
    * order: (int) - ordem do campo na tela
    * guidance: (string) orientação 
    * reference: não lembro quando criei
    * enableFieldsFilter: (boolean) - Se é um campo habilitado a filtros
  * relationShips: Configurações de relacionamento
    * fetchType: (string) fetchType do campo: EAGER|LAZY
    * relationShip: (string) relacionamento ex: OneToOne, ManyToOne...
    * bidirectional: (boolean) se é uma classe que terá um relacionamento bidirecional

### Endpoints

Neste objeto deverá ser implementado os endpoints que deseja ser gerado

abaixo um exemplo da sintaxe:

    {
      "methodName": "listCity",
      "httpMethod": "POST",
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
        ],
        "anonymous": true
      }
    }

* methodName: nome do endpoint
* httpMethod: metodo do endpoint (GET,POST,PUT,DELETE) atualmente só possui suporte ao POST
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


### Events


### Listeners

### OBS:
  * Para geração correta dos arquivos estaticos para DotNet deve possuir a pasta "static"