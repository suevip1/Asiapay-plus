package com.jeequan.jeepay.core.utils;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * HttpClientPoolUtil 工具类 线程池支持高并发
 */
@Slf4j
@Component
public class HttpClientPoolUtil {
    // 默认字符集
    private static final String ENCODING = "UTF-8";

    private static PoolingHttpClientConnectionManager cm = null;

    private static CloseableHttpClient httpClient = null;

    private static final int MAX_TOTAL = 1000;

    private static final int DEFAULT_MAX_PERROUTE = 32;

    //默认content 类型
    private static final String DEFAULT_CONTENT_TYPE = "application/json;charset=utf-8";

    //默认请求超时时间10s
    @Value("${http.connection.request.timeout:10000}")
    private static int defaulTimeOut;

    @Value("${http.connection.request.keepalive:10000}")
    private static int httpDefaultKeepAlive;

    /**
     * 初始化连接池
     */
    private static synchronized void initPools() {
        if (httpClient == null) {
            cm = new PoolingHttpClientConnectionManager();
            cm.setDefaultMaxPerRoute(DEFAULT_MAX_PERROUTE);
            cm.setMaxTotal(MAX_TOTAL);
            httpClient = HttpClients.custom().setKeepAliveStrategy(defaultStrategy).setConnectionManager(cm).build();
        }
    }

