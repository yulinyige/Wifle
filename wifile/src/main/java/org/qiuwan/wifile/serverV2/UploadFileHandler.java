package org.qiuwan.wifile.serverV2;

import android.os.Environment;

import com.sun.net.httpserver.HttpExchange;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.myapache.commons.fileupload.httpexchange.HttpExchangeFileUpload;
import org.qiuwan.wifile.utils.LogUtil;

import java.io.File;
import java.util.List;

/**
 * 上传文件.
 * Note:这里用的是POST方法,所以,不能用Query的方法获取参数.parseRequest中的FileItem已经获取到了所有的参数,每个Item一个参数.包括文件.
 * 可以使用Debug跟踪获取到的FileItem.
 * 网页中的当前路径已经从currentDirectory传递过来了.
 * Post传递过来的格式可以看成k-v.FileItem.getFieldName()获取Key,FileItem.getName()获取值.
 * Created by qiuwan.zheng on 2014/4/16.
 */
public class UploadFileHandler extends AbsHttpHandler {

    private String currentDirectory = "";

    @Override
    public void handle(HttpExchange httpExchange) {
        LogUtil.i("upload file handle.....");
        DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
        diskFileItemFactory.setSizeThreshold(2097152);
        diskFileItemFactory.setRepository(Environment.getExternalStorageDirectory());
        HttpExchangeFileUpload fileUpload = new HttpExchangeFileUpload(diskFileItemFactory);
        try {
            // fileItems 获取到了网页提交表单的数据.
            List<FileItem> fileItems = fileUpload.parseRequest(httpExchange);
            FileItem realFile = null;
            // 遍历所有参数,找到需要的那两个.
            for (FileItem fileItem : fileItems) {
                // 寻找当前目录.
                if (fileItem.isFormField() && "currentDirectory".equals(fileItem.getFieldName())) {
                    LogUtil.i("currentDirectory = " + fileItem.getString());
                    currentDirectory = fileItem.getString();
                }
                // 寻找真实文件.
                if (!fileItem.isFormField() && fileItem.getName() != null) {
                    realFile = fileItem;
                }
            }
            // 如果真实文件找到了,就写入文件.
            if (realFile != null) {
                // 构造本地的文件路径.
                File destFile = new File(Environment.getExternalStorageDirectory() + File.separator + currentDirectory, realFile.getName());
                LogUtil.i("dest file =  " + destFile.getAbsolutePath());
                // 从上传的真实文件流写入本地文件.
                realFile.write(destFile);
            }
            // 返回浏览器.
            responseText(httpExchange, 200, "successful");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
