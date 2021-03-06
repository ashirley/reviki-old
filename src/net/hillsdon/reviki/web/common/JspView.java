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
package net.hillsdon.reviki.web.common;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Includes a JSP from the templates directory.
 * 
 * @author mth
 */
public class JspView implements View {

  public static final String ATTR_CSS_URL = "cssUrl";
  
  private final String _name;

  public JspView(final String name) {
    _name = name;
  }
  
  public String getName() {
    return _name;
  }
  
  public void render(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
    response.setCharacterEncoding("UTF-8");
    response.setContentType("text/html");
    if (request.getAttribute(ATTR_CSS_URL) == null) {
      request.setAttribute(ATTR_CSS_URL, request.getContextPath() + "/resources/default-style.css");
    }

    request.getRequestDispatcher("/WEB-INF/templates/" + _name + ".jsp").include(request, response);
  }

}
