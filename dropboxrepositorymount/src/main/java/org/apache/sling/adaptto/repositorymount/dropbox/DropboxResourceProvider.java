/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sling.adaptto.repositorymount.dropbox;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.oak.commons.PathUtils;
import org.apache.sling.api.resource.AbstractResource;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.spi.resource.provider.ResolveContext;
import org.apache.sling.spi.resource.provider.ResourceContext;
import org.apache.sling.spi.resource.provider.ResourceProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@Component(
    service = {ResourceProvider.class},
    configurationPolicy = ConfigurationPolicy.REQUIRE
)
@Designate(
    ocd = DropboxResourceProvider.Config.class
)
public class DropboxResourceProvider extends ResourceProvider
{
    @ObjectClassDefinition
    public @interface Config {
        String provider_root() default DropboxResourceProvider.DEFAULT_MOUNT_POINT;
        String dropbox_path() default DropboxResourceProvider.DEFAULT_DROPBOX_ROOT;
        String access_token(); // "EfvLliOgyeAAAAAAAAAAC12PeqHF76CfuOr-ct30hih9HUePZiEeqFF0HrNoHtjn"
    }

    public static final String DEFAULT_MOUNT_POINT = "/content/dropboxresource";
    public static final String DEFAULT_DROPBOX_ROOT = "/adaptto";
    private volatile DbxClientV2 m_client;
    private volatile DropboxProvider m_provider;

    @Activate
    public void activate(Config config) throws IOException
    {
        DbxRequestConfig requestConfig = DbxRequestConfig.newBuilder(this.getClass().getName()).build();
        m_client = new DbxClientV2(requestConfig, config.access_token());
        m_provider = new DropboxProvider(m_client).init(
            config.provider_root(),
            config.dropbox_path());
    }

    @Override
    public Resource getResource(ResolveContext resolveContext, String path, ResourceContext resourceContext, Resource resource)
    {
        Map<String, Object> node = m_provider.get(path);

        if (node != null) {
            return convert(PathUtils.getParentPath(path), resolveContext.getResourceResolver(), node);
        }
        else
        {
            return null;
        }
    }

    @Override
    public Iterator<Resource> listChildren(ResolveContext resolveContext, Resource resource)
    {
        Iterator<Map<String, Object>> children = m_provider.getChildren(resource.getPath());

        return new Iterator<Resource>()
        {
            @Override
            public boolean hasNext()
            {
                return children.hasNext();
            }

            @Override
            public Resource next()
            {
                return convert(resource.getPath(), resolveContext.getResourceResolver(), children.next());
            }
        };
    }

    private Resource convert(String path, ResourceResolver resolver, Map<String, Object> node) {
        return new ResourceImpl(PathUtils.concat(path, (String) node.get(JcrConstants.JCR_NAME)),
            (String) node.get(JcrConstants.JCR_PRIMARYTYPE), null,
            node, resolver, createMetaData(0, 0));
    }

    private ResourceMetadata createMetaData(long creationTime, long lastModified) {
        ResourceMetadata metadata = new ResourceMetadata();
        metadata.setCreationTime(creationTime);
        metadata.setModificationTime(lastModified);
        return metadata;
    }

    private class ResourceImpl extends AbstractResource {


        private final String path;
        private final String resourceType;
        private final String resourceSuperType;
        private final ValueMap valueMap;
        private final ResourceResolver resourceResolver;
        private final ResourceMetadata metaData;

        public ResourceImpl(String path, String resourceType, String resourceSuperType, Map<String, Object> valueMap, ResourceResolver resourceResolver, ResourceMetadata metaData)
        {
            this.path = path;
            this.resourceType = resourceType;
            this.resourceSuperType = resourceSuperType;
            this.valueMap = new ValueMapDecorator(Collections.unmodifiableMap(valueMap));
            this.resourceResolver = resourceResolver;
            this.metaData = metaData;
        }

        @Override
        public String getPath()
        {
            return path;
        }

        @Override
        public String getResourceType()
        {
            return resourceType;
        }

        @Override
        public String getResourceSuperType()
        {
            return resourceSuperType;
        }

        @Override
        public ResourceMetadata getResourceMetadata()
        {
            return metaData;
        }

        @Override
        public ValueMap getValueMap()
        {
            return valueMap;
        }

        @Override
        public ResourceResolver getResourceResolver()
        {
            return resourceResolver;
        }
    }
}
