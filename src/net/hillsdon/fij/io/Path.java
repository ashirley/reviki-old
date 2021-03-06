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
package net.hillsdon.fij.io;

import java.io.File;
import java.util.Iterator;

import net.hillsdon.fij.text.Strings;

import static net.hillsdon.fij.core.Functional.iter;

/**
 * Path manipulation.
 * 
 * @author mth
 */
public final class Path {

  /**
   * Joins with File.separator.
   */
  public static String join(final String... paths) {
    return join(iter(paths));
  }
  
  /**
   * Joins with File.separator.
   */
  public static String join(final Iterator<String> paths) {
    return Strings.join(paths, File.separator);
  }

  private Path() {
  }
  
}
