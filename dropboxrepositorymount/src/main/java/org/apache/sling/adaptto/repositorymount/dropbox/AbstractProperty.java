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
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import javax.jcr.Binary;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

public abstract class AbstractProperty extends AbstractItem implements Property {

    private final List<Value> values = new ArrayList<>();

    private final boolean allowMultiple;

    private final int type;

    public AbstractProperty(final RepositoryContext jcr, final String path, final Value value) {
        super(path, jcr);
        if (value != null) {
            this.type = value.getType();
            this.allowMultiple = false;
            this.values.add(value);
        } else {
            this.allowMultiple = false;
            this.type = 0;
        }
    }

    public AbstractProperty(final RepositoryContext jcr, final String path, final Value[] values) {
        super(path, jcr);
        this.type = values[0].getType();
        this.allowMultiple = true;
        this.values.addAll(Arrays.asList(values));
    }

    
    @Override
    public boolean isNode() {
        return false;
    }


    @Override
    public Value getValue() throws ValueFormatException, RepositoryException {
        return values.get(0);
    }

    @Override
    public Value[] getValues() throws ValueFormatException, RepositoryException {
        return values.toArray(new Value[values.size()]);
    }

    @Override
    public String getString() throws ValueFormatException, RepositoryException {
        return values.get(0).getString();
    }

    @Override
    public InputStream getStream() throws ValueFormatException, RepositoryException {
        return values.get(0).getStream();
    }

    @Override
    public Binary getBinary() throws ValueFormatException, RepositoryException {
        return values.get(0).getBinary();
    }

    @Override
    public long getLong() throws ValueFormatException, RepositoryException {
        return values.get(0).getLong();
    }

    @Override
    public double getDouble() throws ValueFormatException, RepositoryException {
        return values.get(0).getDouble();
    }

    @Override
    public BigDecimal getDecimal() throws ValueFormatException, RepositoryException {
        return values.get(0).getDecimal();
    }

    @Override
    public Calendar getDate() throws ValueFormatException, RepositoryException {
        return values.get(0).getDate();
    }

    @Override
    public boolean getBoolean() throws ValueFormatException, RepositoryException {
        return values.get(0).getBoolean();
    }

    @Override
    public Node getNode() throws ItemNotFoundException, ValueFormatException, RepositoryException {
        return this.getParent();
    }

    @Override
    public Property getProperty() throws ItemNotFoundException, ValueFormatException, RepositoryException {
        return this;
    }

    @Override
    public long getLength() throws ValueFormatException, RepositoryException {
        if (this.type == PropertyType.BINARY) {
            return this.getBinary().getSize();
        }
        return this.getString().length();
    }

    @Override
    public long[] getLengths() throws ValueFormatException, RepositoryException {
        final long[] ll = new long[this.values.size()];
        int index = 0;
        for (final Value v : this.values) {
            if (this.type == PropertyType.BINARY) {
                ll[index] = v.getBinary().getSize();
            } else {
                ll[index] = v.getString().length();
            }
            index++;
        }
        return ll;
    }

    @Override
    public int getType() {
        return this.type;
    }

    @Override
    public boolean isMultiple() throws RepositoryException {
        return this.allowMultiple;
    }

    @Override
    public void setValue(Value value) throws ValueFormatException, VersionException, LockException,
            ConstraintViolationException, RepositoryException {
        throw new ConstraintViolationException();
    }

    @Override
    public void setValue(Value[] values) throws ValueFormatException, VersionException, LockException,
            ConstraintViolationException, RepositoryException {
        throw new ConstraintViolationException();
    }

    @Override
    public void setValue(String value) throws ValueFormatException, VersionException, LockException,
            ConstraintViolationException, RepositoryException {
        throw new ConstraintViolationException();
    }

    @Override
    public void setValue(String[] values) throws ValueFormatException, VersionException, LockException,
            ConstraintViolationException, RepositoryException {
        throw new ConstraintViolationException();
    }

    @Override
    public void setValue(InputStream value) throws ValueFormatException, VersionException, LockException,
            ConstraintViolationException, RepositoryException {
        throw new ConstraintViolationException();
    }

    @Override
    public void setValue(Binary value) throws ValueFormatException, VersionException, LockException,
            ConstraintViolationException, RepositoryException {
        throw new ConstraintViolationException();
    }

    @Override
    public void setValue(long value) throws ValueFormatException, VersionException, LockException,
            ConstraintViolationException, RepositoryException {
        throw new ConstraintViolationException();
    }

    @Override
    public void setValue(double value) throws ValueFormatException, VersionException, LockException,
            ConstraintViolationException, RepositoryException {
        throw new ConstraintViolationException();
    }

    @Override
    public void setValue(BigDecimal value) throws ValueFormatException, VersionException, LockException,
            ConstraintViolationException, RepositoryException {
        throw new ConstraintViolationException();
    }

    @Override
    public void setValue(Calendar value) throws ValueFormatException, VersionException, LockException,
            ConstraintViolationException, RepositoryException {
        throw new ConstraintViolationException();
    }

    @Override
    public void setValue(boolean value) throws ValueFormatException, VersionException, LockException,
            ConstraintViolationException, RepositoryException {
        throw new ConstraintViolationException();
    }

    @Override
    public void setValue(Node value) throws ValueFormatException, VersionException, LockException,
            ConstraintViolationException, RepositoryException {
        throw new ConstraintViolationException();
    }
}
