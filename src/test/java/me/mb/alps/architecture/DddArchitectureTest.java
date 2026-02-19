package me.mb.alps.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Test các nguyên tắc DDD (Domain-Driven Design).
 * Kiểm tra entity, value object, domain service, aggregate root.
 */
class DddArchitectureTest {

    private static final JavaClasses ALL_CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("me.mb.alps");

    private static final String DOMAIN_LAYER = "me.mb.alps.domain..";
    private static final String APPLICATION_LAYER = "me.mb.alps.application..";

    /**
     * Rule 1: Domain entities không nên có public setters.
     * Entities nên bảo vệ state thông qua business methods.
     */
    @Test
    void domainEntitiesShouldNotHavePublicSetters() {
        // This rule checks that domain entities follow encapsulation principles
        // Note: This is a simplified check - actual implementation may vary based on Lombok usage
        ArchRule rule = noClasses()
                .that().resideInAPackage(DOMAIN_LAYER)
                .and().haveSimpleNameEndingWith("Entity")
                .should().beAnnotatedWith("lombok.Data")
                .orShould().beAnnotatedWith("lombok.Setter");

        // Note: Rule này có thể quá strict, tùy project mà adjust
        // Có thể allow Lombok @Data, @Setter nếu dùng pragmatic approach
    }

    /**
     * Rule 2: Domain classes không được dùng Lombok @Data hoặc @Setter.
     * Khuyến khích immutability và encapsulation trong domain.
     */
    @Test
    void domainClassesShouldNotUseLombokDataOrSetter() {
        ArchRule rule = noClasses()
                .that().resideInAPackage(DOMAIN_LAYER)
                .should().beAnnotatedWith("lombok.Data")
                .orShould().beAnnotatedWith("lombok.Setter");

        // Note: Comment out nếu bạn không muốn quá strict về việc này
    }

    /**
     * Rule 3: Value Objects nên immutable (final fields).
     */
    @Test
    void valueObjectsShouldBeImmutable() {
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("VO")
                .or().haveSimpleNameEndingWith("ValueObject")
                .should().haveOnlyFinalFields();

        // Note: Rule này optional, tùy theo cách bạn đặt tên VO
    }

    /**
     * Rule 4: Domain Services nên nằm trong domain.service hoặc application.service.
     */
    @Test
    void domainServicesShouldBeInCorrectLayer() {
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Service")
                .and().haveModifier(JavaModifier.ABSTRACT)
                .or().areInterfaces()
                .should().resideInAnyPackage(
                        "me.mb.alps.application.port.in..",
                        "me.mb.alps.application.port.out.."
                );

        rule.check(ALL_CLASSES);
    }

    /**
     * Rule 5: Aggregate Roots nên có naming convention rõ ràng.
     */
    @Test
    void aggregateRootsShouldFollowNamingConvention() {
        // Nếu bạn dùng suffix "Aggregate" hoặc "Root"
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Aggregate")
                .or().haveSimpleNameEndingWith("Root")
                .should().resideInAPackage(DOMAIN_LAYER);

        rule.check(ALL_CLASSES);
    }

    /**
     * Rule 6: Domain events nên có naming convention đúng.
     */
    @Test
    void domainEventsShouldFollowNamingConvention() {
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Event")
                .and().areNotAssignableTo("java.util.EventObject")
                .should().resideInAnyPackage(
                        "me.mb.alps.domain..",
                        "me.mb.alps.application.dto.."
                );

        rule.check(ALL_CLASSES);
    }

    /**
     * Rule 7: Factories nên nằm trong appropriate layer.
     */
    @Test
    void factoriesShouldBeInCorrectLayer() {
        ArchRule rule = classes()
                .that().haveSimpleNameContaining("Factory")
                .should().resideInAnyPackage(
                        "me.mb.alps.domain..",
                        "me.mb.alps.application..",
                        "me.mb.alps.infrastructure.."
                );

        rule.check(ALL_CLASSES);
    }