    /**
     * Http connection keepAlive 设置
     */
    private static ConnectionKeepAliveStrategy defaultStrategy = new ConnectionKeepAliveStrategy() {
        public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
            HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
            int keepTime = httpDefaultKeepAlive;
            while (it.hasNext()) {
                HeaderElement he = it.nextElement();
                String param = he.getName();
                String value = he.getValue();
                if (value != null && param.equalsIgnoreCase("timeout")) {
                    try {
                        return Long.parseLong(value) * 1000;
                    } catch (Exception e) {
                        e.printStackTrace();
                        log.error("format KeepAlive timeout exception, exception:{}", e.toString());
                    }
                }
            }
            return keepTime * 1000;
        }
    };

    /**
     * 创建请求
     *
     * @param uri         请求url
     * @param methodName  请求的方法类型
     * @param contentType contentType类型
     * @param timeout     超时时间
     * @return
     * @throws URISyntaxException
     */
    private static HttpRequestBase getRequest(URI uri, Map<String, Object> headers, String methodName, String contentType, int timeout) throws URISyntaxException {
        if (httpClient == null) {
            initPools();
        }
        HttpRequestBase method = null;
        if (timeout <= 0) {
            timeout = defaulTimeOut;
        }
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(timeout * 1000).setConnectTimeout(timeout * 1000)
                .setConnectionRequestTimeout(timeout * 1000).setExpectContinueEnabled(false).build();

        if (HttpPut.METHOD_NAME.equalsIgnoreCase(methodName)) {
            method = new HttpPut(uri);
        } else if (HttpPost.METHOD_NAME.equalsIgnoreCase(methodName)) {
            method = new HttpPost(uri);
        } else if (HttpGet.METHOD_NAME.equalsIgnoreCase(methodName)) {
            method = new HttpGet(uri);
        } else {
            method = new HttpPost(uri);
        }
        if (headers != null) {
            Header[] allHeader = new BasicHeader[headers.size()];
            int i = 0;
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                allHeader[i] = new BasicHeader(entry.getKey(), entry.getValue().toString());
                i++;
            }
            method.setHeaders(allHeader);
        }
        if (StringUtils.isBlank(contentType)) {
            contentType = DEFAULT_CONTENT_TYPE;
        }
        method.addHeader("Content-Type", contentType);
        method.addHeader("Accept", contentType);
        method.setConfig(requestConfig);
        return method;
    }

    /**
     * 执行GET 请求
     *
     * @param url 请求地址
     * @return
     */
    public static String doGet(String url) {
        return doGet(url, null, null);
    }

    /**
     * 执行GET 请求
     *
     * @param url    请求地址
     * @param params 请求参数
     * @return
     */
    public static String doGet(String url, Map<String, Object> params) {
        return doGet(url, null, params);
    }

    /**
     * 执行GET 请求
     *
     * @param url    请求地址
     * @param params 请求参数
     * @return
     * @throws Exception
     */
    public static String doGet(String url, Map<String, Object> headers, Map<String, Object> params) {
        long startTime = System.currentTimeMillis();
        HttpEntity httpEntity = null;
        HttpRequestBase method = null;
        String responseBody = "";
        try {
            if (httpClient == null) {
                initPools();
            }
            // 创建uri
            URIBuilder builder = new URIBuilder(url);
            // 封装参数
            if (params != null) {
                for (String key : params.keySet()) {
                    builder.addParameter(key, params.get(key).toString());
                }
            }
            URI uri = builder.build();
            // LOGGER.info("请求地址：" + uri);
            method = getRequest(uri, headers, HttpGet.METHOD_NAME, DEFAULT_CONTENT_TYPE, 10);
            HttpContext context = HttpClientContext.create();
            CloseableHttpResponse httpResponse = httpClient.execute(method, context);
            httpEntity = httpResponse.getEntity();
            if (httpEntity != null) {
                responseBody = EntityUtils.toString(httpEntity, "UTF-8");
                //   LOGGER.info("请求URL: " + url + "+  返回状态码：" + httpResponse.getStatusLine().getStatusCode());
            }
            ;
        } catch (HttpHostConnectException e) {
            log.error("默认服务器连接拒绝");
        } catch (Exception e) {
            if (method != null) {
                method.abort();
            }
            e.printStackTrace();
            log.error("execute get request exception, url:" + url + ", exception:" + e.toString() + ",cost time(ms):"
                    + (System.currentTimeMillis() - startTime));
        } finally {
            if (httpEntity != null) {
                try {
                    EntityUtils.consumeQuietly(httpEntity);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("close response exception, url:" + url + ", exception:" + e.toString() + ",cost time(ms):"
                            + (System.currentTimeMillis() - startTime));
                }
            }
        }
        log.debug("总耗时time(ms):{}", (System.currentTimeMillis() - startTime));
        return responseBody;
    }

    /**
     * 执行http post请求 默认采用Content-Type：application/json，Accept：application/json
     *
     * @param url 请求地址
     * @return
     */
    public static String doPost(String url) throws Exception {
        return doPost(url, null, null);
    }

    /**
     * 执行http post请求 默认采用Content-Type：application/json，Accept：application/json
     *
     * @param url    请求地址
     * @param params 请求body参数
     * @return
     */
    public static String doPost(String url, Map<String, Object> params) {
        return doPost(url, null, params);
    }


    /**
     * 执行http post请求 默认采用Content-Type：application/json，Accept：application/json
     *
     * @param url     请求地址
     * @param headers 请求头参数
     * @param params  请求body参数
     * @return
     */
    public static String doPost(String url, Map<String, Object> headers, Map<String, Object> params) {
        JSONObject data = JSONObject.parseObject(JSON.toJSONString(params));
        long startTime = System.currentTimeMillis();
        HttpEntity httpEntity = null;
        HttpEntityEnclosingRequestBase method = null;
        String responseBody = "";
        try {
            if (httpClient == null) {
                initPools();
            }
            method = (HttpEntityEnclosingRequestBase) getRequest(new URI(url), headers, HttpPost.METHOD_NAME, DEFAULT_CONTENT_TYPE, 0);

            //解决中文乱码
            method.addHeader("Content-type", "application/json; chaset=utf-8");
            method.setHeader("Accept", "application/json");
            method.setEntity(new StringEntity(JSON.toJSONString(data), Charset.forName("UTF-8")));

            HttpContext context = HttpClientContext.create();
            CloseableHttpResponse httpResponse = httpClient.execute(method, context);
            httpEntity = httpResponse.getEntity();
            if (httpEntity != null) {
                responseBody = EntityUtils.toString(httpEntity, "UTF-8");
            }

        } catch (Exception e) {
            if (method != null) {
                method.abort();
            }
            e.printStackTrace();
            log.error("execute post request exception, url:" + url + ", exception:" + e.toString() + ", cost time(ms):"
                    + (System.currentTimeMillis() - startTime));
        } finally {
            if (httpEntity != null) {
                try {
                    EntityUtils.consumeQuietly(httpEntity);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("close response exception, url:" + url + ", exception:" + e.toString() + ", cost time(ms):"
                            + (System.currentTimeMillis() - startTime));
                }
            }
        }
        return responseBody;
    }

    public static String doPostJson(String url, String json) {

        long startTime = System.currentTimeMillis();
        HttpEntity httpEntity = null;
        HttpEntityEnclosingRequestBase method = null;
        String responseBody = "";
        try {
            if (httpClient == null) {
                initPools();
            }
            method = (HttpEntityEnclosingRequestBase) getRequest(new URI(url), null, HttpPost.METHOD_NAME, DEFAULT_CONTENT_TYPE, 0);

            //解决中文乱码
            method.addHeader("Content-type", "application/json; chaset=utf-8");
            method.setHeader("Accept", "application/json");
            method.setEntity(new StringEntity(json, Charset.forName("UTF-8")));

            HttpContext context = HttpClientContext.create();
            CloseableHttpResponse httpResponse = httpClient.execute(method, context);
            httpEntity = httpResponse.getEntity();
            if (httpEntity != null) {
                responseBody = EntityUtils.toString(httpEntity, "UTF-8");
            }

        } catch (Exception e) {
            if (method != null) {
                method.abort();
            }
            e.printStackTrace();
            log.error("execute post request exception, url:" + url + ", exception:" + e.toString() + ", cost time(ms):"
                    + (System.currentTimeMillis() - startTime));
        } finally {
            if (httpEntity != null) {
                try {
                    EntityUtils.consumeQuietly(httpEntity);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("close response exception, url:" + url + ", exception:" + e.toString() + ", cost time(ms):"
                            + (System.currentTimeMillis() - startTime));
                }
            }
        }
        return responseBody;
    }

    public static String sendPostForm(String url, Map<String, Object> params) {
        // 创建POST请求
        cn.hutool.http.HttpRequest request = cn.hutool.http.HttpRequest.post(url)
                .contentType("application/x-www-form-urlencoded");

        // 将Map参数添加到请求中
        params.forEach(request::form);

        // 发送请求并接收响应
        cn.hutool.http.HttpResponse response = request.timeout(10000).execute();

        // 返回响应内容
        return response.body();
    }

}
