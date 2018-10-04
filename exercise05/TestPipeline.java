// For week 5
// sestoft@itu.dk * 2014-09-23

// A pipeline of transformers connected by bounded queues.  Each
// transformer consumes items from its input queue and produces items
// on its output queue.

// This is illustrated by generating URLs, fetching the corresponding
// webpages, scanning the pages for links to other pages, and printing
// those links; using four threads connected by three queues:

// UrlProducer --(BlockingQueue<String>)--> 
// PageGetter  --(BlockingQueue<Webpage>)--> 
// LinkScanner --(BlockingQueue<Link>)--> 
// LinkPrinter


// For reading webpages

import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

// For regular expressions
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class TestPipeline {
    private static final ExecutorService executor = Executors.newFixedThreadPool(6);

    public static void main(String[] args) {
        runAsThreads();
    }

    private static void runAsThreads() {
        final BlockingQueue<String> urls = new BoundedQueue<>(1);
        final BlockingQueue<Webpage> pages = new BoundedQueue<>(1);
        final BlockingQueue<Link> uniqueLinks = new BoundedQueue<>(1);
        final BlockingQueue<Link> refPairs = new BoundedQueue<>(1);

        ArrayList<Runnable> runnables = new ArrayList<>();
        runnables.add(new UrlProducer(urls));
        runnables.add(new PageGetter(urls, pages));
        runnables.add(new LinkScanner(pages, refPairs));
        runnables.add(new Uniquifier<>(refPairs, uniqueLinks));
        runnables.add(new LinkPrinter(uniqueLinks));

        List<Future<?>> futures = new ArrayList<Future<?>>();
        for (Runnable runnable : runnables) {
            futures.add(executor.submit(() ->
                    runnable.run()));
        }

        try {
            for (Future<?> fut : futures)
                fut.get();
        } catch (InterruptedException exn) {
            System.out.println("Interrupted: " + exn);
        } catch (ExecutionException exn) {
            throw new RuntimeException(exn.getCause());
        }
    }
}

class UrlProducer implements Runnable {
    private final BlockingQueue<String> output;

    public UrlProducer(BlockingQueue<String> output) {
        this.output = output;
    }

    public void run() {
        for (int i = 0; i < urls.length; i++) {
            output.put(urls[i]);
            //System.out.println("UrlProducer put a url in the queue.");
        }
    }

    private static final String[] urls =
            {
                    "http://www.tv2.dk",
                    "http://www.google.com",
                    "http://www.itu.dk",
            };
}

class PageGetter implements Runnable {
    private final BlockingQueue<String> input;
    private final BlockingQueue<Webpage> output;

    public PageGetter(BlockingQueue<String> input, BlockingQueue<Webpage> output) {
        this.input = input;
        this.output = output;
    }

    public void run() {
        while (true) {
            String url = input.take();
            try {
                String contents = getPage(url, 200);
                output.put(new Webpage(url, contents));
                //System.out.println(this.hashCode() + " :PageGetter just put a page in the queue.");

            } catch (IOException exn) {
                System.out.println(exn);
            }
        }
    }

    public static String getPage(String url, int maxLines) throws IOException {
        // This will close the streams after use (JLS 8 para 14.20.3):
        try (BufferedReader in
                     = new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < maxLines; i++) {
                String inputLine = in.readLine();
                if (inputLine == null)
                    break;
                else
                    sb.append(inputLine).append("\n");
            }
            return sb.toString();
        }
    }
}

class LinkScanner implements Runnable {
    private final BlockingQueue<Webpage> input;
    private final BlockingQueue<Link> output;

    public LinkScanner(BlockingQueue<Webpage> input,
                       BlockingQueue<Link> output) {
        this.input = input;
        this.output = output;
    }

    private final static Pattern urlPattern
            = Pattern.compile("a href=\"(\\p{Graph}*)\"");

