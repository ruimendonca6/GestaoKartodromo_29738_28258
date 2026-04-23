# GestaoKartodromo_29738_28258

## Membros do grupo

- Rui Mendonça
- Alexandre Silva

## Branches Principais

- `main:` Versão final estável
- `develop:` Ambiente de desenvolvimento
- `feature/nome-da-feature:` Desenvolvimento individual de funcionalidades

## Processo de Trabalho

### Nota: Priorizar o uso do SourceTree nas interações com o Git

### 1. Criar uma branch a partir de `develop`

```bash
git checkout develop
git checkout -b feature/nome-da-feature
```

### 2. Trabalhar na feature

- Fazer commits frequentes
- Utilizar mensagens de commit claras e descritivas

Exemplo:

```bash
git commit -m "Add login validation logic"
```

### 3. Push para GitHub

```bash
git push origin feature/nome-da-feature
```

### 4. Criar Pull Request

- Criar Pull Request para `develop`
- Após revisão, fazer merge para `develop`
- Nunca fazer push direto para `main`.

## Convenção de Commits

- `feat:` Nova funcionalidade  
- `fix:` Correção de bug  
- `refactor:` Melhoria interna
- `docs:` Alterações na documentação  
- `chore:` Tarefas técnicas / mudanças triviais

Commits devem ser feitos em ingles.