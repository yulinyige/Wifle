package org.qiuwan.wifile.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.qiuwan.wifile.utils.LogUtil;

/**
 * 已不用.
 * 关键的WebServer,一切的Web服务都是通过它获取.这是在一个子线程执行的操作.ServerSocket接收请求,然后发送给Handler处理.
 * 因为是手机处理所有的请求,可能会慢一点.
 * Create By qiuwan.zheng. 2014.3.3
 */
public class WebServer extends Thread {

    static final String SUFFIX_ZIP = "...zip";
    static final String SUFFIX_DEL = "...delete";
    static final String SUFFIX_UPLOAD = "...upload";

    private int port;
    private String webRoot;
    // 是否循环,起始就是根据它来让服务器不断循环提供服务.如果为false,当然就会停止了.
    private boolean isLoop = false;

    //最关键的就是ServerSocket,一定要理解它的作用.参考: http://developer.android.com/reference/java/net/ServerSocket.html
    private ServerSocket serverSocket = null;

    public WebServer(int port, final String webRoot) {
        super();
        this.port = port;
        this.webRoot = webRoot;
    }

    @Override
    public void run() {
        try {
            // 创建服务器套接字
            serverSocket = new ServerSocket(port);
            // 创建HTTP协议处理器
            BasicHttpProcessor httpproc = new BasicHttpProcessor();
            // 增加HTTP协议拦截器
            httpproc.addInterceptor(new ResponseDate());
            httpproc.addInterceptor(new ResponseServer());
            httpproc.addInterceptor(new ResponseContent());
            httpproc.addInterceptor(new ResponseConnControl());
            // 创建HTTP服务
            HttpService httpService = new HttpService(httpproc,
                    new DefaultConnectionReuseStrategy(),
                    new DefaultHttpResponseFactory());
            // 创建HTTP参数
            HttpParams params = new BasicHttpParams();
            params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 5000)
                    .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024)
                    .setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
                    .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
                    .setParameter(CoreProtocolPNames.ORIGIN_SERVER, "WebServer/1.1");
            // 设置HTTP参数
            httpService.setParams(params);
            // 创建HTTP请求执行器注册表
            HttpRequestHandlerRegistry reqistry = new HttpRequestHandlerRegistry();
            // 增加HTTP请求执行器
            // 下载文件,都交给Ziphandler处理.
            reqistry.register("*" + SUFFIX_ZIP, new HttpZipHandler(webRoot));
            // 删除文件.
            reqistry.register("*" + SUFFIX_DEL, new HttpDelHandler(webRoot));
            // 上传文件.
            reqistry.register("*" + SUFFIX_UPLOAD, new HttpUploadHandler(webRoot));
            reqistry.register("*", new HttpFileHandler(webRoot));
            // 设置HTTP请求执行器
            httpService.setHandlerResolver(reqistry);
            /* 循环接收各客户端 */
            isLoop = true;
            while (isLoop && !Thread.interrupted()) {
                // 接收客户端套接字
                Socket socket = serverSocket.accept();
                // 绑定至服务器端HTTP连接
                DefaultHttpServerConnection conn = new DefaultHttpServerConnection();
                conn.bind(socket, params);
                // 发送到WorkerThread处理请求
                Thread t = new WorkerThread(httpService, conn);
                t.setDaemon(true); // 设为守护线程
                t.start();
            }
        } catch (IOException e) {
            isLoop = false;
            e.printStackTrace();
        } finally {
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (IOException e) {
            }
        }
    }

    public void close() {
        isLoop = false;
        if (serverSocket != null) {
            LogUtil.i("主动关闭.The WebServer is not null. now, try to close it.");
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
