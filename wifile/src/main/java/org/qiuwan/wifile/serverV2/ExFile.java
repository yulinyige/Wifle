package org.qiuwan.wifile.serverV2;

/**
 * 文件属性.
 * Created by qiuwan.zheng on 2014/4/16.
 */
public class ExFile {
    private long bytes;
    private String createDate;
    private String ext;
    private boolean isDirectory;
    private String modifiedDate;
    private String name;
    private String relativePath;
    private String size;
    private String typeDesc;

    public long getBytes() {
        return this.bytes;
    }

    public String getCreateDate() {
        return this.createDate;
    }

    public String getExt() {
        return this.ext;
    }

    public String getModifiedDate() {
        return this.modifiedDate;
    }

    public String getName() {
        return this.name;
    }

    public String getRelativePath() {
        return this.relativePath;
    }

    public String getSize() {
        return this.size;
    }

    public String getTypeDesc() {
        return this.typeDesc;
    }

    public boolean isDirectory() {
        return this.isDirectory;
    }

    public void setBytes(long fileSize) {
        this.bytes = fileSize;
        this.size = formatFileSize(fileSize);
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public void setExt(String ext) {
        this.ext = ext.toLowerCase();
    }

    public void setIsDirectory(boolean isDirectory) {
        this.isDirectory = isDirectory;
    }

    public void setModifiedDate(String modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public void setTypeDesc(String typeDesc) {
        this.typeDesc = typeDesc;
    }

    /**
     * 格式化文件大小表示
     */
    private String formatFileSize(long len) {
        if (len < 1024)
            return len + " B";
        else if (len < 1024 * 1024)
            return len / 1024 + "." + (len % 1024 / 10 % 100) + " KB";
        else if (len < 1024 * 1024 * 1024)
            return len / (1024 * 1024) + "." + len % (1024 * 1024) / 10 % 100 + " MB";
        else
            return len / (1024 * 1024 * 1024) + "." + len % (1024 * 1024 * 1024) / 10 % 100 + " MB";
    }
}
