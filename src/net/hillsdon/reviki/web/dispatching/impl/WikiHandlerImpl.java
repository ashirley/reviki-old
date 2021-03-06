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
package net.hillsdon.reviki.web.dispatching.impl;

import static net.hillsdon.reviki.web.vcintegration.BuiltInPageReferences.COMPLIMENTARY_CONTENT_PAGES;

import java.io.IOException;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.reviki.vc.ChangeNotificationDispatcher;
import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.vc.PageStoreAuthenticationException;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.vc.PageStoreInvalidException;
import net.hillsdon.reviki.vc.impl.CachingPageStore;
import net.hillsdon.reviki.web.common.ConsumedPath;
import net.hillsdon.reviki.web.common.JspView;
import net.hillsdon.reviki.web.common.RequestHandler;
import net.hillsdon.reviki.web.common.View;
import net.hillsdon.reviki.web.dispatching.ResourceHandler;
import net.hillsdon.reviki.web.dispatching.WikiHandler;
import net.hillsdon.reviki.web.handlers.PageHandler;
import net.hillsdon.reviki.web.urls.InternalLinker;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.web.urls.WikiUrls;
import net.hillsdon.reviki.web.urls.impl.ResponseSessionURLOutputFilter;
import net.hillsdon.reviki.web.vcintegration.BuiltInPageReferences;
import net.hillsdon.reviki.web.vcintegration.RequestLifecycleAwareManager;
import net.hillsdon.reviki.wiki.renderer.SvnWikiRenderer;

/**
 * A particular wiki (sub-wiki, whatever).
 * 
 * @author mth
 */
public class WikiHandlerImpl implements WikiHandler {

  private static final class RequestAuthenticationView implements View {
    public void render(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
      response.setHeader("WWW-Authenticate", "Basic realm=\"Wiki login\"");
      response.sendError(401);
    }
  }

  public static final String ATTRIBUTE_WIKI_IS_VALID = "wikiIsValid";

  private final RequestLifecycleAwareManager _requestLifecycleAwareManager;
  private final SvnWikiRenderer _renderer;
  private final CachingPageStore _cachingPageStore;
  private final InternalLinker _internalLinker;
  private final ChangeNotificationDispatcher _syncUpdater;
  private final WikiUrls _wikiUrls;
  private final ResourceHandler _resources;
  private final PageHandler _pageHandler;

  public WikiHandlerImpl(CachingPageStore cachingPageStore, SvnWikiRenderer renderer, InternalLinker internalLinker, ChangeNotificationDispatcher syncUpdater, RequestLifecycleAwareManager requestLifecycleAwareManager, ResourceHandler resources, PageHandler handler, WikiUrls wikiUrls) {
    _cachingPageStore = cachingPageStore;
    _renderer = renderer;
    _internalLinker = internalLinker;
    _syncUpdater = syncUpdater;
    _requestLifecycleAwareManager = requestLifecycleAwareManager;
    _resources = resources;
    _pageHandler = handler;
    _wikiUrls = wikiUrls;
  }

  public View test(HttpServletRequest request, HttpServletResponse response) throws PageStoreInvalidException, Exception {
    return handleInternal(new ConsumedPath(Collections.<String>emptyList()), request, response, new PageHandler() {
      public View handle(ConsumedPath path, HttpServletRequest request, HttpServletResponse response) throws Exception {
        _cachingPageStore.assertValid();
        return null;
      }
    });
  }
  
  public View handle(final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    return handleInternal(path, request, response, new RequestHandler() {
      public View handle(ConsumedPath path, HttpServletRequest request, HttpServletResponse response) throws Exception {
        request.setAttribute(WikiUrls.KEY, _wikiUrls);
        request.setAttribute(JspView.ATTR_CSS_URL, _internalLinker.url(BuiltInPageReferences.CONFIG_CSS.getPath(), "", URLOutputFilter.NULL) + "?ctype=raw");
        request.setAttribute("internalLinker", _internalLinker);
        if ("resources".equals(path.peek())) {
          return _resources.handle(path.consume(), request, response);
        }
        
        _syncUpdater.sync();
        addSideBarEtcToRequest(request, response);
        return _pageHandler.handle(path, request, response);
      }
    });
  }

  private View handleInternal(final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response, final RequestHandler delegate) throws Exception {
    try {
      _requestLifecycleAwareManager.requestStarted(request);
      return delegate.handle(path, request, response);
    }
    catch (PageStoreAuthenticationException ex) {
      return new RequestAuthenticationView();
    }
    catch (Exception ex) {
      // Rather horrible, needed at the moment for auth failures during rendering (linking).
      if (ex.getCause() instanceof PageStoreAuthenticationException) {
        return new RequestAuthenticationView();
      }
      else {
        // Don't try to show wiki header/footer.
        request.setAttribute(ATTRIBUTE_WIKI_IS_VALID, false);
        throw ex;
      }
    }
  }

  private void addSideBarEtcToRequest(final HttpServletRequest request, final HttpServletResponse response) throws PageStoreException, IOException {
    for (PageReference ref : COMPLIMENTARY_CONTENT_PAGES) {
      final String requestVarName = "rendered" + ref.getPath().substring("Config".length());
      PageInfo page = _cachingPageStore.get(ref, -1);
      request.setAttribute(requestVarName, _renderer.render(ref, page.getContent(), new ResponseSessionURLOutputFilter(response)).toXHTML());
    }
  }

}
