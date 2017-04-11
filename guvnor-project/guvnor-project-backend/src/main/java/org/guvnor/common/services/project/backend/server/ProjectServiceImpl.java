/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
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
package org.guvnor.common.services.project.backend.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.guvnor.common.services.project.model.Module;
import org.guvnor.common.services.project.model.POM;
import org.guvnor.common.services.project.model.Project;
import org.guvnor.common.services.project.service.DeploymentMode;
import org.guvnor.common.services.project.service.ModuleService;
import org.guvnor.common.services.project.service.ProjectService;
import org.guvnor.structure.backend.backcompat.BackwardCompatibleUtil;
import org.guvnor.structure.organizationalunit.OrganizationalUnit;
import org.guvnor.structure.organizationalunit.OrganizationalUnitService;
import org.guvnor.structure.repositories.Branch;
import org.guvnor.structure.repositories.Repository;
import org.guvnor.structure.repositories.RepositoryEnvironmentConfigurations;
import org.guvnor.structure.repositories.RepositoryService;
import org.guvnor.structure.server.config.ConfigGroup;
import org.guvnor.structure.server.config.ConfigItem;
import org.guvnor.structure.server.config.ConfigType;
import org.guvnor.structure.server.config.ConfigurationFactory;
import org.guvnor.structure.server.config.ConfigurationService;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.backend.vfs.Path;
import org.uberfire.commons.validation.PortablePreconditions;

public class ProjectServiceImpl
        implements ProjectService {

    protected ConfigurationService configurationService;
    protected BackwardCompatibleUtil backward;
    private ConfigurationFactory configurationFactory;
    private OrganizationalUnitService organizationalUnitService;
    private RepositoryService repositoryService;
    private ModuleService<? extends Module> moduleService;

    @Inject
    public ProjectServiceImpl(final OrganizationalUnitService organizationalUnitService,
                              final RepositoryService repositoryService,
                              final ConfigurationFactory configurationFactory,
                              final ConfigurationService configurationService,
                              final BackwardCompatibleUtil backward,
                              final Instance<ModuleService<? extends Module>> moduleServices) {
        this.organizationalUnitService = organizationalUnitService;
        this.repositoryService = repositoryService;
        this.configurationFactory = configurationFactory;
        this.configurationService = configurationService;
        this.backward = backward;
        moduleService = moduleServices.get();
    }

    @Override
    public Collection<Project> getAllProjects() {
        return null;
    }

    @Override
    public Collection<Project> getAllProjects(final OrganizationalUnit organizationalUnit) {
        final List<Project> result = new ArrayList<>();

        for (final Repository repository : repositoryService.getAllRepositories()) {

            if (organizationalUnit.getRepositories().contains(repository)) {

                // TODO: What if this is empty.
                final Module mainModule = moduleService.resolveModule(repository.getRoot());

                result.add(new Project(organizationalUnit,
                                       repository,
                                       repository.getDefaultBranch().get(),
                                       mainModule));
            }
        }

        return result;
    }

    @Override
    public Project newProject(final OrganizationalUnit organizationalUnit,
                              final String targetRepositoryAlias) {



        // TODO Can't really create a project without a name? -rikkola-
        return null;
    }

    @Override
    public Project newProject(final OrganizationalUnit organizationalUnit,
                              final POM pom,
                              final DeploymentMode mode) {
        //TODO What to do with the mode?
        return newProject(organizationalUnit,
                          pom);
    }

    @Override
    public Project newProject(final OrganizationalUnit organizationalUnit,
                              final POM pom) {

        final Repository repository = repositoryService.createRepository(organizationalUnit,
                                                                         "git",
                                                                         PortablePreconditions.checkNotNull("project name in pom model",
                                                                                                            pom.getName()),
                                                                         new RepositoryEnvironmentConfigurations());
        final Module module = moduleService.newModule(repository.getRoot(),
                                                      pom,
                                                      "");

        Project project = new Project(organizationalUnit,
                                      repository,
                                      repository.getDefaultBranch().get(),
                                      module);
        addSecurityGroups(project);

        return project;
    }

    @Override
    public Project resolveProject(final Repository repository) {
        return resolveProject(repository.getRoot());
    }

    @Override
    public Project resolveProject(final Branch branch) {
        return resolveProject(branch.getPath());
    }

    @Override
    public Project resolveProject(final Module module) {
        return resolveProject(module.getRootPath());
    }

    @Override
    public Project resolveProject(final String name) {

        for (final Project project : getAllProjects()) {
            if (project.getName().equals(name)) {
                return project;
            }
        }

        return null;
    }

    @Override
    public Project resolveProject(final Path path) {

        // TODO: What if the path param is not in default branch?

        final Repository repository = repositoryService.getRepository(Paths.convert(Paths.convert(path).getRoot()));
        final Branch branch = repository.getDefaultBranch().get();

        return new Project(organizationalUnitService.getParentOrganizationalUnit(repository),
                           repository,
                           branch,
                           moduleService.resolveModule(Paths.convert(Paths.convert(branch.getPath()).getRoot())));
    }

    @SuppressWarnings({"rawtypes"})
    @Override
    public void addGroup(final Module module,
                         final String group) {
        ConfigGroup thisProjectConfig = findModuleConfig(module.getRootPath());

        if (thisProjectConfig == null) {
            thisProjectConfig = configurationFactory.newConfigGroup(ConfigType.PROJECT,
                                                                    module.getRootPath().toURI(),
                                                                    "Project '" + module.getModuleName() + "' configuration");
            thisProjectConfig.addConfigItem(configurationFactory.newConfigItem("security:groups",
                                                                               new ArrayList<String>()));
            configurationService.addConfiguration(thisProjectConfig);
        }

        if (thisProjectConfig != null) {
            final ConfigItem<List> groups = backward.compat(thisProjectConfig).getConfigItem("security:groups");
            groups.getValue().add(group);

            configurationService.updateConfiguration(thisProjectConfig);
        } else {
            throw new IllegalArgumentException("Project " + module.getModuleName() + " not found");
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void removeGroup(final Module module,
                            final String group) {
        final ConfigGroup thisProjectConfig = findModuleConfig(module.getRootPath());

        if (thisProjectConfig != null) {
            final ConfigItem<List> groups = backward.compat(thisProjectConfig).getConfigItem("security:groups");
            groups.getValue().remove(group);

            configurationService.updateConfiguration(thisProjectConfig);
        } else {
            throw new IllegalArgumentException("Project " + module.getModuleName() + " not found");
        }
    }

    protected void addSecurityGroups(final Project project) {
        //Copy in Security Roles required to access this resource
        final ConfigGroup moduleConfiguration = findModuleConfig(project.getRootPath());
        if (moduleConfiguration != null) {
            ConfigItem<List<String>> groups = backward.compat(moduleConfiguration).getConfigItem("security:groups");
            if (groups != null) {
                for (String group : groups.getValue()) {
                    project.getGroups().add(group);
                }
            }
        }
    }

    protected ConfigGroup findModuleConfig(final Path moduleRoot) {
        final Collection<ConfigGroup> groups = configurationService.getConfiguration(ConfigType.PROJECT);
        if (groups != null) {
            for (ConfigGroup groupConfig : groups) {
                if (groupConfig.getName().equals(moduleRoot.toURI())) {
                    return groupConfig;
                }
            }
        }
        return null;
    }
}
