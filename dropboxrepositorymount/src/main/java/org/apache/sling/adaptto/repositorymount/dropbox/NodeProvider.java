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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.commons.iterator.NodeIteratorAdapter;
import org.apache.jackrabbit.oak.commons.PathUtils;
import org.apache.sling.adaptto.repositorymount.dropbox.mappings.properties.BinaryProperty;
import org.apache.sling.adaptto.repositorymount.dropbox.mappings.properties.StringProperty;

public class NodeProvider
{
    private final DropboxProvider m_provider;
    private final String m_mountPoint;

    public NodeProvider(String mountPoint,
        DropboxProvider provider) throws RepositoryException {
        m_mountPoint = mountPoint;
        m_provider = provider;
    }


    public DropboxNode getNode(String path, RepositoryContext context) throws RepositoryException {
        if (m_mountPoint.startsWith(path)) {
            return new DropboxNode(this, path.equals("/") ? "" : path, context, null, JcrConstants.NT_FOLDER, null);
        }
        Map<String, Object> node = m_provider.get(path);
        if (node != null) {
            return createNode(PathUtils.getParentPath(path), node, context);
        }
        else {
            return null;
        }
    }

    public NodeIterator getNodes(final String path, RepositoryContext context) throws RepositoryException {
        Iterator<Map<String, Object>> nodes = m_provider.getChildren(path);
        return new NodeIteratorAdapter(new Iterator()
        {
            @Override
            public boolean hasNext()
            {
                return nodes.hasNext();
            }

            @Override
            public Object next()
            {
                try
                {
                    return createNode(path, nodes.next(), context);
                }
                catch (RepositoryException e)
                {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private DropboxNode createNode(String parent, Map<String, Object> node, RepositoryContext context) throws RepositoryException {
        DropboxNode result;

        List<Property> properties = new ArrayList<>();
        Property primary = null;

        for (Map.Entry<String, Object> entry : node.entrySet())  {
            String path = PathUtils.concat(PathUtils.concat(parent, (String) node.get(JcrConstants.JCR_NAME)), entry.getKey());
            Property prop = null;
            if (entry.getValue() instanceof String) {
                properties.add(prop = new StringProperty(context,
                    path, (String) entry.getValue()));
            }
            else if (entry.getKey().equals(JcrConstants.JCR_DATA) && entry.getValue() instanceof Callable) {
                properties.add(prop = new BinaryProperty(context, path, (Callable<InputStream>) entry.getValue()));
            }

            if (JcrConstants.NT_RESOURCE.equals(node.get(JcrConstants.JCR_PRIMARYTYPE)) && entry.getKey().equals(JcrConstants.JCR_DATA)) {
                primary = prop;
            }
        }

        result = new DropboxNode(this, PathUtils.concat(parent, (String) node.get(JcrConstants.JCR_NAME)),
            context, primary, (String) node.get(JcrConstants.JCR_PRIMARYTYPE), null, properties.toArray(new Property[0]));

        return result;
    }
}
