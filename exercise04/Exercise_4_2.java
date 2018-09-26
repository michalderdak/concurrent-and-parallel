import java.util.Arrays;
import java.util.stream.IntStream;

public class Exercise_4_2 {
    public static void main(String[] args) {
        // 1. Create an int array a of size N, for instance for N = 10,000,001.
        // Use method parallelSetAll from utility class Arrays to initialize position a[i] to 1 if i is a
        // prime number and to 0 otherwise. You may use method isPrime from the other prime number related examples.
        int N = 10_000_001;
        int[] a = IntStream.rangeClosed(1, N).toArray();
        Arrays.parallelSetAll(a, (number) -> (isPrime(number)) ? 1 : 0);


        //2. Use method parallelPrefix from utility class Arrays to compute the preﬁx sums of array a.
        // After that operation, the new value of a[i] should be the sum of the old values a[0..i].
        // Therefore, the new value of a[i] is the count of prime numbers smaller than or equal to i,
        // that is, π(i).
        // For instance, the value of a[10_000_000] should be 664,579.
        Arrays.parallelPrefix(a, (nr1, nr2) -> nr1 + nr2);

        //3. Use a for-loop to print the ratio between a[i] and i/ln(i) for 10 values of i equally
        // spaced between N/10 and N.
        int spaceBetween = 1_000_000;
        for (int i = spaceBetween; i < a.length; i += spaceBetween) {
            double nr = a[i];
            double ln_i = Math.log(i);
            double ratio = nr / (i / ln_i);

            System.out.println(ratio);
        }
    }

    private static boolean isPrime(int n) {
        int k = 2;
        while (k * k <= n && n % k != 0)
            k++;
        return n >= 2 && k * k > n;
    }
}