package com.example;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.InputStream;

/**
 * @author pyx
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = HelloWorldApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class TestCustomPoolHttpClient {

    @Test
    public void testSingleNotClose() throws Exception {
        single(getCustomPoolClient());
        continueLogSate();
    }


    @Test
    public void testTwoNewClient() throws Exception {
        CloseableHttpClient customPoolClient1 = getCustomPoolClient();
        CloseableHttpClient customPoolClient2 = getCustomPoolClient();
        single(customPoolClient1);
        single(customPoolClient2);
        continueLogSate();
    }

    @Test
    public void testTwoSameClient() throws Exception {
        CloseableHttpClient defaultClient = getCustomPoolClient();
        single(defaultClient);
        single(defaultClient);
        continueLogSate();
    }

    @Test
    public void testComplexClient() throws Exception {
        new Thread(() -> {
            try {
                log.info("-----req1----");
                single(getCustomPoolClient());
            } catch (Exception e) {
            }
        }).start();
        new Thread(() -> {
            try {
                log.info("-----req2----");
                single(getCustomPoolClient());
            } catch (Exception e) {
            }
        }).start();
        new Thread(() -> {
            try {
                Thread.sleep((30 * 1000));
                log.info("-----req3----");
                single(getCustomPoolClient());
            } catch (Exception e) {
            }
        }).start();
        new Thread(() -> {
            try {
                Thread.sleep((120 * 1000));
                log.info("-----req4----");
                single(getCustomPoolClient());
            } catch (Exception e) {
            }
        }).start();
        continueLogSate();
    }

    private void continueLogSate() throws Exception {
        while (true) {
            Thread.sleep(5 * 1000);
            log.info(HttpPoolUtil.getTotalStats());
        }
    }

    private CloseableHttpClient getCustomPoolClient() {
        CloseableHttpClient httpclient = HttpPoolUtil.getHttpClient();
        return httpclient;
    }

    private void single(CloseableHttpClient httpClient) throws Exception {
        HttpGet httpget = new HttpGet("http://127.0.0.1:8080/hello");
        CloseableHttpResponse response = httpClient.execute(httpget);
        try {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream instream = entity.getContent();
                try {
                    // do something useful
                } finally {
                    instream.close();
                }
            }
        } finally {
            // comment this
            response.close();
        }
    }
}
