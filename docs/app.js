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
        currentVersion: '2.1.0',
        activeSection: 'inicio',
        activeLanguage: 'java',
        mobileNavOpen: false,
        searchOpen: false,
        searchQuery: '',
        showChecksums: false,
        observer: null,
        navigation: [
          { label: 'Visão geral', items: [
            { id: 'inicio', label: 'Introdução' },
            { id: 'primeiros-passos', label: 'Primeiros passos' },
            { id: 'configuracao', label: 'Configuração' }
          ]},
          { label: 'Construindo', items: [
            { id: 'entidades', label: 'Entidades e campos' },
            { id: 'relacionamentos', label: 'Relacionamentos' },
            { id: 'endpoints', label: 'Endpoints' },
            { id: 'autorizacao', label: 'Autorização', badge: '2.1' }
          ]},
          { label: 'Referência', items: [
            { id: 'linguagens', label: 'Linguagens' },
            { id: 'rabbitmq', label: 'RabbitMQ' },
            { id: 'filtros', label: 'Filtros CRUD' },
            { id: 'migracao', label: 'Migração 2.1.0' }
          ]},
          { label: 'Obter', items: [
            { id: 'downloads', label: 'Downloads', badge: 'EXE' }
          ]}
        ],
        projectProperties: [
          { name: 'mainPackage', required: true, description: 'Pacote, namespace ou nome base das saídas.' },
          { name: 'projectName', required: true, description: 'Nome do serviço consumidor.' },
          { name: 'language', required: true, description: 'JAVA, DOTNET ou NODE.' },
          { name: 'entities', required: false, description: 'Entidades e campos do domínio.' },
          { name: 'endpoints', required: false, description: 'Operações HTTP customizadas.' },
          { name: 'messaging', required: false, description: 'Canais RabbitMQ de publicação e consumo.' },
          { name: 'authorization', required: false, description: 'Customização da autorização Java.' }
        ],
        snippets: {
          maven: `<plugin>\n  <groupId>com.potatotech</groupId>\n  <artifactId>gonthera-cli</artifactId>\n  <version>2.1.0</version>\n</plugin>`,
          tree: `.gonthera/\n├── project.json\n├── entities.json\n├── endpoints.json\n├── enums.json\n├── messaging.json\n└── authorization.json`,
          commands: `# Maven\nmvn gonthera-cli:validate\nmvn gonthera-cli:generate-sources\n\n# Executável ou JAR\n./gonthera-cli.exe --validate\njava -jar gonthera-cli-2.1.0.jar`,
          project: `{\n  "mainPackage": "com.example.customer",\n  "projectName": "customer-service",\n  "language": "JAVA"\n}`,
          entity: `{\n  "comment": "Cadastro de clientes",\n  "entityName": "customer",\n  "tableName": "customer",\n  "generateDefaultControllers": true,\n  "controllerAbstract": false,\n  "serviceAbstract": false,\n  "onlyDTO": false,\n  "entityFields": [\n    {\n      "comment": "Identificador único",\n      "fieldName": "id",\n      "list": false,\n      "fieldProperties": {\n        "fieldType": "uuid",\n        "required": true,\n        "valueDefault": ""\n      },\n      "metadata": {\n        "nullable": false,\n        "key": true\n      }\n    }\n  ]\n}`,
          endpoint: `{\n  "comment": "Status público do serviço",\n  "methodName": "healthCheck",\n  "httpMethod": "GET",\n  "grouper": "health",\n  "metadata": {\n    "anonymous": true,\n    "input": [],\n    "output": []\n  },\n  "permissions": {\n    "description": "Consulta o status",\n    "resource": "healthCheck",\n    "premissions": ["VIEW"],\n    "permissionDefault": false\n  }\n}`,
          authorization: `{\n  "authenticateAbstract": false,\n  "tenantConfigurationAbstract": false\n}`,
          rabbit: `{\n  "RabbitMq": {\n    "pub": [\n      {\n        "name": "customerChanged",\n        "queue": "customer.changed",\n        "routingKey": "customer.changed"\n      }\n    ],\n    "sub": [\n      {\n        "name": "customerImported",\n        "queue": "customer.imported"\n      }\n    ]\n  }\n}`
        },
        languages: [
          { id: 'java', name: 'Java', kicker: 'Spring + JPA', title: 'Estrutura em camadas e autorização integrada', description: 'Gera controllers, services transacionais, repositories, entidades, DTOs, converters e recursos Spring.', features: ['CRUD com paginação e filtros', 'Autorização e JWT gerados', 'PostgreSQL e RabbitMQ opcional'], output: `src/main/java/com/example/service_gen/\n├── authorization/\n├── controllers/\n├── converters/\n├── dtos/\n├── entities/\n├── repositories/\n└── services/` },
          { id: 'dotnet', name: '.NET', kicker: 'ASP.NET Core + EF Core', title: 'Controllers virtuais e saída C# organizada', description: 'Gera modelos, DTOs, converters, repositories e controllers concretos ou abstratos.', features: ['Actions CRUD virtuais', 'Controllers abstratos opcionais', 'RabbitMQ integrado ao DI'], output: `CustomerService_gen/\n├── Common/\n├── Controllers/\n├── Converters/\n├── Data/\n├── Dtos/\n├── Entities/\n└── Repositories/` },
          { id: 'node', name: 'Node.js', kicker: 'TypeScript + Express + Prisma', title: 'API TypeScript como ponto de partida', description: 'Gera models, enums, repositories, controllers, rotas, endpoints e schema Prisma.', features: ['Rotas Express registráveis', 'Schema Prisma gerado', 'RabbitMQ opcional com amqplib'], output: `src/generated/\n├── controllers/\n├── endpoints/\n├── enums/\n├── models/\n├── repositories/\n└── routes/\nprisma/schema.prisma` }
        ],
        migrationSteps: [
          'Atualize o Gonthera CLI para <code>2.1.0</code>.',
          'Migre imports para <code>&lt;mainPackage&gt;_gen.authorization</code>.',
          'Adicione JJWT <code>0.11.5</code> ao serviço consumidor.',
          'Atualize interceptors para usar o novo <code>TenantConfiguration</code>.',
          'Crie beans concretos quando algum flag <code>*Abstract</code> estiver ativo.',
          'Garanta <code>TenantContext.clear()</code> ao final da requisição.',
          'Regere e compile o serviço consumidor.'
        ],
        downloads: [
          { platform: 'Windows', name: 'gonthera-cli.exe', icon: '⊞', size: '2,7 MB', href: './downloads/2.1.0/gonthera-cli.exe', sha: 'e69751ab32f5ba8f475e31cea0dcd5d64036125b42ed433f8b96a8bebadffe31' },
          { platform: 'Multiplataforma', name: 'gonthera-cli-2.1.0.jar', icon: 'J', size: '2,6 MB', href: './downloads/2.1.0/gonthera-cli-2.1.0.jar', sha: '4606cf759ef0024e36890d171bb0607c6e4aba8da6f4f4ffbbfd703cab43c002' }
        ]
      };
    },
    computed: {
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
      closeSearch() {
        this.searchOpen = false;
      },
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
