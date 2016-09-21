package org.gumtree.service.directory;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

public class LDAPService {

	private static final String LDAP_SERVER_NAME = "ldap://dc01.nbi.ansto.gov.au:389";
	private static final String LDAP_BASE_DN = "ou=nbiusers,dc=nbi,dc=ansto,dc=gov,dc=au";
	private static final String LDAP_FACTORY_NAME = "com.sun.jndi.ldap.LdapCtxFactory";
	private static final String NAME_GROUP_ADMIN = "notebook_admin";
	private static final String NAME_MANAGER_ADMIN_POSTFIX = "_instrument_scientists";
	private static final String ID_INSTRUMENT_NAME = "gumtree.instrument.id";
	private static final String NAME_GROUP_USER = "proposal_users";
//	private LdapContext context;
	private String managerGroupName;
	
    public static final String DISTINGUISHED_NAME = "distinguishedName";
    public static final String CN = "cn";
    public static final String MEMBER = "member";
    public static final String MEMBER_OF = "memberOf";
    public static final String SEARCH_BY_SAM_ACCOUNT_NAME = "(sAMAccountName={0})";
    public static final String SEARCH_GROUP_BY_GROUP_CN = "(&(objectclass=Group)(cn={0}))";

    public enum GroupLevel {
    	ADMIN,
    	MANAGER,
    	USER,
    	INVALID,
    }
    
	public LDAPService() {
		managerGroupName = System.getProperty(ID_INSTRUMENT_NAME);
		if (managerGroupName != null) {
			managerGroupName += NAME_MANAGER_ADMIN_POSTFIX;
		}
	}

