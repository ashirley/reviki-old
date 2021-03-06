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

import javax.servlet.http.HttpServletRequest;

import net.hillsdon.fij.core.Transform;
import net.hillsdon.reviki.configuration.DeploymentConfiguration;
import net.hillsdon.reviki.web.urls.ApplicationUrls;
import net.hillsdon.reviki.web.urls.WikiUrls;
import net.hillsdon.reviki.web.vcintegration.AbstractRequestLifecycleAware;

public class RequestScopedApplicationUrls extends AbstractRequestLifecycleAware<ApplicationUrls> implements ApplicationUrls {

  public RequestScopedApplicationUrls(final DeploymentConfiguration deploymentConfiguration) {
    super(new Transform<HttpServletRequest, ApplicationUrls>() {
      public ApplicationUrls transform(final HttpServletRequest in) {
        return new ApplicationUrlsImpl(in, deploymentConfiguration);
      }
    });
  }
  
  public WikiUrls get(final String name) {
    return get().get(name);
  }

  public String list() {
    return get().list();
  }

  public String url(final String relative) {
    return get().url(relative);
  }

  public WikiUrls get(String name, String givenWikiName) {
    return get().get(name, givenWikiName);
  }

  public String resource(String path) {
    return get().resource(path);
  }
  
}
