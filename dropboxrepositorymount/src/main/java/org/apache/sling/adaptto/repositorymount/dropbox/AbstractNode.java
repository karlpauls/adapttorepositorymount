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

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.jcr.AccessDeniedException;
import javax.jcr.Binary;
import javax.jcr.InvalidItemStateException;
import javax.jcr.InvalidLifecycleTransitionException;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.MergeException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.ActivityViolationException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionHistory;

import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.commons.iterator.NodeIteratorAdapter;
import org.apache.jackrabbit.commons.iterator.PropertyIteratorAdapter;
import org.apache.jackrabbit.oak.commons.PathUtils;
import org.apache.sling.adaptto.repositorymount.dropbox.mappings.properties.StringProperty;

public abstract class AbstractNode extends AbstractItem implements Node {

    private final String uuid;

    final Map<String, Property> properties = new HashMap<>();
    private final NodeProvider provider;

    public AbstractNode(final String path,
                        final RepositoryContext context,
                        final String primaryType,
                        final String uuid,
        final NodeProvider provider) throws RepositoryException {
        super(path, context);
        this.uuid = uuid;
        if (context.getParentSession() != null)
        {
            this.properties.put(JcrConstants.JCR_PRIMARYTYPE, new StringProperty(context, PathUtils.concat(path, JcrConstants.JCR_PRIMARYTYPE), primaryType));
        }
        this.provider = provider;
    }

    @Override
    public boolean isNew() {
        return false;
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void remove() throws VersionException, LockException, ConstraintViolationException, AccessDeniedException, RepositoryException {
        throw new AccessDeniedException();
    }

    public boolean isNode() {
        return true;
    }

    @Override
    public boolean hasNodes() throws RepositoryException {
        return getNodes().hasNext();
    }

    @Override
    public Node getNode(String relPath) throws PathNotFoundException, RepositoryException {
        return this.context.getSession().getNode(PathUtils.concat(getPath(), relPath));
    }

    @Override
    public boolean hasNode(String relPath) throws RepositoryException {
        return this.context.getSession().nodeExists(PathUtils.concat(getPath(), relPath));
    }

    @Override
    public NodeIterator getNodes() throws RepositoryException {
        return this.provider.getNodes(this.getPath(), this.context);
    }

    @Override
    public NodeIterator getNodes(String namePattern) throws RepositoryException {
        // TODO
        return new NodeIteratorAdapter(Collections.emptyIterator());
    }

    @Override
    public NodeIterator getNodes(String[] nameGlobs) throws RepositoryException {
        // TODO
        return new NodeIteratorAdapter(Collections.emptyIterator());
    }

    @Override
    public String getUUID() throws UnsupportedRepositoryOperationException, RepositoryException {
        if (this.uuid == null) {
            throw new UnsupportedRepositoryOperationException();
        }
        return this.uuid;
    }

    @Override
    public String getIdentifier() throws RepositoryException {
        if (this.uuid == null) {
            return this.getParent().getIdentifier().concat("/").concat(this.getName());
        }
        return this.uuid;
    }

    @Override
    public int getIndex() throws RepositoryException {
        return 0;
    }

    @Override
    public PropertyIterator getReferences() throws RepositoryException {
        return this.getReferences(null);
    }

    @Override
    public PropertyIterator getReferences(String name) throws RepositoryException {
        return new PropertyIteratorAdapter(Collections.emptyList());
    }

    @Override
    public PropertyIterator getWeakReferences() throws RepositoryException {
        return new PropertyIteratorAdapter(Collections.emptyList());
    }

    @Override
    public PropertyIterator getWeakReferences(String name) throws RepositoryException {
        return new PropertyIteratorAdapter(Collections.emptyList());
    }

    @Override
    public void setPrimaryType(String nodeTypeName) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
// TODO
    }

