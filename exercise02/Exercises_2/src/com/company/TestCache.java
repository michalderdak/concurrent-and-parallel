package com.company;// For week 2

// Code from Goetz et al 5.6, written by Brian Goetz and Tim Peierls.
// Modifications by sestoft@itu.dk * 2014-09-08

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.*;
import java.util.function.Function;

public class TestCache {
    public static void main(String[] args) throws InterruptedException {
        long time = System.currentTimeMillis();

        Computable<Long, long[]> f = new Factorizer();

        //exerciseFactorizer(f);
        //exerciseFactorizer(new Memoizer1<Long, long[]>(f));
        //exerciseFactorizer(new Memoizer2<Long, long[]>(f));
        //exerciseFactorizer(new Memoizer3<Long, long[]>(f));
        //exerciseFactorizer(new Memoizer4<Long, long[]>(f));
        //exerciseFactorizer(new Memoizer5<Long, long[]>(f));
        exerciseFactorizer(new Memoizer0<Long, long[]>(f));

        System.out.println(((Factorizer) f).getCount());

        long timeTaken = System.currentTimeMillis() - time;
        System.out.println("Time" + timeTaken + " ms.");
    }

    private static void exerciseFactorizer(Computable<Long, long[]> f) {
        final int threadCount = 16;
        final long start = 10_000_000_000L, range = 20_000L;
        System.out.println(f.getClass());
        startThreads(threadCount, start, range, f);
    }

