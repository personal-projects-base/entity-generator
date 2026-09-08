(function () {
  const { createApp, nextTick } = Vue;

  const CodeBlock = {
    props: ['code', 'lang'],
    methods: {
      async copyCode(event) {
        try {
          await navigator.clipboard.writeText(this.code);
          const button = event.currentTarget;
          const previous = button.textContent;
          button.textContent = 'Copiado!';
          window.setTimeout(() => { button.textContent = previous; }, 1400);
        } catch (_) {
          window.prompt('Copie o conteúdo:', this.code);
        }
      }
    },
    template: `
      <div class="code-card">
        <div class="code-card-top"><span>{{ lang }}</span><button type="button" @click="copyCode">Copiar</button></div>
        <pre><code>{{ code }}</code></pre>
      </div>
    `
  };

  createApp({
    components: { CodeBlock },
    data() {
      return {
        documentationVersion: '2.1.4',
        currentVersion: '2.1.4',
        selectedLanguage: 'java',
        activeSection: 'inicio',
        mobileNavOpen: false,
        searchOpen: false,
        searchQuery: '',
        showChecksums: false,
        observer: null,
        languageOptions: [
          { id: 'java', name: 'Java', icon: 'J', stack: 'Spring + JPA' },
          { id: 'node', name: 'Node.js', icon: 'N', stack: 'Express + Prisma' },
          { id: 'dotnet', name: '.NET', icon: '.N', stack: 'ASP.NET + EF Core' }
        ],
        navigation: [
          { label: 'Visão geral', items: [
            { id: 'inicio', label: 'Introdução' },
            { id: 'primeiros-passos', label: 'Primeiros passos' },
            { id: 'configuracao', label: 'Configuração' }
          ]},
          { label: 'Construindo', items: [
            { id: 'entidades', label: 'Entidades e saída' },
            { id: 'relacionamentos', label: 'Relacionamentos' },
            { id: 'crud', label: 'CRUD e respostas' },
            { id: 'consultas', label: 'Filtros e consultas' }
          ]},
          { label: 'Integração', items: [
            { id: 'mensageria', label: 'RabbitMQ' },
            { id: 'seguranca', label: 'Segurança' },
            { id: 'estrutura', label: 'Aplicação e arquivos' }
          ]},
          { label: 'Obter', items: [
            { id: 'downloads', label: 'Downloads', badge: '2.1.4' }
          ]}
        ],
        projectProperties: [
          { name: 'mainPackage', required: true, description: 'Pacote, namespace ou nome base da saída.' },
          { name: 'projectName', required: true, description: 'Nome do serviço consumidor.' },
          { name: 'language', required: true, description: 'Alvo selecionado na documentação.' },
          { name: 'entities', required: false, description: 'Entidades persistidas e DTOs.' },
          { name: 'endpoints', required: false, description: 'Operações HTTP customizadas.' },
          { name: 'messaging', required: false, description: 'Canais RabbitMQ de publicação e consumo.' },
          { name: 'authorization', required: false, description: 'Customização da autorização quando disponível.' }
        ],
        snippets: {
          tree: `.gonthera/
├── project.json
├── entities.json
├── endpoints.json
├── enums.json
├── messaging.json
└── authorization.json`,
          entityContract: `{
  "comment": "Cadastro de clientes",
  "entityName": "customer",
  "tableName": "customer",
  "generateDefaultControllers": true,
  "onlyDTO": false,
  "entityFields": [
    {
      "comment": "Identificador único",
      "fieldName": "id",
      "list": false,
      "fieldProperties": {
        "fieldType": "uuid",
        "required": true,
        "valueDefault": ""
      },
      "metadata": { "nullable": false, "key": true }
    },
    {
      "comment": "Nome",
      "fieldName": "name",
      "list": false,
      "fieldProperties": {
        "fieldType": "string",
        "required": true,
        "valueDefault": ""
      },
      "metadata": { "nullable": false, "key": false }
    }
  ]
}`,
          endpointContract: `{
  "comment": "Consulta resumida do cliente",
  "methodName": "summary",
  "httpMethod": "GET",
  "grouper": "customer",
  "metadata": {
    "anonymous": false,
    "input": [],
    "output": [{
      "fieldName": "name",
      "list": false,
      "fieldProperties": {
        "fieldType": "string",
        "required": true,
        "valueDefault": ""
      }
    }]
  },
  "permissions": {
    "description": "Consulta resumo",
    "resource": "customerSummary",
    "premissions": ["VIEW"],
    "permissionDefault": false
  }
}`,
          relationContract: `{
  "fieldName": "profile",
  "list": false,
  "fieldProperties": {
    "fieldType": "profile",
    "required": false,
    "valueDefault": ""
  },
  "metadata": { "nullable": true, "key": false },
  "relationShips": {
    "fetchType": "LAZY",
    "relationShip": "OneToOne",
    "mappedBy": "customer",
    "bidirectional": true,
    "reference": false
  }
}`,
          relationRequest: `POST /profile
Content-Type: application/json

{
  "bio": "Perfil de teste",
  "customer": {
    "id": "4a1a00c8-78c1-42e5-8e74-4ae6efe1a9ae"
  }
}`,
          relationResponse: `{
  "id": "4a1a00c8-78c1-42e5-8e74-4ae6efe1a9ae",
  "name": "Cliente",
  "profile": {
    "id": "486df3ee-7871-4692-875d-8cae59d7530b",
    "bio": "Perfil de teste",
    "customer": null
  }
}`,
          rabbit: `{
  "RabbitMq": {
    "pub": [{
      "name": "customerChanged",
      "queue": "customer.changed",
      "routingKey": "customer.changed"
    }],
    "sub": [{
      "name": "customerImported",
      "queue": "customer.imported",
      "routingKey": "customer.imported"
    }]
  }
}`
        },
        targetDocs: {
          java: {
            id: 'java', name: 'Java', icon: 'J', stack: 'Spring Boot + JPA',
            hero: 'Gere uma API Spring em camadas, com persistência, contratos e segurança.',
            command: 'mvn gonthera-cli:generate-sources',
            highlights: [
              { title: 'Arquitetura em camadas', text: 'Controller, Service transacional, Repository, DTO e converter.' },
              { title: 'JPA maduro', text: 'Relacionamentos bidirecionais, autorrelações, filtros e projeções.' },
              { title: 'Segurança integrada', text: 'JWT, permissões, endpoints anônimos e contexto de tenant.' }
            ],
            installTitle: 'Configure o plugin Maven', installText: 'Adicione o repositório de leitura e fixe a versão publicada no pom.xml.', installLang: 'xml',
            install: `<repositories>
  <repository>
    <id>myMavenRepo.read</id>
    <url>https://mymavenrepo.com/repo/go9Ye7KC7xaSZHqFec9g/</url>
  </repository>
</repositories>
<pluginRepositories>
  <pluginRepository>
    <id>myMavenRepo.read</id>
    <url>https://mymavenrepo.com/repo/go9Ye7KC7xaSZHqFec9g/</url>
  </pluginRepository>
</pluginRepositories>
<build>
  <plugins>
    <plugin>
      <groupId>com.gonthera</groupId>
      <artifactId>gonthera-cli</artifactId>
      <version>2.1.4</version>
    </plugin>
  </plugins>
</build>`,
            commands: `mvn gonthera-cli:validate
mvn gonthera-cli:generate-sources

# Alternativa com o JAR
java -jar gonthera-cli-2.1.4.jar --validate
java -jar gonthera-cli-2.1.4.jar`,
            project: `{
  "mainPackage": "com.example.customer",
  "projectName": "customer-service",
  "language": "JAVA"
}`,
            entityTitle: 'Entidade JPA gerada', entityText: 'Campos do contrato viram propriedades tipadas, anotações JPA e métodos Lombok.', entityLang: 'java',
            entityCode: `@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "customer")
public class CustomerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL,
              orphanRemoval = true, fetch = FetchType.LAZY)
    private ProfileEntity profile;
}`,
            output: `src/main/java/com/example/customer_gen/
├── authorization/
├── common/
├── controllers/
├── converters/
├── dtos/
├── endpoints/
├── entities/
├── enums/
├── messaging/
├── repositories/
└── services/`,
            relationLang: 'java', relationTitle: 'Relação JPA no proprietário',
            relationCode: `@OneToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "customer")
private CustomerEntity customer;

// No lado inverso
@OneToOne(mappedBy = "customer", cascade = CascadeType.ALL,
          orphanRemoval = true, fetch = FetchType.LAZY)
private ProfileEntity profile;`,
            relationNotes: ['bidirectional: false define o proprietário e gera a FK.', 'mappedBy aponta para o campo proprietário.', 'reference controla como o converter percorre a relação.', 'OneToMany, ManyToOne, ManyToMany e autorrelações são suportados.'],
            crudLang: 'java', crudTitle: 'Service transacional gerado',
            crudCode: `@Service
public class CustomerService {
    @Autowired protected CustomerRepository repository;
    @Autowired protected CustomerDTOConverter dtoConverter;

    @Transactional
    public CustomerDTO save(CustomerDTO input) {
        var entity = dtoConverter.toEntity(input, null);
        return dtoConverter.toDTO(repository.save(entity), null);
    }

    @Transactional(readOnly = true)
    public ResponseData getAll(RequestData input) {
        // paginação, order, SpecificationFilter e displayFields
    }
}`,
            crudNotes: ['POST e PUT recebem DTOs, sem operações JPA no corpo HTTP.', 'Controllers delegam transação e persistência ao Service.', 'GET, POST e PUT retornam DTOs convertidos.', 'controllerAbstract e serviceAbstract permitem especialização externa.'],
            endpointLang: 'java', endpointTitle: 'Contrato Java gerado',
            endpointCode: `public interface CustomerEndpoint {
    @GetMapping("/customer/summary")
    CustomerSummaryOutput summary();
}

// metadata.anonymous: true adiciona @Anonymous ao método.`,
            endpointNotes: ['input e output devem sempre existir no contrato.', 'O gerador aceita endpoints GET e POST.', 'grouper organiza métodos relacionados.', 'Permissões e @Anonymous integram o endpoint à segurança gerada.'],
            queryLang: 'http',
            queryExample: `GET /customer?size=20&offset=1
  &filter=name eq geo and profile notNull
  &order=name,asc
  &displayFields=id;name;profile.bio`,
            queryNotes: ['eq em String significa contém sem diferenciar caixa; UUID usa igualdade.', 'Datas aceitam gte/ge e lte/le em ISO.', 'Caminhos relacionados usam ponto, como profile.bio.', 'Não misture and e or; não há parênteses, escape ou in.'],
            messagingLang: 'java', messagingTitle: 'Spring AMQP gerado',
            messagingCode: `@RabbitExchange("customer.events")
@Configuration
public class AppRabbitConfig extends RabbitConfig {}

@Autowired
private CustomerChangedPub customerChangedPub;

customerChangedPub.publish(customerDto);`,
            messagingNotes: ['A exchange é declarada com @RabbitExchange.', 'Publishers e subscribers usam Spring AMQP.', 'Nenhuma classe RabbitMQ é gerada quando a configuração está vazia.'],
            securityTitle: 'Autorização Java integrada', securityText: 'O alvo Java gera sua própria infraestrutura de JWT, permissões, anotações e tenant.', securityLang: 'json',
            securityCode: `{
  "authenticateAbstract": false,
  "tenantConfigurationAbstract": false
}

// Variável exigida pelo comportamento padrão
SECRET_JWT=uma-chave-hmac-segura`,
            securityNotes: ['@Anonymous libera endpoints configurados como anônimos.', 'Authenticate possui hooks protegidos para customização.', 'Use TenantContext.clear() em finally ao terminar a requisição.', 'Adicione jjwt-api, jjwt-impl e jjwt-jackson 0.11.5.'],
            runtimeTitle: 'Use as classes geradas pelo Spring', runtimeText: 'A aplicação consumidora fornece suas configurações e especializações fora de _gen.', runtimeLang: 'java',
            runtimeCode: `@SpringBootApplication
public class CustomerApplication {
    public static void main(String[] args) {
        SpringApplication.run(CustomerApplication.class, args);
    }
}`,
            environmentTitle: 'application.yml e ambiente', environmentLang: 'yaml',
            environmentCode: `spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/customer
    username: customer
    password: \${DATABASE_PASSWORD}

# Usado pela autenticação gerada
SECRET_JWT: \${SECRET_JWT}`,
            generatedFiles: ['Entidades, DTOs e converters', 'Repositories e services', 'Controllers e endpoints', 'Autorização e RabbitMQ quando configurados'],
            manualFiles: ['Configuração da aplicação Spring', 'Migrations incrementais', 'Implementações concretas de classes abstratas', 'Interceptors e regras específicas do negócio'],
            caveat: 'O código Java gerado usa Jakarta e Spring 6; o serviço consumidor normalmente executa em Java 17 ou superior.'
          },
          node: {
            id: 'node', name: 'Node.js', icon: 'N', stack: 'TypeScript + Express + Prisma',
            hero: 'Gere uma API TypeScript com CRUD Prisma, relações e respostas padronizadas.',
            command: 'npm run gonthera-cli',
            highlights: [
              { title: 'Contrato previsível', text: 'CRUD, filtros, paginação e projeção seguem o padrão HTTP do projeto.' },
              { title: 'Banco selecionável', text: 'Schema PostgreSQL ou MongoDB com o mesmo contrato HTTP e relações bidirecionais.' },
              { title: 'Base extensível', text: 'Configuração abstrata, Express 5, Swagger e RabbitMQ recuperável.' }
            ],
            release: {
              title: 'Novo na 2.1.4 — MongoDB no gerador Node',
              text: 'A seleção do banco agora acontece em database.provider sem alterar rotas, DTOs, filtros, paginação, projeção ou OpenAPI.',
              items: [
                'PostgreSQL continua como padrão; MONGODB ativa datasource, tipos, relações e transações próprios.',
                'UUID textual é armazenado em _id, sem expor ObjectId no contrato HTTP.',
                'ManyToMany usa arrays internos de IDs e foi validado em MongoDB real com Customer e Tag.',
                'isNull também encontra campos ausentes; notNull exige campo presente e não nulo.',
                'MongoDB usa prisma db push, não gera postgree.sql e exige replica set para as transações Prisma.'
              ]
            },
            installTitle: 'Execute o JAR pelo npm', installText: 'Coloque o JAR em um caminho estável e registre os scripts no package.json.', installLang: 'json',
            install: `{
  "scripts": {
    "gonthera-cli": "java -jar ./tools/gonthera-cli-2.1.4.jar",
    "gonthera-validate": "java -jar ./tools/gonthera-cli-2.1.4.jar --validate",
    "prisma:validate": "prisma validate",
    "prisma:generate": "prisma generate",
    "prisma:migrate": "prisma migrate dev",
    "prisma:push": "prisma db push",
    "dev": "tsx watch src/server.ts",
    "build": "prisma generate && tsc -p tsconfig.json",
    "start": "node dist/server.js",
    "typecheck": "tsc -p tsconfig.json --noEmit"
  },
  "dependencies": {
    "@prisma/client": "5.22.0",
    "amqp-connection-manager": "^5.0.0",
    "amqplib": "^2.0.1",
    "dotenv": "16.4.7",
    "express": "^5.2.1",
    "swagger-ui-express": "^5.0.1"
  },
  "devDependencies": {
    "@types/express": "^5.0.6",
    "@types/node": "22.10.2",
    "@types/swagger-ui-express": "^4.1.8",
    "prisma": "5.22.0",
    "tsx": "4.20.6",
    "typescript": "5.7.2"
  }
}`,
            commands: `npm install
npm run gonthera-validate
npm run gonthera-cli
npm run prisma:validate
npm run prisma:generate
# PostgreSQL: npm run prisma:migrate -- --name initial
# MongoDB: npm run prisma:push
npm run dev`,
            project: `{
  "mainPackage": "com.example.customer",
  "projectName": "customer-service",
  "language": "NODE",
  "database": {
    "provider": "MONGODB"
  }
}`,
            entityTitle: 'DTO TypeScript e schema Prisma MongoDB', entityText: 'O DTO representa o mesmo contrato HTTP nos dois bancos; este schema Mongo mostra os IDs internos que não chegam à API.', entityLang: 'typescript / prisma',
            entityCode: `export interface CustomerDTO {
  id: string;
  name: string;
  profile?: ProfileDTO;
  purchases: PurchaseDTO[];
  tags: TagDTO[];
}

model Customer {
  id        String     @id @default(uuid()) @map("_id")
  name      String
  profile   Profile?
  purchases Purchase[]
  tags      Tag[]      @relation("Customer_Tag_tags", fields: [tagsIds], references: [id])
  tagsIds   String[]   @map("tags_ids")
}`,
            output: `src/generated/
├── common/
├── configuration/database/
├── controllers/
├── converters/
├── documentation/
├── endpoints/
├── enums/
├── messaging/rabbitmq/
├── models/
├── repositories/
└── routes/
prisma/schema.prisma`,
            relationLang: 'prisma', relationTitle: 'OneToOne MongoDB proprietário e inverso',
            relationCode: `model Profile {
  id         String    @id @default(uuid()) @map("_id")
  bio        String?
  customer   Customer  @relation("Profile_Customer_customer", fields: [customerId], references: [id])
  customerId String    @unique @map("customer")
}

model Customer {
  id      String   @id @default(uuid()) @map("_id")
  profile Profile? @relation("Profile_Customer_customer")
}`,
            relationNotes: ['Envie customer: { id } em vez de customerId.', 'reference: true conecta um registro existente.', 'O campo recíproco imediato volta como null para cortar ciclos.', 'OneToMany substitui filhos enviados; ManyToMany substitui somente vínculos.', 'No MongoDB, arrays como tagsIds são internos e não aparecem na API.', 'O ciclo Customer.tags / Tag.customers foi validado com associação, leitura bidirecional, desvínculo e preservação da Tag.', 'OneToOne, ManyToMany e autorrelações são validados antes da geração.'],
            crudLang: 'json / typescript', crudTitle: 'Controller abstrato com CRUD funcional',
            crudCode: `// entities.json
{
  "entityName": "customer",
  "generateDefaultControllers": true,
  "controllerAbstract": true
}

// src/generated/controllers/customer.controller.ts
export abstract class CustomerController {
  constructor(protected readonly repository: CustomerRepository) {}

  async save(request: Request, response: Response): Promise<void> {
    response.json(await this.repository.save(request.body));
  }

  async get(request: Request, response: Response): Promise<void> {
    const fields = queryString(request.query.displayFields, 'displayFields');
    response.json(await this.repository.get(request.params.id, fields));
  }
}`,
            crudNotes: ['controllerAbstract: true exige uma factory concreta e mantém todas as rotas geradas.', 'A subclasse sobrescreve somente os métodos necessários e pode chamar super.', 'Factories de controllers concretos são opcionais.', 'generateDefaultControllers: false continua removendo controller e rota.', 'Repositories executam conversão e persistência relacionada em transações Prisma.', 'A rota continua /:id mesmo quando a chave configurada possui outro nome.'],
            endpointLang: 'typescript', endpointTitle: 'Contrato e rota TypeScript',
            endpointCode: `export interface CustomerSummaryOutput {
  name: string;
}

router.get('/customer/summary', async (_request, response) => {
  const output: CustomerSummaryOutput = await summary();
  response.json(output);
});`,
            endpointNotes: ['O Gonthera gera os tipos de input/output, a estrutura de rota e o contrato OpenAPI.', 'Implemente a regra de negócio fora de src/generated.', 'anonymous e permissões são preservados como metadados OpenAPI; o middleware pertence à aplicação.', 'Registre overrides antes das rotas CRUD geradas.'],
            queryLang: 'http / json',
            queryExample: `GET /customer?size=20&offset=1
  &filter=name eq geo and profile notNull
  &order=name,asc
  &displayFields=id;name;profile.bio

{
  "size": 20,
  "offset": 0,
  "total": 1,
  "contents": []
}`,
            queryNotes: ['eq converte texto, UUID, enum, número e booleano para o Prisma.', 'Datas aceitam gte/ge e lte/le em ISO.', 'Relações usam ponto; coleções aceitam ponto ou *.', 'No MongoDB, isNull inclui campos ausentes e notNull exige campo presente.', 'A entrada offset=1 representa a primeira página e a saída é zero-based.', 'Não misture and e or; não há parênteses ou escape.'],
            messagingLang: 'typescript', messagingTitle: 'Conexão e canais recuperáveis',
            messagingCode: `export class AppRabbitConfig extends RabbitConfig {
  constructor() {
    super({
      exchange: process.env.RABBITMQ_EXCHANGE!,
      url: process.env.RABBITMQ_URL,
      prefetch: 10,
      requeueOnError: false
    });
  }
}

await rabbit.connect();
await customerChangedPub.publish(customerDto);`,
            messagingNotes: ['amqp-connection-manager compartilha e recupera a conexão.', 'Publishers usam confirmação e buffer durante reconexão.', 'Subscribers fazem ack após sucesso e nack sem requeue por padrão.', 'Feche listeners, canais e conexão no shutdown.'],
            securityTitle: 'Segurança pertence à aplicação', securityText: 'O gerador Node ainda não cria autenticação, JWT, tenant ou permissões.', securityLang: 'typescript',
            securityCode: `// Registre segurança antes das rotas geradas.
app.use(authenticationMiddleware);
app.use(authorizationMiddleware);
app.use(createGeneratedRoutes(database.client, controllerFactories));`,
            securityNotes: ['Mantenha middlewares fora de src/generated.', 'Registre rotas customizadas antes das rotas geradas ao sobrescrever um caminho.', 'O Gonthera gera o OpenAPI; a aplicação monta o Swagger UI e pode estender segurança, servidores e paths.'],
            runtimeTitle: 'Conecte o Prisma antes de abrir a porta', runtimeText: 'Estenda DatabaseConfig fora da geração e compartilhe a mesma instância com todas as rotas.', runtimeLang: 'typescript',
            runtimeCode: `import { DatabaseConfig } from './generated/configuration/database/database.config';
import { CustomerController } from './generated/controllers/customer.controller';
import { createGeneratedRoutes, type GeneratedControllerFactories } from './generated/routes';

class AppDatabaseConfig extends DatabaseConfig {}
const database = new AppDatabaseConfig();

class AppCustomerController extends CustomerController {
  override async save(request: Request, response: Response): Promise<void> {
    // regra específica antes ou depois do CRUD padrão
    await super.save(request, response);
  }
}

const controllerFactories = {
  customer: repository => new AppCustomerController(repository)
} satisfies GeneratedControllerFactories;

await database.connect();
const app = express();
app.use(express.json());
app.use(createGeneratedRoutes(database.client, controllerFactories));
const server = app.listen(process.env.PORT ?? 3000);

// Em SIGINT/SIGTERM:
server.close(async () => database.disconnect());`,
            environmentTitle: '.env', environmentLang: 'dotenv',
            environmentCode: `# PostgreSQL
DATABASE_URL="postgresql://user:password@localhost:5432/customer?schema=public"

# MongoDB autenticado e configurado como replica set
# authSource define o banco de autenticação; replicaSet habilita as transações.
# DATABASE_URL="mongodb://user:password@localhost:27017/customer?authSource=admin&replicaSet=rs0"
HOST=127.0.0.1
PORT=3000

RABBITMQ_ENABLED=false
RABBITMQ_URL=amqp://guest:guest@localhost:5672
RABBITMQ_EXCHANGE=customer.events`,
            generatedFiles: ['DTOs, metadados e converters', 'Repositories, controllers e todas as rotas', 'OpenAPI com CRUD, schemas e endpoints', 'Contrato tipado GeneratedControllerFactories', 'DatabaseConfig abstrata', 'schema.prisma PostgreSQL ou MongoDB', 'SQL somente no PostgreSQL', 'RabbitMQ quando configurado'],
            manualFiles: ['package.json e tsconfig.json', '.env e migrations Prisma', 'Implementações concretas e registro de factories', 'AppDatabaseConfig, app.ts e server.ts', 'Middleware de erros, montagem do Swagger UI, extensões OpenAPI e autenticação'],
            caveat: 'MongoDB exige chaves UUID e um servidor replica set. authSource não substitui replicaSet: use prisma migrate no PostgreSQL e prisma db push no MongoDB.'
          },
          dotnet: {
            id: 'dotnet', name: '.NET', icon: '.N', stack: 'ASP.NET Core + Entity Framework Core',
            hero: 'Gere uma API C# organizada com controllers, DTOs e persistência Entity Framework.',
            command: 'java -jar gonthera-cli-2.1.4.jar',
            highlights: [
              { title: 'Saída C# organizada', text: 'Entidades, DTOs, converters, repositories e controllers.' },
              { title: 'Controllers extensíveis', text: 'Actions virtuais e controllers abstratos opcionais.' },
              { title: 'Integrações opcionais', text: 'Entity Framework Core e RabbitMQ com registro em DI.' }
            ],
            installTitle: 'Execute o Gonthera na raiz', installText: 'Use o executável Windows ou o JAR multiplataforma ao lado da pasta .gonthera.', installLang: 'powershell',
            install: `.\\gonthera-cli.exe --validate
.\\gonthera-cli.exe

# Alternativa multiplataforma
java -jar gonthera-cli-2.1.4.jar --validate
java -jar gonthera-cli-2.1.4.jar`,
            commands: `.\\gonthera-cli.exe --validate
.\\gonthera-cli.exe
dotnet restore
dotnet build
dotnet run`,
            project: `{
  "mainPackage": "CustomerService",
  "projectName": "CustomerService",
  "language": "DOTNET"
}`,
            entityTitle: 'Entidade do Entity Framework', entityText: 'O contrato gera propriedades C# e atributos de persistência.', entityLang: 'csharp',
            entityCode: `[Table("customer")]
public class CustomerEntity
{
    [Key]
    public Guid Id { get; set; }

    [Required]
    public string Name { get; set; }

    public virtual ProfileEntity? Profile { get; set; }
}`,
            output: `CustomerService_gen/
├── Common/
├── Controllers/
├── Converters/
├── Data/
├── Dtos/
├── Endpoints/
├── Entities/
├── Enums/
├── Messaging/
└── Repositories/`,
            relationLang: 'csharp', relationTitle: 'Navegações do Entity Framework',
            relationCode: `public virtual CustomerEntity Customer { get; set; }
public Guid CustomerId { get; set; }

// No lado inverso
public virtual ProfileEntity? Profile { get; set; }`,
            relationNotes: ['O contrato define proprietário, inverso, mappedBy e coleções.', 'O DbContext gerado registra as entidades.', 'Revise namespaces e infraestrutura específica da aplicação consumidora.'],
            crudLang: 'csharp', crudTitle: 'Controller ASP.NET gerado',
            crudCode: `[ApiController]
[Route("customer")]
public class CustomerController : ControllerBase
{
    [HttpGet]
    public virtual ActionResult<ResponseData> GetAll([FromQuery] RequestData input)
        => Ok(_customerRepository.GetAll(input));

    [HttpPost]
    public virtual ActionResult<CustomerDTO> Save([FromBody] CustomerDTO input)
        => Ok(CustomerDTOConverter.ToDTO(
            _customerRepository.Add(CustomerDTOConverter.ToEntity(input))));
}`,
            crudNotes: ['Actions CRUD são virtuais.', 'controllerAbstract permite fornecer uma implementação externa.', 'DTOConverter separa o contrato HTTP da entidade persistida.', 'A camada Service separada ainda é exclusiva do Java.'],
            endpointLang: 'csharp', endpointTitle: 'Action ASP.NET gerada',
            endpointCode: `[HttpGet("customer/summary")]
public virtual ActionResult<CustomerSummaryOutput> Summary()
{
    return Ok(new CustomerSummaryOutput());
}`,
            endpointNotes: ['grouper organiza os endpoints gerados.', 'input e output definem os contratos C#.', 'Endpoints anônimos podem receber AllowAnonymous.', 'Implemente integrações específicas no projeto consumidor.'],
            queryLang: 'http',
            queryExample: `GET /customer?size=20&offset=1
  &filter=name eq geo
  &displayFields=id;name`,
            queryNotes: ['DynamicFilter suporta eq e uma família lógica por expressão.', 'Texto usa Contains sem diferenciar caixa; UUID é exato.', 'Coleções usam *, como children*.description eq matriz.', 'Não há isNull, notNull ou operadores de intervalo documentados.'],
            messagingLang: 'csharp', messagingTitle: 'RabbitMQ registrado no DI',
            messagingCode: `[RabbitExchange("customer.events")]
public class AppRabbitConfig : RabbitConfig { }

// No bootstrap da aplicação
services.AddRabbitMessaging(configuration);`,
            messagingNotes: ['Publishers e subscribers são gerados quando há canais.', 'A exchange é definida pelo atributo RabbitExchange.', 'A aplicação consumidora registra a integração no container de DI.'],
            securityTitle: 'Segurança integrada ainda é parcial', securityText: 'O alvo .NET não gera o mesmo conjunto próprio de JWT, tenant e permissões do alvo Java.', securityLang: 'csharp',
            securityCode: `builder.Services.AddAuthentication();
builder.Services.AddAuthorization();

app.UseAuthentication();
app.UseAuthorization();`,
            securityNotes: ['Use a infraestrutura ASP.NET Core do serviço consumidor.', 'Endpoints anônimos gerados podem usar AllowAnonymous.', 'Mantenha políticas e handlers fora do diretório regenerado.'],
            runtimeTitle: 'Registre o contexto e os repositories', runtimeText: 'A aplicação consumidora fornece connection string, migrations e composição do runtime.', runtimeLang: 'csharp',
            runtimeCode: `builder.Services.AddDbContext<CustomDbContext>(options =>
    options.UseNpgsql(connectionString));

builder.Services.AddControllers();
var app = builder.Build();
app.MapControllers();
app.Run();`,
            environmentTitle: 'appsettings.json', environmentLang: 'json',
            environmentCode: `{
  "ConnectionStrings": {
    "Default": "Host=localhost;Database=customer;Username=customer;Password=..."
  },
  "RabbitMq": {
    "Host": "localhost",
    "Exchange": "customer.events"
  }
}`,
            generatedFiles: ['Entidades, DTOs e converters', 'Repositories e controllers', 'CustomDbContext e auxiliares de DI', 'Endpoints e RabbitMQ opcionais'],
            manualFiles: ['Projeto .csproj e pacotes NuGet', 'Connection string e migrations', 'Bootstrap ASP.NET Core', 'Autenticação e regras do domínio'],
            caveat: 'Os diretórios organizam fisicamente a saída, mas vários templates ainda usam o namespace raiz compartilhado e referências específicas da infraestrutura consumidora.'
          }
        },
        downloads: [
          { platform: 'Windows', name: 'gonthera-cli.exe', icon: '⊞', size: '2,7 MB', href: './downloads/2.1.4/gonthera-cli.exe', sha: '4f7b53025bcc73f9b729fc1b1e74d88e59bba30e8e4a9c0d2c10edce2ec60e28' },
          { platform: 'Multiplataforma', name: 'gonthera-cli-2.1.4.jar', icon: 'J', size: '2,6 MB', href: './downloads/2.1.4/gonthera-cli-2.1.4.jar', sha: '605ea85ad7f07e553e1d81b32285e415341c3686b0d55626d0ea705331aa2c7f' }
        ]
      };
    },
    computed: {
      target() { return this.targetDocs[this.selectedLanguage]; },
      flatNavigation() {
        return this.navigation.flatMap(group => group.items.map(item => ({ ...item, group: group.label })));
      },
      searchResults() {
        const query = this.normalize(this.searchQuery);
        if (!query) return [];
        return this.flatNavigation.filter(item => {
          const section = document.getElementById(item.id);
          return this.normalize(`${item.label} ${item.group} ${section ? section.dataset.title || section.textContent : ''}`).includes(query);
        }).slice(0, 8);
      }
    },
    mounted() {
      this.setupObserver();
      window.addEventListener('keydown', this.handleShortcut);
      if (window.location.hash) this.activeSection = window.location.hash.slice(1);
    },
    beforeUnmount() {
      if (this.observer) this.observer.disconnect();
      window.removeEventListener('keydown', this.handleShortcut);
    },
    methods: {
      normalize(value) {
        return String(value || '').normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase();
      },
      selectLanguage(language) {
        this.selectedLanguage = language;
        this.mobileNavOpen = false;
      },
      setupObserver() {
        this.observer = new IntersectionObserver(entries => {
          const visible = entries.filter(entry => entry.isIntersecting).sort((a, b) => b.intersectionRatio - a.intersectionRatio);
          if (visible[0]) this.activeSection = visible[0].target.id;
        }, { rootMargin: '-18% 0px -62% 0px', threshold: [0, 0.1, 0.3] });
        document.querySelectorAll('.doc-section').forEach(section => this.observer.observe(section));
      },
      handleShortcut(event) {
        if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'k') {
          event.preventDefault();
          this.openSearch();
        }
        if (event.key === 'Escape') this.closeSearch();
      },
      openSearch() {
        this.searchOpen = true;
        this.searchQuery = '';
        nextTick(() => this.$refs.searchInput && this.$refs.searchInput.focus());
      },
      closeSearch() { this.searchOpen = false; },
      async copy(text, event) {
        try {
          await navigator.clipboard.writeText(text);
          const button = event.currentTarget;
          button.textContent = 'Copiado!';
          window.setTimeout(() => { button.textContent = 'Copiar'; }, 1400);
        } catch (_) {
          window.prompt('Copie o comando:', text);
        }
      }
    }
  }).mount('#app');
})();
