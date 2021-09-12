package com.ldap.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.*;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Service;

import javax.naming.Name;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;

@Service
public class LdapClient {

    @Autowired
    LdapTemplate ldapTemplate;

    @Autowired
    ContextSource contextSource;

    @Autowired
    Environment environment;

    /**
     * Authenticates an existing user
     */
    public void authenticate(String username, String password) {

        contextSource.getContext(
                "cn=" + username + "ou=users," + environment.getRequiredProperty("ldap.partitionSuffix"),
                password
        );

    }

    /**
     * bind() method of LdapTemplate is used to create an entry in the LDAP server
     */
    public void create(String username, String password) {
        Name dn = LdapNameBuilder.newInstance()
                .add("ou", "users")
                .add("cn", username)
                .build();

        DirContextAdapter context = new DirContextAdapter(dn);
        context.setAttributeValues(
                "objectclass",
                new String[]{"top", "person", "organizationalPerson", "inetOrgPerson"});

        context.setAttributeValue("cn", username);
        context.setAttributeValue("sn", username);
        context.setAttributeValue("userPassword", digestSHA(password));

        ldapTemplate.bind(context);
    }

    private String digestSHA(final String password) {
        String base64;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA");
            digest.update(password.getBytes());
            base64 = Base64
                    .getEncoder()
                    .encodeToString(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return "{SHA}" + base64;
    }

    public void modify(String username, String password) {
        Name dn = LdapNameBuilder.newInstance()
                .add("ou", "users")
                .add("cn", username)
                .build();

        DirContextOperations context = ldapTemplate.lookupContext(dn);

        context.setAttributeValues(
                "objectclass",
                new String[]{"top", "person", "organizationalPerson", "inetOrgPerson"});

        context.setAttributeValue("cn", username);
        context.setAttributeValue("sn", username);
        context.setAttributeValue("userPassword",
                digestSHA(password));

        ldapTemplate.modifyAttributes(context);
    }

    public List<String> search(String username) {
        return ldapTemplate.search(
                "ou=users",
                "cn=" + username,
                //AttributesMapper is used to get the desired attribute value from the entries found
                (AttributesMapper<String>) attrs -> (String) attrs.get("cn").get()
        );
    }

}
