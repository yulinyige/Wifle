package org.myapache.commons.fileupload.httpexchange;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import org.apache.commons.fileupload.RequestContext;

import java.io.IOException;
import java.io.InputStream;

/**
 * 第三方类,可以不管
 * Created by qiuwan.zheng on 2014/4/16.
 */
public class HttpExchangeRequestContext implements RequestContext {
    private HttpExchange request;

    public HttpExchangeRequestContext(HttpExchange httpExchange) {
        this.request = httpExchange;
    }

    public String getCharacterEncoding() {
        this.request.getRequestHeaders();
        return "utf-8";
    }

    public int getContentLength() {
        Headers localHeaders = this.request.getRequestHeaders();
        try {
            return Integer.parseInt(localHeaders.getFirst("Content-Length"));
        } catch (NumberFormatException localNumberFormatException) {
        }
        return 0;
    }

    public String getContentType() {
        return this.request.getRequestHeaders().getFirst("Content-Type");
    }

    public InputStream getInputStream() throws IOException {
        return this.request.getRequestBody();
    }

    public String toString() {
        return "ContentLength=" + getContentLength() + ", ContentType=" + getContentType();
    }
}
