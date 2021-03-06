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
package net.hillsdon.reviki.search;

import net.hillsdon.fij.core.Transform;
import net.hillsdon.reviki.wiki.macros.AbstractListOfPagesMacro;

import java.util.Map;

/**
 * A match from a search.
 *
 * Considered equal by page.
 *
 * @author mth
 */
public class SearchMatch implements Comparable<SearchMatch> {

  public static final Transform<SearchMatch, String> TO_PAGE_NAME = new Transform<SearchMatch, String>() {
    public String transform(SearchMatch in) {
      return in.getPage();
    }
  };

  public static final Transform<String,SearchMatch> FROM_PAGE_NAME = new Transform<String, SearchMatch>(){
    public SearchMatch transform(final String s) {
      return new SearchMatch(s, null, null);
    }
  };
  
  private final String _page;
  private final String _htmlExtract;
  private Map<String, String> _pageProperties;


  public SearchMatch(final String page, final String htmlExtract, final Map<String, String> pageProperties) {
    _page = page;
    _htmlExtract = htmlExtract;
    _pageProperties = pageProperties;
  }

  /**
   * @return The page that matched.
   */
  public String getPage() {
    return _page;
  }

  /**
   * @return HTML extract showing match in context if requested and available,
   *         otherwise null.
   */
  public String getHtmlExtract() {
    return _htmlExtract;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof SearchMatch) {
      return _page.equals(((SearchMatch) obj)._page);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return _page.hashCode();
  }

  public int compareTo(final SearchMatch searchMatch) {
    return _page.compareTo(searchMatch._page);
  }

  public Map<String, String> getPageProperties() {
    return _pageProperties;
  }
}
