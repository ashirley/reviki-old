package net.hillsdon.reviki.wiki.macros;

import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.wiki.renderer.context.PageRenderContext;
import net.hillsdon.reviki.wiki.renderer.macro.Macro;
import net.hillsdon.reviki.wiki.renderer.macro.ResultFormat;

/**
 * This marks a piece of wiki markup as being the value for a key value pair.
 * e.g. <<keyedValue: foo wiki **mark**up>> identifies key=foo, value="wiki **mark**up"
 *
 * TODO: use my argument parser when I can recover it from bunga.dsl.local!
 */
public class KeyedValue implements Macro {
  public String getName() {
    return "keyedValue";
  }

  public ResultFormat getResultFormat() {
    return ResultFormat.WIKI;
  }

  public String handle(PageReference page, String remainder, PageRenderContext context) throws Exception {
    String[] split = remainder.trim().split(" ", 2);
    if (split.length != 2) {
      throw new ParseException("There must be at least 1 space in the arguments to this macro");
    }
    String key = split[0];
    String value = split[1];

    context.setPageProperties(key, value);
    return value;
  }

  private class ParseException extends Exception {
    private ParseException(String s) {
      super(s);
    }

    private ParseException(String s, Throwable throwable) {
      super(s, throwable);
    }
  }

}
