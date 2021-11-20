package gisscos.studentcard.components;

import gisscos.studentcard.services.IUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.WebEndpointResponse;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
@WebEndpoint(id = "custom-health")
public class CustomHealthEndpoint {

    private final HealthEndpoint delegate;
    private final IUserDetailsService userService;

    @Autowired
    public CustomHealthEndpoint(HealthEndpoint delegate, IUserDetailsService userService) {
        this.delegate = delegate;
        this.userService = userService;
    }

    @ReadOperation
    public WebEndpointResponse<HealthComponent> health(Principal principal){
        if(!userService.isSuperUser(principal)){
            return new WebEndpointResponse<>(null, HttpStatus.FORBIDDEN.value());
        }

        HealthComponent health = this.delegate.health();
        return new WebEndpointResponse<>(health, HttpStatus.OK.value());
    }
}