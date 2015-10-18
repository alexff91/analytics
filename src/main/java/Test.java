import javastat.StatisticalAnalysis;
import javastat.inference.ChisqTest;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;

/**
 * Created by Александр on 28.06.2015.
 */
public class Test {
  public static void main(String[] args)
  throws NoSuchAlgorithmException, UnsupportedEncodingException {
    String[] colvar1 = {"M", "F", "M", "M", "M", "F", "F", "M", "F", "M",
        "F", "F", "M", "F", "M", "M", "F", "F", "M", "F",
        "M", "F", "F", "F", "F", "F", "M", "F", "M", "F",
        "F", "M", "M", "F", "M", "F", "F", "F", "M", "F",
        "F", "F", "M", "M", "F", "F", "F", "M", "F", "F"};
    String[] colvar = {"M", "F", "M", "M", "M", "F", "F", "M", "F", "M",
        "F", "F", "M", "F", "M", "M", "F", "F", "M", "F",
        "M", "F", "F", "F", "F", "F", "M", "F", "M", "F",
        "F", "M", "M", "F", "M", "F", "F", "F", "M", "F",
        "F", "F", "M", "M", "F", "F", "F", "M", "F", "F"};
    String[] rowvar = {"E", "A", "R", "E", "E", "A", "A", "A", "A", "E",
        "E", "A", "A", "A", "R", "R", "A", "A", "A", "E",
        "R", "R", "E", "A", "A", "A", "R", "E", "A", "R",
        "R", "R", "R", "A", "R", "A", "E", "A", "R", "A",
        "E", "R", "E", "R", "A", "A", "R", "E", "E", "A"};

    // Null constructor
    ChisqTest testclass2 = new ChisqTest();
    double testStatistic = testclass2.testStatistic(colvar, rowvar);
    double pValue = testclass2.pValue(colvar, rowvar);
    MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
    byte[] hash = messageDigest.digest("hello".getBytes("UTF-8"));
    StringBuilder stringBuilder = new StringBuilder();
    for (int i = 0; i < hash.length; i++) {
      stringBuilder.append(Integer.toString((hash[i] & 0xff) + 0x100, 16).substring(1));
    }
    stringBuilder.toString();
    // Obtains the information about the output
    System.out.println(stringBuilder);
  }
}
