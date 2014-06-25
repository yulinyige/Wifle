package org.qiuwan.wifile.server;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.RequestLine;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import java.io.IOException;

/**
 * 已不用
 * Created by qiuwan.zheng on 2014/4/15.
 */
public class HttpUploadHandler implements HttpRequestHandler {

    private String webRoot;

    public HttpUploadHandler(final String webRoot) {
        this.webRoot = webRoot;
    }

    @Override
    public void handle(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext) throws HttpException, IOException {
//        ServletFileUpload.isMultipartContent();
        RequestLine requestLine = httpRequest.getRequestLine();
    }
}
