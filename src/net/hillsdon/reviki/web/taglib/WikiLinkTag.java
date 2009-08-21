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
package net.hillsdon.reviki.web.taglib;

import net.hillsdon.reviki.web.urls.InternalLinker;
import net.hillsdon.reviki.web.urls.URLOutputFilter;

/**
 * Uses an {@link InternalLinker} to create links to wiki pages.
 * 
 * @copyright
 * @author mth
 */
public class WikiLinkTag extends AbstractWikiLinkTag {

  private static final long serialVersionUID = 1L;

  protected String doOutput(InternalLinker linker, URLOutputFilter urlOutputFilter) {
    return linker.aHref(getPage(), getPage(), getExtra(), urlOutputFilter);
  }
  
}

