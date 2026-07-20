# Regras de versão e documentação

Estas regras se aplicam a qualquer alteração realizada no Gonthera CLI.

## Changelog

- Toda alteração deve atualizar o `CHANGELOG.md` na mesma entrega.
- A entrada deve explicar objetivamente o comportamento alterado e ser registrada na seção adequada, como correção, nova funcionalidade, melhoria, quebra de compatibilidade ou atualização de dependência.
- Correções diferentes não devem ser adicionadas posteriormente a uma versão de correção já fechada. Cada nova correção recebe uma versão própria.

## Handoffs

- Toda nova funcionalidade ou mudança de contrato/comportamento deve atualizar os handoffs afetados na mesma entrega.
- Atualize `docs/handoff/gonthera-cli-project/HANDOFF.md` quando mudar o comportamento do gerador, da configuração, das saídas ou das integrações de backend.
- Atualize `docs/handoff/HANDOFF_FRONTEND.md` quando mudar o contrato JSON ou o comportamento consumido pelo frontend.
- Uma correção que apenas restaura o comportamento já documentado não exige alteração no handoff, mas continua exigindo changelog e nova versão.

## Versionamento

- Toda alteração publicável deve incrementar a versão do projeto no `pom.xml`.
- Cada correção deve gerar uma versão distinta. Não acumule correções novas em uma versão de correção já fechada.
- Toda quebra de compatibilidade deve gerar uma versão distinta e ser destacada no changelog e nos handoffs afetados.
- Funcionalidades podem ser acumuladas em uma mesma versão ainda não fechada.
- Antes de iniciar ou concluir um novo pacote de funcionalidades, pergunte ao responsável se deve ser criada uma nova versão naquele momento ou se as funcionalidades devem continuar acumuladas na versão em desenvolvimento.
- Atualizações de changelog, handoff, testes e documentação que fazem parte da mesma alteração não provocam incrementos adicionais: elas acompanham a versão dessa alteração.

## Checklist obrigatório

Antes de considerar qualquer alteração concluída:

1. Confirmar que a versão no `pom.xml` foi incrementada conforme o tipo da entrega.
2. Atualizar o `CHANGELOG.md`.
3. Atualizar os handoffs aplicáveis quando houver funcionalidade nova ou mudança de contrato/comportamento.
4. Confirmar, para pacotes de funcionalidades, se a decisão é lançar uma nova versão ou continuar acumulando.
5. Executar os testes proporcionais à alteração e registrar o resultado na entrega.
