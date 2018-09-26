// Week 3
// sestoft@itu.dk * 2015-09-09

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


public class TestWordStream {
  public static void main(String[] args) {
    long time = System.currentTimeMillis();

    String filename = "C:/Users/Tomas/source/repos/ITU/concurrent-and-parallel/exercise04/usr/share/dict/words";

    test(filename);

    /* Task 7 different measurements
    var result = test(filename);
    var result2 = test(filename);
    var result3 = test(filename);
    System.out.println(String.format("min: %d, max: %d, avg: %f", result.min().getAsInt(),result2.max().getAsInt(),result3.average().getAsDouble()));
    */

    long timeTaken = System.currentTimeMillis() - time;
    System.out.println("Time" + timeTaken + " ms.");
  }

  public static void test(String filename){
    try {
    BufferedReader reader = new BufferedReader(new FileReader(filename));

    var lines = reader.lines(); // Task 1 print all words

    var first100 = lines.limit(100); // Task 2 print first 100 words

    var atleast22characters = lines.filter(l -> l.length() >= 22); // Task 3-4 print words with atleast 22 characters

    var palindroms = lines.filter(l -> isPalindrome(l)); // Task 5 pallindrome

    var palindromsParallel = lines.parallel().filter(l -> isPalindrome(l)); // Task 6 pallindrome parallel

    var linesLenght = lines.mapToInt(l -> l.length()); // Task 7 different measurements

    var groupedByLenght = lines.collect(Collectors.groupingBy(l -> l.length())); // Task 8 grouped by word lenght

    lines.limit(100).forEach(l-> System.out.println(letters(l))); // Task 9

    var timesEisUsedInWords = lines.map(l -> letters(l)).filter(f -> f.containsKey('e')).mapToInt(x->x.get('e')).reduce((x,y) -> x + y).getAsInt();// Task 10  

    var anagram1 = lines.collect(Collectors.groupingBy(l-> letters(l))).entrySet().stream().filter(x -> x.getValue().size()>=2); // 11 Task anagram pattern sequantional

    var anagram2 = lines.collect(Collectors.groupingBy(l-> letters(l))).entrySet().stream().parallel().filter(x -> x.getValue().size()>=2);// Task 11 anagram pattern parallel 
    
    var anagramGroupByConcurent = lines.collect(Collectors.groupingByConcurrent(l-> letters(l))).entrySet().stream().filter(x -> x.getValue().size()>=2);// Task 13

    System.out.println(res);
    } 

    catch (IOException exn) { 
      System.out.println(exn);
    }   
  }
  public static boolean isPalindrome(String s) {

    int length = s.length();
    if (length < 2) return true;    
    else return s.charAt(0) != s.charAt(length - 1) ? false :
            isPalindrome(s.substring(1, length - 1));
  }

  public static Map<Character,Integer> letters(String s) {
    s = s.toLowerCase();
    Map<Character,Integer> res = new TreeMap<>();
    for (int i = 0; i < s.length(); i++) {
      char charAt = s.charAt(i);

      if (!res.containsKey(charAt))
      {
          res.put(charAt, 1);
      }
      else
      {
          res.put(charAt, res.get(charAt) + 1);
      }
    }
    return res;
}
}
