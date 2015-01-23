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
import SearchLucene.SearchResult;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.log4j.Logger;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * Simple command-line based search demo.
 */
public class SearchFiles {

    private static final Logger logger_ = Logger.getLogger(SearchFiles.class);

    private static SearchFiles _instance = null;
    private static final Lock createLock_ = new ReentrantLock();

    public static SearchFiles getInstance() {

        if (_instance == null) {
            createLock_.lock();
            try {
                if (_instance == null) {
                    _instance = new SearchFiles();
                }
            } finally {
                createLock_.unlock();
            }
        }
        return _instance;
    }

    private SearchFiles() {
    }

    public SearchResult search(String indexpath, String keySearch, int from, int to) throws ParseException{
        List<Integer> list = new ArrayList<>();
        SearchResult sr = new SearchResult();

        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_4_10_0);

        QueryParser parser = new QueryParser(Version.LUCENE_4_10_0, "key", analyzer);
        Query query = parser.createPhraseQuery("key", keySearch);
        
        
        
        
        try (IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexpath)))) {
            IndexSearcher searcher = new IndexSearcher(reader);

            TopScoreDocCollector collector = TopScoreDocCollector.create(10000, true);

            searcher.search(query, collector);
            ScoreDoc[] hits = collector.topDocs().scoreDocs;
            for (int i = from; i <= to; i++) {
                if (i > (hits.length - 1)) {
                    break;
                }
                int docId = hits[i].doc;
                Document d = searcher.doc(docId);
                list.add(Integer.parseInt(d.get("id")));

            }

            sr.data = list;
            sr.error_msg = "sucess";
            sr.total = hits.length;
        } catch (Exception e) {
            logger_.info(e.toString());
            sr.data = new ArrayList<>();
            sr.error_msg = "error!!!";
            sr.total = 0;
        }

        return sr;
    }

}
