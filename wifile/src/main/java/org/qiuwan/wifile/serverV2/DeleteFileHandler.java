package org.qiuwan.wifile.serverV2;

import android.net.Uri;
import android.os.Environment;

import com.sun.net.httpserver.HttpExchange;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;

/**
 * 删除文件.
 * Created by qiuwan.zheng on 2014/4/16.
 */
public class DeleteFileHandler extends AbsHttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) {
        if (root == null) {
            root = Environment.getExternalStorageDirectory();
        }
        URI uri = httpExchange.getRequestURI();
        Uri uri1 = Uri.parse(uri.toString());
        String path = uri1.getQueryParameter("f");
        try {
            File file = new File(root + File.separator + path);
            if (file.isDirectory()) {
                FileUtils.deleteDirectory(file);
            } else {
                if (file.exists()) {
                    file.delete();
                }
            }
            responseText(httpExchange, 200, "successful");
        } catch (IOException ex) {
            ex.printStackTrace();
            response404(httpExchange);
        }
    }
}
