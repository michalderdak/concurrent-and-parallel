package com.company;// For week 2
// sestoft@itu.dk * 2014-09-04

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.LongAdder;

class SimpleHistogram {
    public static void main(String[] args) {
        long initialTime = System.currentTimeMillis();

        //Start
        final int range = 5_000_000;
        final int nrOfThreads = 10;
        final Histogram histogram = new Histogram5(30);
        final AtomicInteger counter = new AtomicInteger(0);
        startThreads(nrOfThreads, range / nrOfThreads, counter, histogram);
        System.out.println("Reached");
        //dump(histogram);
        //System.out.println(Arrays.toString(histogram.getBins()));
        //End

        long timeTaken = System.currentTimeMillis() - initialTime;
        System.out.println("Total time taken: " + timeTaken + "ms");
    }

    public static void dump(Histogram histogram) {
        int totalCount = 0;
        for (int bin = 0; bin < histogram.getSpan(); bin++) {
            System.out.printf("%4d: %9d%n", bin, histogram.getCount(bin));
            totalCount += histogram.getCount(bin);
        }
        System.out.printf("      %9d%n", totalCount);
    }

    public static void startThreads(int nrOfThreads, int rangePerThread, final AtomicInteger counter, final Histogram histogram) {
        Thread[] threads = new Thread[nrOfThreads];
        for (int threadNr = 0; threadNr < threads.length; threadNr++) {
            final int from = threadNr * rangePerThread;
            final int to = from + rangePerThread;
            threads[threadNr] = new Thread(() -> {
                for (int i = from; i < to; i++) {
                    int nrOfFactors = TestCountFactors.countFactors(i);
                    counter.addAndGet(nrOfFactors);
                    histogram.increment(nrOfFactors);
                }
            });
            threads[threadNr].start();
        }
        Thread binCheckerThread = new Thread(()->{
            while (true)
            {
                System.out.println(Arrays.toString(histogram.getBins()));
            }
        });
        binCheckerThread.start();
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

interface Histogram {
    public void increment(int bin);

    public int getCount(int bin);

    public int getSpan();

    public int[] getBins();
}

class Histogram1 implements Histogram {
    private int[] counts;

    public Histogram1(int span) {
        this.counts = new int[span];
    }

    public void increment(int bin) {
        counts[bin] = counts[bin] + 1;
    } //read-modify-write

    public int getCount(int bin) {
        return counts[bin];
    } //Possible "stale data"

    public int getSpan() {
        return counts.length;
    } //Possible "stale data"

    public int[] getBins() {
        return counts;
    } //Returning array directly means someone can change the values.
}

class Histogram2 implements Histogram {
    private volatile int[] counts;

    public Histogram2(int span) {
        this.counts = new int[span];
    }

    public synchronized void increment(int bin) {
        counts[bin] = counts[bin] + 1;
    }

    public int getCount(int bin) {
        return counts[bin];
    }

    public int getSpan() {
        return counts.length;
    }

    public int[] getBins() {
        return counts.clone();
    }
}

class Histogram3 implements Histogram {
    private final AtomicInteger[] counts;

    public Histogram3(int span) {
        this.counts = new AtomicInteger[span];
        for (int i = 0; i < counts.length; i++) {
            this.counts[i] = new AtomicInteger();
        }
    }

    public void increment(int bin) {
        counts[bin].incrementAndGet();
    }

    public int getCount(int bin) {
        return counts[bin].get();
    }

    public int getSpan() {
        return counts.length;
    }

    public int[] getBins() {
        int[] newArray = new int[counts.length];
        for (int i = 0; i < counts.length; i++) {
            newArray[i] = counts[i].get();
        }
        return newArray;
    }
}

class Histogram4 implements Histogram {
    private final AtomicIntegerArray counts;

    public Histogram4(int span) {
        this.counts = new AtomicIntegerArray(span);
    }

    public void increment(int bin) {
        counts.addAndGet(bin, 1);
    }

    public int getCount(int bin) {
        return counts.get(bin);
    }

    public int getSpan() {
        return counts.length();
    }

    public int[] getBins() {
        int[] newArray = new int[counts.length()];
        for (int i = 0; i < counts.length(); i++) {
            newArray[i] = counts.get(i);
        }
        return newArray;
    }
}

class Histogram5 implements Histogram {
    private final LongAdder[] counts;

    public Histogram5(int span) {
        this.counts = new LongAdder[span];
        for (int i = 0; i < counts.length; i++) {
            this.counts[i] = new LongAdder();
        }
    }

    public void increment(int bin) {
        counts[bin].increment();
    }

    public int getCount(int bin) {
        return counts[bin].intValue();
    }

    public int getSpan() {
        return counts.length;
    }

    public int[] getBins() {
        int[] newArray = new int[counts.length];
        for (int i = 0; i < counts.length; i++) {
            newArray[i] = counts[i].intValue();
        }
        return newArray;
    }
}