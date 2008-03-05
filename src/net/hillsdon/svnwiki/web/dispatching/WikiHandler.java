/**
 * Copyright 2007 Matthew Hillsdon
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
package net.hillsdon.svnwiki.web.dispatching;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.configuration.PageStoreConfiguration;
import net.hillsdon.svnwiki.configuration.PerWikiInitialConfiguration;
import net.hillsdon.svnwiki.search.ExternalCommitAwareSearchEngine;
import net.hillsdon.svnwiki.search.LuceneSearcher;
import net.hillsdon.svnwiki.vc.ConfigPageCachingPageStore;
import net.hillsdon.svnwiki.vc.PageInfo;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStoreAuthenticationException;
import net.hillsdon.svnwiki.vc.PageStoreException;
import net.hillsdon.svnwiki.vc.PageStoreFactory;
import net.hillsdon.svnwiki.web.common.ConsumedPath;
import net.hillsdon.svnwiki.web.common.RequestHandler;
import net.hillsdon.svnwiki.web.handlers.PageHandler;
import net.hillsdon.svnwiki.web.vcintegration.BasicAuthPassThroughPageStoreFactory;
import net.hillsdon.svnwiki.web.vcintegration.RequestScopedThreadLocalPageStore;
import net.hillsdon.svnwiki.wiki.InternalLinker;
import net.hillsdon.svnwiki.wiki.MarkupRenderer;
import net.hillsdon.svnwiki.wiki.RenderedPageFactory;
import net.hillsdon.svnwiki.wiki.WikiGraph;
import net.hillsdon.svnwiki.wiki.WikiGraphImpl;
import net.hillsdon.svnwiki.wiki.macros.IncomingLinksMacro;
import net.hillsdon.svnwiki.wiki.macros.OutgoingLinksMacro;
import net.hillsdon.svnwiki.wiki.macros.SearchMacro;
import net.hillsdon.svnwiki.wiki.renderer.SvnWikiRenderer;
import net.hillsdon.svnwiki.wiki.renderer.macro.Macro;
import net.hillsdon.svnwiki.wiki.xquery.XQueryMacro;

/**
 * A particular wiki (sub-wiki, whatever).
 * 
 * @author mth
 */
public class WikiHandler implements RequestHandler {

  static final String ATTRIBUTE_WIKI_IS_VALID = "wikiIsValid";
  
  private final RequestScopedThreadLocalPageStore _pageStore;
  private final SvnWikiRenderer _renderer;
  private final ConfigPageCachingPageStore _cachingPageStore;
  
  private final PageHandler _handler;
  private final InternalLinker _internalLinker;

  private ExternalCommitAwareSearchEngine _searchEngine;

  public WikiHandler(final PerWikiInitialConfiguration configuration, final String contextPath) {
    _searchEngine = new ExternalCommitAwareSearchEngine(new LuceneSearcher(configuration.getSearchIndexDirectory(), new RenderedPageFactory(new MarkupRenderer() {
      public void render(PageReference page, String in, Writer out) throws IOException, PageStoreException {
        _renderer.render(page, in, out);
      }
    })));
    PageStoreFactory factory = new BasicAuthPassThroughPageStoreFactory(configuration.getUrl(), _searchEngine);
    _pageStore = new RequestScopedThreadLocalPageStore(factory);
    _searchEngine.setPageStore(_pageStore);
    _cachingPageStore = new ConfigPageCachingPageStore(_pageStore);
    _internalLinker = new InternalLinker(contextPath, configuration.getGivenWikiName(), _cachingPageStore);
    WikiGraph wikiGraph = new WikiGraphImpl(_cachingPageStore, _searchEngine);
    
    List<Macro> macros = Arrays.<Macro>asList(new XQueryMacro(), new IncomingLinksMacro(wikiGraph), new OutgoingLinksMacro(wikiGraph), new SearchMacro(_searchEngine));
    _renderer = new SvnWikiRenderer(new PageStoreConfiguration(_pageStore), _internalLinker, macros);
    _handler = new PageHandler(_cachingPageStore, _searchEngine, _renderer, wikiGraph);
  }

  public void handle(final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    request.setAttribute("cssUrl", _internalLinker.url("ConfigCss") + "?raw");
    request.setAttribute("internalLinker", _internalLinker);
    try {
      // Handle the lifecycle of the thread-local request dependent page store.
      _pageStore.create(request);
      try {
        _searchEngine.syncWithExternalCommits();
        addSideBarToRequest(request);
        _handler.handle(path, request, response);
      }
      finally {
        _pageStore.destroy();
      }
    }
    catch (PageStoreAuthenticationException ex) {
      requestAuthentication(response);
    }
    catch (Exception ex) {
      // Rather horrible, needed at the moment for auth failures during rendering (linking).
      if (ex.getCause() instanceof PageStoreAuthenticationException) {
        requestAuthentication(response);
      }
      else {
        // Don't try to show wiki header/footer.
        request.setAttribute(ATTRIBUTE_WIKI_IS_VALID, false);
        throw ex;
      }
    }
  }

  private void addSideBarToRequest(final HttpServletRequest request) throws PageStoreException, IOException {
    PageReference sidebar = new PageReference("ConfigSideBar");
    StringWriter sidebarHtml = new StringWriter();
    PageInfo configSideBar = _cachingPageStore.get(sidebar, -1);
    _renderer.render(sidebar, configSideBar.getContent(), sidebarHtml);
    request.setAttribute("sidebar", sidebarHtml.toString());
  }

  private void requestAuthentication(final HttpServletResponse response) throws IOException {
    response.setHeader("WWW-Authenticate", "Basic realm=\"Wiki login\"");
    response.sendError(401);
  }

}
