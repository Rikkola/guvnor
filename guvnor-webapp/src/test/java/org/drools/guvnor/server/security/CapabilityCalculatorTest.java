/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.guvnor.server.security;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

import org.drools.guvnor.client.configurations.Capability;
import org.junit.Test;

public class CapabilityCalculatorTest {

    @Test
    public void testAdmin() {
        CapabilityCalculator loader = new CapabilityCalculator();
        List<RoleBasedPermission> perms = new ArrayList<RoleBasedPermission>();
        perms.add(new RoleBasedPermission("s", RoleTypes.ADMIN, null, null));

        List<Capability> caps = loader.calcCapabilities(perms);
        for (Capability capability : caps) {
            assertTrue(caps.contains(capability));
        }
    }

    @Test
    public void testCapabilitiesCalculate() {
        CapabilityCalculator loader = new CapabilityCalculator();
        List<RoleBasedPermission> perms = new ArrayList<RoleBasedPermission>();
        perms.add(new RoleBasedPermission("", RoleTypes.PACKAGE_DEVELOPER, null, null));
        perms.add(new RoleBasedPermission("", RoleTypes.ANALYST, null, null));
        List<Capability> caps = loader.calcCapabilities(perms);
        assertTrue(caps.contains(Capability.SHOW_PACKAGE_VIEW));

        perms = new ArrayList<RoleBasedPermission>();
        perms.add(new RoleBasedPermission("", RoleTypes.PACKAGE_ADMIN, null, null));
        caps = loader.calcCapabilities(perms);
        assertTrue(caps.contains(Capability.SHOW_PACKAGE_VIEW));

        perms = new ArrayList<RoleBasedPermission>();
        perms.add(new RoleBasedPermission("", RoleTypes.PACKAGE_READONLY, null, null));
        caps = loader.calcCapabilities(perms);
        assertTrue(caps.contains(Capability.SHOW_PACKAGE_VIEW));

        perms = new ArrayList<RoleBasedPermission>();
        perms.add(new RoleBasedPermission("", RoleTypes.PACKAGE_READONLY, null, null));
        perms.add(new RoleBasedPermission("", RoleTypes.PACKAGE_READONLY, null, null));
        perms.add(new RoleBasedPermission("", RoleTypes.ANALYST_READ, null, null));
        perms.add(new RoleBasedPermission("", RoleTypes.PACKAGE_DEVELOPER, null, null));
        caps = loader.calcCapabilities(perms);
        assertTrue(caps.contains(Capability.SHOW_PACKAGE_VIEW));
        assertTrue(caps.contains(Capability.SHOW_CREATE_NEW_ASSET));
        assertFalse(caps.contains(Capability.SHOW_CREATE_NEW_PACKAGE));
        assertTrue(caps.contains(Capability.SHOW_QA));

        perms = new ArrayList<RoleBasedPermission>();
        perms.add(new RoleBasedPermission("", RoleTypes.PACKAGE_READONLY, null, null));
        perms.add(new RoleBasedPermission("", RoleTypes.PACKAGE_READONLY, null, null));
        perms.add(new RoleBasedPermission("", RoleTypes.ANALYST, null, null));
        perms.add(new RoleBasedPermission("", RoleTypes.PACKAGE_ADMIN, null, null));
        caps = loader.calcCapabilities(perms);
        assertTrue(caps.contains(Capability.SHOW_PACKAGE_VIEW));
        assertTrue(caps.contains(Capability.SHOW_CREATE_NEW_ASSET));
        assertTrue(caps.contains(Capability.SHOW_CREATE_NEW_PACKAGE));
        assertTrue(caps.contains(Capability.SHOW_DEPLOYMENT));
        assertTrue(caps.contains(Capability.SHOW_DEPLOYMENT_NEW));
        assertTrue(caps.contains(Capability.SHOW_QA));

        perms = new ArrayList<RoleBasedPermission>();
        perms.add(new RoleBasedPermission("", RoleTypes.PACKAGE_READONLY, null, null));
        perms.add(new RoleBasedPermission("", RoleTypes.PACKAGE_READONLY, null, null));
        perms.add(new RoleBasedPermission("", RoleTypes.ADMIN, null, null));
        perms.add(new RoleBasedPermission("", RoleTypes.PACKAGE_ADMIN, null, null));
        caps = loader.calcCapabilities(perms);

    }

}
