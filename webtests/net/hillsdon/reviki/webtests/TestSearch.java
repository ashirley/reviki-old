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
package net.hillsdon.reviki.webtests;

import net.hillsdon.fij.text.Escape;
import net.hillsdon.reviki.text.WikiWordUtils;

import com.gargoylesoftware.htmlunit.html.HtmlLink;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.xml.XmlPage;

public class TestSearch extends WebTestSupport {

  public void testLinkToOpenSearchAvailableFromRegularPages() throws Exception {
    HtmlPage results = getWikiPage("FrontPage");
    HtmlLink link = (HtmlLink) results.getByXPath("/html/head/link[@rel='search']").iterator().next();
    // Session crap on the end.
    assertURL(link.getHrefAttribute(), BASE_URL + "/pages/test/FindPage/opensearch.xml");
    @SuppressWarnings("unused")
    XmlPage xml = (XmlPage) results.getWebClient().getPage(results.getFullyQualifiedUrl(link.getHrefAttribute()));
  }

  public void testSearchOffersToCreateWikiPageThatDoesntExistWhenWeSearchForAWikiWord() throws Exception {
    String name = uniqueWikiPageName("ThisDoesNotExist");
    HtmlPage results = search(getWikiPage("FrontPage"), name);
    assertTrue(results.asText().contains("Create new page " + name));
    assertURL(BASE_URL + "/pages/test/" + name, results.getAnchorByName("create").getHrefAttribute());
    //results.getAnchorByHref(BASE_URL + "/pages/test/" + name);
  }

  /**
   * Search by WikiWord and Wiki Word.
   */
  public void testNewPageCanBeFoundByNameInSearchIndex() throws Exception {
    String name = uniqueWikiPageName("SearchIndexTest");
    HtmlPage page = editWikiPage(name, "Should be found by search", "", true);
    assertSearchFindsPageUsingQuery(page, name, "found by search");
    assertSearchFindsPageUsingQuery(page, name, WikiWordUtils.pathToTitle(name));
    HtmlPage searchResult = search(page, name);
    // Goes directly to the page.
    assertEquals(page.getWebResponse().getRequestUrl(), searchResult.getWebResponse().getRequestUrl());
  }

  public void testPageWithSpacesShowsInBacklinks() throws Exception {
    final String refers = uniqueWikiPageName("SearchSpaces BacklinkTesta");
    final String referred = uniqueWikiPageName("SearchSpaces BacklinkTestb");
    editWikiPage(refers, referred, "", true);
    final HtmlPage page = getWikiPage(referred);
    getAnchorByHrefContains(page, Escape.urlEncodeUTF8(refers));
  }
}
