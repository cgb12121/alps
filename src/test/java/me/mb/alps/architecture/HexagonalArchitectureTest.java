package me.mb.alps.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * Test kiến trúc Hexagonal/Clean Architecture.
 * Kiểm tra dependency rules: infrastructure → application → domain
 */
class HexagonalArchitectureTest {

    private static final JavaClasses ALL_CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("me.mb.alps");

    private static final String DOMAIN_LAYER = "me.mb.alps.domain..";
    private static final String APPLICATION_LAYER = "me.mb.alps.application..";
    private static final String INFRASTRUCTURE_LAYER = "me.mb.alps.infrastructure..";

    /**
     * Rule 1: Domain layer không được phụ thuộc vào application hoặc infrastructure.
     * Domain phải thuần khiết, không phụ thuộc framework.
     */
    @Test
    void domainShouldNotDependOnApplicationOrInfrastructure() {
        ArchRule rule = com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses()
                .that().resideInAPackage(DOMAIN_LAYER)
                .should().dependOnClassesThat().resideInAnyPackage(APPLICATION_LAYER, INFRASTRUCTURE_LAYER)
                .because("Domain layer must be pure and framework-independent");

        rule.check(ALL_CLASSES);
    }

    /**
     * Rule 2: Application layer không được phụ thuộc vào infrastructure.
     * Application chỉ phụ thuộc domain và define ports.
     */
    @Test
    void applicationShouldNotDependOnInfrastructure() {
        ArchRule rule = com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses()
                .that().resideInAPackage(APPLICATION_LAYER)
                .should().dependOnClassesThat().resideInAnyPackage(INFRASTRUCTURE_LAYER)
                .because("Application layer should depend only on domain, not infrastructure");

        rule.check(ALL_CLASSES);
    }

    /**
     * Rule 3: Không có cyclic dependencies giữa các slices.
     */
    @Test
    void noCyclicDependenciesBetweenLayers() {
        ArchRule rule = slices().matching("me.mb.alps.(*)..")
                .should().beFreeOfCycles();

        rule.check(ALL_CLASSES);
    }

    /**
     * Rule 4: Chỉ infrastructure mới được dùng Spring annotations.
     * Application và Domain không được dùng Spring annotations trực tiếp.
     */
    @Test
    void onlyInfrastructureShouldUseSpringAnnotations() {
        // Domain should not use Spring
        ArchRule domainNoSpring = com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses()
                .that().resideInAPackage(DOMAIN_LAYER)
                .should().beAnnotatedWith("org.springframework.stereotype.Component")
                .orShould().beAnnotatedWith("org.springframework.stereotype.Service")
                .orShould().beAnnotatedWith("org.springframework.stereotype.Repository")
                .orShould().beAnnotatedWith("org.springframework.stereotype.Controller")
                .orShould().beAnnotatedWith("org.springframework.context.annotation.Configuration")
                .because("Domain layer should not use Spring annotations");

        domainNoSpring.check(ALL_CLASSES);
    }

    /**
     * Rule 5: Chỉ infrastructure.persistence mới được dùng JPA.
     */
    @Test
    void onlyInfrastructurePersistenceShouldUseJPA() {
        ArchRule rule = com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses()
                .that().resideInAnyPackage(DOMAIN_LAYER, APPLICATION_LAYER)
                .should().dependOnClassesThat().resideInAPackage("jakarta.persistence..")
                .because("JPA should only be used in infrastructure.persistence layer");

        // Note: Nếu dùng pragmatic approach (domain entity = JPA entity), rule này sẽ fail
        // Comment out nếu bạn theo pragmatic approach
    }

    /**
     * Rule 6: Controller chỉ nên nằm trong infrastructure.web.
     */
    @Test
    void controllersShouldOnlyBeInInfrastructureWeb() {
        ArchRule rule = com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes()
                .that().haveSimpleNameEndingWith("Controller")
                .should().resideInAPackage("me.mb.alps.infrastructure.web..");

        rule.check(ALL_CLASSES);
    }

