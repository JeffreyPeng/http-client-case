package com.example;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.util.concurrent.TimeUnit;

@Slf4j
public class HttpPoolUtil {

    public static final String UTF8 = "UTF-8";
    public static volatile boolean isClosed = false;

    public static final int maxTotalPool = 150;
    public static final int MAX_TIMEOUT = 8000;
    public static final int RequestTimeout = 5000;

    private static RequestConfig requestConfig;
    private static HttpClientBuilder httpClientBuilder;
    private static PoolingHttpClientConnectionManager poolConnManager;
    private static IdleConnectionMonitorThread idleConnectionMonitorThread;

    static {
        // 设置连接池
        poolConnManager = new PoolingHttpClientConnectionManager();
        poolConnManager.setMaxTotal(maxTotalPool);
        poolConnManager.setDefaultMaxPerRoute(maxTotalPool);

        RequestConfig.Builder configBuilder = RequestConfig.custom();
        // 设置连接超时
        configBuilder.setConnectTimeout(MAX_TIMEOUT);
        // 设置读取超时
        configBuilder.setSocketTimeout(MAX_TIMEOUT);
        // 设置从连接池获取连接实例的超时
        configBuilder.setConnectionRequestTimeout(RequestTimeout);
        // 在提交请求之前 测试连接是否可用（有性能问题）
        //configBuilder.setStaleConnectionCheckEnabled(true);
        idleConnectionMonitorThread = new IdleConnectionMonitorThread(poolConnManager);
        idleConnectionMonitorThread.start();
        requestConfig = configBuilder.build();
        httpClientBuilder = HttpClients.custom().setConnectionManager(poolConnManager).setDefaultRequestConfig(requestConfig);
        log.info(">>>>>>>>>>> PoolingHttpClientConnectionManager初始化成功 >>>>>>>>>>>");
    }

    /**
     * 从http连接池里获取客户端实例
     *
     * @return httpClient
     */
    public static CloseableHttpClient getHttpClient() {
        CloseableHttpClient httpClient = httpClientBuilder.build();
        return httpClient;
    }

    /**
     * 获得状态
     */
    public static String getTotalStats() {
        return poolConnManager.getTotalStats().toString();
    }

    /**
     * 关闭连接池资源
     */
    public static void closePool() {
        if (!isClosed) {
            isClosed = true;
            poolConnManager.close();
        }
    }

    public static class IdleConnectionMonitorThread extends Thread {

        private final HttpClientConnectionManager connMgr;
        private volatile boolean shutdown;

        public IdleConnectionMonitorThread(HttpClientConnectionManager connMgr) {
            super();
            this.connMgr = connMgr;
        }

        @Override
        public void run() {
            try {
                while (!shutdown) {
                    synchronized (this) {
                        wait(30 * 1000);
                        // Close expired connections
                        connMgr.closeExpiredConnections();
                        // Optionally, close connections
                        // that have been idle longer than 30 sec
                        connMgr.closeIdleConnections(30, TimeUnit.SECONDS);
                    }
                }
            } catch (InterruptedException ex) {
                // terminate
            }
        }

        public void shutdown() {
            shutdown = true;
            synchronized (this) {
                notifyAll();
            }
        }
    }
}