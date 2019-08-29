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
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessControlException;
import java.security.Principal;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import javax.jcr.AccessDeniedException;
import javax.jcr.Credentials;
import javax.jcr.InvalidItemStateException;
import javax.jcr.InvalidSerializedDataException;
import javax.jcr.Item;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.LoginException;
import javax.jcr.NamespaceException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.ReferentialIntegrityException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.ValueFactory;
import javax.jcr.Workspace;
import javax.jcr.lock.LockException;
import javax.jcr.lock.LockManager;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.InvalidNodeTypeDefinitionException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeDefinitionTemplate;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeDefinition;
import javax.jcr.nodetype.NodeTypeExistsException;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.NodeTypeTemplate;
import javax.jcr.nodetype.PropertyDefinitionTemplate;
import javax.jcr.observation.ObservationManager;
import javax.jcr.query.QueryManager;
import javax.jcr.retention.RetentionManager;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.AccessControlPolicy;
import javax.jcr.security.AccessControlPolicyIterator;
import javax.jcr.security.Privilege;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionManager;

import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.api.JackrabbitWorkspace;
import org.apache.jackrabbit.api.security.JackrabbitAccessControlManager;
import org.apache.jackrabbit.api.security.JackrabbitAccessControlPolicy;
import org.apache.jackrabbit.api.security.authorization.PrivilegeManager;
import org.apache.jackrabbit.api.security.principal.PrincipalManager;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.AuthorizableExistsException;
import org.apache.jackrabbit.api.security.user.AuthorizableTypeException;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.Query;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.jackrabbit.oak.commons.PathUtils;
import org.apache.sling.api.resource.path.Path;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class DropboxSession implements JackrabbitSession
{
    private final DropboxRepository m_repository;
    private final JackrabbitSession m_parentSession;
    private final String m_mountPoint;
    private final NodeProvider m_provider;
    private final RepositoryContext m_context;

    public DropboxSession(DropboxRepository repository, JackrabbitSession parentSession, NodeProvider provider, String mountPoint)
    {
        m_repository = repository;
        m_parentSession = parentSession;
        m_mountPoint = mountPoint;
        m_provider = provider;
        m_context = new RepositoryContext(repository, this, parentSession);
    }

    @Override
    public Repository getRepository()
    {
        return m_repository;
    }

    @Override
    public String getUserID()
    {
        return m_parentSession.getUserID();
    }

    @Override
    public String[] getAttributeNames()
    {
        return m_parentSession.getAttributeNames();
    }

    @Override
    public Object getAttribute(String name)
    {
        return m_parentSession.getAttribute(name);
    }

    @Override
    public Node getRootNode() throws RepositoryException
    {
        return m_parentSession.getRootNode();
    }

    @Override
    public Session impersonate(Credentials credentials) throws LoginException, RepositoryException
    {
        return new DropboxSession(m_repository, m_parentSession, m_provider, m_mountPoint);
    }

    @Override
    public Node getNodeByUUID(String uuid) throws ItemNotFoundException, RepositoryException
    {
        throw new ItemNotFoundException("Not implemented");
    }

    @Override
    public Node getNodeByIdentifier(String id) throws ItemNotFoundException, RepositoryException
    {
        throw new ItemNotFoundException("Not implemented");
    }

    @Override
    public boolean hasPermission(String absPath, String... actions) throws RepositoryException
    {
        return true;
    }

    @Override
    public PrincipalManager getPrincipalManager() throws AccessDeniedException, UnsupportedRepositoryOperationException, RepositoryException
    {
        return null;
    }

    @Override
    public UserManager getUserManager() throws AccessDeniedException, UnsupportedRepositoryOperationException, RepositoryException {
        return new UserManager()
        {
            @Override
            public Authorizable getAuthorizable(String id) throws RepositoryException
            {
                return null;
            }

            @Override
            public <T extends Authorizable> T getAuthorizable(String id, Class<T> authorizableClass) throws AuthorizableTypeException, RepositoryException
            {
                return null;
            }

            @Override
            public Authorizable getAuthorizable(Principal principal) throws RepositoryException
            {
                return null;
            }

            @Override
            public Authorizable getAuthorizableByPath(String path) throws UnsupportedRepositoryOperationException, RepositoryException
            {
                return null;
            }

            @Override
            public Iterator<Authorizable> findAuthorizables(String relPath, String value) throws RepositoryException
            {
                return null;
            }

            @Override
            public Iterator<Authorizable> findAuthorizables(String relPath, String value, int searchType) throws RepositoryException
            {
                return null;
            }

            @Override
            public Iterator<Authorizable> findAuthorizables(Query query) throws RepositoryException
            {
                return null;
            }

            @Override
            public User createUser(String userID, String password) throws AuthorizableExistsException, RepositoryException
            {
                return null;
            }

            @Override
            public User createUser(String userID, String password, Principal principal, String intermediatePath) throws AuthorizableExistsException, RepositoryException
            {
                return null;
            }

            @Override
            public User createSystemUser(String userID, String intermediatePath) throws AuthorizableExistsException, RepositoryException
            {
                return null;
            }

            @Override
            public Group createGroup(String groupID) throws AuthorizableExistsException, RepositoryException
            {
                return null;
            }

            @Override
            public Group createGroup(Principal principal) throws AuthorizableExistsException, RepositoryException
            {
                return null;
            }

            @Override
            public Group createGroup(Principal principal, String intermediatePath) throws AuthorizableExistsException, RepositoryException
            {
                return null;
            }

            @Override
            public Group createGroup(String groupID, Principal principal, String intermediatePath) throws AuthorizableExistsException, RepositoryException
            {
                return null;
            }

            @Override
            public boolean isAutoSave()
            {
                return false;
            }

            @Override
            public void autoSave(boolean enable) throws UnsupportedRepositoryOperationException, RepositoryException
            {

            }
        };
    }

    @Override
    public Item getItemOrNull(String s) throws RepositoryException {
        Item result = m_provider.getNode(s, m_context);
        if (result == null) {
            Node parent = m_provider.getNode(PathUtils.getParentPath(s), m_context);
            if (parent != null && parent.hasProperty(PathUtils.getName(s))) {
                result = parent.getProperty(PathUtils.getName(s));
            }
        }

        return result;
    }

    @Override
    public Property getPropertyOrNull(String s) throws RepositoryException {
        Node parent = m_provider.getNode(PathUtils.getParentPath(s), m_context);
        if (parent != null && parent.hasProperty(PathUtils.getName(s))) {
            return parent.getProperty(PathUtils.getName(s));
        }
        return null;
    }

    @Override
    public Node getNodeOrNull(String s) throws RepositoryException {
        return m_provider.getNode(s, m_context);
    }

    @Override
    public Item getItem(String absPath) throws PathNotFoundException, RepositoryException {
        final Item item = this.getItemOrNull(absPath);
        if (item == null) {
            throw new PathNotFoundException(absPath);
        }
        return item;
    }

    @Override
    public Node getNode(String absPath) throws PathNotFoundException, RepositoryException {
        final Node node = this.getNodeOrNull(absPath);
        if (node == null) {
            throw new PathNotFoundException(absPath);
        }
        return node;
    }

    @Override
    public Property getProperty(String absPath) throws PathNotFoundException, RepositoryException {
        final Property prop = this.getPropertyOrNull(absPath);
        if (prop == null) {
            throw new PathNotFoundException(absPath);
        }
        return prop;
    }

    @Override
    public boolean itemExists(String absPath) throws RepositoryException {
        return nodeExists(absPath) || propertyExists(absPath);
    }

    @Override
    public boolean nodeExists(String absPath) throws RepositoryException {
        return this.getNodeOrNull(absPath) != null;
    }

    @Override
    public boolean propertyExists(String absPath) throws RepositoryException {
        return this.getPropertyOrNull(absPath) != null;
    }

    @Override
    public void move(String srcAbsPath, String destAbsPath) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, RepositoryException
    {

    }

    @Override
    public void removeItem(String absPath) throws VersionException, LockException, ConstraintViolationException, AccessDeniedException, RepositoryException
    {

    }

    @Override
    public void save() throws AccessDeniedException, ItemExistsException, ReferentialIntegrityException, ConstraintViolationException, InvalidItemStateException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException
    {

    }

    @Override
    public void refresh(boolean keepChanges) throws RepositoryException
    {

    }

    @Override
    public boolean hasPendingChanges() throws RepositoryException
    {
        return false;
    }

    @Override
    public ValueFactory getValueFactory() throws UnsupportedRepositoryOperationException, RepositoryException
    {
        return null;
    }

    @Override
    public boolean hasPermission(String absPath, String actions) throws RepositoryException
    {
        return true;
    }

    @Override
    public void checkPermission(String absPath, String actions) throws AccessControlException, RepositoryException
    {

    }

    @Override
    public boolean hasCapability(String methodName, Object target, Object[] arguments) throws RepositoryException
    {
        return false;
    }

    @Override
    public ContentHandler getImportContentHandler(String parentAbsPath, int uuidBehavior) throws PathNotFoundException, ConstraintViolationException, VersionException, LockException, RepositoryException
    {
        return null;
    }

    @Override
    public void importXML(String parentAbsPath, InputStream in, int uuidBehavior) throws IOException, PathNotFoundException, ItemExistsException, ConstraintViolationException, VersionException, InvalidSerializedDataException, LockException, RepositoryException
    {

    }

    @Override
    public void exportSystemView(String absPath, ContentHandler contentHandler, boolean skipBinary, boolean noRecurse) throws PathNotFoundException, SAXException, RepositoryException
    {

    }

    @Override
    public void exportSystemView(String absPath, OutputStream out, boolean skipBinary, boolean noRecurse) throws IOException, PathNotFoundException, RepositoryException
    {

    }

    @Override
    public void exportDocumentView(String absPath, ContentHandler contentHandler, boolean skipBinary, boolean noRecurse) throws PathNotFoundException, SAXException, RepositoryException
    {

    }

    @Override
    public void exportDocumentView(String absPath, OutputStream out, boolean skipBinary, boolean noRecurse) throws IOException, PathNotFoundException, RepositoryException
    {

    }

    @Override
    public void setNamespacePrefix(String prefix, String uri) throws NamespaceException, RepositoryException
    {

    }

    @Override
    public String[] getNamespacePrefixes() throws RepositoryException
    {
        return new String[0];
    }

    @Override
    public String getNamespaceURI(String prefix) throws NamespaceException, RepositoryException
    {
        return null;
    }

    @Override
    public String getNamespacePrefix(String uri) throws NamespaceException, RepositoryException
    {
        return null;
    }

    @Override
    public void logout()
    {

    }

    @Override
    public boolean isLive()
    {
        return true;
    }

    @Override
    public void addLockToken(String lt)
    {

    }

    @Override
    public String[] getLockTokens()
    {
        return new String[0];
    }

    @Override
    public void removeLockToken(String lt)
    {

    }

    @Override
    public AccessControlManager getAccessControlManager() throws UnsupportedRepositoryOperationException, RepositoryException
    {
        return new JackrabbitAccessControlManager() {

            @Override
            public void setPolicy(String absPath, AccessControlPolicy policy)
                throws PathNotFoundException, javax.jcr.security.AccessControlException, AccessDeniedException,
                LockException, VersionException, RepositoryException {
                //throw new RepositoryException("AccessControlManager.setPolicy");
            }

            @Override
            public void removePolicy(String absPath, AccessControlPolicy policy)
                throws PathNotFoundException, javax.jcr.security.AccessControlException, AccessDeniedException,
                LockException, VersionException, RepositoryException {
                throw new RepositoryException("AccessControlManager.removePolicy");
            }

            @Override
            public Privilege privilegeFromName(String privilegeName)
                throws javax.jcr.security.AccessControlException, RepositoryException {
                return m_context.getParentSession().getAccessControlManager().privilegeFromName(privilegeName);
            }

            @Override
            public boolean hasPrivileges(String absPath, Privilege[] privileges)
                throws PathNotFoundException, RepositoryException {

                return true;
            }

            @Override
            public Privilege[] getSupportedPrivileges(String absPath) throws PathNotFoundException, RepositoryException {
                return new Privilege[0];
            }

            @Override
            public Privilege[] getPrivileges(final String absPath) throws PathNotFoundException, RepositoryException {
                return new Privilege[0];
            }

            @Override
            public AccessControlPolicy[] getPolicies(String absPath)
                throws PathNotFoundException, AccessDeniedException, RepositoryException {
                throw new RepositoryException("AccessControlManager.getPolicies");
            }

            @Override
            public AccessControlPolicy[] getEffectivePolicies(String absPath)
                throws PathNotFoundException, AccessDeniedException, RepositoryException {
                throw new RepositoryException("AccessControlManager.getEffectivePolicies");
            }

            @Override
            public AccessControlPolicyIterator getApplicablePolicies(String absPath)
                throws PathNotFoundException, AccessDeniedException, RepositoryException {
                throw new RepositoryException("AccessControlManager.getApplicablePolicies");
            }

            @Override
            public boolean hasPrivileges(String absPath, Set<Principal> principals, Privilege[] privileges)
                throws PathNotFoundException, AccessDeniedException, RepositoryException {
                return hasPrivileges(absPath, privileges);
            }

            @Override
            public Privilege[] getPrivileges(String absPath, Set<Principal> principals)
                throws PathNotFoundException, AccessDeniedException, RepositoryException {
                return getPrivileges(absPath);
            }

            @Override
            public JackrabbitAccessControlPolicy[] getPolicies(Principal principal) throws AccessDeniedException,
                javax.jcr.security.AccessControlException, UnsupportedRepositoryOperationException, RepositoryException {
                throw new RepositoryException("AccessControlManager.getPolicies");
            }

            @Override
            public AccessControlPolicy[] getEffectivePolicies(Set<Principal> principals) throws AccessDeniedException,
                javax.jcr.security.AccessControlException, UnsupportedRepositoryOperationException, RepositoryException {
                throw new RepositoryException("AccessControlManager.getEffectivePolicies");
            }

            @Override
            public JackrabbitAccessControlPolicy[] getApplicablePolicies(Principal principal) throws AccessDeniedException,
                javax.jcr.security.AccessControlException, UnsupportedRepositoryOperationException, RepositoryException {
                throw new RepositoryException("AccessControlManager.getApplicablePolicies");
            }
        };
    }

    @Override
    public RetentionManager getRetentionManager() throws UnsupportedRepositoryOperationException, RepositoryException
    {
        return null;
    }

    @Override
    public Workspace getWorkspace() {
        return new JackrabbitWorkspace() {
            @Override
            public Session getSession() {
                return DropboxSession.this;
            }

            @Override
            public String getName() {
                return "default";
            }

            @Override
            public void copy(String srcAbsPath, String destAbsPath) throws ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, RepositoryException {
                throw new RepositoryException();
            }

            @Override
            public void copy(String srcWorkspace, String srcAbsPath, String destAbsPath) throws NoSuchWorkspaceException, ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, RepositoryException {
                throw new RepositoryException();
            }

            @Override
            public void clone(String srcWorkspace, String srcAbsPath, String destAbsPath, boolean removeExisting) throws NoSuchWorkspaceException, ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, RepositoryException {
                throw new RepositoryException();
            }

            @Override
            public void move(String srcAbsPath, String destAbsPath) throws ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, RepositoryException {
                throw new RepositoryException();
            }

            @Override
            public void restore(Version[] versions, boolean removeExisting) throws ItemExistsException, UnsupportedRepositoryOperationException, VersionException, LockException, InvalidItemStateException, RepositoryException {
                throw new RepositoryException();
            }

            @Override
            public LockManager getLockManager() throws UnsupportedRepositoryOperationException, RepositoryException {
                // this is never used
                return null;
            }

            @Override
            public QueryManager getQueryManager() throws RepositoryException {
                return new JCRQueryManager(DropboxSession.this, Collections.singleton(new Path(m_mountPoint)));
            }

            @Override
            public NamespaceRegistry getNamespaceRegistry() throws RepositoryException {
                return new NamespaceRegistry() {

                    @Override
                    public void unregisterNamespace(String prefix) throws NamespaceException, UnsupportedRepositoryOperationException,
                        AccessDeniedException, RepositoryException {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void registerNamespace(String prefix, String uri) throws NamespaceException,
                        UnsupportedRepositoryOperationException, AccessDeniedException, RepositoryException {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public String[] getURIs() throws RepositoryException {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public String getURI(String prefix) throws NamespaceException, RepositoryException {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public String[] getPrefixes() throws RepositoryException {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public String getPrefix(String uri) throws NamespaceException, RepositoryException {
                        // TODO Auto-generated method stub
                        return null;
                    }
                };
            }

            @Override
            public NodeTypeManager getNodeTypeManager() throws RepositoryException {
                return new NodeTypeManager() {
                    @Override
                    public NodeType getNodeType(String nodeTypeName) throws NoSuchNodeTypeException, RepositoryException {
                        return null;
                    }

                    @Override
                    public boolean hasNodeType(String name) throws RepositoryException {
                        return false;
                    }

                    @Override
                    public NodeTypeIterator getAllNodeTypes() throws RepositoryException {
                        return null;
                    }

                    @Override
                    public NodeTypeIterator getPrimaryNodeTypes() throws RepositoryException {
                        return null;
                    }

                    @Override
                    public NodeTypeIterator getMixinNodeTypes() throws RepositoryException {
                        return null;
                    }

                    @Override
                    public NodeTypeTemplate createNodeTypeTemplate() throws UnsupportedRepositoryOperationException, RepositoryException {
                        return null;
                    }

                    @Override
                    public NodeTypeTemplate createNodeTypeTemplate(NodeTypeDefinition ntd) throws UnsupportedRepositoryOperationException, RepositoryException {
                        return null;
                    }

                    @Override
                    public NodeDefinitionTemplate createNodeDefinitionTemplate() throws UnsupportedRepositoryOperationException, RepositoryException {
                        return null;
                    }

                    @Override
                    public PropertyDefinitionTemplate createPropertyDefinitionTemplate() throws UnsupportedRepositoryOperationException, RepositoryException {
                        return null;
                    }

                    @Override
                    public NodeType registerNodeType(NodeTypeDefinition ntd, boolean allowUpdate) throws InvalidNodeTypeDefinitionException, NodeTypeExistsException, UnsupportedRepositoryOperationException, RepositoryException {
                        return null;
                    }

                    @Override
                    public NodeTypeIterator registerNodeTypes(NodeTypeDefinition[] ntds, boolean allowUpdate) throws InvalidNodeTypeDefinitionException, NodeTypeExistsException, UnsupportedRepositoryOperationException, RepositoryException {
                        return null;
                    }

                    @Override
                    public void unregisterNodeType(String name) throws UnsupportedRepositoryOperationException, NoSuchNodeTypeException, RepositoryException {

                    }

                    @Override
                    public void unregisterNodeTypes(String[] names) throws UnsupportedRepositoryOperationException, NoSuchNodeTypeException, RepositoryException {

                    }
                };
            }

            @Override
            public ObservationManager getObservationManager() throws UnsupportedRepositoryOperationException, RepositoryException {
                // this is never called
                return null;
            }

            @Override
            public VersionManager getVersionManager() throws UnsupportedRepositoryOperationException, RepositoryException {
                // this is never called
                return null;
            }

            @Override
            public String[] getAccessibleWorkspaceNames() throws RepositoryException {
                // this is never called
                return null;
            }

            @Override
            public ContentHandler getImportContentHandler(String parentAbsPath, int uuidBehavior) throws PathNotFoundException, ConstraintViolationException, VersionException, LockException, AccessDeniedException, RepositoryException {
                // this is never called
                return null;
            }

            @Override
            public void importXML(String parentAbsPath, InputStream in, int uuidBehavior) throws IOException, VersionException, PathNotFoundException, ItemExistsException, ConstraintViolationException, InvalidSerializedDataException, LockException, AccessDeniedException, RepositoryException {
                // this is never called
            }

            @Override
            public void createWorkspace(String name) throws AccessDeniedException, UnsupportedRepositoryOperationException, RepositoryException {
                // this is never called
            }

            @Override
            public void createWorkspace(String s, InputSource inputSource) throws AccessDeniedException, RepositoryException {
                // this is never called
            }

            @Override
            public PrivilegeManager getPrivilegeManager() throws RepositoryException {
                // this is never called
                return null;
            }

            @Override
            public void createWorkspace(String name, String srcWorkspace) throws AccessDeniedException, UnsupportedRepositoryOperationException, NoSuchWorkspaceException, RepositoryException {
                // this is never called
            }

            @Override
            public void deleteWorkspace(String name) throws AccessDeniedException, UnsupportedRepositoryOperationException, NoSuchWorkspaceException, RepositoryException {
                // this is never called
            }
        };
    }
}
