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
import java.util.Map;
import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.sling.jcr.base.spi.RepositoryMount;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@Component(service = RepositoryMount.class,
    configurationPolicy = ConfigurationPolicy.REQUIRE
)
@Designate(
    ocd = DropboxRepository.Config.class
)
public class DropboxRepository implements RepositoryMount
{
    @ObjectClassDefinition
    public @interface Config {
        String org_apache_sling_jcr_base_RepositoryMount_MOUNT__POINTS() default DropboxRepository.DEFAULT_MOUNT_POINT;
        String dropbox_path() default DropboxRepository.DROPBOX_ROOT;
        String access_token();
    }

    public static final String DEFAULT_MOUNT_POINT = "/content/dropbox";
    public static final String DROPBOX_ROOT = "/adaptto";

    private final String m_mountPoint;
    private final DbxClientV2 m_client;
    private volatile DropboxProvider m_provider;

    @Activate
    public DropboxRepository(Config config) throws IOException
    {
        m_mountPoint = config.org_apache_sling_jcr_base_RepositoryMount_MOUNT__POINTS();
        DbxRequestConfig requestConfig = DbxRequestConfig.newBuilder(this.getClass().getName()).build();
        m_client = new DbxClientV2(requestConfig, config.access_token());
        m_provider = new DropboxProvider(m_client).init(
            m_mountPoint,
            config.dropbox_path()
        );
    }

    @Override
    public Session login(Credentials credentials, String workspaceName, Map<String, Object> attributes) throws LoginException, NoSuchWorkspaceException, RepositoryException
    {
        JackrabbitSession parentSession = (JackrabbitSession) attributes.get(RepositoryMount.PARENT_SESSION_KEY);
        return new DropboxSession(this, parentSession, new NodeProvider(m_mountPoint, m_provider),m_mountPoint);
    }

    @Override
    public Session login(Credentials credentials, String workspaceName) throws LoginException, NoSuchWorkspaceException, RepositoryException
    {
        throw new LoginException("Not supported");
    }

    @Override
    public Session login(Credentials credentials) throws LoginException, RepositoryException
    {
        throw new LoginException("Not supported");
    }

    @Override
    public Session login(String workspaceName) throws LoginException, NoSuchWorkspaceException, RepositoryException
    {
        throw new LoginException("Not supported");
    }

    @Override
    public Session login() throws LoginException, RepositoryException
    {
        throw new LoginException("Not supported");
    }

    @Override
    public void shutdown()
    {

    }

    @Override
    public String[] getDescriptorKeys()
    {
        return new String[0];
    }

    @Override
    public boolean isStandardDescriptor(String key)
    {
        return false;
    }

    @Override
    public boolean isSingleValueDescriptor(String key)
    {
        return false;
    }

    @Override
    public Value getDescriptorValue(String key)
    {
        return null;
    }

    @Override
    public Value[] getDescriptorValues(String key)
    {
        return new Value[0];
    }

    @Override
    public String getDescriptor(String key)
    {
        return null;
    }
}