	public boolean bindServer(String username, String password) {
		Hashtable<String, Object> env = new Hashtable<String, Object>();
		env.put(Context.PROVIDER_URL, LDAP_SERVER_NAME);
		env.put(Context.INITIAL_CONTEXT_FACTORY, LDAP_FACTORY_NAME);
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, username);
		env.put(Context.SECURITY_CREDENTIALS, password);
		boolean isPassed = false;
		try {
			LdapContext context = new InitialLdapContext(env, null);
			isPassed = true;
		} catch (NamingException e) {
//			System.err.println("failed to create Context");
			e.printStackTrace();
		}
//		try {
//			context.bind(username, password);
//			isPassed = true;
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		return isPassed;
	}
	
	public GroupLevel validateUser(String username, String password) throws NamingException{

		Hashtable<String, Object> env = new Hashtable<String, Object>();
		env.put(Context.PROVIDER_URL, LDAP_SERVER_NAME);
		env.put(Context.INITIAL_CONTEXT_FACTORY, LDAP_FACTORY_NAME);
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, username);
		env.put(Context.SECURITY_CREDENTIALS, password);
		LdapContext context = null;
		try {
			context = new InitialLdapContext(env, null);
		} catch (NamingException e) {
		}
		if (context != null) {
			String userDistinguishedName = getDistinguishedName(context, username);
			try {
				if (bindGroup(context, userDistinguishedName, NAME_GROUP_ADMIN)) {
					return GroupLevel.ADMIN;
				}
			} catch (Exception e) {
			}
			try {
				if (bindGroup(context, userDistinguishedName, managerGroupName)) {
					return GroupLevel.MANAGER;
				}
			} catch (Exception e) {
			}
//				if (bindGroup(context, userDistinguishedName, NAME_GROUP_USER)) {
//					return GroupLevel.USER;
//				}
			context.close();
			return GroupLevel.USER;
		}
		return GroupLevel.INVALID;
	}
	
	private boolean bindGroup(LdapContext context, String userDistinguishedName, String groupname) 
			throws NamingException {
		String search = "(&(objectCategory=group)(name=" + groupname + "))";
		SearchControls searchCtls = new SearchControls();
		String[] attrIDs = {MEMBER};
        searchCtls.setReturningAttributes(attrIDs);
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		NamingEnumeration result = context.search(LDAP_BASE_DN, search, searchCtls);
		if (result.hasMore()){
			Object group = result.next();
			SearchResult re = (SearchResult) group;
			Attribute members = re.getAttributes().get(MEMBER);
			
			if (members != null) {
				for (int i = 0; i < members.size(); i++) {
					String memberText = String.valueOf(members.get(i));
					if (memberText.equals(userDistinguishedName)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private String getDistinguishedName(LdapContext context, String username) throws NamingException {
		SearchControls searchCtls = new SearchControls();

        // Specify the attributes to return
//        if (attributes != null) {
//            searchCtls.setReturningAttributes(attributes);
//        }

        // Specify the search scope
        String[] attrIDs = {DISTINGUISHED_NAME};
        searchCtls.setReturningAttributes(attrIDs);
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        NamingEnumeration answer = context.search(LDAP_BASE_DN, "sAMAccountName="
                + username.substring(0, username.indexOf("@")), searchCtls);
        Attributes attributes = ((SearchResult) answer.next()).getAttributes();
        Attribute atb = attributes.get(DISTINGUISHED_NAME);
        return String.valueOf(atb.get());
	}
	
//    public boolean authenticate(String username, String password) throws NamingException {
//		Hashtable<String, Object> env = new Hashtable<String, Object>();
//		env.put(Context.PROVIDER_URL, "ldap://dc01.nbi.ansto.gov.au:389");
//		env.put(Context.INITIAL_CONTEXT_FACTORY,
//			    "com.sun.jndi.ldap.LdapCtxFactory");
//		env.put(Context.SECURITY_AUTHENTICATION, "simple");
//		env.put(Context.SECURITY_PRINCIPAL, username);
//		env.put(Context.SECURITY_CREDENTIALS, password);
//		
//        DirContext ctx = null;
//        String defaultSearchBase = "DC=DOMAIN,DC=com";
//        String groupDistinguishedName = "DN=CN=DLS-APP-MyAdmin-C,OU=DLS File Permissions,DC=DOMAIN,DC=com";
//
//        try {
//            ctx = new InitialDirContext(env);
//
//            // userName is SAMAccountName
//            SearchResult sr = executeSearchSingleResult(ctx, SearchControls.SUBTREE_SCOPE, defaultSearchBase,
//                    MessageFormat.format( "(sAMAccountName={0})", new Object[] {username}),
//                    new String[] {DISTINGUISHED_NAME, CN, MEMBER_OF}
//                    );
//
//            String groupCN = getCN(groupDistinguishedName);
//            HashMap processedUserGroups = new HashMap();
//            HashMap unProcessedUserGroups = new HashMap();
//
//            // Look for and process memberOf
//            Attribute memberOf = sr.getAttributes().get(MEMBER_OF);
//            if (memberOf != null) {
//                for ( Enumeration e1 = memberOf.getAll() ; e1.hasMoreElements() ; ) {
//                    String unprocessedGroupDN = e1.nextElement().toString();
//                    String unprocessedGroupCN = getCN(unprocessedGroupDN);
//                    // Quick check for direct membership
//                    if (isSame (groupCN, unprocessedGroupCN) && isSame (groupDistinguishedName, unprocessedGroupDN)) {
//                    	System.err.println(username + " is authorized.");
//                        return true;
//                    } else {
//                        unProcessedUserGroups.put(unprocessedGroupDN, unprocessedGroupCN);
//                    }
//                }
//                if (userMemberOf(ctx, defaultSearchBase, processedUserGroups, unProcessedUserGroups, groupCN, groupDistinguishedName)) {
//                	System.err.println(username + " is authorized.");
//                    return true;
//                }
//            }
//
//            System.err.println(username + " is NOT authorized.");
//            return false;
//        } catch (AuthenticationException e) {
//        	System.err.println(username + " is NOT authenticated");
//            return false;
//        } catch (NamingException e) {
//            throw e;
//        } finally {
//            if (ctx != null) {
//                try {
//                    ctx.close();
//                } catch (NamingException e) {
//                    throw e;
//                }
//            }
//        }
//    }
//    
//	public static boolean userMemberOf(DirContext ctx, String searchBase, HashMap processedUserGroups, 
//			HashMap unProcessedUserGroups, String groupCN, String groupDistinguishedName) throws NamingException {
//        HashMap newUnProcessedGroups = new HashMap();
//        for (Iterator entry = unProcessedUserGroups.keySet().iterator(); entry.hasNext();) {
//            String  unprocessedGroupDistinguishedName = (String) entry.next();
//            String unprocessedGroupCN = (String)unProcessedUserGroups.get(unprocessedGroupDistinguishedName);
//            if ( processedUserGroups.get(unprocessedGroupDistinguishedName) != null) {
//            	System.err.println("Found  : " + unprocessedGroupDistinguishedName +" in processedGroups. skipping further processing of it..." );
//                // We already traversed this.
//                continue;
//            }
//            if (isSame (groupCN, unprocessedGroupCN) && isSame (groupDistinguishedName, unprocessedGroupDistinguishedName)) {
//            	System.err.println("Found Match DistinguishedName : " + unprocessedGroupDistinguishedName +", CN : " + unprocessedGroupCN );
//                return true;
//            }
//        }
//
//        for (Iterator entry = unProcessedUserGroups.keySet().iterator(); entry.hasNext();) {
//            String  unprocessedGroupDistinguishedName = (String) entry.next();
//            String unprocessedGroupCN = (String)unProcessedUserGroups.get(unprocessedGroupDistinguishedName);
//
//            processedUserGroups.put(unprocessedGroupDistinguishedName, unprocessedGroupCN);
//
//            // Fetch Groups in unprocessedGroupCN and put them in newUnProcessedGroups
//            NamingEnumeration ns = executeSearch(ctx, SearchControls.SUBTREE_SCOPE, searchBase,
//                    MessageFormat.format( "(&(objectclass=Group)(cn={0}))", new Object[] {unprocessedGroupCN}),
//                    new String[] {CN, DISTINGUISHED_NAME, MEMBER_OF});
//
//            // Loop through the search results
//            while (ns.hasMoreElements()) {
//                SearchResult sr = (SearchResult) ns.next();
//
//                // Make sure we're looking at correct distinguishedName, because we're querying by CN
//                String userDistinguishedName = sr.getAttributes().get(DISTINGUISHED_NAME).get().toString();
//                if (!isSame(unprocessedGroupDistinguishedName, userDistinguishedName)) {
//                	System.err.println("Processing CN : " + unprocessedGroupCN + ", DN : " + unprocessedGroupDistinguishedName +", Got DN : " + userDistinguishedName +", Ignoring...");
//                    continue;
//                }
//
//                System.err.println("Processing for memberOf CN : " + unprocessedGroupCN + ", DN : " + unprocessedGroupDistinguishedName);
//                // Look for and process memberOf
//                Attribute memberOf = sr.getAttributes().get(MEMBER_OF);
//                if (memberOf != null) {
//                    for ( Enumeration e1 = memberOf.getAll() ; e1.hasMoreElements() ; ) {
//                        String unprocessedChildGroupDN = e1.nextElement().toString();
//                        String unprocessedChildGroupCN = getCN(unprocessedChildGroupDN);
//                        System.err.println("Adding to List of un-processed groups : " + unprocessedChildGroupDN +", CN : " + unprocessedChildGroupCN);
//                        newUnProcessedGroups.put(unprocessedChildGroupDN, unprocessedChildGroupCN);
//                    }
//                }
//            }
//        }
//        if (newUnProcessedGroups.size() == 0) {
//            System.err.println("newUnProcessedGroups.size() is 0. returning false...");
//            return false;
//        }
//
//        //  process unProcessedUserGroups
//        return userMemberOf(ctx, searchBase, processedUserGroups, newUnProcessedGroups, groupCN, groupDistinguishedName);
//    }
//	
//    public static String getCN(String cnName) {
//        if (cnName != null && cnName.toUpperCase().startsWith("CN=")) {
//            cnName = cnName.substring(3);
//        }
//        int position = cnName.indexOf(',');
//        if (position == -1) {
//            return cnName;
//        } else {
//            return cnName.substring(0, position);
//        }
//    }
//    
//    public static boolean isSame(String target, String candidate) {
//        if (target != null && target.equalsIgnoreCase(candidate)) {
//            return true;
//        }
//        return false;
//    }
//    
//    private static NamingEnumeration executeSearch(DirContext ctx, int searchScope,  String searchBase, String searchFilter, String[] attributes) throws NamingException {
//        // Create the search controls
//        SearchControls searchCtls = new SearchControls();
//
//        // Specify the attributes to return
//        if (attributes != null) {
//            searchCtls.setReturningAttributes(attributes);
//        }
//
//        // Specify the search scope
//        searchCtls.setSearchScope(searchScope);
//
//        // Search for objects using the filter
//        NamingEnumeration result = ctx.search(searchBase, searchFilter,searchCtls);
//        return result;
//    }
//    
//    private static SearchResult executeSearchSingleResult(DirContext ctx, int searchScope,  String searchBase, String searchFilter, String[] attributes) throws NamingException {
//        NamingEnumeration result = executeSearch(ctx, searchScope,  searchBase, searchFilter, attributes);
//
//        SearchResult sr = null;
//        // Loop through the search results
//        while (result.hasMoreElements()) {
//            sr = (SearchResult) result.next();
//            break;
//        }
//        return sr;
//    }
}
