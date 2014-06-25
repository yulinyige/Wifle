package org.qiuwan.wifile.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

/**
 * 已不用
 * 工具类,知道用途就可以了.
 */
public class FileUtil {

    /**
     * 已不用.
     *
     * @param mContext
     * @return
     */
    public static String getHtmlFilePath(Context mContext) {
        return mContext.getExternalFilesDir(null) + "/wifile";
    }

    /**
     * 已不用
     *
     * @param mContext
     * @return
     */
    public static boolean isExistHtmlFile(Context mContext) {
        String path = getHtmlFilePath(mContext);
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        File file = new File(path);
        if (file.exists()) {
            return true;
        }
        return false;
    }

    /**
     * 已不用
     *
     * @param mContext
     * @return
     */
    public static boolean assetsCopy(Context mContext) {
        AssetManager manager = mContext.getAssets();
        try {
//            Environment.getExternalStorageDirectory()
            String path = getHtmlFilePath(mContext);
            if (TextUtils.isEmpty(path)) {
                LogUtil.i("The external has something wrong,close is.");
                return false;
            }
            LogUtil.i("The assets file path is: " + path);
            assetsCopy(manager, "wifile", path);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 已不用
     * 递归复制文件
     *
     * @param manager
     * @param assetsPath
     * @param dirPath
     * @throws IOException
     */
    public static void assetsCopy(AssetManager manager, String assetsPath, String dirPath)
            throws IOException {
        String[] list = manager.list(assetsPath);
        if (list.length == 0) { // 文件
            InputStream in = manager.open(assetsPath);
            File file = new File(dirPath);
            file.getParentFile().mkdirs();
            file.createNewFile();
            FileOutputStream fout = new FileOutputStream(file);
            /* 复制 */
            byte[] buf = new byte[1024];
            int count;
            while ((count = in.read(buf)) != -1) {
                fout.write(buf, 0, count);
                fout.flush();
            }
            /* 关闭 */
            in.close();
            fout.close();
        } else { // 目录
            for (String path : list) {
                assetsCopy(manager, assetsPath + "/" + path, dirPath + "/" + path);
            }
        }
    }

    //--------------------------------------------------------------

    /**
     * 已不用
     * 删除指定的文件
     */
    public static void deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * 已不用
     * 删除文件夹
     *
     * @param file
     */
    public static void deleteDir(File file) {
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            } else if (file.isDirectory()) {
                File files[] = file.listFiles();
                for (int i = 0; i < files.length; i++) {
                    deleteDir(files[i]);
                }
            }
            file.delete();
        }
    }

}