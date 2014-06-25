package org.qiuwan.wifile.server;

import java.io.IOException;

import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpException;
import org.apache.http.HttpServerConnection;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpService;
import org.qiuwan.wifile.utils.LogUtil;

/**
 * 已不用
 */
public class WorkerThread extends Thread {

    private final HttpService httpservice;
    private final HttpServerConnection conn;

    public WorkerThread(final HttpService httpservice, final HttpServerConnection conn) {
        super();
        this.httpservice = httpservice;
        this.conn = conn;
    }

    @Override
    public void run() {
        HttpContext context = new BasicHttpContext();
        try {
            while (!Thread.interrupted() && this.conn.isOpen()) {
                this.httpservice.handleRequest(this.conn, context);
            }
        } catch (ConnectionClosedException ex) {
            ex.printStackTrace();
            LogUtil.e("Client closed connection");
        } catch (IOException ex) {
            ex.printStackTrace();
            LogUtil.e("I/O error: " + ex.getMessage());
        } catch (HttpException ex) {
            ex.printStackTrace();
            LogUtil.e("Unrecoverable HTTP protocol violation: " + ex.getMessage());
        } finally {
            try {
                this.conn.shutdown();
            } catch (IOException ignore) {
            }
        }
    }

}
