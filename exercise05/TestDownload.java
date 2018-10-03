// For week 5
// sestoft@itu.dk * 2014-09-19

import java.net.URL;
import java.util.HashMap;
import com.sun.javafx.collections.MappingChange.Map;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.IntToDoubleFunction;

public class TestDownload {

    private static final ExecutorService executor = Executors.newWorkStealingPool();
    private static final String[] urls =
            {"http://www.itu.dk", "http://www.di.ku.dk", "http://www.miele.de",
                    "http://www.microsoft.com", "http://www.amazon.com", "http://www.dr.dk",
                    "http://www.vg.no", "http://www.tv2.dk", "http://www.google.com",
                    "http://www.ing.dk", "http://www.dtu.dk", "http://www.eb.dk",
                    "http://www.nytimes.com", "http://www.guardian.co.uk", "http://www.lemonde.fr",
                    "http://www.welt.de", "http://www.dn.se", "http://www.heise.de", "http://www.wsj.com",
                    "http://www.bbc.co.uk", "http://www.dsb.dk", "http://www.bmw.com", "https://www.cia.gov"
            };

    public static void main(String[] args) throws IOException {
        // String url = "https://www.wikipedia.org/";
        // String page = getPage(url, 10);

        HashMap<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < 5; i++) {
            Timer timer = new Timer();
            map = getPagesParallel(urls, 200);
            double time = timer.check() * 1e9;
            System.out.printf("%6.1f ns%n", time);
        }
        for (HashMap.Entry<String, String> entry : map.entrySet()) {
            System.out.println(entry.getKey());
            System.out.println("Nr of charecters in body: " + entry.getValue().length());
        }
    }


    public static HashMap<String, String> getPagesParallel(String[] urls, int maxLines) {
        HashMap<String, String> urlMap = new HashMap<String, String>();
        List<Future<?>> futures = new ArrayList<Future<?>>();
        for (int x = 0; x < urls.length; x++) {
            int x_temp = x;
            futures.add(executor.submit(() ->
            {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(new URL(urls[x_temp]).openStream()))) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < maxLines; i++) {
                        String inputLine = in.readLine();
                        if (inputLine == null)
                            break;
                        else
                            sb.append(inputLine).append("\n");
                    }
                    urlMap.put(urls[x_temp], sb.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
        }
        try {
            for (Future<?> fut : futures)
                fut.get();
        } catch (InterruptedException exn) {
            System.out.println("Interrupted: " + exn);
        } catch (ExecutionException exn) {
            throw new RuntimeException(exn.getCause());
        }
        return urlMap;
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

    public static HashMap<String, String> getPages(String[] urls, int maxLines) throws IOException {
        HashMap<String, String> urlMap = new HashMap<String, String>();
        for (int x = 0; x < urls.length; x++) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(new URL(urls[x]).openStream()))) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < maxLines; i++) {
                    String inputLine = in.readLine();
                    if (inputLine == null)
                        break;
                    else
                        sb.append(inputLine).append("\n");
                }
                urlMap.put(urls[x], sb.toString());
            }
        }
        return urlMap;
    }
}