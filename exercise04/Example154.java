// Example 154 from page 123 of Java Precisely third edition (The MIT Press 2016)
// Author: Peter Sestoft (sestoft@itu.dk)

import java.util.function.BiFunction;
import java.util.function.Consumer;  
import java.util.function.Function;  

class Example154 {
  public static void main(String[] args) {
    FunList<Integer> empty = new FunList<>(null),
      list1 = cons(9, cons(13, cons(0, empty))),                  // 9 13 0       
      list2 = cons(7, list1),                                     // 7 9 13 0     
      list3 = cons(8, list1),                                     // 8 9 13 0     
      list4 = list1.insert(1, 12),                                // 9 12 13 0    
      list5 = list2.removeAt(3),                                  // 7 9 13       
      list6 = list5.reverse(),                                    // 13 9 7       
      list7 = list5.append(list5);                                // 7 9 13 7 9 13
    System.out.println(list1);
    System.out.println(list2);
    System.out.println(list3);
    System.out.println(list4);
    System.out.println(list5);
    System.out.println(list6);
    System.out.println(list7);
    FunList<Double> list8 = list5.map(i -> 2.5 * i);              // 17.5 22.5 32.5
    System.out.println(list8); 
    double sum = list8.reduce(0.0, (res, item) -> res + item),    // 72.5
       product = list8.reduce(1.0, (res, item) -> res * item);    // 12796.875
    System.out.println(sum);
    System.out.println(product);
    FunList<Boolean> list9 = list5.map(i -> i < 10);              // true true false 
    System.out.println(list9);
    boolean allBig = list8.reduce(true, (res, item) -> res && item > 10);
    System.out.println(allBig);
  }

  public static <T> FunList<T> cons(T item, FunList<T> list) { 
    return list.insert(0, item);
  }
}

class FunList<T> {
  final Node<T> first;

  protected static class Node<U> {
    public final U item;
    public final Node<U> next;

    public Node(U item, Node<U> next) {
      this.item = item; 
      this.next = next; 
    }
  }

  public FunList(Node<T> xs) {    
    this.first = xs;
  }

  public FunList() { 
    this(null);
  }

  public int getCount() {
    Node<T> xs = first;
    int count = 0;
    while (xs != null) {
      xs = xs.next;
      count++;
    }
    return count;
  }

  public T get(int i) {
    return getNodeLoop(i, first).item;
  }

  // Loop-based version of getNode
  protected static <T> Node<T> getNodeLoop(int i, Node<T> xs) {
    while (i != 0) {
      xs = xs.next;
      i--;
    }
    return xs;    
  }

  // Recursive version of getNode
  protected static <T> Node<T> getNodeRecursive(int i, Node<T> xs) {    // Could use loop instead
    return i == 0 ? xs : getNodeRecursive(i-1, xs.next);
  }

  public static <T> FunList<T> cons(T item, FunList<T> list) { 
    return list.insert(0, item);
  }

  public FunList<T> insert(int i, T item) { 
    return new FunList<T>(insert(i, item, this.first));
  }

  protected static <T> Node<T> insert(int i, T item, Node<T> xs) { 
    return i == 0 ? new Node<T>(item, xs) : new Node<T>(xs.item, insert(i-1, item, xs.next));
  }

  public FunList<T> removeAt(int i) {
    return new FunList<T>(removeAt(i, this.first));
  }

  protected static <T> Node<T> removeAt(int i, Node<T> xs) {
    return i == 0 ? xs.next : new Node<T>(xs.item, removeAt(i-1, xs.next));
  }

  public FunList<T> reverse() {
    Node<T> xs = first, reversed = null;
    while (xs != null) {
      reversed = new Node<T>(xs.item, reversed);
      xs = xs.next;      
    }
    return new FunList<T>(reversed);
  }

  public FunList<T> append(FunList<T> ys) {
    return new FunList<T>(append(this.first, ys.first));
  }

