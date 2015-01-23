/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package handler;

import SearchLucene.DataIndex;
import SearchLucene.SearchResult;
import SearchLucene.SearchLuceneService;
import org.apache.thrift.TException;
import com.vng.jcore.common.Config;
import java.util.ArrayList;
import core.IndexFiles;
import core.SearchFiles;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.queryparser.classic.ParseException;

/**
 *
 * @author walle
 */
public class SearchLuceneHandler implements SearchLuceneService.Iface {
    private static final Logger logger_ = Logger.getLogger(SearchLuceneHandler.class);

    @Override
    public void indexData(DataIndex di, String key) throws TException {
        try {
            if (Config.getParam("index", key) != null) {
                Document doc = new Document();
                doc.add(new StringField("id", String.valueOf(di.id), Field.Store.YES));
                doc.add(new TextField("key", di.keysearch, Field.Store.YES));
                IndexFiles.getInstance().indexData(Config.getParam("index", key), doc);
            }
        } catch (Exception e) {
            logger_.info(e.toString());
        }

    }

    @Override
    public void removeData(int id, String key) throws TException {

        try {
            if (Config.getParam("index", key) != null) {

                IndexFiles.getInstance().removeData(Config.getParam("index", key), id);
            }
        } catch (Exception e) {
            logger_.info(e.toString());
        }
    }

    @Override
    public SearchResult SearchData(String keyapp,String keysearch, int from, int to) throws TException {
        try {

            if(Config.getParam("index", keyapp) != null){
               return SearchFiles.getInstance().search(Config.getParam("index", keyapp), keysearch, from, to);

            }else{
                SearchResult sr = new SearchResult();
                sr.data = new ArrayList<>();
                sr.error_msg = "keyapp not found!!!";
                sr.error_code = 1;
                sr.total = 0;
                return sr;
            }

        } catch (ParseException ex) {
            logger_.info(ex.toString());
            SearchResult sr = new SearchResult();
            sr.data = new ArrayList<>();
            sr.error_msg = "error!!!";
            sr.total = 0;
            return sr;

        }
    }

}
