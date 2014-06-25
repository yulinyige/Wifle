package org.qiuwan.wifile.serverV2;

import android.content.res.AssetManager;
import android.net.Uri;

import com.sun.net.httpserver.HttpExchange;

import org.qiuwan.wifile.WifileApplication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

/**
 * Created by qiuwan.zheng on 2014/4/16.
 */
public class AssetIconHandler extends AbsHttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) {
        URI uri = httpExchange.getRequestURI();
        Uri uri1 = Uri.parse(uri.toString());
        String ext = uri1.getQueryParameter("f");
        AssetManager assetManager = WifileApplication.getContext().getAssets();
        OutputStream outputStream = httpExchange.getResponseBody();
        try {
            httpExchange.getResponseHeaders().add("Content-Type", "image/png");
            httpExchange.sendResponseHeaders(200, 0);
            writeOutputStream(assetManager, ext, outputStream);
        } catch (IOException localIOException1) {
            try {
                writeOutputStream(assetManager, "file", outputStream);
            } catch (IOException localIOException2) {
                localIOException2.printStackTrace();
            }
        }
    }

    private void writeOutputStream(AssetManager assetManager, String ext, OutputStream outputStream)
            throws IOException {
        InputStream is = assetManager.open("file/img/ext/" + ext + ".png");
        byte[] buffer = new byte[1024];
        while (true) {
            int read = is.read(buffer);
            if (read == -1) {
                outputStream.close();
                is.close();
                return;
            }
            outputStream.write(buffer, 0, read);
        }
    }
}
