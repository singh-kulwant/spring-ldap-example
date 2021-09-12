package com.ldap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.ldap.repository.config.EnableLdapRepositories;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

@Configuration
@PropertySource("classpath:application.properties")
@ComponentScan(basePackages = {"com.ldap.*"})
@EnableLdapRepositories(basePackages = "com.ldap.**")
public class ApplicationConfig {

    @Autowired
    Environment environment;

    /***
     * ContextSource is used for creating the LdapTemplate
     */
    @Bean
    public LdapContextSource contextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(environment.getProperty("ldap.url"));
        contextSource.setBase(environment.getProperty("ldap.partitionSuffix"));
        contextSource.setUserDn(environment.getProperty("ldap.principal"));
        contextSource.setPassword(environment.getProperty("ldap.password"));
        return contextSource;
    }

    /***
     * LdapTemplate is used for creation and modification of LDAP entries
     */
    @Bean
    public LdapTemplate ldapTemplate() {
        return new LdapTemplate(contextSource());
    }



}
