/**
 * Copyright 2008 Matthew Hillsdon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hillsdon.reviki.wiki.macros;

import static java.util.Collections.sort;
import static net.hillsdon.fij.core.Functional.map;
import static net.hillsdon.fij.text.Strings.join;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import net.hillsdon.reviki.search.SearchMatch;
import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.wiki.renderer.context.PageRenderContext;
import net.hillsdon.reviki.wiki.renderer.macro.Macro;
import net.hillsdon.reviki.wiki.renderer.macro.ResultFormat;

//TODO: don't call parser.parse twice, once here and once in our subclasses!

public abstract class AbstractListOfPagesMacro implements Macro {
  private static final String SORT_ARG_NAME = "sort";

  private final MacroArgumentParser _argParser = new MacroArgumentParser(getAllowedArgs(), SORT_ARG_NAME);

  /**
   * Compare based on a given page property.
   * Note: this comparator imposes orderings that are inconsistent with equals.
   */
  private class PagePropSorter implements Comparator<SearchMatch> {
    private String _keyName;

    private PagePropSorter(String keyName) {
      _keyName = keyName;
    }

    public int compare(final SearchMatch left, final SearchMatch right) {
      String leftPropValue = left.getPageProperties().get(_keyName);
      String rightPropValue = right.getPageProperties().get(_keyName);

      if (leftPropValue == null && rightPropValue == null) {
        //if neither has the sort key, they are the same
        return 0;
      }
      else if (leftPropValue == null) {
        //left doesn't have a sort key and is therefore at the end.
        return 1;
      }
      else if (rightPropValue == null) {
        //right doesn't have a sort key and is therefore at the end.
        return -1;
      }
      else {
        //otherwise compare them as strings
        return leftPropValue.compareTo(rightPropValue);
      }
    }
  }

  public final String handle(final PageReference page, final String remainder, PageRenderContext context) throws Exception {
    List<SearchMatch> pages = new ArrayList<SearchMatch>(getPages(remainder));
    try {
      final Map<String, String> args = getArgParser().parse(remainder);
      if (args.containsKey(SORT_ARG_NAME)) {
        Collections.sort(pages, new PagePropSorter(args.get(SORT_ARG_NAME)));
      }
      else {
        //just sort alphabetically.
        sort(pages);
      }
    }
    catch(MacroArgumentParser.ParseException e) {
      //just sort alphabetically.
      sort(pages);
    }

    return join(map(pages.iterator(), SearchMatch.TO_PAGE_NAME), "  * ", "\n", "");
  }

  public final ResultFormat getResultFormat() {
    return ResultFormat.WIKI;
  }

  protected MacroArgumentParser getArgParser(){
    return _argParser;
  }

  protected abstract Collection<String> getAllowedArgs();


  protected abstract Collection<SearchMatch> getPages(String remainder) throws Exception;
}
