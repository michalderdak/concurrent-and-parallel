// For week 1
// sestoft@itu.dk * 2015-10-29

public class TestLocking0 {
  public static void main(String[] args) {
    final int count = 1_000_000;

    var m = new Mystery();

    var t1 = new Thread(() -> { 
      for (int i=0; i < count; i++) {
        synchronized (m) {
          m.addInstance(1); 
        }
      }
    });

    var t2 = new Thread(() -> { 
	    for (int i=0; i < count; i++) {
        Mystery.addStatic(1); 
      }
    });
    
    t1.start(); 
    t2.start();

    try { 
      t1.join(); 
      t2.join(); 
    } 
    catch (InterruptedException exn) { 
      System.out.println(exn.getMessage());
    }

    System.out.printf("Sum is %f and should be %f%n", Mystery.sum(), 2.0 * count);
  }
}

class Mystery {
  private static Object _lock = new Object();
  private static double _sum = 0;

  public static void addStatic(double x) {
    synchronized (_lock) {
      _sum += x;
    }
  }

  public void addInstance(double x) {
    synchronized (_lock) {
      _sum += x;
    }
  }

  public static double sum() {
    return _sum;
  }
}
