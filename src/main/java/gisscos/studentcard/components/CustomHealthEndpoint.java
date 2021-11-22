//package gisscos.studentcard.components;
//
//import gisscos.studentcard.services.IUserDetailsService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.actuate.endpoint.SecurityContext;
//import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
//import org.springframework.boot.actuate.endpoint.web.WebEndpointResponse;
//import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;
//import org.springframework.boot.actuate.health.*;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Component;
//
//import java.security.Principal;
//import java.util.Set;
//
//@Component
//@WebEndpoint(id = "custom-health")
//public class CustomHealthEndpoint {
//
//    @Autowired(required = false)
//    private HealthEndpoint delegate;
//
//    @Autowired
//    private IUserDetailsService userService;
//
//    @ReadOperation
//    public WebEndpointResponse<HealthComponent> health(Principal principal){
//
////        if(!userService.isSuperUser(principal)){
////            return new WebEndpointResponse<>(null, HttpStatus.FORBIDDEN.value());
////        }
//
//        HealthComponent health = this.delegate.health();
//        return new WebEndpointResponse<>(health, HttpStatus.OK.value());
//    }
//}