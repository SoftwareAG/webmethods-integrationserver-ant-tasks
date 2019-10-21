package com.softwareag.wx.is.ant.tasks;

import java.io.File;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataUtil;

public class WmCheckInstalledFixes extends WmServerCondition
{
  private String fixList = null;
  private String dirInstalledFixes = null;

  public String getFolder()
  {
    return "wm.server.query";
  }

  public String getService()
  {
    return "getSystemAttributes";
  }

  public boolean eval() throws BuildException
  {
    if (isEmptyOrNullString(fixList))
    {
      return true;
    }

    prepare();

    boolean allPatchesInstalled = true;
    IData pipeline = invoke();

    IDataCursor pipelineCursor = pipeline.getCursor();
    String[] patches = IDataUtil.getStringArray(pipelineCursor, "patches");
    pipelineCursor.destroy();

    Vector vector = new Vector();
    if (patches != null)
    {
      for (int i = 0; i < patches.length; i++)
      {
        vector.add(patches[i]);
      }
    }

    // Any additional fixes that are considered to be installed?
    if (!isEmptyOrNullString(dirInstalledFixes))
    {
      File dir = new File(dirInstalledFixes);
      String[] chld = dir.list();
      if (chld != null)
      {
        for (int i = 0; i < chld.length; i++)
        {
          if (!vector.contains(chld[i]))
          {
            vector.add(chld[i]);
          }
        }
      }
    }
    String[] requiredPatches = fixList.split(",");

    for (int i = 0; i < requiredPatches.length; i++)
    {
      if (!vector.contains(requiredPatches[i].trim()))
      {
        log("Missing required fix: '" + requiredPatches[i].trim() + "'", Project.MSG_DEBUG);
        allPatchesInstalled = false;
        break;
      }
    }
    return allPatchesInstalled;
  }

  public String getFixList()
  {
    return fixList;
  }

  public void setFixList(String fixList)
  {
    this.fixList = fixList;
  }

  public String getDirInstalledFixes()
  {
    return dirInstalledFixes;
  }

  public void setDirInstalledFixes(String dirInstalledFixes)
  {
    this.dirInstalledFixes = dirInstalledFixes;
  }
}