package com.revinate.grpcspringsecurity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.ManagementServerProperties;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.provider.expression.OAuth2WebSecurityExpressionHandler;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.access.expression.WebExpressionVoter;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

@Configuration
@Order(ManagementServerProperties.ACCESS_OVERRIDE_ORDER)
@EnableResourceServer
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private SecurityProperties securityProperties;

    @Autowired
    public SecurityConfiguration(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Bean
    public UserDetailsService inMemoryUserService() {
        return new InMemoryUserDetailsManager(Arrays.asList(
                new User(securityProperties.getUser().getName(),
                        securityProperties.getUser().getPassword(),
                        securityProperties.getUser().getRole().stream()
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                .collect(Collectors.toList()))));
    }

    @Bean
    public AuthenticationProvider inMemoryAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(inMemoryUserService());
        return provider;
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy("" +
                "ROLE_RESTRICTED > ROLE_ADMIN " +
                "ROLE_ADMIN > ROLE_USER " +
                "ROLE_USER > ROLE_VIEWER");
        return roleHierarchy;
    }

    @Bean
    public WebExpressionVoter webExpressionVoter() {
        OAuth2WebSecurityExpressionHandler expressionHandler = new OAuth2WebSecurityExpressionHandler();
        expressionHandler.setRoleHierarchy(roleHierarchy());
        WebExpressionVoter webExpressionVoter = new WebExpressionVoter();
        webExpressionVoter.setExpressionHandler(expressionHandler);
        return webExpressionVoter;
    }

    @Bean
    public AffirmativeBased accessDecisionManager() {
        return new AffirmativeBased(Collections.singletonList(webExpressionVoter()));
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(inMemoryAuthenticationProvider());
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                    .antMatchers("/actuator/health").permitAll()
                    .antMatchers("/actuator/**").hasRole("ADMIN")
                    .anyRequest().hasRole("RESTRICTED")
                    .accessDecisionManager(accessDecisionManager())
                .and()
                .httpBasic().realmName(securityProperties.getBasic().getRealm()).and()
                .sessionManagement().sessionCreationPolicy(securityProperties.getSessions()).and()
                .csrf().disable();
    }
}
