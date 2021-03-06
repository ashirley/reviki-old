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
package net.hillsdon.reviki.vc;

import net.hillsdon.fij.core.Transform;

/**
 * Quite possibly this class is more trouble that it is worth.
 * 
 * @author mth
 */
public interface PageReference extends Comparable<PageReference> {

  public static final Transform<PageReference, String> TO_NAME = new Transform<PageReference, String>() {
    public String transform(PageReference in) {
      return in.getPath();
    }
  };
  
  /**
   * @return A user friendly title.
   */
  String getTitle();
  
  /**
   * @return The name of the page.
   */
  String getName();
  
  /**
   * @return The path used by the page store to store the page.
   */
  String getPath();
  
  /**
   * @return {@link #getPath()}.
   */
  String toString();

}

