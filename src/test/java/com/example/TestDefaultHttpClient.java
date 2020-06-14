package com.example;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
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
public class TestDefaultHttpClient {

    @Test
    public void testSingleNotClose() throws Exception {
        single(getDefaultClient());
        Thread.sleep(5 * 60 * 1000);
    }


    @Test
    public void testTwoNewClient() throws Exception {
        CloseableHttpClient defaultClient1 = getDefaultClient();
        CloseableHttpClient defaultClient2 = getDefaultClient();
        single(defaultClient1);
        single(defaultClient2);
        Thread.sleep(5 * 60 * 1000);
    }

    @Test
    public void testTwoSameClient() throws Exception {
        CloseableHttpClient defaultClient = getDefaultClient();
        single(defaultClient);
        single(defaultClient);
        Thread.sleep(5 * 60 * 1000);
    }

    private CloseableHttpClient getDefaultClient() {
        CloseableHttpClient httpclient = HttpClients.createDefault();
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
                    // comment this
                    instream.close();
                }
            }
        } finally {
            // comment this
            response.close();
        }
    }
}
