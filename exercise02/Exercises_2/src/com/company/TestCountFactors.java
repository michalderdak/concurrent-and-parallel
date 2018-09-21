package com.company;// For week 2
// sestoft@itu.dk * 2014-08-29

import java.util.concurrent.atomic.AtomicInteger;

class TestCountFactors {
    public static void main(String[] args) {
        for (int counting = 0; counting < 10; counting++) {
            long initialTime = System.currentTimeMillis();
            //Start
            final int range = 5_000_000;
            final int nrOfThreads = 10;
            final AtomicInteger counter = new AtomicInteger(0);
            startThreads(nrOfThreads, range / nrOfThreads, counter);
            System.out.printf("Total number of factors is %9d%n", counter.get());
            //End
            long timeTaken = System.currentTimeMillis() - initialTime;
            System.out.println("Total time taken: " + timeTaken + "ms");
        }
    }

    public static void startThread(Runnable workTodo) {
        Thread t = new Thread(workTodo);
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //Solution to Exercise 2.2.3
    public static void startThreads(int nrOfThreads, int rangePerThread, final AtomicInteger counter) {
        Thread[] threads = new Thread[nrOfThreads];
        for (int threadNr = 0; threadNr < threads.length; threadNr++) {
            //Assigns the range to each thread using "rangePerThread" and "threadNr".
            final int from = threadNr * rangePerThread;
            final int to = from + rangePerThread;
            threads[threadNr] = new Thread(() -> {
                for (int i = from; i < to; i++) {
                    counter.addAndGet(countFactors(i));
                }
            });
            threads[threadNr].start();
        }
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void startThreadsUpdated(int nrOfThreads, int rangePerThread, final AtomicInteger counter) {
        Thread[] threads = new Thread[nrOfThreads];
        for (int threadNr = 0; threadNr < threads.length; threadNr++) {
            //Assigns the range to each thread using "rangePerThread" and "threadNr".
            final int from = threadNr * rangePerThread;
            final int to = from + rangePerThread;
            threads[threadNr] = new Thread(() -> {
                for (int i = from; i < to; i++) {
                    counter.addAndGet(countFactors(i));
                }
            });
            threads[threadNr].start();
        }
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static int countFactors(int p) {
        if (p < 2)
            return 0;
        int factorCount = 1, k = 2;
        while (p >= k * k) {
            if (p % k == 0) {
                factorCount++;
                p /= k;
            } else
                k++;
        }
        return factorCount;
    }
}

class MyAtomicInteger {
    private int value;

    MyAtomicInteger(int initialValue) {
        value = initialValue;
    }

    public synchronized int get() {
        return value;
    }

    public synchronized int addAndGet(int amount) {
        value += amount;
        return value;
    }
}