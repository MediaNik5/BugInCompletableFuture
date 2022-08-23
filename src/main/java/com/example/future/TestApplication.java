package com.example.future;

import lombok.SneakyThrows;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@SpringBootApplication
public class TestApplication{

    public static void main(String[] args){
        SpringApplication.run(TestApplication.class, args);
    }
    private static final RestTemplate restTemplate = new RestTemplate();
    static void makeRequest(){
        restTemplate.execute("http://localhost:8080/", HttpMethod.GET, request -> {}, response -> null);
    }
}

@Component
class Asyncer implements CommandLineRunner{
    @Override
    public void run(String... args) throws Exception{
        long time = System.nanoTime();
        try{
            System.out.println("Start 1");
            CompletableFuture.runAsync(TestApplication::makeRequest).get(1, TimeUnit.SECONDS);
            System.out.println("End 1");
        }catch(TimeoutException e){
            System.out.println("Timeout 1");
        }
        long endTime = System.nanoTime();
        System.out.println((endTime - time)/1_000_000 + " ms");
    }
}
@Component
class AsyncerInsideAnotherAsyncer implements CommandLineRunner{

    @Override
    public void run(String... args) throws Exception{
        long time = System.nanoTime();
        try{
            System.out.println("Start 2");
            CompletableFuture.runAsync(() -> {
                long innerTime = System.nanoTime();
                try{
                    System.out.println("Start inner");
                    CompletableFuture.runAsync(TestApplication::makeRequest).get(1, TimeUnit.SECONDS);
                    System.out.println("End inner");
                }catch(InterruptedException | ExecutionException | TimeoutException e){
                    System.out.println("Inner timeout");
                }
                long innerTimeEnd = System.nanoTime();
                System.out.println((innerTimeEnd - innerTime)/1_000_000 + " ms");
            }).get(1, TimeUnit.SECONDS);
            System.out.println("End 2");
        }catch(TimeoutException e){
            System.out.println("Timeout 2");
        }
        long endTime = System.nanoTime();
        System.out.println((endTime - time)/1_000_000 + " ms");
    }
}




@RestController
class ControllerWithHighWork{
    @SneakyThrows
    @GetMapping
    public String get(){
        Thread.sleep(2000);
        return "Hello";
    }
}