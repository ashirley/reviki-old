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
package net.hillsdon.reviki.web.urls.impl;

import net.hillsdon.fij.text.Escape;
import net.hillsdon.reviki.configuration.WikiConfiguration;
import net.hillsdon.reviki.web.urls.ApplicationUrls;

/**
 * Wiki URLs with a base URL determined by the ApplicationUrls.
 * 
 * @author mth
 */
public class WikiUrlsImpl extends AbstractWikiUrls {

  private final ApplicationUrls _applicationUrls;
  private final WikiConfiguration _configuration;

  public WikiUrlsImpl(final ApplicationUrls applicationUrls, final WikiConfiguration configuration) {
    _applicationUrls = applicationUrls;
    _configuration = configuration;
  }
  
  public String pagesRoot() {
    String fixedBaseUrl = _configuration.getFixedBaseUrl();
    if (fixedBaseUrl != null) {
     if (!fixedBaseUrl.endsWith("/")) {
       fixedBaseUrl += "/";
     }
     return fixedBaseUrl; 
    }
    
    final String givenWikiName = _configuration.getWikiName();
    String relative = "/pages/";
    if (givenWikiName != null) {
      relative += Escape.urlEncodeUTF8(givenWikiName) + "/";
    }
    return _applicationUrls.url(relative);
  }

}
