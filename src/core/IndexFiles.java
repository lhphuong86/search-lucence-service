package core;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.log4j.Logger;

/**
 * Index all text files under a directory.
 * <p>
 * This is a command-line application demonstrating simple Lucene indexing. Run
 * it with no command-line arguments for usage information.
 */
public class IndexFiles {

    private static final Logger logger_ = Logger.getLogger(IndexFiles.class);
    private static IndexFiles _instance = null;
    private static final Lock createLock_ = new ReentrantLock();

    public static IndexFiles getInstance() {

        if (_instance == null) {
            createLock_.lock();
            try {
                if (_instance == null) {
                    _instance = new IndexFiles();
                }
            } finally {
                createLock_.unlock();
            }
        }
        return _instance;
    }

    private IndexFiles() {

    }

    public void removeData(String index_path, int id) throws IOException {

        if (checkFileExists(index_path)) {

            Directory dir = FSDirectory.open(new File(index_path));
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_4_10_0);
            IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_4_10_0, analyzer);
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

            IndexWriter iw = new IndexWriter(dir, iwc);

            iw.deleteDocuments(new Term("id", String.valueOf(id)));
            iw.close();
        }

    }

    public void indexData(String index_dir, Document doc) throws IOException {
        boolean create_folder = false;
        if (!checkFileExists(index_dir)) {
            create_folder = createFolder(index_dir);
        }

        Directory dir = FSDirectory.open(new File(index_dir));
        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_4_10_0);
        IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_4_10_0, analyzer);
        if (create_folder) {
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        } else {
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        }

        try ( IndexWriter iw = new IndexWriter(dir, iwc)) {
            if (iw.getConfig().getOpenMode() == OpenMode.CREATE) {
                iw.addDocument(doc);
            } else {
                iw.updateDocument(new Term("id", doc.getField("id").stringValue()), doc);
            }
            iw.close();

        } catch (Exception e) {
            logger_.info(e.toString());
        } 
    }

    private boolean checkFileExists(String path) {
        File f = new File(path);
        return f.exists();
    }

    private boolean createFolder(String path) {
        File f = new File(path);
        if (!f.exists()) {
            return f.mkdir();
        }
        return false;
    }

}
