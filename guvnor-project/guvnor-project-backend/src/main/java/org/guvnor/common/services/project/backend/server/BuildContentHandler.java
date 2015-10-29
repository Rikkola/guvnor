/*
 * Copyright 2015 JBoss Inc
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

import org.apache.maven.model.Build;
import org.apache.maven.model.Plugin;

class BuildContentHandler {

    org.guvnor.common.services.project.model.Build fromPomModelToClientModel( final Build from ) {

        if ( from != null ) {
            org.guvnor.common.services.project.model.Build build = new org.guvnor.common.services.project.model.Build();
            if ( from.getPlugins() != null ) {
                for (Plugin plugin : from.getPlugins()) {
                    build.getPlugins().add( fromPomModelToClientModel( plugin ) );
                }
            }
            return build;
        } else {
            return null;
        }
    }

    public Build update( final org.guvnor.common.services.project.model.Build from,
                         Build to ) {
        if ( from == null ) {
            return null;
        } else {
            if ( to == null ) {
                to = new Build();
            }

            if ( from.getPlugins() != null ) {
                to.setPlugins( new PluginUpdater( to.getPlugins() ).update( from.getPlugins() ) );
            }

            return to;
        }
    }

    private Plugin update( org.guvnor.common.services.project.model.Plugin from,
                           Plugin to ) {

        to.setGroupId( from.getGroupId() );
        to.setArtifactId( from.getArtifactId() );
        to.setVersion( from.getVersion() );
        to.setExtensions( from.isExtensions() );

        new DependencyUpdater( to.getDependencies() ).updateDependencies( from.getDependencies() );

        return to;
    }

    private org.guvnor.common.services.project.model.Plugin fromPomModelToClientModel( final Plugin from ) {
        org.guvnor.common.services.project.model.Plugin plugin = new org.guvnor.common.services.project.model.Plugin();

        plugin.setGroupId( from.getGroupId() );
        plugin.setArtifactId( from.getArtifactId() );
        plugin.setVersion( from.getVersion() );
        plugin.setExtensions( from.isExtensions() );

        plugin.setDependencies( new DependencyContentHandler().fromPomModelToClientModel( from.getDependencies() ) );

        return plugin;
    }

}
