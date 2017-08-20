package com.royalmail.test;

import java.io.IOException;
import java.util.Hashtable;

import javax.naming.AuthenticationException;
import javax.naming.AuthenticationNotSupportedException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;

public class LDAPSearch {

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) throws NamingException {

		final String ldapAdServer = "ldap://rmgp.royalmailgroup.net";
		
		final String ldapSearchBase = "OU=Users, OU=Royal Mail, OU=RMG, DC=rmgp,DC=royalmailgroup,DC=net";
		
		if(args.length < 3){
			System.out.println("Usage LDAPSearch <userName> <userId> <password>");
			System.exit(-1);
		}
		
		String ldapUsername = args[0];
		String ldapAttrUsername = args[1];
		String ldapPassword = args[2];
		
		LDAPSearch ldap = new LDAPSearch();
		
		Hashtable env = new Hashtable();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, ldapAdServer);
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		
		env.put(Context.SECURITY_PRINCIPAL, ldapUsername);
		env.put(Context.SECURITY_CREDENTIALS, ldapPassword);

		try {
			LdapContext ctx = new InitialLdapContext(env, null);
			
			System.out.println("Connected");
			System.out.println("Environment " + ctx.getEnvironment());

			String authenticatedUser = (String) ctx.getEnvironment().get(Context.SECURITY_PRINCIPAL);
			
			System.out.println("Authenticated User : " + authenticatedUser);
			// do something useful with the context...
			SearchResult srLdapUser = ldap.findAccountByAccountName(ctx, ldapSearchBase, ldapAttrUsername);
			
			ctx.close();

		} catch (AuthenticationNotSupportedException ex) {
			System.out.println("The authentication is not supported by the server " + ex.getMessage());
		} catch (AuthenticationException ex) {
			System.out.println("incorrect password or username " + ex.getMessage());
		} catch (NamingException ex) {
			System.out.println("error when trying to create the context " + ex.getMessage());
			ex.printStackTrace();
		}
	}
	
	
	public SearchResult findAccountByAccountName(LdapContext ctx, String ldapSearchBase, String accountName) throws NamingException {
		String searchFilter = "(&(objectClass=*)(cn=" + accountName + "*))";
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        int pageSize = 3000;
        try {
			ctx.setRequestControls(new Control[]{ 
			        new PagedResultsControl(pageSize, Control.CRITICAL) });
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        NamingEnumeration<SearchResult> results = ctx.search(ldapSearchBase, searchFilter, searchControls);

        SearchResult searchResult = null;
        if(results.hasMoreElements()) {
             searchResult = (SearchResult) results.nextElement();

             System.out.println("Result " + searchResult.getName());
            if(results.hasMoreElements()) {
                System.err.println("Matched multiple users for the accountName: " + accountName);
                return null;
            }
        }
        
        return searchResult;
    }	
}