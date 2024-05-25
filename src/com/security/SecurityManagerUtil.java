package com.security;

import java.security.*;

public class SecurityManagerUtil {

    // Static initializer to set up the policy
    static {
        Policy.setPolicy(new Policy() {
            public PermissionCollection getPermissions(CodeSource codesource) {
                Permissions permissions = new Permissions();
                permissions.add(new AllPermission());
                return permissions;
            }
        });
        System.setSecurityManager(new SecurityManager());
    }

    // Public method to check and enforce AllPermission
    public static void enforceAllPermissions() {
        try {
            AccessController.checkPermission(new AllPermission());
            System.out.println("All permissions are granted and enforced.");
        } catch (SecurityException e) {
            System.out.println("Security Exception: Insufficient permissions.");
            e.printStackTrace();
        }
    }
}