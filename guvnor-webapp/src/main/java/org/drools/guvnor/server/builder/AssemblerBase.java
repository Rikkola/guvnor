/*
 * Copyright 2011 JBoss Inc
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.drools.guvnor.server.builder;

import org.drools.builder.conf.DefaultPackageNameOption;
import org.drools.guvnor.client.common.AssetFormats;
import org.drools.repository.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * This assembles packages in the BRMS into binary package objects, and deals
 * with errors etc. Each content type is responsible for contributing to the
 * package.
 */
abstract class AssemblerBase {

    protected PackageItem packageItem;

    protected BRMSPackageBuilder builder;

    protected AssemblyErrorLogger errorLogger = new AssemblyErrorLogger();

    protected AssemblerBase(PackageItem packageItem) {
        this.packageItem = packageItem;

        createBuilder();
    }

    public void createBuilder() {
        try {
            Properties properties = loadConfigurationProperties();
            properties.setProperty(DefaultPackageNameOption.PROPERTY_NAME,
                    this.packageItem.getName());
            builder = BRMSPackageBuilder.getInstance(BRMSPackageBuilder.getJars(packageItem),
                    properties);
        } catch (IOException e) {
            throw new RulesRepositoryException("Unable to load configuration properties for package.",
                    e);
        }
    }

    /**
     * Load all the .properties and .conf files into one big happy Properties instance.
     */
    private Properties loadConfigurationProperties() throws IOException {
        Properties bigHappyProperties = new Properties();
        AssetItemIterator assetItemIterator = getAssetItemIterator(AssetFormats.PROPERTIES, AssetFormats.CONFIGURATION);
        while (assetItemIterator.hasNext()) {
            AssetItem assetItem = assetItemIterator.next();
            assetItem.getContent();
            Properties properties = new Properties();
            properties.load(assetItem.getBinaryContentAttachment());
            bigHappyProperties.putAll(properties);
        }
        return bigHappyProperties;
    }

    public boolean hasErrors() {
        return errorLogger.hasErrors();
    }

    public List<ContentAssemblyError> getErrors() {
        return this.errorLogger.getErrors();
    }

    protected AssetItemIterator getAssetItemIterator(String... assetFormats) {
        AssetItemIterator assetItemIterator = this.packageItem.listAssetsByFormat(assetFormats);
        ((VersionedAssetItemIterator) assetItemIterator).setReturnAssetsWithVersionsSpecifiedByDependencies(true);
        return assetItemIterator;
    }

    protected Iterator<AssetItem> getAllAssets() {
        Iterator<AssetItem> iterator = packageItem.getAssets();
        ((VersionedAssetItemIterator) iterator).setReturnAssetsWithVersionsSpecifiedByDependencies(true);
        return iterator;
    }
}
