package com.example.api_translation.domain.services.impl;

import com.example.api_translation.app.response.WordResponse;
import com.example.api_translation.domain.data.Customer;
import com.example.api_translation.domain.entities.ProxyObject;
import com.example.api_translation.domain.entities.Word;
import com.example.api_translation.domain.exceptions.BusinessException;
import com.example.api_translation.domain.exceptions.ErrorMessage;
import com.example.api_translation.domain.reponse.CustomerResponse;
import com.example.api_translation.domain.reponse.ShopResponse;
import com.example.api_translation.domain.repositories.WordRepository;
import com.example.api_translation.domain.services.WordService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class WordServiceImpl implements WordService {

    @Autowired
    private WordRepository wordRepository;

    @Value("${oxford.application_id}")
    private String applicationId;

    @Value("${oxford.application_key}")
    private String applicationKey;

    @Value("${oxford.translation_api}")
    private String apiTranslation;

    @Override
    public WordResponse getWord(String sourceLang, String targetLang, String word) {
        ModelMapper modelMapper = new ModelMapper();
        Word w = wordRepository.findBySourceLangAndTargetLangAndWord(sourceLang, targetLang,word);
        if (w == null) {
            String content = getContent(sourceLang, targetLang, word);
            w = Word.builder().word(word).sourceLang(sourceLang)
                    .targetLang(targetLang).content(content).build();
            w = wordRepository.save(w);
        }

        return modelMapper.map(w, WordResponse.class);
    }

    private String getContent(String sourceLang, String targetLang, String word) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.set("app_id", applicationId);
        headers.set("app_key", applicationKey);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        String url = apiTranslation + sourceLang + "/" + targetLang + "/" + word;
        try {
            return restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
        } catch(Exception e) {
            e.printStackTrace();
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessage.WORD_NOT_FOUND);
        }

    }

    private List<ProxyObject> getListProxy() {
        RestTemplate restTemplate = new RestTemplate();
        String proxy = restTemplate.getForObject("https://api.proxyscrape.com/v2/?request=displayproxies&protocol=http&timeout=10000&country=all&ssl=all&anonymity=all", String.class);
        String[] proxyArray = proxy.split("\r\n");
        return Arrays.asList(proxyArray).parallelStream().map(item -> {
            String[] tem = item.split(":");
            return new ProxyObject(tem[0], Integer.parseInt(tem[1]));
        }).collect(Collectors.toList());
    }

    private List<Customer> getListCustomerByName() {
        RestTemplate restTemplate = new RestTemplate();
        List<String> customerNames = new ArrayList<>();
        for(int i = 1; i <= 10; i++) {
            customerNames.add("customer" + i);
        }
        Map<String, Object> mapBody = new HashMap<>();
        mapBody.put("type", "customers");
        mapBody.put("name", customerNames);

        CustomerResponse customerResponse = restTemplate.postForObject("http://127.0.0.1:8000/api/v1/token",mapBody, CustomerResponse.class);
        return customerResponse.getData();
    }

    private List<String> getListShop() {
        RestTemplate restTemplate = new RestTemplate();
        List<String> customerNames = new ArrayList<>();
        for(int i = 1; i <= 10; i++) {
            customerNames.add("store" + i);
        }
        Map<String, Object> mapBody = new HashMap<>();
        mapBody.put("type", "shops");
        mapBody.put("name", customerNames);

        ShopResponse customerResponse = restTemplate.postForObject("http://127.0.0.1:8000/api/v1/token",mapBody, ShopResponse.class);
        return customerResponse.getData();
    }

    public String checkProxy() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(10000);
        requestFactory.setReadTimeout(10000);

        List<Customer> customers = getListCustomerByName();

        List<String> shops = getListShop();

        List<ProxyObject> proxyList = getListProxy();

        for (ProxyObject item : proxyList) {
            CompletableFuture.runAsync(() -> {
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(item.getHost(), item.getPort()));
                requestFactory.setProxy(proxy);
                RestTemplate restTemplate = new RestTemplate(requestFactory);
                try {
                    restTemplate.getForObject("https://restcountries.com/v3.1/all", String.class);
                    System.out.println(item.getHost());
                    try {
                        restTemplate.getForObject("https://api-dev.staging-cvalue.jp/api/v1/shops/charge/coin", String.class);
                    } catch (Exception e) {
                        System.out.println("oke" + item.getHost());
                        System.out.println(e.getMessage());
                    }


                } catch (Exception e) {
                   //e.printStackTrace();
                }
            });

        }

        return "oke";
    }
}