    public void run() {
        while (true) {
            Webpage page = input.take();
            //System.out.println("LinkScanner just retrieved a link from the queue.");
            //      System.out.println("LinkScanner: " + page.url);
            // Extract links from the page's <a href="..."> anchors
            Matcher urlMatcher = urlPattern.matcher(page.contents);
            while (urlMatcher.find()) {
                String link = urlMatcher.group(1);
                output.put(new Link(page.url, link));
            }
        }
    }
}

class LinkPrinter implements Runnable {
    private final BlockingQueue<Link> input;
    private Integer counter = 1;

    public LinkPrinter(BlockingQueue<Link> input) {
        this.input = input;
    }

    public void run() {
        while (true) {

            Link link = input.take();
            System.out.printf(counter + ": %s links to %s%n", link.from, link.to);
            counter++;
        }
    }
}


class Webpage {
    public final String url, contents;

    public Webpage(String url, String contents) {
        this.url = url;
        this.contents = contents;
    }
}

class Link {
    public final String from, to;

    public Link(String from, String to) {
        this.from = from;
        this.to = to;
    }

    // Override hashCode and equals so can be used in HashSet<Link>

    public int hashCode() {
        return (from == null ? 0 : from.hashCode()) * 37
                + (to == null ? 0 : to.hashCode());
    }

    public boolean equals(Object obj) {
        Link that = obj instanceof Link ? (Link) obj : null;
        return that != null
                && (from == null ? that.from == null : from.equals(that.from))
                && (to == null ? that.to == null : to.equals(that.to));
    }
}

// Different from java.util.concurrent.BlockingQueue: Allows null
// items, and methods do not throw InterruptedException.

interface BlockingQueue<T> {
    void put(T item);

    T take();
}

class OneItemQueue<T> implements BlockingQueue<T> {
    private T item;
    private boolean full = false;

    public void put(T item) {
        synchronized (this) {
            while (full) {
                try {
                    this.wait();
                } catch (InterruptedException exn) {
                }
            }
            full = true;
            this.item = item;
            this.notifyAll();
        }
    }

    public T take() {
        synchronized (this) {
            while (!full) {
                try {
                    this.wait();
                } catch (InterruptedException exn) {
                }
            }
            full = false;
            this.notifyAll();
            return item;
        }
    }
}

class Uniquifier<T> implements Runnable {
    private final BlockingQueue<T> inputQueue;
    private final BlockingQueue<T> outputQueue;
    private final HashSet<T> hashSet;

    @Override
    public void run() {
        while (true) {
            T item = inputQueue.take();
            if (!hashSet.contains(item)) //Checks if the link has already been found
            {
                hashSet.add(item);
                outputQueue.put(item);
                //System.out.println("Uniquifier just added a unique link to the queue.");
            }
        }
    }

    public Uniquifier(BlockingQueue<T> inputQueue, BlockingQueue<T> outputQueue) {
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
        hashSet = new HashSet<>();
    }
}

class BoundedQueue<T> implements BlockingQueue<T> {
    private volatile ArrayList<T> items;
    private volatile boolean full = false;
    private volatile boolean empty = true;
    private volatile int length = 0;

    public BoundedQueue(Integer length) {
        this.length = length;
        items = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            items.add(null);
        }
    }

    public void put(T item) {
        synchronized (this) {
            while (full) {
                try {
                    this.wait(500);
                } catch (InterruptedException exn) {
                }
            }
            empty = false;
            items.set(length - 1, item);
            if (!items.contains(null))
                full = true;
            this.notifyAll();
        }
    }

    public T take() {
        synchronized (this) {
            while (empty) {
                try {
                    this.wait(500);
                } catch (InterruptedException exn) {
                }
            }
            T item = items.get(0);
            if (item == null)
            {
                empty = true;
                full = false;
                this.notifyAll();
                return take();
            }
            else
            {
                full = false;
                items.set(0, null);
                this.notifyAll();
                return item;
            }
        }
    }
}