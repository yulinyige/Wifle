package org.qiuwan.wifile.serverV2;

import android.net.Uri;
import android.os.Environment;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import org.qiuwan.wifile.utils.LogUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLEncoder;

/**
 * 下载文件.
 * Created by qiuwan.zheng on 2014/4/16.
 */
public class DownloadFileHandler extends AbsHttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) {
        // 获取SD卡根目录.
        if (root == null) {
            root = Environment.getExternalStorageDirectory();
        }
        // 解析URL请求...
        URI uri = httpExchange.getRequestURI();
        Uri uri1 = Uri.parse(uri.toString());
        // 获取URL中 f 的参数.
        String file = uri1.getQueryParameter("f");
        try {
            File destFile = new File(root.getAbsolutePath() + File.separator + file);
            LogUtil.i("Start to download file....  Path = " + destFile.getAbsolutePath());
            if ((destFile.exists()) && (destFile.canRead())) {
                Headers headers = httpExchange.getResponseHeaders();
                headers.add("Content-Type", "application/force-download");
                headers.add("Content-Type", "application/octet-stream");
                String encodeString = URLEncoder.encode(destFile.getName(), "utf-8");
                headers.add("Content-Disposition", "attachment; filename=" + encodeString);
                FileInputStream fileInputStream = new FileInputStream(destFile);
                httpExchange.sendResponseHeaders(200, 0);
                OutputStream outputStream = httpExchange.getResponseBody();
                byte[] buffer = new byte[8192];
                int count;
                while ((count = fileInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, count);
                }
                fileInputStream.close();
                outputStream.close();
            } else {
                response404(httpExchange);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            response404(httpExchange);
        }
    }
}
