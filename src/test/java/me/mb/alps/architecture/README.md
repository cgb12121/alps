# ArchUnit Architecture Tests

## Overview

This package contains [ArchUnit](https://www.archunit.org/) tests to enforce architectural patterns and coding standards for the ALPS project, following **DDD (Domain-Driven Design)** and **Hexagonal Architecture** principles.

## Architecture Principles

### Layer Dependencies

```
infrastructure → application → domain
```

- **Domain**: Pure business logic, no framework dependencies
- **Application**: Use cases, ports (in/out), DTOs, exceptions
- **Infrastructure**: Adapters, controllers, persistence implementations

## Test Files

### 1. HexagonalArchitectureTest.java

Enforces hexagonal architecture rules:

| Rule | Description |
|------|-------------|
| `domainShouldNotDependOnApplicationOrInfrastructure` | Domain must be pure |
| `applicationShouldNotDependOnInfrastructure` | Application depends only on domain |
| `noCyclicDependenciesBetweenLayers` | No cycles between layers |
| `onlyInfrastructureShouldUseSpringAnnotations` | Spring only in infrastructure |
| `onlyInfrastructurePersistenceShouldUseJPA` | JPA only in persistence layer |
| `controllersShouldOnlyBeInInfrastructureWeb` | Controllers in web layer |
| `useCaseImplementationsShouldBeInApplicationService` | Use cases in application.service |
| `portsShouldFollowNamingConvention` | Ports: *UseCase (in), *Port (out) |
| `adaptersShouldImplementPorts` | Adapters implement port interfaces |
| `dtosShouldBeInCorrectPackages` | Request/Response DTOs in correct packages |
| `exceptionsShouldFollowNamingConvention` | Exceptions end with "Exception" |
| `jpaRepositoriesShouldOnlyBeInInfrastructurePersistenceJpa` | JPA repos in jpa package |
| `globalExceptionHandlerShouldBeInInfrastructureWeb` | Handler in web layer |
| `entitiesShouldBeInDomainEntity` | Entities in domain.entity |
| `enumsShouldBeInDomainEnums` | Enums in domain.enums |

### 2. DddArchitectureTest.java

Enforces DDD patterns and conventions:

| Rule | Description |
|------|-------------|
| `domainEntitiesShouldNotHavePublicSetters` | Encapsulated state |
| `domainClassesShouldNotUseLombokDataOrSetter` | No @Data/@Setter in domain |
| `valueObjectsShouldBeImmutable` | VO immutability |
| `domainServicesShouldBeInCorrectLayer` | Services in correct layer |
| `aggregateRootsShouldFollowNamingConvention` | Aggregate naming |
| `domainEventsShouldFollowNamingConvention` | Event naming |
| `factoriesShouldBeInCorrectLayer` | Factory placement |
| `specificationsShouldBeInDomain` | Specifications in domain |
| `repositoryPortsShouldBeInApplicationPortOut` | Repository ports |
| `commandsAndQueriesShouldBeSeparated` | CQRS light (Command/Query) |
| `domainShouldNotDependOnExternalLibraries` | Domain isolation |
| `applicationServicesShouldImplementUseCasePorts` | Services implement use cases |
| `dtosShouldBeSimpleDataHolders` | DTO simplicity |
| `adaptersShouldFollowNamingConvention` | Adapter naming |
| `businessExceptionsShouldBeInDomainOrApplication` | Exception placement |

## Running Tests

### Run all architecture tests
```bash
mvn test -Dtest=me.mb.alps.architecture.*
```

### Run specific test class
```bash
mvn test -Dtest=HexagonalArchitectureTest
mvn test -Dtest=DddArchitectureTest
```

### Run single test method
```bash
mvn test -Dtest=HexagonalArchitectureTest#domainShouldNotDependOnApplicationOrInfrastructure
```

## Package Structure

```
me.mb.alps
├── domain                    # Pure domain (no dependencies)
│   ├── entity
│   └── enums
├── application               # Use cases, ports, DTOs
│   ├── dto
│   │   ├── request
│   │   └── response
│   ├── exception
│   ├── port.in              # Inbound ports (UseCase interfaces)
│   ├── port.out             # Outbound ports (Port interfaces)
│   └── service              # Use case implementations
└── infrastructure            # Adapters, frameworks
    ├── persistence
    │   ├── adapter          # Port implementations
    │   └── jpa              # Spring Data repositories
    ├── web                  # Controllers, handlers
    ├── workflow             # Camunda workflows
    └── rules                # Drools rules
```

## Naming Conventions

| Type | Pattern | Location |
|------|---------|----------|
| Inbound Port | `*UseCase` | `application.port.in` |
| Outbound Port | `*Port` | `application.port.out` |
| Request DTO | `*Request` | `application.dto.request` |
| Response DTO | `*Response` | `application.dto.response` |
| Exception | `*Exception` | `domain.exception` / `application.exception` |
| Adapter | `*Adapter` | `infrastructure.persistence.adapter` |
| JPA Repository | `*Repository` | `infrastructure.persistence.jpa` |
| Controller | `*Controller` | `infrastructure.web` |
| Entity | `*Entity` | `domain.entity` |
| Aggregate | `*Aggregate` | `domain.entity` |
| Value Object | `*VO` / `*ValueObject` | `domain.entity` |
| Domain Event | `*Event` | `domain` |
| Command | `*Command` | `application.port.in` |
| Query | `*Query` | `application.port.in` |

## Customization

### Strict vs Pragmatic Mode

Some rules may be too strict depending on your approach:

1. **Pragmatic Approach** (domain entity = JPA entity):
   - Comment out `onlyInfrastructurePersistenceShouldUseJPA`
   - Comment out `domainShouldNotDependOnExternalLibraries`

2. **Strict/Purist Approach** (domain POJO + separate JPA entity):
   - Enable all rules
   - Add MapStruct for entity mapping

### Adjusting Rules

Edit the test files to:
- Relax strict rules (comment out or modify)
- Add new rules for project-specific conventions
- Change naming patterns to match your standards

## CI/CD Integration

Architecture tests run automatically with:
```bash
mvn clean verify
```

Failed architecture tests will break the build, preventing architectural violations from being merged.

## Troubleshooting

### Common Failures

1. **"Domain depends on infrastructure"**: Check for accidental imports of Spring/JPA in domain classes
2. **"Cyclic dependencies"**: Refactor to break circular references between packages
3. **"Wrong package location"**: Move classes to correct packages per naming conventions

### Ignoring Specific Violations

For legitimate exceptions, use `@ArchIgnore` or modify the rule to exclude specific classes.

## References

- [ArchUnit Documentation](https://www.archunit.org/userguide/html/000_Index.html)
- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
- [Domain-Driven Design](https://martinfowler.com/bliki/DomainDrivenDesign.html)
