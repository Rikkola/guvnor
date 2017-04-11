/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.guvnor.common.services.project.model;

import java.util.ArrayList;
import java.util.Collection;

import org.guvnor.common.services.project.security.ProjectResourceType;
import org.guvnor.structure.organizationalunit.OrganizationalUnit;
import org.guvnor.structure.repositories.Branch;
import org.guvnor.structure.repositories.Repository;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.uberfire.backend.vfs.Path;
import org.uberfire.commons.data.Cacheable;
import org.uberfire.security.ResourceType;
import org.uberfire.security.authz.RuntimeContentResource;
import org.uberfire.util.URIUtil;

import static org.uberfire.commons.validation.PortablePreconditions.checkNotNull;

@Portable
public class Project
        implements RuntimeContentResource,
                   Cacheable {

    public static final ProjectResourceType RESOURCE_TYPE = new ProjectResourceType();
    private Repository repository;
    private Branch branch;
    private Module mainModule;
    private OrganizationalUnit organizationalUnit;
    private boolean requiresRefresh = true;

    private Collection<String> groups = new ArrayList<>();

    public Project() {
    }

    public Project(final OrganizationalUnit organizationalUnit,
                   final Repository repository,
                   final Branch branch,
                   final Module mainModule) {
        this.organizationalUnit = checkNotNull("organizationalUnit",
                                               organizationalUnit);
        this.repository = checkNotNull("repository",
                                       repository);
        this.branch = checkNotNull("branch",
                                   branch);
        this.mainModule = mainModule;
    }

    public OrganizationalUnit getOrganizationalUnit() {
        return organizationalUnit;
    }

    public Repository getRepository() {
        return repository;
    }

    public Branch getBranch() {
        return branch;
    }

    public Module getMainModule() {
        return mainModule;
    }

    public Collection<String> getGroups() {
        return groups;
    }

    @Override
    public String getIdentifier() {
        return branch.getPath().toURI();
    }

    public String getEncodedIdentifier() {
        return URIUtil.encodeQueryString(getIdentifier());
    }

    @Override
    public ResourceType getResourceType() {
        return RESOURCE_TYPE;
    }

    @Override
    public boolean requiresRefresh() {
        return requiresRefresh;
    }

    public String getName() {
        if (mainModule != null && mainModule.getModuleName() != null) {
            return mainModule.getModuleName();
        } else {
            return repository.getAlias();
        }
    }

    @Override
    public void markAsCached() {
        this.requiresRefresh = false;
    }

    public Path getRootPath() {
        return branch.getPath();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Project project = (Project) o;

        if (requiresRefresh != project.requiresRefresh) {
            return false;
        }
        if (!repository.equals(project.repository)) {
            return false;
        }
        if (!branch.equals(project.branch)) {
            return false;
        }
        if (mainModule != null ? !mainModule.equals(project.mainModule) : project.mainModule != null) {
            return false;
        }
        if (!organizationalUnit.equals(project.organizationalUnit)) {
            return false;
        }
        return groups.equals(project.groups);
    }

    @Override
    public int hashCode() {
        int result = ~~repository.hashCode();
        result = 31 * result + ~~branch.hashCode();
        result = 31 * result + (mainModule != null ? ~~mainModule.hashCode() : 0);
        result = 31 * result + ~~organizationalUnit.hashCode();
        result = 31 * result + (requiresRefresh ? 1 : 0);
        result = 31 * result + ~~groups.hashCode();
        return result;
    }
}
