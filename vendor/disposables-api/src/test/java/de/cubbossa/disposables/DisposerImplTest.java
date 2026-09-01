package de.cubbossa.disposables;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class DisposerImplTest {

  public static class TestDisposable implements Disposable {

    String symbol;
    Collection<String> append;

    public TestDisposable(String symbol, Collection<String> append) {
      this.symbol = symbol;
      this.append = append;
      append.add(symbol);
    }

    @Override
    public void dispose() {
      append.add(symbol);
    }
  }

  @Test
  void test1() {

    Collection<String> teststring = new ArrayList<>();
    Disposer disposer = Disposer.disposer();
    TestDisposable a = new TestDisposable("a", teststring);
    TestDisposable b = new TestDisposable("b", teststring);
    TestDisposable c = new TestDisposable("c", teststring);
    disposer.register(a, b);
    disposer.register(b, c);
    disposer.dispose(a);
    assertEquals(
        "abccba",
        String.join("", teststring)
    );
  }

  @Test
  void test3() {

    Collection<String> teststring = new ArrayList<>();
    Disposer disposer = Disposer.disposer();
    TestDisposable a = new TestDisposable("a", teststring);
    TestDisposable b = new TestDisposable("b", teststring);
    TestDisposable c = new TestDisposable("c", teststring);
    disposer.dispose(a);
    disposer.dispose(b);
    assertEquals(
        "abcab",
        String.join("", teststring)
    );
  }

  @Test
  void test4() {

    Collection<String> teststring = new ArrayList<>();
    Disposer disposer = Disposer.disposer();
    TestDisposable a = new TestDisposable("a", teststring);
    TestDisposable b = new TestDisposable("b", teststring);
    disposer.register(a, b);
    disposer.dispose(b);
    assertEquals(
        "abb",
        String.join("", teststring)
    );
  }

  @Test
  void circles() {

    Disposer disposer = Disposer.disposer();

    TestDisp a = new TestDisp();
    TestDisp b = new TestDisp();
    TestDisp c = new TestDisp();

    disposer.register(a, b);
    disposer.register(b, c);
    disposer.register(c, a);

    assertDoesNotThrow(() -> disposer.dispose(a));
  }

  class TestDisp implements Disposable {}

}
