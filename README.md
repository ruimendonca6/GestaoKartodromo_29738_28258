# Gestão de Kartódromo

Projeto académico desenvolvido por **Rui Mendonça** e **Alexandre Silva**, com o objetivo de criar um sistema completo para gestão de um kartódromo.

O projeto está organizado em módulos Java, incluindo uma aplicação Desktop e uma aplicação Web.

---

## Membros do Grupo

| Nº Aluno | Nome            | E-mail
| -------- | --------------- | ---------------------------- 
| 29738    | Rui Mendonça    | ruimendonca@ipvc.pt
| 28258    | Alexandre Silva | alexandremiguelsilva@ipvc.pt

---

## Estrutura do Projeto

```text
GestaoKartodromo_29738_28258/
│
├── kartodromo-core/
│   └── Módulo principal com regras de negócio, modelos, DAO e persistência
│
├── kartodromo-desktop/
│   └── Aplicação Desktop desenvolvida em Java Swing
│
├── kartodromo-web/
│   └── Aplicação Web desenvolvida em Java
│
├── data/
│   └── Base de dados local H2
│
├── pom.xml
│   └── Ficheiro Maven principal
│
└── README.md
```

---

## Tecnologias Utilizadas

* Java
* Java Swing
* Maven
* Hibernate ORM
* H2 Database
* Java Web
* Git
* GitHub
* SourceTree
* Trello
* Apache PDFBox

---

## Módulo `kartodromo-core`

Contém a lógica principal da aplicação.

Inclui:

* Entidades/modelos
* Serviços de negócio
* DAO
* Configuração Hibernate
* Persistência de dados

Principais pacotes:

```text
pt.kartodromo.core.bll
pt.kartodromo.core.dal
pt.kartodromo.core.model
pt.kartodromo.core.config
```

---

## Módulo `kartodromo-desktop`

Aplicação desktop desenvolvida em Java Swing.

Funcionalidades implementadas:

* Autenticação de utilizadores
* Gestão de perfis
* Dashboard
* Gestão de clientes
* Gestão de categorias
* Gestão de karts
* Gestão de corridas
* Gestão de reservas
* Gestão de pistas
* Calendário
* Disponibilidade
* Manutenção
* Histórico de corridas
* Estatísticas
* Notificações
* Relatórios PDF
* Perfil do utilizador

---

## Módulo `kartodromo-web`

Módulo destinado à versão web da aplicação.

Estrutura atual:

```text
pt.kartodromo.web.auth
pt.kartodromo.web.config
pt.kartodromo.web.controller
```

Inclui controladores para:

* Login
* Dashboard
* Clientes
* Categorias
* Karts
* Corridas
* Reservas
* Pistas
* Calendário
* Disponibilidade
* Manutenção
* Estatísticas
* Notificações
* Relatórios
* Utilizadores
* Perfil

---

## Base de Dados

O projeto utiliza base de dados local **H2** com persistência em ficheiro.

A base de dados é guardada na pasta:

```text
data/
```

Exemplo de configuração:

```xml
<property name="hibernate.connection.url">
    jdbc:h2:file:./data/kartodromo-db;AUTO_SERVER=TRUE;MODE=PostgreSQL
</property>
```

---

## Branches Principais

### `main`

Versão final estável.

### `develop`

Ambiente principal de desenvolvimento.

### `feature/nome-da-feature`

Branches usadas para desenvolvimento individual de funcionalidades.

Exemplo:

```bash
feature/gestao-clientes
feature/gestao-reservas
feature/dashboard
```

---

## Processo de Trabalho

Recomenda-se o uso do **SourceTree** para facilitar a gestão do Git.

### 1. Criar uma branch a partir de `develop`

```bash
git checkout develop
git pull origin develop
git checkout -b feature/nome-da-feature
```

---

### 2. Trabalhar na funcionalidade

Boas práticas:

* Fazer commits frequentes;
* Usar mensagens claras;
* Testar antes de fazer commit;
* Atualizar a branch com `develop` sempre que necessário.

Exemplo:

```bash
git add .
git commit -m "feat: add reservation management"
```

---

### 3. Enviar a branch para o GitHub

```bash
git push origin feature/nome-da-feature
```

---

### 4. Integrar alterações

Após terminar a funcionalidade:

1. Criar Pull Request para `develop`;
2. Rever alterações;
3. Resolver conflitos;
4. Fazer merge.

---

## Estado Atual do Projeto

### Versão Desktop

Estado: concluída.

Principais funcionalidades implementadas:

* CRUD completo das entidades principais;
* Autenticação persistente;
* Controlo de permissões por perfil;
* Dashboard personalizado;
* Exportação de relatórios PDF;
* Persistência de dados com H2;
* Gestão operacional completa do kartódromo.

### Versão Web

Estado: em desenvolvimento.

---

## Licença

Projeto desenvolvido exclusivamente para fins académicos.
