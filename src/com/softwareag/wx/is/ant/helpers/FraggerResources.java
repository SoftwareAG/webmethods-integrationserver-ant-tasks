package com.softwareag.wx.is.ant.helpers;

import java.io.File;

import com.wm.app.b2b.server.Resources;

public class FraggerResources extends Resources
{
  private File packagesDir;

  public FraggerResources(File packagesDir)
  {
    super(packagesDir.getParent(), false);
    this.packagesDir = packagesDir;
  }

  public File getPackagesDir()
  {
    return packagesDir != null ? packagesDir : super.getPackagesDir();
  }
}