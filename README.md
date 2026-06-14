# Gestão de Kartódromo

Projeto académico desenvolvido por **Rui Mendonça (29738)** e **Alexandre Silva (28258)** no âmbito da unidade curricular **Projeto II**, com o objetivo de criar um sistema completo para gestão operacional de um kartódromo.

O projeto foi desenvolvido utilizando uma arquitetura modular baseada em Maven, sendo composto por uma camada de negócio reutilizável (**kartodromo-core**), uma aplicação Desktop (**kartodromo-desktop**) e uma aplicação Web (**kartodromo-web**).

---

# Membros do Grupo

| Nº Aluno | Nome            | E-mail                                                              |
| -------- | --------------- | ------------------------------------------------------------------- |
| 29738    | Rui Mendonça    | [ruimendonca@ipvc.pt](mailto:ruimendonca@ipvc.pt)                   |
| 28258    | Alexandre Silva | [alexandremiguelsilva@ipvc.pt](mailto:alexandremiguelsilva@ipvc.pt) |

---

# Estrutura do Projeto

```text
GestaoKartodromo_29738_28258/
│
├── kartodromo-core/
│   ├── model/
│   ├── dal/
│   ├── bll/
│   └── config/
│
├── kartodromo-desktop/
│   ├── ui/
│   ├── auth/
│   └── relatorios/
│
├── kartodromo-web/
│   ├── auth/
│   ├── config/
│   ├── controller/
│   ├── templates/
│   ├── static/css/
│   ├── static/js/
│   └── static/images/
│
├── data/
│   └── kartodromo-db.mv.db
│
├── pom.xml
│
└── README.md
```

---

# Tecnologias Utilizadas

## Linguagens

* Java 21
* HTML5
* CSS3
* JavaScript

## Frameworks e Bibliotecas

* Spring Boot
* Thymeleaf
* Hibernate ORM
* Jakarta Persistence
* Apache PDFBox

## Base de Dados

* H2 Database

## Ferramentas

* Maven
* Git
* GitHub
* SourceTree
* Trello
* Visual Studio Code
* IntelliJ IDEA

---

# Módulo kartodromo-core

Contém toda a lógica de negócio partilhada entre as aplicações Desktop e Web.

## Principais funcionalidades

* Entidades JPA/Hibernate
* Regras de negócio
* Persistência de dados
* DAOs genéricos e específicos
* Serviços de negócio
* Configuração Hibernate

## Entidades

* Cliente
* Utilizador
* CategoriaKart
* Kart
* Reserva
* Corrida
* Resultado
* Pista
* Manutencao

## Serviços

* ClienteService
* UtilizadorService
* CategoriaKartService
* KartService
* ReservaService
* CorridaService
* ResultadoService
* PistaService
* ManutencaoService
* NotificacaoService

---

# Módulo kartodromo-desktop

Aplicação Desktop desenvolvida em Java Swing.

## Funcionalidades Implementadas

### Autenticação

* Login
* Controlo de permissões
* Perfis ADMIN, FUNCIONARIO e CLIENTE

### Gestão

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
* Resultados
* Estatísticas
* Notificações
* Relatórios PDF
* Utilizadores
* Perfil

### Estatísticas

* Receita por período
* Corridas realizadas
* Taxa de cancelamento
* Taxa de ocupação das pistas

---

# Módulo kartodromo-web

Aplicação Web desenvolvida com Spring Boot e Thymeleaf.

## Estrutura

```text
pt.kartodromo.web.auth
pt.kartodromo.web.config
pt.kartodromo.web.controller
```

## Controladores Implementados

* LoginController
* DashboardController
* ClienteController
* CategoriaController
* KartController
* CorridaController
* ReservaController
* PistaController
* CalendarioController
* DisponibilidadeController
* ManutencaoController
* ResultadoController
* NotificacaoController
* EstatisticasController
* RelatoriosController
* UtilizadoresController
* PerfilController

## Funcionalidades Web

### Dashboard

* Indicadores operacionais
* Resumo diário
* Estatísticas rápidas

### Gestão Operacional

* Clientes
* Categorias
* Karts
* Corridas
* Reservas
* Pistas
* Manutenção
* Resultados
* Utilizadores

### Planeamento

* Calendário
* Disponibilidade

### Relatórios e Estatísticas

* Receita por período
* Corridas realizadas
* Reservas canceladas
* Taxa de cancelamento
* Taxa de ocupação por pista
* Relatórios operacionais

### Segurança

* Autenticação baseada em sessão
* Filtro de autenticação
* Controlo de acesso por perfil

---

# Base de Dados

O sistema utiliza uma base de dados H2 persistente em ficheiro.

Localização:

```text
data/
```

Configuração:

```xml
<property name="hibernate.connection.url">
    jdbc:h2:file:./data/kartodromo-db;AUTO_SERVER=TRUE;MODE=PostgreSQL
</property>
```

---

# Branches Principais

## main

Versão estável e pronta para entrega.

## develop

Branch principal de desenvolvimento.

## feature/*

Branches dedicadas ao desenvolvimento de novas funcionalidades.

Exemplos:

```bash
feature/dashboard
feature/reservas
feature/pistas
feature/estatisticas
feature/web-auth
```

---

# Estado Atual do Projeto

## Aplicação Desktop

Estado: Concluída

Funcionalidades:

* CRUD completo
* Autenticação
* Relatórios PDF
* Estatísticas
* Gestão operacional completa

## Aplicação Web

Estado: Concluída

Funcionalidades:

* Autenticação
* Dashboard
* Gestão de entidades
* Calendário
* Disponibilidade
* Relatórios
* Estatísticas avançadas
* Gestão de utilizadores
* Perfil

---

# Arquitetura

O sistema segue uma arquitetura em camadas:

```text
UI
│
Controllers
│
BLL (Services)
│
DAL (DAO)
│
Hibernate ORM
│
H2 Database
```

A camada de negócio encontra-se centralizada no módulo **kartodromo-core**, permitindo reutilização entre as versões Desktop e Web.

---

# Licença

Projeto desenvolvido exclusivamente para fins académicos no âmbito da unidade curricular Projeto II do curso de Engenharia Informática do Instituto Politécnico de Viana do Castelo.
