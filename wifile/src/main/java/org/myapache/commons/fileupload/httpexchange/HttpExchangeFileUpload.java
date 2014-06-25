package org.myapache.commons.fileupload.httpexchange;

import com.sun.net.httpserver.HttpExchange;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;

import java.io.IOException;
import java.util.List;

/**
 * 第三方类,可以不管.
 * Created by qiuwan.zheng on 2014/4/16.
 */
public class HttpExchangeFileUpload extends FileUpload {

    public HttpExchangeFileUpload() {
    }

    public HttpExchangeFileUpload(FileItemFactory fileItemFactory) {
        super(fileItemFactory);
    }

    public static final boolean isMultipartContent(HttpExchange request) {
        if (!"post".equals(request.getRequestMethod().toLowerCase())) {
            return false;
        }
        String contentType = request.getRequestHeaders().getFirst("Content-Type");
        if (contentType != null && contentType.toLowerCase().startsWith("multipart/")) {
            return true;
        }
        return false;
    }

    public FileItemIterator getItemIterator(HttpExchange httpExchange)
            throws FileUploadException, IOException {
        return super.getItemIterator(new HttpExchangeRequestContext(httpExchange));
    }

    public List<FileItem> parseRequest(HttpExchange httpExchange) throws FileUploadException {
        return parseRequest(new HttpExchangeRequestContext(httpExchange));
    }
}
