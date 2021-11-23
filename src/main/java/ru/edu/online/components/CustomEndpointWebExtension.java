package ru.edu.online.components;

import ru.edu.online.entities.enums.UserRole;
import ru.edu.online.services.IUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.ApiVersion;
import org.springframework.boot.actuate.endpoint.SecurityContext;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.WebEndpointResponse;
import org.springframework.boot.actuate.endpoint.web.annotation.EndpointWebExtension;
import org.springframework.boot.actuate.health.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@EndpointWebExtension(endpoint = HealthEndpoint.class)
public class CustomEndpointWebExtension extends HealthEndpointWebExtension {

    @Autowired
    private IUserDetailsService userService;

    private static final String[] NO_PATH = {};
    public CustomEndpointWebExtension(HealthContributorRegistry registry, HealthEndpointGroups groups) {
        super(registry, groups);
    }

    @ReadOperation
    public WebEndpointResponse<HealthComponent> health(ApiVersion apiVersion, SecurityContext securityContext) {
        if (userService.getUserRole(securityContext.getPrincipal()) != UserRole.ADMIN) {
            return new WebEndpointResponse<>(null, HttpStatus.FORBIDDEN.value());
        }

        return health(apiVersion, securityContext, false, NO_PATH);
    }

}