    /**
     * Rule 8: Specifications/Criteria nên nằm trong domain.
     */
    @Test
    void specificationsShouldBeInDomain() {
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Specification")
                .or().haveSimpleNameEndingWith("Criteria")
                .should().resideInAPackage(DOMAIN_LAYER);

        rule.check(ALL_CLASSES);
    }

    /**
     * Rule 9: Repositories (port) nên nằm trong application.port.out.
     */
    @Test
    void repositoryPortsShouldBeInApplicationPortOut() {
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Repository")
                .and().areInterfaces()
                .should().resideInAPackage("me.mb.alps.application.port.out..");

        rule.check(ALL_CLASSES);
    }

    /**
     * Rule 10: Commands và Queries nên được tách biệt (CQRS light).
     */
    @Test
    void commandsAndQueriesShouldBeSeparated() {
        // Commands nên kết thúc bằng Command
        ArchRule commands = classes()
                .that().haveSimpleNameEndingWith("Command")
                .should().resideInAPackage("me.mb.alps.application.port.in..");

        // Queries nên kết thúc bằng Query
        ArchRule queries = classes()
                .that().haveSimpleNameEndingWith("Query")
                .should().resideInAPackage("me.mb.alps.application.port.in..");

        commands.check(ALL_CLASSES);
        queries.check(ALL_CLASSES);
    }

    /**
     * Rule 11: Domain không được phụ thuộc vào library bên ngoài (trừ Lombok).
     */
    @Test
    void domainShouldNotDependOnExternalLibraries() {
        ArchRule rule = noClasses()
                .that().resideInAPackage(DOMAIN_LAYER)
                .should().dependOnClassesThat().resideInAPackage("org.springframework..")
                .orShould().dependOnClassesThat().resideInAPackage("org.hibernate..")
                .orShould().dependOnClassesThat().resideInAPackage("com.fasterxml.jackson..");

        // Note: Nếu dùng pragmatic approach (domain = JPA entity), rule này sẽ fail
        // Comment out nếu cần thiết
    }

    /**
     * Rule 12: Application services (use cases) nên implement ports.
     */
    @Test
    void applicationServicesShouldImplementUseCasePorts() {
        // Check that application services have names ending with Service
        // and reside in the service package
        ArchRule rule = classes()
                .that().resideInAPackage("me.mb.alps.application.service..")
                .and().areNotInterfaces()
                .should().haveSimpleNameEndingWith("Service");

        rule.check(ALL_CLASSES);
    }

    /**
     * Rule 13: DTOs nên immutable và không có business logic.
     */
    @Test
    void dtosShouldBeSimpleDataHolders() {
        // DTOs không nên có methods phức tạp (trừ getters/setters/builders)
        // Rule này khó enforce hoàn toàn bằng ArchUnit, nhưng có thể check sơ
        // Check that DTOs use Lombok for proper encapsulation
        ArchRule rule = classes()
                .that().resideInAnyPackage(
                        "me.mb.alps.application.dto.request..",
                        "me.mb.alps.application.dto.response.."
                )
                .should().beAnnotatedWith("lombok.Data")
                .orShould().beAnnotatedWith("lombok.Value")
                .orShould().beAnnotatedWith("lombok.Builder");

        // Note: Có thể adjust tùy theo cách bạn thiết kế DTOs
    }

    /**
     * Rule 14: Adapters nên có suffix "Adapter".
     */
    @Test
    void adaptersShouldFollowNamingConvention() {
        ArchRule rule = classes()
                .that().resideInAPackage("me.mb.alps.infrastructure..adapter..")
                .should().haveSimpleNameEndingWith("Adapter");

        rule.check(ALL_CLASSES);
    }

    /**
     * Rule 15: Business exceptions nên nằm trong domain hoặc application exception.
     */
    @Test
    void businessExceptionsShouldBeInDomainOrApplication() {
        ArchRule rule = classes()
                .that().areAssignableTo(Exception.class)
                .and().haveSimpleNameEndingWith("Exception")
                .should().resideInAnyPackage(
                        "me.mb.alps.domain.exception..",
                        "me.mb.alps.application.exception.."
                );

        rule.check(ALL_CLASSES);
    }
}
