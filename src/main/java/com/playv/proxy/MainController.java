package com.playv.proxy;

import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Enumeration;

@RestController
public class MainController {
    @GetMapping("/binance/**")
    public ResponseEntity<String> proxy(HttpServletRequest request, HttpServletResponse response) throws IOException, URISyntaxException {

        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setConnectionRequestTimeout(360000); //6m
        httpRequestFactory.setConnectTimeout(360000); //6m
        httpRequestFactory.setReadTimeout(360000); //6m

        // restTempate tobe bean
        RestTemplate restTemplate = new RestTemplate(httpRequestFactory);

        // url
        String originReqURL = request.getRequestURI().replaceAll("^/binance", "");
        String originQueryString = request.getQueryString();
        String urlStr = "https://api.binance.com" + originReqURL + (StringUtils.isEmpty(originQueryString) ? "" : "?"+originQueryString);

        URI url = new URI(urlStr);

        HttpMethod method = HttpMethod.GET;

        // header
        Enumeration<String> headerNames = request.getHeaderNames();
        MultiValueMap<String, String> headerMap = new LinkedMultiValueMap<>();
        while(headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);

            headerMap.add(headerName, headerValue);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application","json", StandardCharsets.UTF_8));
        headers.add("Accept", "*/*");
        headers.add("X-MBX-APIKEY", headerMap.getFirst("x-mbx-apikey"));

        // http entity (body, header)
        HttpEntity<String> httpEntity = new HttpEntity<String>(headers);
        ResponseEntity<String> res = restTemplate.exchange(url, method, httpEntity, String.class);

        return res;
    }
}
