package org.qiuwan.wifile.serverV2;

import android.net.UrlQuerySanitizer;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;

import org.apache.commons.io.FilenameUtils;
import org.qiuwan.wifile.utils.LogUtil;

import java.io.File;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * 显示文件列表Handler.
 * Created by qiuwan.zheng on 2014/4/16.
 */
public class ListFileHandler extends AbsHttpHandler {

    private static HashMap<String, String> fileTypes = new HashMap();

    static {
        fileTypes.put("png", "file_type_png");
        fileTypes.put("gif", "file_type_gif");
        fileTypes.put("jpg", "file_type_jpg");
        fileTypes.put("bmp", "file_type_bmp");
        fileTypes.put("txt", "file_type_txt");
        fileTypes.put("pdf", "file_type_pdf");
        fileTypes.put("mp3", "file_type_mp3");
        fileTypes.put("mp4", "file_type_mp4");
        fileTypes.put("ogg", "file_type_ogg");
        fileTypes.put("mpg", "file_type_mpg");
        fileTypes.put("rar", "file_type_rar");
        fileTypes.put("zip", "file_type_zip");
        fileTypes.put("apk", "file_type_apk");
    }

    private String getFileTypeDesc(String ext) {
        String fileTypeDescription = fileTypes.get(ext);
        if (fileTypeDescription == null) {
            fileTypeDescription = "file_type_unknown";
        }
        return fileTypeDescription;
    }

    private List<ExFile> parseBreadcrumb(String parent) {
        List list = new ArrayList<ExFile>();
        if (TextUtils.isEmpty(parent)) {
            ExFile file = new ExFile();
            file.setName("Home");
            file.setRelativePath("");
            list.add(file);
            return list;
        }
        String p = parent;
        while (true) {
            if (TextUtils.isEmpty(p)) {
                Collections.reverse(list);
                return list;
            }
            p = FilenameUtils.getPathNoEndSeparator(p);
            String n = FilenameUtils.getName(p);
            ExFile file = new ExFile();
            if (TextUtils.isEmpty(n)) {
                n = "Home";
            }
            file.setName(n);
            file.setRelativePath(p);
            list.add(file);
        }
    }

    private ExFile[] toExFile(File basePath, File[] files) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        ExFile[] exFiles = new ExFile[files.length];
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            ExFile exFile = new ExFile();
            exFile.setIsDirectory(file.isDirectory());
            exFile.setName(file.getName());
            exFile.setModifiedDate(sdf.format(new Date(file.lastModified())));
            exFile.setBytes(file.length());
            exFile.setRelativePath(basePath.toURI().relativize(file.toURI()).getPath());
            if (file.isDirectory()) {
                exFile.setExt("folder");
                exFile.setTypeDesc("file_type_folder");
            } else {
                exFile.setExt(FilenameUtils.getExtension(file.getName()));
                exFile.setTypeDesc(getFileTypeDesc(exFile.getExt()));
            }
            exFiles[i] = exFile;
        }
        return exFiles;
    }

    @Override
    public void handle(HttpExchange httpExchange) {
        LogUtil.i("start to list files");
        File diskRoot = Environment.getExternalStorageDirectory();
        URI uri = httpExchange.getRequestURI();
        String query = uri.getQuery();
        UrlQuerySanitizer sanitizer = new UrlQuerySanitizer();
        sanitizer.setAllowUnregisteredParamaters(true);
        sanitizer.parseQuery(query);
        String parent = sanitizer.getValue("p");
        List<ExFile> breadcrumb = parseBreadcrumb(parent);
        File[] files;
        if (parent != null || !TextUtils.isEmpty(parent)) {
            files = new File(diskRoot.getAbsoluteFile() + File.separator + parent).listFiles();
        } else {
            files = diskRoot.listFiles();
        }
        Arrays.sort(files, new ComparatorByLastModified());
        ExFile[] exFiles = toExFile(diskRoot, files);
//        for (int i = 0; i < breadcrumb.size(); i++) {
//            LogUtil.i("braedCrumb ==> " + breadcrumb.get(i).getName());
//        }
//        for (int i = 0; i < exFiles.length; i++) {
//            LogUtil.i(exFiles[i].getName());
//        }
        Object[] obj = {parent, breadcrumb, exFiles};
        String response = new Gson().toJson(obj);
        responseJson(httpExchange, 200, response);
    }
}
