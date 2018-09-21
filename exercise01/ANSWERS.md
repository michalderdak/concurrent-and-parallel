# Exercise 1.1
## 1. 
Different number values ranging lower than expected number. This  is due two threads altering the same field 
`private long count` at the same time without proper locking.
## 2. 
It's more likely that race condition happens when you do something 200 000 times more often. If we try running the
code multiple times, sooner or later the result will be incorrect.
## 3.
It would not change anything. The results should be the same as `count = count + 1;`, `count += 1` and `count++` are the different interpretation of incrementing the field `count`. 
## 4. 
As the `count` field is not `synchronized` the same effect as with only incrementing the value still happens.
Two threads are trying to access the value at the same time (even though threads call different functions they alter the same `count` value) thus making one of the alterations invalid.

`Synchronized` should solve this issue - the corrent result of `0` is returned.
## 5. 
        I. Count is 8768 and should be 20000000, Count is -75933 and should be 20000000
        II. Count is 60706 and should be 20000000, Count is -2337 and should be 20000000
        III. Count is -39176 and should be 20000000, Count is 3337732 and should be 20000000
        IV. Count is 0 and should be 20000000, Count is 0 and should be 20000000

As two methods alter the same field `count` and locking only neither or one of the blocks of the code inside one of the methods, results in the other thread
accessing the field `count` through the other function without waiting for the previous thread to finish and thus creating chaos.
To make sure the `count` field is thread safe we should `synchronized` it.

# Exercise 1.2
## 1. 
If running two threads at once there's no guarantee that they both will be sequential without the synchonization, thus
printing out nonsence from time to time.

Without `synchronized`:

```--|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-||--|-|-|-|-|-|-|-|-|-|-|-|-|-|-||--|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-||```
## 2.
By using `synchronized` on the `print` method we prevent two or more threads accessing the method at the same time. Thread `t1` access the `print` method,
where it does its thing. Thread `t2` wants to access the method `print` while `t1` is doing its thing and because of `synchronized` it waits until `t1` is finished.

With `synchronized`:

```-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|```

It is also worth mentioning that the runtime of the program now takes approximately up to twice as long.
## 3. 
```
class PrinterHelper {
    private Object _lock = new Object();

    public void print() {
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
```
## 4.

# Exercise 1.3
## 1. 
Yes. I have observed the same issue where threads runs forever.

## 2.
Yes. It reads the value now.

## 3.
No. The issue of thread not seeing the value appears again. The `synchronized` is however required only on `get` method as that is the method that is shared between them.

## 4.
Yes the program behaves as expected. The `volatile` moves the value of the `int value` into the main memory and restricts it to be stored inside of the cache.

# Exercise 1.4
## 1.
Total milliseconds => 7066
## 2.
Total milliseconds => 1672. It's faster.
## 3.
No, the result changes.
## 4.
It does not need to be `synchronized` because it is always used after the threads are done with their execution.

# Exercise 1.5
## 1.
- Sum is 1819840.000000 and should be 2000000.000000
- Sum is 1943500.000000 and should be 2000000.000000
- Sum is 1849024.000000 and should be 2000000.000000

It does not seem to be precise.
## 2.
If a `synchronized` is used on a static method the lock is applied on the class not on the instance of the class (object).
## 3.
I decided to use separate object as a lock for both static and non-static methods. This way threads could refer to the same lock alocated in one place in
the memory.

```
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
```

# Exercise 1.6
## 1.
By introducing `synchronized` to the methods
## 2.
It depends. This is really simple software that can be executed really fast, however making the class thread-safe can lead to queues of threads waiting to peform any of the three actions (`get`,`set`,`add`)
## 3.
It would not achieve thread-safety as one thread can be reading a value from the array while the other thread can be setting a different value for that same index in
the array. Yes, they both use locks, however those locks work only if two threads call same method at once, not the different methods.
It would not achieve visibility, because locking is not used properly and `volatile` is not used on the field to ensure visibility.

# Exercise 1.7
## 1. and 2.
By introducing one static lock `Object` and synchronize pieces of the code where `totalSize` and `allLists` are accessed.

# Exercise 1.8
## 1.
`synchronized` used on static methods locks the whole class instead of the instance (object) of the class. Thus if there are two classes and each thread accesses
different class the locking wouldn't work.
## 2.
Usage of shared lock object solves the issue. The threads are looking on the `_lock` object to see if they can access `synchronized` code block instead of 
looking and locking the class.

```Java
class MysteryA {
  protected static long count = 0;
  protected static Object _lock = new Object();

  public static void increment() {
    synchronized (_lock) {
      count++;
    }
  }

  public static long get() { 
    return count; 
  }
}

class MysteryB extends MysteryA {
  public static void increment4() {
    synchronized (_lock) {
    count += 4;
    }
  }
}
```