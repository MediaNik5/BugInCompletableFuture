package com.example.future;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TestApplication{

    public static void main(String[] args) throws ExecutionException, InterruptedException{
        oneCompletableFuture();
        oneCompletableFutureInsideAnother();

        // don't kill main thread, because other threads will not have enough
        // time to finish
        Thread.sleep(2000);
    }

    private static void oneCompletableFuture() throws InterruptedException, ExecutionException{
        long time = System.nanoTime();
        try{
            System.out.println("1 started");
            CompletableFuture.runAsync(() -> {
                try{
                    Thread.sleep(2000);
                }catch(InterruptedException e){
                    throw new RuntimeException(e);
                }
            }).get(1, TimeUnit.SECONDS);
            System.out.println("1 completed successfully");
        }catch(TimeoutException e){
            System.out.println("1 timed out");
        }
        long endTime = System.nanoTime();
        System.out.println("1 took " + (endTime - time)/1_000_000 + " ms");
    }

    private static void oneCompletableFutureInsideAnother() throws InterruptedException, ExecutionException{
        long time = System.nanoTime();
        try{
            System.out.println("2 started");
            CompletableFuture.runAsync(() -> {
                long innerTime = System.nanoTime();
                try{
                    System.out.println("inner started");
                    CompletableFuture.runAsync(() -> {
                        try{
                            Thread.sleep(2000);
                        }catch(InterruptedException e){
                            throw new RuntimeException(e);
                        }
                    }).get(1, TimeUnit.SECONDS);
                    System.out.println("inner completed successfully");
                }catch(InterruptedException | ExecutionException | TimeoutException e){
                    System.out.println("inner timed out");
                }
                long innerTimeEnd = System.nanoTime();
                System.out.println("inner took " + (innerTimeEnd - innerTime)/1_000_000 + " ms");
            }).get(1, TimeUnit.SECONDS);
            System.out.println("2 completed successfully");
        }catch(TimeoutException e){
            System.out.println("2 timed out");
        }
        long endTime = System.nanoTime();
        System.out.println("2 took " + (endTime - time)/1_000_000 + " ms");
    }
}

