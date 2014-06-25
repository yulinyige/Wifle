package org.qiuwan.wifile.serverV2;

import android.content.res.AssetManager;
import android.webkit.MimeTypeMap;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import org.apache.commons.io.FilenameUtils;
import org.qiuwan.wifile.WifileApplication;
import org.qiuwan.wifile.utils.LogUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by qiuwan.zheng on 2014/4/16.
 */
public class AssetFileHandler extends AbsHttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) {
        LogUtil.i("AssetFileHandler --> handle");
        String path = httpExchange.getRequestURI().getPath();
        if (("".equals(path)) || ("/".equals(path))) {
            path = "/index.html";
        }
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String ext = FilenameUtils.getExtension(path);
        AssetManager assetManager = WifileApplication.getContext().getAssets();
        try {
            InputStream inputStream = assetManager.open("file" + path);
            Headers headers = httpExchange.getResponseHeaders();
            if (mime.hasExtension(ext)) {
                headers.add("Content-Type", mime.getMimeTypeFromExtension(ext));
            }
            httpExchange.sendResponseHeaders(200, 0);
            OutputStream outputStream = httpExchange.getResponseBody();
            byte[] buffer = new byte[8192];
            int count;
            while ((count = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, count);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            responseText(httpExchange, 404, "404 NOT FOUND");
        }
    }
}
