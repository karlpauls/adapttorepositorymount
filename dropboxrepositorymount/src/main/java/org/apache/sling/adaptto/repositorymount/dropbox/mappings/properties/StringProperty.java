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
package org.apache.sling.adaptto.repositorymount.dropbox.mappings.properties;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;

import org.apache.sling.adaptto.repositorymount.dropbox.AbstractProperty;
import org.apache.sling.adaptto.repositorymount.dropbox.RepositoryContext;

public class StringProperty extends AbstractProperty
{

    public StringProperty(final RepositoryContext jcr, final String path, final String value) throws RepositoryException {
        super(jcr, path, createValue( jcr, value == null ? "" : value));
    }

    public StringProperty(final RepositoryContext jcr, final String path, final String[] values) throws RepositoryException {
        super(jcr, path, createValues(jcr,values));
    }

    private static Value createValue(final RepositoryContext jcr, final String val) throws RepositoryException {
        return jcr.getParentSession().getValueFactory().createValue(val == null ? "" : val);
    }

    private static Value[] createValues(final RepositoryContext parent, final String[] values) throws RepositoryException {
        final Value[] result = new Value[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = createValue(parent, values[i]);
        }
        return result;
    }

    @Override
    public PropertyDefinition getDefinition() throws RepositoryException {
        return new PropertyDefinition() {
            @Override
            public int getRequiredType() {
                return PropertyType.STRING;
            }

            @Override
            public String[] getValueConstraints() {
                return new String[0];
            }

            @Override
            public Value[] getDefaultValues() {
                return new Value[0];
            }

            @Override
            public boolean isMultiple() {
                try {
                    return StringProperty.this.isMultiple();
                } catch (RepositoryException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public String[] getAvailableQueryOperators() {
                return new String[0];
            }

            @Override
            public boolean isFullTextSearchable() {
                return false;
            }

            @Override
            public boolean isQueryOrderable() {
                return false;
            }

            @Override
            public NodeType getDeclaringNodeType() {
                return null;
            }

            @Override
            public String getName() {
                try {
                    return StringProperty.this.getName();
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
                return true;
            }
        };
    }
}
