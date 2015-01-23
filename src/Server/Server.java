package Server;

import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.transport.TNonblockingServerTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import com.vng.jcore.common.Config;
import com.vng.jcore.common.LogUtil;
import org.apache.log4j.Logger;
import SearchLucene.SearchLuceneService;
import handler.SearchLuceneHandler;
import org.apache.thrift.transport.TTransportException;


public class Server {

    private static final Logger logger_ = Logger.getLogger(Server.class);

    public static SearchLuceneHandler handler;
    public static SearchLuceneService.Processor processor;
    public static void main(String[] args) {
        
        LogUtil.init();
       
        try {
                
            handler = new SearchLuceneHandler();
            processor = new SearchLuceneService.Processor(handler);
            
            Runnable simple = new Runnable() {
                @Override
                public void run() {

                    runNonBlockingServer(processor);
                    
                }
            };

            new Thread(simple).start();
        } catch (Exception x) {
            logger_.info(x.toString());
        }
    }
    public static void runNonBlockingServer(SearchLuceneService.Processor processor){
          try {
            int port_listen = Integer.valueOf(Config.getParam("server", "port_listen"));
            TNonblockingServerTransport serverTransport = new TNonblockingServerSocket(port_listen);

            TServer server = new TNonblockingServer(new TNonblockingServer.Args(serverTransport).
                    processor(processor));
           
            server.serve();
            logger_.info("Service Search Start On Port: " + port_listen);
        } catch (NumberFormatException | TTransportException e) {
            logger_.info(e.toString());
        }
       
    }



}
