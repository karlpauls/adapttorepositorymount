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

import javax.jcr.AccessDeniedException;
import javax.jcr.InvalidItemStateException;
import javax.jcr.Item;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.ItemVisitor;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.ReferentialIntegrityException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;

import org.apache.jackrabbit.oak.commons.PathUtils;

public abstract class AbstractItem implements Item {
    protected final String path;
    protected final RepositoryContext context;

    public AbstractItem(String path, RepositoryContext context) {
        this.path = path;
        this.context = context;
    }

    @Override
    public String getPath() throws RepositoryException {
        return path;
    }

    @Override
    public String getName() throws RepositoryException {
        return PathUtils.getName(path);
    }

    @Override
    public Item getAncestor(int depth) throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        return this.context.getSession().getItem(PathUtils.getAncestorPath(path, depth));
    }

    @Override
    public Node getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        return this.context.getSession().getNode(PathUtils.getParentPath(path));
    }

    @Override
    public int getDepth() throws RepositoryException {
        return PathUtils.getDepth(path);
    }

    @Override
    public Session getSession() throws RepositoryException {
        return this.context.getSession();
    }

    @Override
    public boolean isSame(Item otherItem) throws RepositoryException {
        return getPath().equals(otherItem.getPath());
    }

    @Override
    public void accept(ItemVisitor visitor) throws RepositoryException {
        if (isNode()) {
            visitor.visit((Node) this);
        } else {
            visitor.visit((Property) this);
        }
    }

    @Override
    public void save() throws AccessDeniedException, ItemExistsException, ConstraintViolationException, InvalidItemStateException, ReferentialIntegrityException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException {
        this.context.getSession().save();
    }

    @Override
    public void refresh(final boolean keepChanges) throws InvalidItemStateException, RepositoryException {
        this.context.getSession().refresh(keepChanges);
    }

    @Override
    public boolean isModified()
    {
        return false;
    }

    @Override
    public boolean isNew()
    {
        return false;
    }

    @Override
    public void remove() throws RepositoryException
    {
        throw new ConstraintViolationException();
    }
}
