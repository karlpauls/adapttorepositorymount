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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.concurrent.Callable;
import javax.jcr.Binary;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;

import org.apache.sling.adaptto.repositorymount.dropbox.AbstractProperty;
import org.apache.sling.adaptto.repositorymount.dropbox.RepositoryContext;

public class BinaryProperty extends AbstractProperty
{

    public BinaryProperty(final RepositoryContext jcr,
                          final String path,
                          final Callable<InputStream> streamProvider) throws RepositoryException {
        super(jcr, path, createValue(streamProvider));
    }

    private static Value createValue(Callable<InputStream> streamProvider) {
        return new Value() {

            private volatile ByteArrayOutputStream buffer;

            @Override
            public String getString() throws ValueFormatException, IllegalStateException, RepositoryException {
                return null;
            }

            @Override
            public InputStream getStream() throws RepositoryException {
                return getBinary().getStream();
            }

            @Override
            public Binary getBinary() throws RepositoryException {
                return new Binary() {
                    @Override
                    public InputStream getStream() throws RepositoryException {
                        if (buffer == null) {
                            buffer = new ByteArrayOutputStream();

                            final byte[] b = new byte[64 * 1024];

                            try (final InputStream input = streamProvider.call()) {
                                for (int i = input.read(b); i != -1; i = input.read(b)) {
                                    buffer.write(b, 0, i);
                                }
                                buffer.close();
                            } catch (final Exception e) {
                                throw new RepositoryException(e);
                            }
                        }
                        return new ByteArrayInputStream(buffer.toByteArray());
                    }

                    @Override
                    public int read(byte[] b, long position) throws IOException, RepositoryException {
                        InputStream stream = getStream();
                        for (int i = 0; i < position; i++) {
                            if (stream.read() == -1) {
                                return -1;
                            }
                        }
                        return stream.read(b);
                    }

                    @Override
                    public long getSize() throws RepositoryException {
                        if (buffer == null) {
                            getStream();
                        }
                        return buffer.size();
                    }

                    @Override
                    public void dispose() {
                    }
                };
            }

            @Override
            public long getLong() throws ValueFormatException, RepositoryException {

                return buffer == null ? -1 : buffer.size();
            }

            @Override
            public double getDouble() throws ValueFormatException, RepositoryException {
                return 0;
            }

            @Override
            public BigDecimal getDecimal() throws ValueFormatException, RepositoryException {
                return null;
            }

            @Override
            public Calendar getDate() throws ValueFormatException, RepositoryException {
                return null;
            }

            @Override
            public boolean getBoolean() throws ValueFormatException, RepositoryException {
                return false;
            }

            @Override
            public int getType() {
                return PropertyType.BINARY;
            }
        };
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
                    return BinaryProperty.this.isMultiple();
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
                    return BinaryProperty.this.getName();
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
