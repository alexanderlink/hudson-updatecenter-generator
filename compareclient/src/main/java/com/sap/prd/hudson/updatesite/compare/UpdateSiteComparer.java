package com.sap.prd.hudson.updatesite.compare;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hudsonci.update.client.UpdateCenterUtils;
import org.hudsonci.update.client.model.Core;
import org.hudsonci.update.client.model.Plugin;
import org.hudsonci.update.client.model.UpdateSiteMetadata;

public class UpdateSiteComparer
{

  private String extractJSON(String updateSiteDefinition)
  {
    final String START = "updateCenter.post(";
    final String END = ");";
    String json = updateSiteDefinition.trim();
    int idx = json.indexOf(START);
    if (idx >= 0) json = json.substring(idx + START.length());
    if (json.endsWith(END)) json = json.substring(0, json.length() - END.length());
    return json.trim();
  }

  public void compare(URL jsonUrl1, URL jsonUrl2) throws IOException
  {
    String json1 = extractJSON(IOUtils.toString(jsonUrl1));
    String json2 = extractJSON(IOUtils.toString(jsonUrl2));
    compare(json1, json2);
  }

  public void compare(File jsonFile1, URL jsonUrl2) throws IOException
  {
    String json1 = extractJSON(FileUtils.readFileToString(jsonFile1));
    String json2 = extractJSON(IOUtils.toString(jsonUrl2));
    compare(json1, json2);
  }

  public void compare(URL jsonUrl1, File jsonFile2) throws IOException
  {
    String json1 = extractJSON(IOUtils.toString(jsonUrl1));
    String json2 = extractJSON(FileUtils.readFileToString(jsonFile2));
    compare(json1, json2);
  }

  public void compare(File jsonFile1, File jsonFile2) throws IOException
  {
    String json1 = extractJSON(FileUtils.readFileToString(jsonFile1));
    String json2 = extractJSON(FileUtils.readFileToString(jsonFile2));
    compare(json1, json2);
  }

  public void compare(String json1, String json2) throws IOException
  {
    UpdateSiteMetadata meta1 = UpdateCenterUtils.parse(json1);
    UpdateSiteMetadata meta2 = UpdateCenterUtils.parse(json2);
    compareUpdateSiteMetadata(meta1, meta2);
  }

  public void compareUpdateSiteMetadata(UpdateSiteMetadata meta1, UpdateSiteMetadata meta2)
  {
    compareCore(meta1.getCore(), meta2.getCore());
    comparePlugins(meta1.getPlugins(), meta2.getPlugins());
    meta1.getConnectionCheckUrl();
    meta1.getId();
    meta1.getSignature();
    meta1.getUpdateCenterVersion();
  }

  private void comparePlugins(Map<String, Plugin> map1, Map<String, Plugin> map2)
  {
    HashMap<String, Plugin> map2Remaining = new HashMap<String, Plugin>(map2);
    for (Map.Entry<String, Plugin> entry1 : map1.entrySet()) {
      Plugin plugin2 = map2Remaining.remove(entry1.getKey());
      comparePlugin(entry1.getValue(), plugin2);
    }
    for (Map.Entry<String, Plugin> entry2 : map2Remaining.entrySet()) {
      comparePlugin(null, entry2.getValue());
    }
  }

  private void comparePlugin(Plugin p1, Plugin p2)
  {
    if (p1 == null && p2 != null) {
      System.out.println("Added plugin: " + toString(p2));
    }
    else if (p2 == null && p1 != null) {
      System.out.println("Removed plugin: " + toString(p1));
    }
    else {
      String prefix = "plugin." + p1.getName() + ".";
      compareStrings(prefix + "name", p1.getName(), p2.getName());
      compareStrings(prefix + "version", p1.getVersion(), p2.getVersion());
      compareStrings(prefix + "url", p1.getUrl(), p2.getUrl());
      compareStrings(prefix + "title", p1.getTitle(), p2.getTitle());
    }
  }

  private String toString(Plugin plugin)
  {
    return String.format("%s:%s", plugin.getName(), plugin.getVersion());
  }

  private void compareCore(Core c1, Core c2)
  {
    compareStrings("core.name", c1.getName(), c2.getName());
    compareStrings("core.version", c2.getVersion(), c2.getVersion());
    compareStrings("core.url", c2.getUrl(), c2.getUrl());
    compareStrings("core.buildDate", c2.getBuildDate(), c2.getBuildDate());
  }

  private void compareStrings(String id, String s1, String s2)
  {

    if (!StringUtils.equals(s1, s2)) System.out.println(String.format("%s differs: '%s' / '%s'", id, s1, s2));
  }

  /**
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) throws IOException
  {
    if (args.length != 2) {
      System.out.println("Usage: UpdateSiteComparer <jsonUrlOrPath1> <jsonUrlOrPath2>");
      return;
    }
    URL url1 = getUrl(args[0]);
    URL url2 = getUrl(args[1]);
    File file1 = getFile(args[0]);
    File file2 = getFile(args[1]);

    if (url1 == null && file1 == null) throw new IllegalArgumentException("JSON 1 not found: " + args[0]);
    if (url2 == null && file2 == null) throw new IllegalArgumentException("JSON 2 not found: " + args[1]);

    UpdateSiteComparer comparer = new UpdateSiteComparer();
    if (file1 != null && file2 != null) {
      comparer.compare(file1, file2);
    }
    else if (url1 != null && url2 != null) {
      comparer.compare(url1, url2);
    }
    else if (file1 != null && url2 != null) {
      comparer.compare(file1, url2);
    }
    else if (url1 != null && file2 != null) {
      comparer.compare(url1, file2);
    }
  }

  private static File getFile(String path)
  {
    File file = new File(path);
    if (file.isFile()) return file;
    return null;
  }

  private static URL getUrl(String url)
  {
    try {
      return new URL(url);
    }
    catch (MalformedURLException e) {
      return null;
    }
  }

}
