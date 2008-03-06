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
package net.hillsdon.reviki.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;

/**
 * Wherein we go to mad lengths to store the SVN URL and search index somewhere.
 * 
 * Additional run-time configuration options are stored in SVN rather than
 * on the file-system so that they benefit from versioning and backup.
 * 
 * @author mth
 */
public class DeploymentConfiguration {

  private static final String DEFAULT_CONFIG_DIR_NAME = "svnwiki-data";
  private static final String SEARCH_INDEX_DIR_NAME = "search-index";
  private static final String CONFIG_FILE_NAME = "svnwiki.properties";
  // Properties file keys:
  private static final String KEY_PREFIX_SVN_URL = "svn-url-";
  private static final String KEY_DEFAULT_WIKI = "default-wiki";
  
  private final Properties _properties = new Properties();

  public SVNURL getUrl(final String wikiName) {
    String url = (String) _properties.getProperty(KEY_PREFIX_SVN_URL + wikiName);
    try {
      return SVNURL.parseURIDecoded(url);
    }
    catch (SVNException ex) {
      return null;
    }
  }

  /**
   * @param wikiName 
   * @return Somewhere writable to put the search index, or null if that is not possible.
   */
  public File getSearchIndexDirectory(final String wikiName) {
    File searchDir = getWritableChildDir(getConfigurationLocation(), SEARCH_INDEX_DIR_NAME);
    return searchDir == null ? null : getWritableChildDir(searchDir, wikiName);
  }
  
  public File getWritableChildDir(final File dir, final String child) {
    File indexDir = new File(dir, child);
    if (!indexDir.exists()) {
      if (!indexDir.mkdir()) {
        return null;
      }
    }
    if (indexDir.isDirectory() && indexDir.canWrite()) {
      return indexDir;
    }
    return null;
  }
  
  public void setUrl(final String wikiName, final String url) throws IllegalArgumentException {
    try {
      SVNURL svnUrl = SVNURL.parseURIDecoded(url);
      _properties.setProperty(KEY_PREFIX_SVN_URL + wikiName, svnUrl.toDecodedString());
    }
    catch (SVNException e) {
      throw new IllegalArgumentException("Invalid SVN URL", e);
    }
    if (getDefaultWiki() == null) {
      setDefaultWiki(wikiName);
    }
  }

  public boolean isComplete(final String wikiName) {
    return getUrl(wikiName) != null;
  }

  /**
   * @return A configuration location if we can, otherwise null.
   */
  private File getConfigurationLocation() {
    String location = null;
    try {
      location = System.getProperty("svnwiki.data");
    }
    catch (SecurityException ex) {
    }
    if (location == null) {
      try {
        location = System.getenv("SVNWIKI_DATA");
      }
      catch (SecurityException ex) {
      }
    }
    if (location == null) {
      try {
        String home = System.getProperty("user.home");
        location = home + File.separator + DEFAULT_CONFIG_DIR_NAME;
      }
      catch (SecurityException ex) {
      }
    }
    if (location == null) {
      return null;
    }
    File dir = new File(location);
    try {
      if (!dir.exists()) {
        if (!dir.mkdir()) {
          return null;
        }
      }
    }
    catch (SecurityException ex) {
      return null;
    }
    return dir;
  }
  
  public synchronized void load() {
    File location = getConfigurationLocation();
    if (location != null) {
      File file = new File(location, CONFIG_FILE_NAME);
      try {
        FileInputStream in = new FileInputStream(file);
        try {
          _properties.clear();
          _properties.load(in);
        }
        finally {
          in.close();
        }
      }
      catch (IOException ex) {
        // We swallow errors for now. 
      }
    }
  }

  public synchronized void save() {
    File location = getConfigurationLocation();
    if (location != null && location.canWrite()) {
      File file = new File(location, CONFIG_FILE_NAME);
      try {
        FileOutputStream in = new FileOutputStream(file);
        try {
          _properties.store(in, "svnwiki configuration details");
        }
        finally {
          in.close();
        }
      }
      catch (IOException ex) {
        // We swallow errors for now.
      }
    }
  }

  public void setDefaultWiki(final String wikiName) {
    _properties.setProperty(KEY_DEFAULT_WIKI, wikiName);
  }

  public String getDefaultWiki() {
    return _properties.getProperty(KEY_DEFAULT_WIKI);
  }
  
  public Collection<String> getWikiNames() {
    List<String> names = new ArrayList<String>();
    for (Object o : _properties.keySet()) {
      String key =  (String) o;
      if (key.startsWith(KEY_PREFIX_SVN_URL)) {
        names.add(key.substring(KEY_PREFIX_SVN_URL.length(), key.length()));
      }
    }
    return names;
  }

}