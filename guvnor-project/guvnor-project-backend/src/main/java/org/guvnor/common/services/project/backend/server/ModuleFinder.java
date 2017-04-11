package org.guvnor.common.services.project.backend.server;

import java.util.HashSet;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.guvnor.common.services.project.model.Module;
import org.guvnor.common.services.project.model.POM;
import org.guvnor.common.services.project.service.POMService;
import org.guvnor.structure.repositories.Branch;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.commons.validation.PortablePreconditions;
import org.uberfire.io.IOService;
import org.uberfire.java.nio.file.DirectoryStream;
import org.uberfire.java.nio.file.Files;
import org.uberfire.rpc.SessionInfo;
import org.uberfire.security.authz.AuthorizationManager;

import static org.guvnor.common.services.project.utils.ModuleResourcePaths.POM_PATH;

/**
 * Created by tonirikkola on 04/04/2017.
 */
@ApplicationScoped
public class ModuleFinder {

    private SessionInfo sessionInfo;
    private POMService pomService;
    private IOService ioService;
    private AuthorizationManager authorizationManager;

    private ResourceResolver resourceResolver;

    public ModuleFinder() {
    }

    // TODO: Is this needed right now or just Project search?

    @Inject
    public ModuleFinder(final @Named("ioStrategy") IOService ioService,
                        final AuthorizationManager authorizationManager,
                        final SessionInfo sessionInfo,
                        final POMService pomService) {
        this.ioService = ioService;
        this.authorizationManager = authorizationManager;
        this.sessionInfo = sessionInfo;
        this.pomService = pomService;
    }

    public Set<Module> find(final ResourceResolver resourceResolver,
                            final Branch branch,
                            final boolean secure) {

        this.resourceResolver = PortablePreconditions.checkNotNull("resourceResolver",
                                                                   resourceResolver);

        return new Finder(PortablePreconditions.checkNotNull("branch",
                                                             branch),
                          PortablePreconditions.checkNotNull("secure",
                                                             secure)).find();
    }

    private class Finder {

        private final Branch branch;
        private final boolean secure;

        private final Set<Module> authorizedModules = new HashSet<Module>();

        public Finder(final Branch branch,
                      final boolean secure) {
            this.branch = branch;
            this.secure = secure;
        }

        public Set<Module> find() {
            if (branch == null) {
                return authorizedModules;
            }

            findProject(Paths.convert(branch.getPath()));

            return authorizedModules;
        }

        private void findProject(final org.uberfire.java.nio.file.Path folderPath) {
            final org.uberfire.java.nio.file.Path pomPath = folderPath.resolve(POM_PATH);

            if (Files.exists(pomPath)) {

//                final Module module = resourceResolver.resolveModule(Paths.convert(pomPath));
//
//                if (!secure || authorizationManager.authorize(module,
//                                                              sessionInfo.getIdentity())) {
//                    addProject(module);
//
//                    final DirectoryStream<org.uberfire.java.nio.file.Path> nioRepositoryPaths = ioService.newDirectoryStream(folderPath);
//                    try {
//                        for (final org.uberfire.java.nio.file.Path nioRepositoryPath : nioRepositoryPaths) {
//
//                            if (Files.isDirectory(nioRepositoryPath)) {
//                                findProject(nioRepositoryPath);
//                            }
//                        }
//                    } finally {
//                        nioRepositoryPaths.close();
//                    }
//                }
            }
        }

        private void addProject(final Module module) {
            final POM projectPom = pomService.load(module.getPomXMLPath());
            module.setPom(projectPom);
            authorizedModules.add(module);
        }
    }
}
