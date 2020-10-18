package com.swg.httpclinet.pool;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolTest {
    private static final int THREAD_COUNT = 5;
    private static ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_COUNT);

    public static void main(String[] args) {

        for(int i=0; i<50; i++){
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    Random random = new Random();
                    try {
                        System.out.println("save data");
                        Thread.sleep(random.nextInt(5)*1000);
                    }catch (InterruptedException e){
                    }

                }
            });
        }
        threadPool.shutdown();
    }
}
