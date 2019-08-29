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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DeletedMetadata;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.GetMetadataErrorException;
import com.dropbox.core.v2.files.ListFolderContinueErrorException;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.LookupError;
import com.dropbox.core.v2.files.Metadata;
import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.oak.commons.PathUtils;

public class DropboxProvider
{
    private final DbxClientV2 m_client;
    private final ConcurrentHashMap<String, Map<String, Object>> m_nodes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<Map<String, Object>>> m_nodesTree = new ConcurrentHashMap<>();

    public DropboxProvider(DbxClientV2 client) {
        m_client = client;
    }

    public DropboxProvider init(String root, String path) throws IOException
    {
        m_nodes.clear();
        m_nodesTree.clear();

        try
        {
            Metadata metadata = m_client.files().getMetadata(path);
            convert(root, path, metadata);
        }
        catch (DbxException e)
        {
            if (e instanceof GetMetadataErrorException)
            {
                LookupError lookupError = ((GetMetadataErrorException) e).errorValue.getPathValue();
                if (lookupError.isNotFound())
                {
                    return this;
                }
            }
            throw new IOException(e);
        }
        return this;
    }

    private Map<String, Object> convert(String root, String path, Metadata m) throws IOException {
        Map<String, Object> node;
        if (m instanceof FileMetadata) {
            node = createFile(root, m.getPathLower());
        }
        else {
            node = createDirectory(root);
            List<Map<String, Object>> nodes = new ArrayList<>();
            ListFolderResult result;
            try {
                result = m_client.files().listFolder(m.getPathLower());
                boolean hasMoreResults = true;
                while (hasMoreResults) {
                    for (Metadata entry : result.getEntries()) {
                        if (!(entry instanceof DeletedMetadata)) {
                            String nextRoot = PathUtils.concat(root, entry.getName());
                            String nextPath = PathUtils.concat(path, entry.getName());
                            nodes.add(convert(nextRoot,nextPath,entry));
                        }
                    }
                    result = m_client.files().listFolderContinue(result.getCursor());
                    hasMoreResults = result.getHasMore();
                }
            } catch (ListFolderContinueErrorException e) {
                throw new IOException("Unable to find nodes " + path, e);
            } catch (Exception ex) {
                throw new IOException("Unable to find nodes " + path, ex);
            }
            if (!nodes.isEmpty()) {
                m_nodesTree.put(root, nodes);
            }
        }
        m_nodes.put(root, node);
        return node;
    }

    private Map<String, Object> createFile(String root, String path_lower) {
        Map<String, Object> result = new HashMap<>();
        result.put(JcrConstants.JCR_PRIMARYTYPE, JcrConstants.NT_FILE);
        result.put(JcrConstants.JCR_NAME, PathUtils.getName(root));

        String contentPath = PathUtils.concat(root, JcrConstants.JCR_CONTENT);
        Map<String, Object> content = new HashMap<>();
        content.put(JcrConstants.JCR_PRIMARYTYPE, JcrConstants.NT_RESOURCE);
        content.put(JcrConstants.JCR_NAME, JcrConstants.JCR_CONTENT);
        content.put(JcrConstants.JCR_DATA, (Callable<InputStream>) () ->
        {
            try
            {
                return m_client.files().download(path_lower).getInputStream();
            }
            catch (DbxException e)
            {
                throw new IOException(String.format("Unable to download file %s.", path_lower), e);
            }
        });

        m_nodes.put(contentPath, content);

        m_nodesTree.put(root, Arrays.asList(content));

        return result;
    }

    private Map<String, Object> createDirectory(String root) {
        Map<String, Object> result = new HashMap<>();
        result.put(JcrConstants.JCR_PRIMARYTYPE, "sling:Folder");
        result.put(JcrConstants.JCR_NAME, PathUtils.getName(root));
        return result;
    }

    public Map<String, Object> get(String path) {
        return m_nodes.get(path);
    }

    public Iterator<Map<String, Object>> getChildren(String path) {
        List<Map<String,Object>> nodes = m_nodesTree.get(path);
        return nodes != null ? nodes.iterator() : Collections.emptyIterator();
    }
}
