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

import javax.jcr.Item;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;


public class DropboxNode extends AbstractNode
{
    private final Item primary;

    public DropboxNode(NodeProvider provider, String path, RepositoryContext context, Item primary, String primaryType, String uuid, Property... properties) throws RepositoryException
    {
        super(path, context, primaryType, uuid, provider);
        this.primary = primary;
        for (Property property : properties) {
            this.properties.put(property.getName(), property);
        }
    }

    @Override
    public Item getPrimaryItem() throws ItemNotFoundException, RepositoryException
    {
        if (primary != null) {
            return primary;
        }
        else {
            throw new ItemNotFoundException();
        }
    }
}
