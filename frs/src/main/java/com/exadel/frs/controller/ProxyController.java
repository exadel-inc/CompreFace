package com.exadel.frs.controller;

import com.exadel.frs.proxy.response.RecognizeResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/proxy")
public class ProxyController {

    @Value("${proxy.baseUrl}")
    private String baseUrl;

    private static final String API_KEY_HEADER = "X-Api-Key";

    @GetMapping("/all")
    public List<String> all(@RequestHeader(API_KEY_HEADER) String apiKey) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<List<String>> response = restTemplate.exchange(baseUrl + "/all", HttpMethod.GET,
                request(apiKey), new ParameterizedTypeReference<List<String>>() {
                });
        return response.getBody();
    }

    @PostMapping("/recognize")
    public List<RecognizeResponse> recognize(@RequestHeader(API_KEY_HEADER) String apiKey,
                                             @RequestParam("file") MultipartFile file,
                                             @RequestParam("limit") String limit) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<List<RecognizeResponse>> response = restTemplate.exchange(baseUrl + "/recognize", HttpMethod.POST,
                fileRequest(apiKey, file, limit), new ParameterizedTypeReference<List<RecognizeResponse>>() {
                });
        return response.getBody();
    }

    @DeleteMapping("/remove/{face_name}")
    public ResponseEntity<String> remove(@RequestHeader(API_KEY_HEADER) String apiKey,
                                         @PathVariable String face_name) {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.exchange(baseUrl + "/remove/" + face_name, HttpMethod.DELETE, request(apiKey),
                String.class);
    }

    @PostMapping("/retrain")
    public ResponseEntity<String> retrain(@RequestHeader(API_KEY_HEADER) String apiKey) {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.postForEntity(baseUrl + "/retrain", request(apiKey), String.class);
    }

    @PostMapping("/save/{face_name}")
    public ResponseEntity<String> save(@RequestHeader(API_KEY_HEADER) String apiKey,
                                       @RequestParam("file") MultipartFile file,
                                       @PathVariable String face_name) {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.postForEntity(baseUrl + "/save/" + face_name, fileRequest(apiKey, file, null), String.class);
    }

    private HttpEntity<?> request(String apiKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(API_KEY_HEADER, apiKey);
        return new HttpEntity<>(headers);
    }

    private HttpEntity fileRequest(String apiKey, MultipartFile file, String limit) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(API_KEY_HEADER, apiKey);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource());
        if (limit != null) {
            body.add("limit", limit);
        }
        return new HttpEntity<>(body, headers);
    }

}
