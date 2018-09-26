
import java.util.function.DoubleSupplier;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public class ExerciseFour {
    public static void main(String[] args) {
        int N = 999_999_999;
        IntStream ns = IntStream.range(1, N);

        long initialTime = System.currentTimeMillis();

        // START

        // Part 1., 2. 
        // double result = ns.parallel().mapToDouble(n -> n).map(n -> 1 / n).sum();

        // Part 3.
        // double result = 0;

        // for(double i = 1; i <= N; i++) {
        //     result += 1 / i;
        // }

        // Part 4.
        // double result = DoubleStream.generate(new DoubleSupplier(){
        //     double N = 0;
        //     @Override
        //     public double getAsDouble() {
        //         N += 1;

        //         return 1 / N;
        //     }
        // }).limit(N).sum();
        
        // Part 5.
        double result = DoubleStream.generate(new DoubleSupplier(){
            double N = 0;

            @Override
            public double getAsDouble() {
                N += 1;

                return 1 / N;
            }
        }).limit(N).parallel().sum();

        //END

        System.out.printf("Time elapsed =  %d ms %n", System.currentTimeMillis() - initialTime);
        System.out.printf("Sum = %20.16f%n", result);
    }
} 