    private static void startThreads(int threadCount, long start, long range, Computable<Long, long[]> f) {
        Thread[] threads = new Thread[threadCount];
        for (int threadNr = 0; threadNr < threads.length; threadNr++) {
            final long from1 = start;
            final long to1 = start + range;

            final long from2 = start + range + ((threadNr * range) / 4);
            final long to2 = from2 + range;

            threads[threadNr] = new Thread(() -> {
                for (long i = from1; i < to1; i++) {
                    try {
                        f.compute(i);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                for (long i = from2; i < to2; i++) {
                    try {
                        f.compute(i);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
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

    private static void print(long[] arr) {
        for (long x : arr)
            System.out.print(" " + x);
        System.out.println();
    }
}


// Interface that represents a function from A to V
interface Computable<A, V> {
    V compute(A arg) throws InterruptedException;
}


// Prime factorization is a function from Long to long[]
class Factorizer implements Computable<Long, long[]> {
    // For statistics only, count number of calls to factorizer:
    private final AtomicLong count = new AtomicLong(0);

    public long getCount() {
        return count.longValue();
    }

    public long[] compute(Long wrappedP) {
        count.getAndIncrement();
        long p = wrappedP;
        ArrayList<Long> factors = new ArrayList<Long>();
        long k = 2;
        while (p >= k * k) {
            if (p % k == 0) {
                factors.add(k);
                p /= k;
            } else
                k++;
        }
        // Now k * k > p and no number in 2..k divides p
        factors.add(p);
        long[] result = new long[factors.size()];
        for (int i = 0; i < result.length; i++)
            result[i] = factors.get(i);
        return result;
    }
}

class Memoizer0<A, V> implements Computable<A, V> {
    private final Map<A, V> cache = new ConcurrentHashMap<A, V>();
    private final Computable<A, V> c;

    public Memoizer0(Computable<A, V> c) {
        this.c = c;
    }

    public V compute(final A arg) throws InterruptedException {
        V result = cache.computeIfAbsent(arg, a -> {
            try {
                return c.compute(a);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            }
        });
        return result;
    }


    public static RuntimeException launderThrowable(Throwable t) {
        if (t instanceof RuntimeException)
            return (RuntimeException) t;
        else if (t instanceof Error)
            throw (Error) t;
        else
            throw new IllegalStateException("Not unchecked", t);
    }
}

/**
 * Initial cache attempt using HashMap and synchronization;
 * suffers from lack of parallelism due to coarse locking.
 * From Goetz p. 103
 *
 * @author Brian Goetz and Tim Peierls
 */
class Memoizer1<A, V> implements Computable<A, V> {
    private final Map<A, V> cache = new HashMap<A, V>();
    private final Computable<A, V> c;

    public Memoizer1(Computable<A, V> c) {
        this.c = c;
    }

    public synchronized V compute(A arg) throws InterruptedException {
        V result = cache.get(arg);
        if (result == null) {
            result = c.compute(arg);
            cache.put(arg, result);
        }
        return result;
    }
}


/**
 * Memoizer2
 * Replacing HashMap with ConcurrentHashMap for better parallelism.
 * From Goetz p. 105
 *
 * @author Brian Goetz and Tim Peierls
 */
class Memoizer2<A, V> implements Computable<A, V> {
    private final Map<A, V> cache = new ConcurrentHashMap<A, V>();
    private final Computable<A, V> c;

    public Memoizer2(Computable<A, V> c) {
        this.c = c;
    }

    public V compute(A arg) throws InterruptedException {
        V result = cache.get(arg);
        if (result == null) {
            result = c.compute(arg);
            cache.put(arg, result);
        }
        return result;
    }
}


/**
 * Memoizer3
 * Create a Future and register in cache immediately.
 * Calls: ft.run() -> eval.call() -> c.compute(arg)
 * From Goetz p. 106
 *
 * @author Brian Goetz and Tim Peierls
 */
class Memoizer3<A, V> implements Computable<A, V> {
    private final Map<A, Future<V>> cache
            = new ConcurrentHashMap<A, Future<V>>();
    private final Computable<A, V> c;

    public Memoizer3(Computable<A, V> c) {
        this.c = c;
    }

    public V compute(final A arg) throws InterruptedException {
        Future<V> f = cache.get(arg);
        if (f == null) {
            Callable<V> eval = new Callable<V>() {
                public V call() throws InterruptedException {
                    return c.compute(arg);
                }
            };
            FutureTask<V> ft = new FutureTask<V>(eval);
            cache.put(arg, ft);
            f = ft;
            ft.run();
        }
        try {
            return f.get();
        } catch (ExecutionException e) {
            throw launderThrowable(e.getCause());
        }
    }

    public static RuntimeException launderThrowable(Throwable t) {
        if (t instanceof RuntimeException)
            return (RuntimeException) t;
        else if (t instanceof Error)
            throw (Error) t;
        else
            throw new IllegalStateException("Not unchecked", t);
    }
}


/**
 * Memoizer4, hybrid of variant of Goetz's Memoizer3 and Memoizer.  If
 * arg not in cache, create Future, then atomically putIfAbsent in
 * cache, then run on calling thread.
 */

class Memoizer4<A, V> implements Computable<A, V> {
    private final Map<A, Future<V>> cache
            = new ConcurrentHashMap<A, Future<V>>();
    private final Computable<A, V> c;

    public Memoizer4(Computable<A, V> c) {
        this.c = c;
    }

    public V compute(final A arg) throws InterruptedException {
        Future<V> f = cache.get(arg);
        if (f == null) {
            Callable<V> eval = new Callable<V>() {
                public V call() throws InterruptedException {
                    return c.compute(arg);
                }
            };
            FutureTask<V> ft = new FutureTask<V>(eval);
            f = cache.putIfAbsent(arg, ft);
            if (f == null) {
                f = ft;
                ft.run();
            }
        }
        try {
            return f.get();
        } catch (ExecutionException e) {
            throw launderThrowable(e.getCause());
        }
    }

    public static RuntimeException launderThrowable(Throwable t) {
        if (t instanceof RuntimeException)
            return (RuntimeException) t;
        else if (t instanceof Error)
            throw (Error) t;
        else
            throw new IllegalStateException("Not unchecked", t);
    }
}

/**
 * Memoizer5, modern variant of Memoizer4 using the new Java 8
 * computeIfAbsent.  Atomically test whether arg is in cache and if
 * not create Future and put it there, then run on calling thread.
 */

class Memoizer5<A, V> implements Computable<A, V> {
    private final Map<A, Future<V>> cache
            = new ConcurrentHashMap<A, Future<V>>();
    private final Computable<A, V> c;

    public Memoizer5(Computable<A, V> c) {
        this.c = c;
    }

    public V compute(final A arg) throws InterruptedException {
        // AtomicReference is used as a simple assignable holder; no atomicity needed
        final AtomicReference<FutureTask<V>> ftr = new AtomicReference<>();
        Future<V> f = cache.computeIfAbsent(arg, (A argv) -> {
            Callable<V> eval = () -> c.compute(argv);
            ftr.set(new FutureTask<>(eval));
            return ftr.get();
        });
        // Important to run() the future outside the computeIfAbsent():
        if (ftr.get() != null)
            ftr.get().run();
        try {
            return f.get();
        } catch (ExecutionException e) {
            throw launderThrowable(e.getCause());
        }
    }

    public static RuntimeException launderThrowable(Throwable t) {
        if (t instanceof RuntimeException)
            return (RuntimeException) t;
        else if (t instanceof Error)
            throw (Error) t;
        else
            throw new IllegalStateException("Not unchecked", t);
    }
}


/**
 * Final implementation of Memoizer using cheap get() followed by
 * atomic putIfAbsent.
 * From Goetz p. 108
 *
 * @author Brian Goetz and Tim Peierls
 */
class Memoizer<A, V> implements Computable<A, V> {
    private final ConcurrentMap<A, Future<V>> cache
            = new ConcurrentHashMap<A, Future<V>>();
    private final Computable<A, V> c;

    public Memoizer(Computable<A, V> c) {
        this.c = c;
    }

    public V compute(final A arg) throws InterruptedException {
        while (true) {
            Future<V> f = cache.get(arg);
            if (f == null) {
                Callable<V> eval = new Callable<V>() {
                    public V call() throws InterruptedException {
                        return c.compute(arg);
                    }
                };
                FutureTask<V> ft = new FutureTask<V>(eval);
                f = cache.putIfAbsent(arg, ft);
                if (f == null) {
                    f = ft;
                    ft.run();
                }
            }
            try {
                return f.get();
            } catch (CancellationException e) {
                cache.remove(arg, f);
            } catch (ExecutionException e) {
                throw launderThrowable(e.getCause());
            }
        }
    }


    /**
     * Coerce a checked Throwable to an unchecked RuntimeException.
     * <p>
     * sestoft@itu.dk 2014-09-07: This method converts a Throwable
     * (which is a checked exception) into a RuntimeException (which is
     * an unchecked exception) or an IllegalStateException (which is a
     * subclass of RuntimeException and hence unchecked).  It is unclear
     * why RuntimeException and Error are treated differently; both are
     * unchecked.  A simpler (but grosser) approach is to simply throw a
     * new RuntimeException(t), thus wrapping the Throwable, but that
     * may lead to a RuntimeException containing a RuntimeException
     * which is a little strange.  The original
     * java.util.concurrent.ExecutionException that wrapped the
     * Throwable is itself checked and therefore needs to be caught and
     * turned into something less obnoxious.
     *
     * @author Brian Goetz and Tim Peierls
     */

    public static RuntimeException launderThrowable(Throwable t) {
        if (t instanceof RuntimeException)
            return (RuntimeException) t;
        else if (t instanceof Error)
            throw (Error) t;
        else
            throw new IllegalStateException("Not unchecked", t);
    }
}