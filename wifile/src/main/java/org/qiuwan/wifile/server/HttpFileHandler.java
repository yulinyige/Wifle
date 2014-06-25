package org.qiuwan.wifile.server;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.qiuwan.wifile.service.WebService;
import org.qiuwan.wifile.utils.LogUtil;

/**
 * 已不用
 * 文件请求处理.列出文件.
 */
public class HttpFileHandler implements HttpRequestHandler {

    private String webRoot;
    private String filePath;

    public HttpFileHandler(final String webRoot) {
        this.webRoot = webRoot;
        filePath = WebService.htmlFilePath;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {

        String target = URLDecoder.decode(request.getRequestLine().getUri(), "UTF-8");
        final File file = new File(this.webRoot, target);

        LogUtil.i("File Handler. target = " + target);

        if (!file.exists()) { // 不存在
            response.setStatusCode(HttpStatus.SC_NOT_FOUND);
            StringBuilder sb = new StringBuilder();
            sb.append("<html><head><title> 404 Page not found!</title><meta charset=\"UTF-8\">");
            sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + filePath + "/css/style.css\" media=\"all\">");
            sb.append("<script type=\"text/javascript\" src=\"" + filePath + "/js/jquery-1.7.2.min.js\"></script>");
            sb.append("<script type=\"text/javascript\" src=\"" + filePath + "/js/jquery.backstretch.min.js\"></script>");
            sb.append("<script type=\"text/javascript\"> $.backstretch(\"" + filePath + "/img/bg.jpg\");</script>");
            sb.append("</head><body><header class=\"dark\"><div id=\"logo\"><img src=\"" + filePath + "/img/logo.png\"/></div></header>");
            sb.append("<div class=\"content-wrapper\"><div class=\"content\"><img src=\"" + filePath + "/img/404.png\"/></div></div></body></html>");
            StringEntity entity = new StringEntity(sb.toString(), "UTF-8");
            response.setHeader("Content-Type", "text/html");
            response.setEntity(entity);
        } else if (file.canRead()) { // 可读
            response.setStatusCode(HttpStatus.SC_OK);
            HttpEntity entity = null;
            if (file.isDirectory()) { // 文件夹
                entity = createDirListHtml(file, target);
                response.setHeader("Content-Type", "text/html");
            } else { // 文件
                String contentType = URLConnection
                        .guessContentTypeFromName(file.getAbsolutePath());
                contentType = null == contentType ? "charset=UTF-8"
                        : contentType + "; charset=UTF-8";
                entity = new FileEntity(file, contentType);
                response.setHeader("Content-Type", contentType);
            }
            response.setEntity(entity);
        } else { // 不可读
            response.setStatusCode(HttpStatus.SC_FORBIDDEN);
            StringBuilder sb = new StringBuilder();
            sb.append("<html><head><title> 304 没有权限!</title><meta charset=\"UTF-8\">");
            sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + filePath + "/css/style.css\" media=\"all\">");
            sb.append("<script type=\"text/javascript\" src=\"" + filePath + "/js/jquery-1.7.2.min.js\"></script>");
            sb.append("<script type=\"text/javascript\" src=\"" + filePath + "/js/jquery.backstretch.min.js\"></script>");
            sb.append("<script type=\"text/javascript\"> $.backstretch(\"" + filePath + "/img/bg.jpg\");</script>");
            sb.append("</head><body><header class=\"dark\"><div id=\"logo\"><img src=\"" + filePath + "/img/logo.png\"/></div></header>");
            sb.append("<div class=\"content-wrapper\"><div class=\"content\"><img src=\"" + filePath + "/img/forbiton.png\"/></div></div></body></html>");
            StringEntity entity = new StringEntity(sb.toString(), "UTF-8");
            response.setHeader("Content-Type", "text/html");
            response.setEntity(entity);
        }
    }

    /**
     * 浏览文件
     */
    private StringEntity createDirListHtml(File dir, String target) throws UnsupportedEncodingException {
        StringBuffer sb = new StringBuffer();
        sb.append("<html><head><title>Wifile文件分享</title><meta charset=\"UTF-8\">");
        sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + filePath + "/css/style.css\" media=\"all\">");
        sb.append("<script type=\"text/javascript\" src=\"" + filePath + "/js/jquery-1.7.2.min.js\"></script>");
        sb.append("<script type=\"text/javascript\" src=\"" + filePath + "/js/jquery.backstretch.min.js\"></script>");
        sb.append("<script type=\"text/javascript\" src=\"" + filePath + "/js/wifile.js\"></script>");
        sb.append("<script type=\"text/javascript\"> $.backstretch(\"" + filePath + "/img/bg.jpg\");</script>");
        sb.append("</head><body><header class=\"dark\"> <div id=\"logo\"><img src=\"" + filePath + "/img/logo.png\" alt=\"\" /></div></header>");

        sb.append("<div class=\"content-wrapper\"><div class=\"content\"><table cellspacing=\"0\" id=\"table\"> ");
        //table头部
        sb.append(" <thead class=\"table-head\"> <tr>  <th>Name</th> <th>File size</th> <th>Date</th> <th>Operation</th>  </tr> </thead> ");
        sb.append("<tbody class=\"table-body\"> ");
        // 上传到当前目录
        if (dir.isDirectory()) {
            sb.append("<form method=\"POST\" enctype=\"multipart/form-data\" action=\"fup.cgi\">\n" +
                    "  <input type=\"file\" name=\"upfile\">" + "  <input type=\"submit\" value=\"上传\">" +
                    "</form>");
        }
        //返回目录
        if (!isSamePath(dir.getAbsolutePath(), this.webRoot)) {
            sb.append("<tr>\n<td><a class=\"icon up\" href=\"..\"> Back </a></td>\n<td></td>\n<td></td>\n<td></td>\n</tr>\n");
        }
        //目录列表
        File[] files = dir.listFiles();
        if (null != files) {
            sort(files); // 排序
            for (File f : files) {
                appendRow(sb, f);
            }
        }
        sb.append("</tbody> </table> </div></div>");
        sb.append("<div class=\"site-generator-wrapper\"><div class=\"site-generator\"> 0800360210 陈伟基</div></div>");
        sb.append("</body></html>");

        return new StringEntity(sb.toString(), "UTF-8");
    }

    private boolean isSamePath(String a, String b) {
        String left = a.substring(b.length(), a.length()); // a以b开头
        if (left.length() >= 2) {
            return false;
        }
        if (left.length() == 1 && !left.equals("/")) {
            return false;
        }
        return true;
    }

    /**
     * 排序：文件夹、文件，再各安字符顺序
     */
    private void sort(File[] files) {
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                if (f1.isDirectory() && !f2.isDirectory()) {
                    return -1;
                } else if (!f1.isDirectory() && f2.isDirectory()) {
                    return 1;
                } else {
                    return f1.toString().compareToIgnoreCase(f2.toString());
                }
            }
        });
    }

    private SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd ahh:mm");

    private void appendRow(StringBuffer sb, File f) {

        String clazz, link, size;
        if (f.isDirectory()) {
            clazz = "icon dir";
            link = f.getName() + "/";
            size = "";
        } else {
            clazz = "icon file";
            link = f.getName();
            size = formatFileSize(f.length());
        }
        sb.append("<tr>\n<td><a class=\"");
        sb.append(clazz);
        sb.append("\" href=\"");
        sb.append(link);
        sb.append("\">");
        sb.append(link);
        sb.append("</a></td>\n");
        sb.append("<td class=\"detailsColumn\">");
        sb.append(size);
        sb.append("</td>\n<td class=\"detailsColumn\">");
        sb.append(sdf.format(new Date(f.lastModified())));
        sb.append("</td>\n<td class=\"operateColumn\">");
        sb.append("<span><a href=\"");
        sb.append(link);
        sb.append(WebServer.SUFFIX_ZIP);
        sb.append("\">下载</a> </span>");
        if (f.canWrite() && !hasWifileDir(f)) {
            sb.append("<span><a href=\"");
            sb.append(link);
            sb.append(WebServer.SUFFIX_DEL);
            sb.append("\" onclick=\"return confirmDelete('");
            sb.append(link);
            sb.append(WebServer.SUFFIX_DEL);
            sb.append("')\">删除</a></span>");
        }
        sb.append("</td>\n</tr>\n");
    }

    public static boolean hasWifileDir(File f) {
        String path = f.isDirectory() ? f.getAbsolutePath() + "/" : f
                .getAbsolutePath();
        return path.indexOf("/wifile/") != -1;
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
