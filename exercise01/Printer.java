public class Printer {
    public static void main(String[] args) {
        // sorry but while(true) seems a bit too much
        int until = 100;

        Thread t1 = new Thread(() -> {
            for (int i=0; i < until; i++) {
                PrinterHelper.print();
            } 
        });
      
        Thread t2 = new Thread(() -> {
            for (int i=0; i < until; i++) {
                PrinterHelper.print();
            }
        });
      
        t1.start();
        t2.start(); 
          
        try { 
            t1.join();
            t2.join();
        }
        catch (InterruptedException exn) { 
            System.out.print(exn.getMessage());
        }
    }
}

class PrinterHelper {
    private static Object _lock = new Object();

    public static void print() {
        synchronized (_lock) {
            System.out.print("-");

            try { 
                Thread.sleep(50); 
            } 
            catch (InterruptedException exn) {
                System.out.print(exn.getMessage());
            }
            System.out.print("|");
        }
    }
}