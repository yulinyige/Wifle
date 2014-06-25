package org.qiuwan.wifile.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

/**
 * 已不用
 */
public class HttpZipHandler implements HttpRequestHandler {

    /**
     * 缓冲字节长度1M=1024*1024B
     */
    private static final int BUFFER_LENGTH = 1048576;

    private String webRoot;

    public HttpZipHandler(final String webRoot) {
        this.webRoot = webRoot;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response,
                       HttpContext context) throws HttpException, IOException {
        String target = request.getRequestLine().getUri();
        target = target.substring(0,
                target.length() - WebServer.SUFFIX_ZIP.length());
        final File file = new File(this.webRoot, target);
        HttpEntity entity = new EntityTemplate(new ContentProducer() {
            @Override
            public void writeTo(OutputStream outStream) throws IOException {
                zip(file, outStream);
            }
        });
        response.setStatusCode(HttpStatus.SC_OK);
        response.setHeader("Content-Type", "application/octet-stream");
        response.addHeader("Content-Disposition",
                "attachment;filename=" + file.getName() + ".zip");
        response.addHeader("Location", target);
        response.setEntity(entity);
    }

    /**
     * 压缩目录至输出流
     *
     * @param inputFile    压缩目录
     * @param outputStream 输出流
     * @throws java.io.IOException
     */
    private void zip(File inputFile, OutputStream outputStream) throws IOException {
        ZipOutputStream zos;
        try {
            // 创建ZIP文件输出流
            zos = new ZipOutputStream(outputStream);
            // 递归压缩文件进zip文件流
            zip(zos, inputFile, inputFile.getName());
        } catch (IOException e) {
            throw e; // 抛出IOException
        }
        try {
            if (null != zos) {
                zos.close();
            }
        } catch (IOException e) {
        }
    }

    /**
     * 递归压缩文件进zip文件流
     */
    private void zip(ZipOutputStream zos, File file, String base)
            throws IOException {
        if (file.isDirectory()) { // 目录时
            File[] files = file.listFiles();
            zos.putNextEntry(new ZipEntry(base + "/"));
            base = base.length() == 0 ? "" : base + "/";
            if (null != files && files.length > 0) {
                for (File f : files) {
                    zip(zos, f, base + f.getName()); // 递归
                }
            }
        } else {
            zos.putNextEntry(new ZipEntry(base)); // 加入一个新条目
            FileInputStream fis = new FileInputStream(file); // 创建文件输入流
            int count; // 读取计数
            byte[] buffer = new byte[BUFFER_LENGTH]; // 缓冲字节数组
            /* 写入zip输出流 */
            while ((count = fis.read(buffer)) != -1) {
                zos.write(buffer, 0, count);
                zos.flush();
            }
            fis.close(); // 关闭文件输入流
        }
    }

}
