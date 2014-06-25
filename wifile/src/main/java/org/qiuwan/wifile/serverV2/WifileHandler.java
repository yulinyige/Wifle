package org.qiuwan.wifile.serverV2;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.qiuwan.wifile.utils.LogUtil;

import java.io.IOException;

/**
 * Created by qiuwan.zheng on 2014/4/16.
 */
public class WifileHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String path = httpExchange.getRequestURI().getPath().toLowerCase();
        AbsHttpHandler httpHandler;
        switch (path) {
            case "/list":
                httpHandler = new ListFileHandler();
                break;
            case "/down":
                httpHandler = new DownloadFileHandler();
                break;
            case "/upload":
                httpHandler = new UploadFileHandler();
                break;
            case "/icon":
                httpHandler = new AssetIconHandler();
                break;
            case "/delete":
                httpHandler = new DeleteFileHandler();
                break;
            default:
                httpHandler = new AssetFileHandler();
                break;
        }
        httpHandler.handle(httpExchange);
    }
}
