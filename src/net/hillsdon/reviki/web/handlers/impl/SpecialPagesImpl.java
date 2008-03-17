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
package net.hillsdon.reviki.web.handlers.impl;

import java.util.LinkedHashMap;

import net.hillsdon.reviki.web.handlers.SpecialPage;
import net.hillsdon.reviki.web.handlers.SpecialPages;

/**
 * Aggregates the special pages.
 * 
 * @author mth
 */
public class SpecialPagesImpl extends LinkedHashMap<String, SpecialPage> implements SpecialPages {
  
  private static final long serialVersionUID = 1L;

  public SpecialPagesImpl(final SpecialPage... specialPages) {
    for (SpecialPage page : specialPages) {
      put(page.getName(), page);
    }
  }
  
}
