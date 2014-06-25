package org.qiuwan.wifile.serverV2;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by qiuwan.zheng on 2014/4/16.
 * Note:outputStream 需要关闭之后才会输出.
 */
public abstract class AbsHttpHandler {

    protected File root;

    public abstract void handle(HttpExchange httpExchange);

    protected void response(HttpExchange httpExchange, int statusCode, String mimeType, String response) {
        OutputStream os = null;
        if (mimeType != null) {
            Headers headers = httpExchange.getResponseHeaders();
            headers.add("Content_type", mimeType);
        }
        try {
            httpExchange.sendResponseHeaders(statusCode, 0);
            os = httpExchange.getResponseBody();
            os.write(response.getBytes());
            // 要关闭了才有输出.
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                os.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    protected void response404(HttpExchange paramHttpExchange) {
        responseText(paramHttpExchange, 404, "404 NOT FOUND");
    }

    protected void responseNotDelete(HttpExchange paramHttpExchange) {
        responseText(paramHttpExchange, 404, "删除文件出错啦. 正在急救中...");
    }

    protected void responseHtml(HttpExchange paramHttpExchange, int paramInt, String paramString) {
    }

    protected void responseJson(HttpExchange paramHttpExchange, int paramInt, String paramString) {
        response(paramHttpExchange, paramInt, "application/json; charset=utf-8", paramString);
    }

    protected void responseText(HttpExchange paramHttpExchange, int paramInt, String paramString) {
        response(paramHttpExchange, paramInt, "text/plain", paramString);
    }

    /**
     * 获取请求的方法.
     *
     * @param httpExchange
     * @return 请求方法.大写的字符串.
     */
    protected String getRequestMethod(HttpExchange httpExchange) {
        return httpExchange.getRequestMethod().toUpperCase();
    }
}
