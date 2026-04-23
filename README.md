# Kartodromo - Projeto Java (DAL + BLL)

## Estrutura do projeto

- `kartodromo-core`:
  - Entidades (`model`)
  - Camada de acesso a dados (`dal`)
  - Camada de logica de negocio (`bll`)
  - Configuracao Hibernate (`config`)
  - Main de demonstracao (`core/demo/CoreDemoApplication`)

- `kartodromo-desktop`:
  - Modulo desktop com gestao de clientes, categorias, karts, corridas e reservas
  - Depende do `kartodromo-core`

- `kartodromo-web`:
  - Modulo web (arranque inicial)
  - Depende do `kartodromo-core`

## Main principal para a entrega

Classe principal:
- `pt.kartodromo.Main` (no modulo `kartodromo-core`)

Classe de demonstracao completa:
- `pt.kartodromo.core.demo.CoreDemoApplication`

## Nota

A BD usada no exemplo e H2 em memoria, configurada no `HibernateUtil`.
