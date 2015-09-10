package io.bit3.jsass;

import static org.junit.Assert.assertEquals;

import io.bit3.jsass.context.StringContext;
import io.bit3.jsass.importer.Import;
import io.bit3.jsass.importer.Importer;

import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;

/**
 * This test covers the issue #12.
 *
 * @see https://github.com/bit3/jsass/issues/12
 *
 * The problem of the problem was, that the previous import entries base was incorrect.
 */
public class Issue12Test {
  public static final String SOURCE_URI = "classpath:/some/path/to/style.scss";
  public static final String SOURCE_BASE = "classpath:/some/path/to/style.scss";

  public static final String IMPORT1_URI = "import1";
  public static final String IMPORT1_BASE = "classpath:/some/path/to/import1.scss";

  public static final String IMPORT2_URI = "import2";
  public static final String IMPORT2_BASE = "classpath:/some/path/to/import2.scss";

  private Compiler compiler;
  private Options options;

  /**
   * Set up the compiler and the compiler options for each run.
   *
   * <p>Using the system temp directory as output directory.</p>
   *
   * @throws URISyntaxException Throws if the resource URI is invalid.
   */
  @Before
  public void setUp() throws URISyntaxException {
    compiler = new Compiler();

    options = new Options();
    options.setOutputStyle(OutputStyle.COMPACT);
    options.getImporters().add(new TestImporter());
  }

  @Test
  public void testIssue() throws Exception {
    StringContext context = new StringContext(
        String.format("@import \"%s\";", IMPORT1_URI),
        new URI(SOURCE_URI),
        new URI("style.css"),
        options
    );
    Output output = compiler.compile(context);
    assertEquals("hello{content:'World'}", output.getCss());
  }

  public static class TestImporter implements Importer {
    @Override
    public Collection<Import> apply(String url, Import previous) {
      try {
        if (IMPORT1_URI.equals(url)) {
          assertEquals(SOURCE_URI, previous.getUri().toString());
          assertEquals(SOURCE_BASE, previous.getBase().toString());

          Import importFile = new Import(
              new URI(IMPORT1_URI),
              new URI(IMPORT1_BASE),
              String.format("@import \"%s\";", IMPORT2_URI)
          );
          return Collections.singleton(importFile);
        }

        if (IMPORT2_URI.equals(url)) {
          assertEquals(IMPORT1_URI, previous.getUri().toString());
          assertEquals(IMPORT1_BASE, previous.getBase().toString());

          Import importFile = new Import(
              new URI(IMPORT2_URI),
              new URI(IMPORT2_BASE),
              "hello{content:'World'}"
          );
          return Collections.singleton(importFile);
        }
      } catch (URISyntaxException exception) {
        throw new RuntimeException(exception);
      }

      throw new RuntimeException("Unexpected import");
    }
  }
}