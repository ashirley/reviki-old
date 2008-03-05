package net.hillsdon.svnwiki.wiki.renderer;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.hillsdon.svnwiki.text.Escape;
import net.hillsdon.svnwiki.vc.PageReference;

public abstract class AbstractRegexNode implements RenderNode {

  private final List<RenderNode> _children = new ArrayList<RenderNode>();
  private final Pattern _matchRe;

  public AbstractRegexNode(final String matchRe) {
    _matchRe = Pattern.compile(matchRe);
  }

  public List<RenderNode> getChildren() {
    return _children;
  }

  public AbstractRegexNode addChildren(final RenderNode... rules) {
    _children.addAll(asList(rules));
    return this;
  }

  public String render(final PageReference page, final String text) {
    if (text == null || text.length() == 0) {
      return "";
    }
    RenderNode earliestRule = null;
    Matcher earliestMatch = null;
    int earliestIndex = Integer.MAX_VALUE;
    for (RenderNode child : _children) {
      Matcher matcher = child.find(text);
      if (matcher != null) {
        if (matcher.start() < earliestIndex) {
          earliestIndex = matcher.start();
          earliestMatch = matcher;
          earliestRule = child;
        }
      }
    }
    if (earliestRule != null) {
      String result = "";
      // Just output the stuff before the match.
      result += Escape.html(text.substring(0, earliestMatch.start()));
      // Handle the match and recurse.
      result += earliestRule.handle(page, earliestMatch);
      result += render(page, text.substring(earliestMatch.end()));
      return result;
    }
    return Escape.html(text);
  }

  public Matcher find(final String text) {
    Matcher matcher = _matchRe.matcher(text);
    return matcher.find() && confirmMatchFind(matcher) ? matcher : null;
  }

  protected abstract boolean confirmMatchFind(final Matcher matcher);

}