    @Override
    public void addMixin(String mixinName) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        // TODO
    }

    @Override
    public void removeMixin(String mixinName) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        // TODO// TODO

    }

    @Override
    public boolean canAddMixin(String mixinName) throws NoSuchNodeTypeException, RepositoryException {
        return false;
    }

    @Override
    public NodeDefinition getDefinition() throws RepositoryException {
        // TODO
        return new NodeDefinition() {
            @Override
            public NodeType[] getRequiredPrimaryTypes() {
                return new NodeType[0];
            }

            @Override
            public String[] getRequiredPrimaryTypeNames() {
                return new String[0];
            }

            @Override
            public NodeType getDefaultPrimaryType() {
                return null;
            }

            @Override
            public String getDefaultPrimaryTypeName() {
                return null;
            }

            @Override
            public boolean allowsSameNameSiblings() {
                return false;
            }

            @Override
            public NodeType getDeclaringNodeType() {
                return null;
            }

            @Override
            public String getName() {
                try {
                    return AbstractNode.this.getName();
                } catch (RepositoryException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public boolean isAutoCreated() {
                return false;
            }

            @Override
            public boolean isMandatory() {
                return false;
            }

            @Override
            public int getOnParentVersion() {
                return 0;
            }

            @Override
            public boolean isProtected() {
                return false;
            }
        };
    }

    @Override
    public Version checkin() throws VersionException, UnsupportedRepositoryOperationException, InvalidItemStateException, LockException, RepositoryException {
        // versioning is not supported
        throw new UnsupportedRepositoryOperationException();
    }

    @Override
    public void checkout() throws UnsupportedRepositoryOperationException, LockException, ActivityViolationException, RepositoryException {
        // versioning is not supported
        throw new UnsupportedRepositoryOperationException();
    }

    @Override
    public void doneMerge(Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException {
        // versioning is not supported
        throw new UnsupportedRepositoryOperationException();
    }

    @Override
    public void cancelMerge(Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException {
        // versioning is not supported
        throw new UnsupportedRepositoryOperationException();
    }

    @Override
    public void update(String srcWorkspace) throws NoSuchWorkspaceException, AccessDeniedException, LockException, InvalidItemStateException, RepositoryException {
        // workspace operation is not supported
        throw new UnsupportedRepositoryOperationException();
    }

    @Override
    public NodeIterator merge(String srcWorkspace, boolean bestEffort) throws NoSuchWorkspaceException, AccessDeniedException, MergeException, LockException, InvalidItemStateException, RepositoryException {
        // workspace operation is not supported
        throw new UnsupportedRepositoryOperationException();
    }

    @Override
    public String getCorrespondingNodePath(String workspaceName) throws ItemNotFoundException, NoSuchWorkspaceException, AccessDeniedException, RepositoryException {
        // workspace operation is not supported
        throw new UnsupportedRepositoryOperationException();
    }

    @Override
    public NodeIterator getSharedSet() throws RepositoryException {
        // TODO
        return null;
    }

    @Override
    public void removeSharedSet() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        // TODO

    }

    @Override
    public void removeShare() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        // TODO

    }

    @Override
    public boolean isCheckedOut() throws RepositoryException {
        return true;
    }

    @Override
    public void restore(String versionName, boolean removeExisting) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
        // TODO

    }

    @Override
    public void restore(Version version, boolean removeExisting) throws VersionException, ItemExistsException, InvalidItemStateException, UnsupportedRepositoryOperationException, LockException, RepositoryException {
        // TODO

    }

    @Override
    public void restore(Version version, String relPath, boolean removeExisting) throws PathNotFoundException, ItemExistsException, VersionException, ConstraintViolationException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
        // TODO

    }

    @Override
    public void restoreByLabel(String versionLabel, boolean removeExisting) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
        // TODO

    }

    @Override
    public VersionHistory getVersionHistory() throws UnsupportedRepositoryOperationException, RepositoryException {
        // TODO
        return null;
    }

    @Override
    public Version getBaseVersion() throws UnsupportedRepositoryOperationException, RepositoryException {
        // TODO
        return null;
    }

    @Override
    public Lock lock(boolean isDeep, boolean isSessionScoped) throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException {
        // locking is not supported
        throw new UnsupportedRepositoryOperationException();
    }

    @Override
    public Lock getLock() throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, RepositoryException {
        // locking is not supported
        throw new UnsupportedRepositoryOperationException();
    }

    @Override
    public void unlock() throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException {
        // locking is not supported
        throw new UnsupportedRepositoryOperationException();
    }

    @Override
    public boolean holdsLock() throws RepositoryException {
        // locking is not supported
        return false;
    }

    @Override
    public boolean isLocked() throws RepositoryException {
        // locking is not supported
        return false;
    }

    @Override
    public void followLifecycleTransition(String transition) throws UnsupportedRepositoryOperationException, InvalidLifecycleTransitionException, RepositoryException {
        // not supported
        throw new UnsupportedRepositoryOperationException();
    }

    @Override
    public String[] getAllowedLifecycleTransistions() throws UnsupportedRepositoryOperationException, RepositoryException {
        // not supported
        throw new UnsupportedRepositoryOperationException();
    }

    @Override
    public Property getProperty(final String relPath) throws PathNotFoundException, RepositoryException {
        if (relPath.startsWith("/")) {
            return this.getSession().getProperty(PathUtils.relativize(this.getPath(), relPath));
        }
        final String fullPath = this.getPath().concat("/").concat(relPath);
        if (relPath.contains("/")) {
            return this.getSession().getProperty(fullPath);
        }
        final Property prop = this.properties.get(relPath);
        if (prop == null) {
            throw new PathNotFoundException("Property " + fullPath);
        }
        return prop;
    }

    @Override
    public PropertyIterator getProperties() throws RepositoryException {
        return new PropertyIteratorAdapter(this.properties.values());
    }

    @Override
    public PropertyIterator getProperties(String namePattern) throws RepositoryException {
        // TODO
        return new PropertyIteratorAdapter(Collections.emptyIterator());
    }

    @Override
    public PropertyIterator getProperties(String[] nameGlobs) throws RepositoryException {
        // TODO
        return new PropertyIteratorAdapter(Collections.emptyIterator());
    }

    @Override
    public boolean hasProperty(String relPath) throws RepositoryException {
        if (relPath.startsWith("/")) {
            return this.getSession().propertyExists(PathUtils.relativize(this.getPath(), relPath));
        }
        final String fullPath = this.getPath().concat("/").concat(relPath);
        if (relPath.contains("/")) {
            return this.getSession().propertyExists(fullPath);
        }
        return this.properties.containsKey(relPath);
    }

    @Override
    public boolean hasProperties() throws RepositoryException {
        return !this.properties.isEmpty();
    }

    @Override
    public Property setProperty(String name, Value value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        throw new ConstraintViolationException();
    }

    @Override
    public Property setProperty(String name, Value value, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        throw new ConstraintViolationException();
    }

    @Override
    public Property setProperty(String name, Value[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        throw new ConstraintViolationException();
    }

    @Override
    public Property setProperty(String name, Value[] values, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        throw new ConstraintViolationException();
    }

    @Override
    public Property setProperty(String name, String[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return setProperty(name, values, PropertyType.STRING);
    }

    @Override
    public Property setProperty(String name, String[] values, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        throw new ConstraintViolationException();
    }

    @Override
    public Property setProperty(String name, String value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        throw new ConstraintViolationException();
    }

    @Override
    public Property setProperty(String name, String value, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        throw new ConstraintViolationException();
    }

    @Override
    public Property setProperty(String name, InputStream value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        throw new ConstraintViolationException();
    }

    @Override
    public Property setProperty(String name, Binary value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        throw new ConstraintViolationException();
    }

    @Override
    public Property setProperty(String name, boolean value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        throw new ConstraintViolationException();
    }

    @Override
    public Property setProperty(String name, double value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        throw new ConstraintViolationException();
    }

    @Override
    public Property setProperty(String name, BigDecimal value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        throw new ConstraintViolationException();
    }

    @Override
    public Property setProperty(String name, long value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        throw new ConstraintViolationException();
    }

    @Override
    public Property setProperty(String name, Calendar value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        throw new ConstraintViolationException();
    }

    @Override
    public Property setProperty(String name, Node value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        throw new ConstraintViolationException();
    }

    @Override
    public NodeType getPrimaryNodeType() throws RepositoryException {
        final Property typeProp = this.properties.get(JcrConstants.JCR_PRIMARYTYPE);
        return this.context.getParentSession().getWorkspace().getNodeTypeManager().getNodeType(typeProp.getValue().getString());
    }

    @Override
    public NodeType[] getMixinNodeTypes() throws RepositoryException {
        final Property typeProp = this.properties.get(JcrConstants.JCR_MIXINTYPES);
        if (typeProp == null || typeProp.getValues().length == 0) {
            return new NodeType[0];
        }
        final NodeType[] result = new NodeType[typeProp.getValues().length];
        int index = 0;
        for (final Value val : typeProp.getValues()) {
            result[index] = this.context.getParentSession().getWorkspace().getNodeTypeManager().getNodeType(val.getString());
            index++;
        }
        return result;
    }

    @Override
    public boolean isNodeType(String nodeTypeName) throws RepositoryException {
        final Property typeProp = this.properties.get(JcrConstants.JCR_PRIMARYTYPE);
        if (nodeTypeName.equals(typeProp.getValue().getString())) {
            return true;
        }
        final NodeType nd = this.getPrimaryNodeType();
        if (nd.isNodeType(nodeTypeName)) {
            return true;
        }
        for (final NodeType nt : this.getMixinNodeTypes()) {
            if (nt.isNodeType(nodeTypeName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Node addNode(String relPath) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        //throw new ConstraintViolationException();
        return null;
    }

    @Override
    public Node addNode(String relPath, String primaryNodeTypeName) throws ItemExistsException, PathNotFoundException, NoSuchNodeTypeException, LockException, VersionException, ConstraintViolationException, RepositoryException {
        //throw new ConstraintViolationException();
        return null;
    }

    @Override
    public void orderBefore(String srcChildRelPath, String destChildRelPath) throws UnsupportedRepositoryOperationException, VersionException, ConstraintViolationException, ItemNotFoundException, LockException, RepositoryException
    {

    }
}
