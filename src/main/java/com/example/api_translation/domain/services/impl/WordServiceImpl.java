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

    private String local = "http://localhost:8000";

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

    String transferCoin(TransferCoinDTO transferCoinDTO, String token, RestTemplate restTemplate, int index) {
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
            System.out.println(index);
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

    private List<Customer> getListCustomerByName(int number) {
        RestTemplate restTemplate = new RestTemplate();
        List<String> customerNames = new ArrayList<>();
        for (int i = 1; i <= number; i++) {
            customerNames.add("customer" + i);
        }
        Map<String, Object> mapBody = new HashMap<>();
        mapBody.put("type", "customers");
        mapBody.put("name", customerNames);

        CustomerResponse customerResponse = restTemplate.postForObject(local + "/api/v1/token", mapBody, CustomerResponse.class);
        return customerResponse.getData();
    }

    private List<String> getListShop(int number) {
        RestTemplate restTemplate = new RestTemplate();
        List<String> customerNames = new ArrayList<>();
        for (int i = 1; i <= number; i++) {
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
        ExecutorService executor = Executors.newFixedThreadPool(customers.size());
        int length = customers.size();
        List<Future> futures = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            final String shopToken = shops.get(i);
            ChargeCoinDTO chargeCoinDTO = new ChargeCoinDTO(customers.get(i).getPublicKey(), 5);
            Future<Long> future = executor.submit(() -> {
                try {
                    long startTime = System.nanoTime();
                    String result = sendWithToken(chargeCoinDTO, shopToken, null);
                    System.out.println(result);
                    long endTime = System.nanoTime();
                    return (endTime - startTime) / 1000000;
                } catch (Exception e) {
                    return null;
                }
            });
            futures.add(future);
        }
        int total = 0, successRequest = 0;
        double max = 0, min = 30000;
        for (Future future : futures) {
            Double time = Double.valueOf(future.get().toString());
            if (time != null) {
                total++;
                if (time >= max) {
                    max = time;
                }
                if (time <= min) {
                    min = time;
                }
                if (time < 30000) {
                    successRequest++;
                }
            }
            System.out.println(time);
        }
        System.out.println("Số request hoàn tất: " + total);
        System.out.println("Số request thành công: " + successRequest);
        System.out.println("Max: " + max);
        System.out.println("Min: " + min);

        executor.shutdown();

    }

    private void transferCoin(List<Customer> customers) throws ExecutionException, InterruptedException {
        RestTemplate restTemplate = new RestTemplate();
        ExecutorService executor = Executors.newFixedThreadPool(customers.size());
        int length = customers.size();
        List<Future> futures = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            final int index = i;
            Future<Long> future = executor.submit(() -> {
                try {
                    TransferCoinDTO transferCoinDTO = null;
                Customer sender = null;
                if (index < length - 1) {
                    sender = customers.get(index);
                    transferCoinDTO = new TransferCoinDTO(customers.get(index).getCoinId(), customers.get(index + 1).getPublicKey());
                } else {
                    sender = customers.get(length - 1);
                    transferCoinDTO = new TransferCoinDTO(customers.get(length -1).getCoinId(), customers.get(0).getPublicKey());
                }
                long startTime = System.nanoTime();
                if (!transferCoinDTO.getCoin_wallets().get(0).getId().isEmpty()) {
                    transferCoin(transferCoinDTO, sender.getToken(), null, index);
                }
                long endTime = System.nanoTime();
                return (endTime - startTime)/ 1000000;
                } catch (Exception e) {
                    return null;
                }
            });
            futures.add(future);
        }
        int total = 0, successRequest = 0;
        double max = 0, min = 30000;
        for (Future future : futures) {
            Double time = Double.valueOf(future.get().toString());
            if (time != null) {
                total++;
                if (time >= max) {
                    max = time;
                }
                if (time <= min) {
                    min = time;
                }
                if (time < 30000) {
                    successRequest++;
                }
            }
            System.out.println(time);
        }
        System.out.println("Số request hoàn tất: " + total);
        System.out.println("Số request thành công: " + successRequest);
        System.out.println("Max: " + max);
        System.out.println("Min: " + min);

        executor.shutdown();

    }

    public String charge(int num) throws ExecutionException, InterruptedException {

        List<Customer> customers = getListCustomerByName(num);
        System.out.println("-------------Charge coin performance-------------");

        List<String> shops = getListShop(num);
        chargeCoin(customers, shops);

        return "oke";
    }

    public String transfer(int num) throws ExecutionException, InterruptedException {
        List<Customer> customers = getListCustomerByName(num);
        System.out.println("-----------Transfer coin performance---------------");
        transferCoin(customers);

        return "oke";
    }
}