  protected static <T> Node<T> append(Node<T> xs, Node<T> ys) {
    return xs == null ? ys : new Node<T>(xs.item, append(xs.next, ys));
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean equals(Object that) {
    return equals((FunList<T>)that);             // Unchecked cast
  }

  public boolean equals(FunList<T> that) {
    return that != null && equals(this.first, that.first);
  }

  // Could be replaced by a loop
  protected static <T> boolean equals(Node<T> xs1, Node<T> xs2) {
    return xs1 == xs2 
        || xs1 != null && xs2 != null && xs1.item == xs2.item && equals(xs1.next, xs2.next);
  }

  public <U> FunList<U> map(Function<T,U> f) {
    return new FunList<U>(map(f, first));
  }

  protected static <T,U> Node<U> map(Function<T,U> f, Node<T> xs) {
    return xs == null ? null : new Node<U>(f.apply(xs.item), map(f, xs.next));
  }

  public <U> U reduce(U x0, BiFunction<U,T,U> op) {
    return reduce(x0, op, first);
  }

  // Could be replaced by a loop
  protected static <T,U> U reduce(U x0, BiFunction<U,T,U> op, Node<T> xs) {
    return xs == null ? x0 : reduce(op.apply(x0, xs.item), op, xs.next);
  }

  // This loop is an optimized version of a tail-recursive function 
  public void forEach(Consumer<T> cons) {
    Node<T> xs = first;
    while (xs != null) {
      cons.accept(xs.item);
      xs = xs.next;
    }
  }

  @Override 
  public String toString() {
    StringBuilder sb = new StringBuilder();
    forEach(item -> sb.append(item).append(" "));
    return sb.toString();
  }

  //Implementations for Exercise 4.1
  public FunList<T> remove(T x) {
  	return new FunList<T>(remove(x, this.first));
  }

  protected static <T> Node<T> remove(T x, Node<T> xs) {
  	if (xs == null)
  		return null;
  	else {
  		if (xs.item == x) {
  			return remove(x, xs.next);
  		}
  		return new Node<T>(xs.item, remove(x, xs.next));
  	}
  }

  public int count(Predicate<T> p) {
      return count(p, 0, this.first);
  }

  protected static <T> Integer count(Predicate<T> p, int total, Node<T> xs) {
      if (xs == null) {
          return total;
      }
      if (p.test(xs.item))
          total++;
      return count(p, total, xs.next);
  }

  public FunList<T> filter(Predicate<T> p) {
      return new FunList<>(filter(p, this.first));
  }

  protected static <T> Node<T> filter(Predicate<T> p, Node<T> xs) {
      if (xs == null) {
          return null;
      }
      if (p.test(xs.item))
          return new Node<T>(xs.item, filter(p, xs.next));
      return filter(p, xs.next); //This one should pass
  }

  public FunList<T> removeFun(T x) {
      return this.filter((item) -> item != x);
  }

  protected static <T> FunList<T> flatten(FunList<FunList<T>> xss) {
      FunList<T> newList = new FunList<T>(); //The single list to return
      Node<FunList<T>> dummy = xss.first;
      //Need to go through the list of lists, then append each one to the newList.
      while (dummy != null) {
          newList = newList.append(dummy.item);
          dummy = dummy.next;
      }
      return newList;
  }

  protected static <T> FunList<T> flattenFun(FunList<FunList<T>> xss) {
      FunList<T> newList = new FunList<T>(); //The single list to return
      return xss.reduce(newList, (FunList<T> list1, FunList<T> list2)-> list1.append(list2));
  }

  public <U> FunList<U> flatmap(Function<T, FunList<U>> f) {
      FunList<U> newFlattenedList = new FunList<U>();
      Node<T> node = this.first;
      while(node != null)
      {
          FunList<U> computedList = f.apply(node.item);
          newFlattenedList = newFlattenedList.append(computedList);
          node = node.next;
      }
      return newFlattenedList;
  }

  public <U> FunList<U> flatmapFun(Function<T, FunList<U>> f) {
      return flatten(this.map(f));
  }

  public FunList<T> scan(BinaryOperator<T> f) {
      throw new NotImplementedException();
  }
}