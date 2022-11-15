package com.example.api_translation.domain.services.impl;

import com.example.api_translation.app.dtos.ChargeCoinDTO;
import com.example.api_translation.app.dtos.TransferCoinDTO;
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
import com.google.gson.Gson;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.*;
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

    private String local = "https://3e1a-1-52-236-61.ap.ngrok.io";

    private String dev = "https://api-dev.staging-cvalue.jp";

    @Override
    public WordResponse getWord(String sourceLang, String targetLang, String word) {
        ModelMapper modelMapper = new ModelMapper();
        Word w = wordRepository.findBySourceLangAndTargetLangAndWord(sourceLang, targetLang, word);
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
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessage.WORD_NOT_FOUND);
        }

    }

    String sendWithToken(ChargeCoinDTO chargeCoinDTO, String token, RestTemplate restTemplate) {
        Gson gson = new Gson();
        if (restTemplate == null) {
            restTemplate = new RestTemplate();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);

        HttpEntity<String> entity = new HttpEntity<>(gson.toJson(chargeCoinDTO), headers);
            return restTemplate.postForObject(local + "/api/v1/shops/charge/coin ", entity, String.class);

    }

    String transferCoin(TransferCoinDTO transferCoinDTO, String token, RestTemplate restTemplate) {
        Gson gson = new Gson();
        if (restTemplate == null) {
            restTemplate = new RestTemplate();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);

        HttpEntity<String> entity = new HttpEntity<>(gson.toJson(transferCoinDTO), headers);
        try {
            return restTemplate.postForObject(local + "/api/v1/customers/transfer/coin", entity, String.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
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
        for (int i = 1; i <= 100; i++) {
            customerNames.add("customer" + i);
        }
        Map<String, Object> mapBody = new HashMap<>();
        mapBody.put("type", "customers");
        mapBody.put("name", customerNames);

        CustomerResponse customerResponse = restTemplate.postForObject(local + "/api/v1/token", mapBody, CustomerResponse.class);
        return customerResponse.getData();
    }

    private List<String> getListShop() {
        RestTemplate restTemplate = new RestTemplate();
        List<String> customerNames = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            customerNames.add("store" + i);
        }
        Map<String, Object> mapBody = new HashMap<>();
        mapBody.put("type", "shops");
        mapBody.put("name", customerNames);

        ShopResponse customerResponse = restTemplate.postForObject(local + "/api/v1/token", mapBody, ShopResponse.class);
        return customerResponse.getData();
    }

    private void chargeCoin(List<Customer> customers, List<String> shops) throws ExecutionException, InterruptedException {
        RestTemplate restTemplate = new RestTemplate();
        ExecutorService executor = Executors.newFixedThreadPool(100);
        int length = customers.size();
        List<Future> futures = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            final String shopToken = shops.get(i);
            ChargeCoinDTO chargeCoinDTO = new ChargeCoinDTO(customers.get(i).getPublicKey(), 5);
            Future<Long> future = executor.submit(() -> {
                try {
                    long startTime = System.nanoTime();
                    sendWithToken(chargeCoinDTO, shopToken, null);
                    long endTime = System.nanoTime();
                    return (endTime - startTime) / 1000000;
                } catch(Exception e) {
                    return null;
                }
            });
            futures.add(future);
        }
        int total = 0;
        for (Future future : futures) {
            Object time = future.get();
            if (time != null) {
                total++;
            }
            System.out.println(time);
        }
        System.out.println(total);

        executor.shutdown();

    }

    public String checkProxy() throws ExecutionException, InterruptedException {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(10000);
        requestFactory.setReadTimeout(10000);

        List<Customer> customers = getListCustomerByName();

        List<String> shops = getListShop();
        chargeCoin(customers, shops);

//        int length = customers.size();
//        int total = 0;
//        for (int i = 0; i < length; i++) {
//
//            final String shopToken = shops.get(i);
//            final int index = i;
//        //    CompletableFuture<Long> charge =
//                    CompletableFuture.supplyAsync(() -> {
//                TransferCoinDTO transferCoinDTO = null;
//                Customer sender = null;
//                if (index < 99) {
//                    sender = customers.get(index);
//                    transferCoinDTO = new TransferCoinDTO(customers.get(index).getCoinId(), customers.get(index + 1).getPublicKey());
//                } else {
//                    sender = customers.get(99);
//                    transferCoinDTO = new TransferCoinDTO(customers.get(99).getCoinId(), customers.get(0).getPublicKey());
//                }
//                long startTime = System.nanoTime();
//                if (!transferCoinDTO.getCoin_wallets().get(0).getId().isEmpty()) {
//                    transferCoin(transferCoinDTO, sender.getToken(), null);
//                }
//                long endTime = System.nanoTime();
//                return endTime - startTime;
//            }).thenApply(value -> {
//                System.out.println(value / 1000000);
//                return 0;
//            });
////            total += charge.get() / 1000000;
////            System.out.println(charge.get() / 1000000 + 's');
//        }
//
//        System.out.println("Trung binh: " + (double) total / 100);


//        List<ProxyObject> proxyList = getListProxy();
//
//        for (int i = 0; i < length; i++) {
//            int index = i % 100;
//
//            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyList.get(i).getHost(), proxyList.get(i).getPort()));
//            requestFactory.setProxy(proxy);
//            RestTemplate restTemplate = new RestTemplate(requestFactory);
//            try {
//                ChargeCoinDTO chargeCoinDTO = new ChargeCoinDTO(customers.get(index).getPublicKey(), 5);
//                final String shopToken = shops.get(index);
//                CompletableFuture.supplyAsync(() -> {
//                    long startTime = System.nanoTime();
//                    sendWithToken(chargeCoinDTO, shopToken, restTemplate);
//                    long endTime = System.nanoTime();
//                    return endTime - startTime;
//                }).thenApply(value -> {
//                    System.out.println(value/1000000);
//                    return 0;
//                });
//
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//        }

        return "oke";
    }
}
