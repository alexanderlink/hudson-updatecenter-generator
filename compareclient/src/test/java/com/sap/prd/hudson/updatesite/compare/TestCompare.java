package com.sap.prd.hudson.updatesite.compare;

import java.io.IOException;

import junit.framework.TestCase;

import com.sap.prd.hudson.updatesite.compare.UpdateSiteComparer;

public class TestCompare extends TestCase
{

  public void testFileVsFile() throws IOException
  {
    UpdateSiteComparer.main(new String[] {
        "src/test/resources/update-center1.json",
        "src/test/resources/update-center2.json" });
  }

  public void testUrlVsFile() throws IOException
  {
    UpdateSiteComparer.main(new String[] {
        "http://teamcontext:8080/TbsUpdateCenter/update-center.json",
        "src/test/resources/update-center2.json" });
  }

  public void testUrlVsUrl() throws IOException
  {
    UpdateSiteComparer.main(new String[] {
        "http://teamcontext:8080/TbsUpdateCenter_v1.15.6/update-center.json",
        "http://teamcontext:8080/TbsUpdateCenter/update-center.json" });
  }

  public void testFileVsUrl() throws IOException
  {
    UpdateSiteComparer.main(new String[] {
        "src/test/resources/update-center2.json",
        "http://teamcontext:8080/TbsUpdateCenter/update-center.json" });
  }

}
