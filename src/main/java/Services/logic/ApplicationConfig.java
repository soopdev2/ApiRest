package Services.logic;

import jakarta.ws.rs.core.Application;
import java.util.Set;
import java.util.HashSet;

/**
 * Application configuration class for JAX-RS
 * This class registers the resources for the JAX-RS application.
 * Ensure that all required resources are added to the Set<Class<?>> resources.
 * Do not modify the addRestResourceClasses method as it is automatically populated with all resources defined in the project.
 * If required, comment out calling this method in getClasses().
 * 
 * @Author Salvatore
 */
@jakarta.ws.rs.ApplicationPath("webresources")
public class ApplicationConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new HashSet<>();
        addRestResourceClasses(resources);
        return resources;
    }

    /**
     * Populates the resource set with all resources defined in the project.
     * Automatically generated, do not modify.
     */
    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(Services.Filter.JWTAuthenticationFilter.class);
        resources.add(Services.logic.AuthenticationService.class);
        resources.add(SkillProof.Controllers.DomandaController.class);
        resources.add(SkillProof.Controllers.QuestionarioController.class);
        resources.add(SkillProof.Controllers.StatisticheController.class);
        resources.add(SkillProof.Controllers.UtenteController.class);
    }
}
