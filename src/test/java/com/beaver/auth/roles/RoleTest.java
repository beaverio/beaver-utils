package com.beaver.auth.roles;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    @Test
    void testRoleHierarchy() {
        // Test that higher roles have access to lower role operations
        assertTrue(Role.OWNER.hasAccess(Role.ADMIN));
        assertTrue(Role.OWNER.hasAccess(Role.WRITE));
        assertTrue(Role.OWNER.hasAccess(Role.READ));
        assertTrue(Role.OWNER.hasAccess(Role.OWNER));

        assertTrue(Role.ADMIN.hasAccess(Role.WRITE));
        assertTrue(Role.ADMIN.hasAccess(Role.READ));
        assertTrue(Role.ADMIN.hasAccess(Role.ADMIN));

        assertTrue(Role.WRITE.hasAccess(Role.READ));
        assertTrue(Role.WRITE.hasAccess(Role.WRITE));

        assertTrue(Role.READ.hasAccess(Role.READ));
    }

    @Test
    void testRoleHierarchyDenial() {
        // Test that lower roles cannot access higher role operations
        assertFalse(Role.READ.hasAccess(Role.WRITE));
        assertFalse(Role.READ.hasAccess(Role.ADMIN));
        assertFalse(Role.READ.hasAccess(Role.OWNER));

        assertFalse(Role.WRITE.hasAccess(Role.ADMIN));
        assertFalse(Role.WRITE.hasAccess(Role.OWNER));

        assertFalse(Role.ADMIN.hasAccess(Role.OWNER));
    }

    @Test
    void testCanManageRole() {
        // Test role management permissions
        assertTrue(Role.OWNER.canManageRole(Role.ADMIN));
        assertTrue(Role.OWNER.canManageRole(Role.WRITE));
        assertTrue(Role.OWNER.canManageRole(Role.READ));
        assertTrue(Role.OWNER.canManageRole(Role.OWNER)); // Owner can manage other owners

        assertTrue(Role.ADMIN.canManageRole(Role.WRITE));
        assertTrue(Role.ADMIN.canManageRole(Role.READ));
        assertFalse(Role.ADMIN.canManageRole(Role.ADMIN)); // Admin cannot manage other admins
        assertFalse(Role.ADMIN.canManageRole(Role.OWNER));

        assertTrue(Role.WRITE.canManageRole(Role.READ));
        assertFalse(Role.WRITE.canManageRole(Role.WRITE)); // Write cannot manage other writes
        assertFalse(Role.WRITE.canManageRole(Role.ADMIN));
        assertFalse(Role.WRITE.canManageRole(Role.OWNER));

        assertFalse(Role.READ.canManageRole(Role.READ)); // Read cannot manage anything
        assertFalse(Role.READ.canManageRole(Role.WRITE));
        assertFalse(Role.READ.canManageRole(Role.ADMIN));
        assertFalse(Role.READ.canManageRole(Role.OWNER));
    }

    @Test
    void testRoleLevels() {
        assertEquals(0, Role.READ.getLevel());
        assertEquals(1, Role.WRITE.getLevel());
        assertEquals(2, Role.ADMIN.getLevel());
        assertEquals(3, Role.OWNER.getLevel());
    }

    @Test
    void testToString() {
        assertEquals("READ", Role.READ.toString());
        assertEquals("WRITE", Role.WRITE.toString());
        assertEquals("ADMIN", Role.ADMIN.toString());
        assertEquals("OWNER", Role.OWNER.toString());
    }
}