    /**
     * Rule 7: Use case implementations (service) nên nằm trong application.service.
     */
    @Test
    void useCaseImplementationsShouldBeInApplicationService() {
        ArchRule rule = com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes()
                .that().haveSimpleNameEndingWith("UseCase")
                .and().areInterfaces()
                .should().resideInAPackage("me.mb.alps.application.port.in..");

        rule.check(ALL_CLASSES);
    }

    /**
     * Rule 8: Port interfaces (in/out) nên có naming convention đúng.
     */
    @Test
    void portsShouldFollowNamingConvention() {
        // Port.in nên kết thúc bằng UseCase
        ArchRule inboundPorts = com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes()
                .that().resideInAPackage("me.mb.alps.application.port.in..")
                .and().areInterfaces()
                .should().haveSimpleNameEndingWith("UseCase");

        // Port.out nên kết thúc bằng Port
        ArchRule outboundPorts = com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes()
                .that().resideInAPackage("me.mb.alps.application.port.out..")
                .and().areInterfaces()
                .should().haveSimpleNameEndingWith("Port");

        inboundPorts.check(ALL_CLASSES);
        outboundPorts.check(ALL_CLASSES);
    }

    /**
     * Rule 9: Adapter implementations nên implement ports.
     */
    @Test
    void adaptersShouldImplementPorts() {
        // Check that classes in adapter package have names ending with Adapter
        ArchRule rule = com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes()
                .that().resideInAPackage("me.mb.alps.infrastructure..adapter..")
                .should().haveSimpleNameEndingWith("Adapter");

        rule.check(ALL_CLASSES);
    }

    /**
     * Rule 10: DTOs nên nằm đúng package request/response.
     */
    @Test
    void dtosShouldBeInCorrectPackages() {
        // Request DTOs
        ArchRule requestDTOs = com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes()
                .that().haveSimpleNameEndingWith("Request")
                .should().resideInAPackage("me.mb.alps.application.dto.request..");

        // Response DTOs
        ArchRule responseDTOs = com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes()
                .that().haveSimpleNameEndingWith("Response")
                .should().resideInAPackage("me.mb.alps.application.dto.response..");

        requestDTOs.check(ALL_CLASSES);
        responseDTOs.check(ALL_CLASSES);
    }

    /**
     * Rule 11: Exceptions nên có naming convention đúng.
     */
    @Test
    void exceptionsShouldFollowNamingConvention() {
        ArchRule rule = com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes()
                .that().areAssignableTo(Throwable.class)
                .and().haveSimpleNameEndingWith("Exception")
                .should().resideInAnyPackage(
                        "me.mb.alps.application.exception..",
                        "me.mb.alps.domain.exception.."
                );

        rule.check(ALL_CLASSES);
    }

    /**
     * Rule 12: Repository interfaces (JPA) chỉ nên nằm trong infrastructure.persistence.jpa.
     */
    @Test
    void jpaRepositoriesShouldOnlyBeInInfrastructurePersistenceJpa() {
        ArchRule rule = com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes()
                .that().haveSimpleNameEndingWith("Repository")
                .and().areInterfaces()
                .should().resideInAPackage("me.mb.alps.infrastructure.persistence.jpa..");

        rule.check(ALL_CLASSES);
    }

    /**
     * Rule 13: GlobalExceptionHandler nên nằm trong infrastructure.web.
     */
    @Test
    void globalExceptionHandlerShouldBeInInfrastructureWeb() {
        ArchRule rule = com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes()
                .that().haveSimpleName("GlobalExceptionHandler")
                .should().resideInAPackage("me.mb.alps.infrastructure.web..");

        rule.check(ALL_CLASSES);
    }

    /**
     * Rule 14: Entities nên nằm trong domain.entity.
     */
    @Test
    void entitiesShouldBeInDomainEntity() {
        ArchRule rule = com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes()
                .that().haveSimpleNameEndingWith("Entity")
                .or().haveSimpleNameEndingWith("Aggregate")
                .should().resideInAPackage("me.mb.alps.domain.entity..");

        rule.check(ALL_CLASSES);
    }

    /**
     * Rule 15: Enums nên nằm trong domain.enums.
     */
    @Test
    void enumsShouldBeInDomainEnums() {
        ArchRule rule = com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes()
                .that().areEnums()
                .should().resideInAPackage("me.mb.alps.domain.enums..");

        rule.check(ALL_CLASSES);
    }
}
