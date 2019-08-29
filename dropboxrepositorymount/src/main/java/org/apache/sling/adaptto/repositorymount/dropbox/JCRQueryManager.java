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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.jcr.Binary;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
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
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.version.VersionException;

import org.apache.jackrabbit.commons.iterator.NodeIteratorAdapter;
import org.apache.jackrabbit.oak.commons.PathUtils;
import org.apache.sling.api.resource.path.Path;

public class JCRQueryManager implements QueryManager {

    private final DropboxSession session;
    private final Set<Path> repositoryRoots;

    public JCRQueryManager(final DropboxSession session, final Set<Path> repositoryRoots) {
        this.session = session;
        this.repositoryRoots = repositoryRoots;
    }

    @SuppressWarnings("deprecation")
    @Override
    public Query createQuery(final String statement, final String language) throws InvalidQueryException, RepositoryException {
        // check for known queries

        return new Query() {
            @Override
            public QueryResult execute() throws InvalidQueryException, RepositoryException {
                NodeIterator nodeIter = null;
                if (Query.XPATH.equals(language)) {
                    final boolean startsWith = statement.startsWith("/jcr:root/");
                    final boolean endsWith = statement.endsWith("/*[((@hidden = 'false' or not(@hidden)))] order by @jcr:created descending");

                    if (startsWith && endsWith) {
                        final int endPath = statement.indexOf("/*");
                        final int startPath = 9; // /jcr:root
                        final String queryPath = statement.substring(startPath, endPath);
                        Set<String> roots = new HashSet<>();
                        for (Path root : repositoryRoots) {
                            if (queryPath.equals(PathUtils.getParentPath(root.getPath()))) {
                                roots.add(root.getPath());
                            }
                        }
                        if (!roots.isEmpty()) {
                            List<Node> rootNodes = new ArrayList<>();
                            for (String root : roots){
                                // return root node
                                final Node rootNode = session.getNodeOrNull(root);
                                // this is just a sanity check, the node should always exist
                                if ( rootNode != null ) {
                                    rootNodes.add(rootNode);
                                }
                            }
                                // this is just a sanity check, the node should always exist
                            if (!rootNodes.isEmpty()) {
                                 nodeIter = new NodeIteratorAdapter(rootNodes);
                            }
                        } else if (repositoryRoots.stream().anyMatch(root -> root.matches(queryPath))) {
                            // return children
                            final Node parent = session.getNodeOrNull(queryPath);
                            if (parent != null) {
                                nodeIter = parent.getNodes();
                            }
                        }
                    }
                }
                final NodeIterator nI = nodeIter;

                return new QueryResult() {
                    @Override
                    public String[] getColumnNames() throws RepositoryException {
                        return new String[0];
                    }

                    @Override
                    public RowIterator getRows() throws RepositoryException {
                        if (nI == null) {
                            return null;
                        }
                        return new RowIterator() {

                            private Row createRow(final Node n) {
                                return new Row() {

                                    @Override
                                    public Value[] getValues() throws RepositoryException {
                                        final List<Value> values = new ArrayList<>();
                                        values.add(this.getValue("jcr:path"));
                                        final PropertyIterator pI = n.getProperties();
                                        while (pI.hasNext()) {
                                            final Property p = pI.nextProperty();
                                            values.add(p.getValue());
                                        }
                                        return values.toArray(new Value[values.size()]);
                                    }

                                    @Override
                                    public Value getValue(String columnName) throws ItemNotFoundException, RepositoryException {
                                        if ("jcr:path".equals(columnName)) {
                                            return new Value() {

                                                @Override
                                                public int getType() {
                                                    return PropertyType.STRING;
                                                }

                                                @Override
                                                public String getString() throws ValueFormatException, IllegalStateException, RepositoryException {
                                                    return n.getPath();
                                                }

                                                @Override
                                                public InputStream getStream() throws RepositoryException {
                                                    // TODO Auto-generated method stub
                                                    return null;
                                                }

                                                @Override
                                                public long getLong() throws ValueFormatException, RepositoryException {
                                                    // TODO Auto-generated method stub
                                                    return 0;
                                                }

                                                @Override
                                                public double getDouble() throws ValueFormatException, RepositoryException {
                                                    // TODO Auto-generated method stub
                                                    return 0;
                                                }

                                                @Override
                                                public BigDecimal getDecimal() throws ValueFormatException, RepositoryException {
                                                    // TODO Auto-generated method stub
                                                    return null;
                                                }

                                                @Override
                                                public Calendar getDate() throws ValueFormatException, RepositoryException {
                                                    // TODO Auto-generated method stub
                                                    return null;
                                                }

                                                @Override
                                                public boolean getBoolean() throws ValueFormatException, RepositoryException {
                                                    // TODO Auto-generated method stub
                                                    return false;
                                                }

                                                @Override
                                                public Binary getBinary() throws RepositoryException {
                                                    // TODO Auto-generated method stub
                                                    return null;
                                                }
                                            };
                                        }
                                        final Property p = n.getProperty(columnName);
                                        if (p == null) {
                                            throw new ItemNotFoundException(columnName);
                                        }
                                        return p.getValue();
                                    }

                                    @Override
                                    public double getScore(String selectorName) throws RepositoryException {
                                        return 0;
                                    }

                                    @Override
                                    public double getScore() throws RepositoryException {
                                        return 0;
                                    }

                                    @Override
                                    public String getPath(String selectorName) throws RepositoryException {
                                        return null;
                                    }

                                    @Override
                                    public String getPath() throws RepositoryException {
                                        return n.getPath();
                                    }

                                    @Override
                                    public Node getNode(String selectorName) throws RepositoryException {
                                        return null;
                                    }

                                    @Override
                                    public Node getNode() throws RepositoryException {
                                        return n;
                                    }
                                };
                            }

                            @Override
                            public Object next() {
                                return createRow(nI.nextNode());
                            }

                            @Override
                            public boolean hasNext() {
                                // TODO Auto-generated method stub
                                return nI.hasNext();
                            }

                            @Override
                            public void skip(long skipNum) {
                                // TODO Auto-generated method stub
                                nI.skip(skipNum);
                            }

                            @Override
                            public long getSize() {
                                // TODO Auto-generated method stub
                                return nI.getSize();
                            }

                            @Override
                            public long getPosition() {
                                // TODO Auto-generated method stub
                                return nI.getPosition();
                            }

                            @Override
                            public Row nextRow() {
                                return createRow(nI.nextNode());
                            }
                        };
                    }

                    @Override
                    public NodeIterator getNodes() throws RepositoryException {
                        return nI;
                    }

                    @Override
                    public String[] getSelectorNames() throws RepositoryException {
                        return new String[0];
                    }
                };
            }

            @Override
            public void setLimit(long limit) {

            }

            @Override
            public void setOffset(long offset) {

            }

            @Override
            public String getStatement() {
                return statement;
            }

            @Override
            public String getLanguage() {
                return language;
            }

            @Override
            public String getStoredQueryPath() throws ItemNotFoundException, RepositoryException {
                return null;
            }

            @Override
            public Node storeAsNode(String absPath) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, UnsupportedRepositoryOperationException, RepositoryException {
                return null;
            }

            @Override
            public void bindValue(String varName, Value value) throws IllegalArgumentException, RepositoryException {

            }

            @Override
            public String[] getBindVariableNames() throws RepositoryException {
                return new String[0];
            }
        };
    }

    @Override
    public QueryObjectModelFactory getQOMFactory() {
        return null;
    }

    @Override
    public Query getQuery(Node node) throws InvalidQueryException, RepositoryException {
        return null;
    }

    @SuppressWarnings("deprecation")
    @Override
    public String[] getSupportedQueryLanguages() throws RepositoryException {
        return new String[]{Query.JCR_JQOM, Query.JCR_SQL2, Query.SQL, Query.XPATH};
    }
}
