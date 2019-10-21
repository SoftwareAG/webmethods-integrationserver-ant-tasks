package com.softwareag.wx.is.ant.tasks;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.optional.windows.Attrib;

import com.softwareag.wx.is.ant.helpers.FraggerResources;
import com.wm.app.b2b.server.NodeUtil;
import com.wm.app.b2b.server.Resources;
import com.wm.lang.ns.NSName;

public class WmFrag extends Task
{
  private String packageName;
  private File packagesDir;
  private String node;
  private boolean preserveReadOnlyNodeFiles;
  private boolean failOnUnlockedNodeFiles;

  private Resources resources;

  public String getPackage()
  {
    return packageName;
  }

  public void setPackage(String packageName)
  {
    this.packageName = packageName;
  }

  /**
   * @return the node
   */
  public String getNode()
  {
    return node;
  }

  /**
   * @param node
   *          the node to set
   */
  public void setNode(String node)
  {
    this.node = node;
  }

  /**
   * @return the preserveReadOnlyNodeFiles
   */
  public boolean isPreserveReadOnlyNodeFiles()
  {
    return preserveReadOnlyNodeFiles;
  }

  /**
   * @param preserveReadOnlyNodeFiles the preserveReadOnlyNodeFiles to set
   */
  public void setPreserveReadOnlyNodeFiles(boolean preserveReadOnlyNodeFiles)
  {
    this.preserveReadOnlyNodeFiles = preserveReadOnlyNodeFiles;
  }

  /**
   * @return the failOnUnlockedNodeFiles
   */
  public boolean isFailOnUnlockedNodeFiles()
  {
    return failOnUnlockedNodeFiles;
  }

  /**
   * @param failOnUnlockedNodeFiles the failOnUnlockedNodeFiles to set
   */
  public void setFailOnUnlockedNodeFiles(boolean failOnUnlockedNodeFiles)
  {
    this.failOnUnlockedNodeFiles = failOnUnlockedNodeFiles;
  }

  public File getPackagesDir()
  {
    return packagesDir;
  }

  public void setPackagesDir(File packagesDir)
  {
    this.packagesDir = packagesDir;
  }

  protected void prepare() throws BuildException
  {
    if (getPackage() == null || getPackage().length() == 0)
    {
      throw new BuildException("Attribute 'package' is required.");
    }
    resources = new FraggerResources(packagesDir);
    File nsFolder = resources.getPackageNSDir(getPackage());
    if (!nsFolder.exists() || !nsFolder.isDirectory())
    {
      throw new BuildException("Folder '" + nsFolder + "' does not exist or is not a folder.");
    }
  }

  public void execute() throws BuildException
  {
    prepare();
    fragPackage();
  }


  private void fragPackage()
  {
    String suffix = new SimpleDateFormat("HHmmssSSS").format(new Date());
    ArrayList fileList = new ArrayList();
    try
    {
      NodeUtil fragger = new NodeUtil(resources);
      File nsFolder = resources.getPackageNSDir(packageName);
      if (node != null)
      {
        nsFolder = new File(nsFolder, node.replace(".", "/"));
      }
      
      collectReadOnlyNodeFiles(fileList, nsFolder);
      if (!preserveReadOnlyNodeFiles)
      {
        if (failOnUnlockedNodeFiles && fileList.size() > 0)
        {
          throw new BuildException("File '" + fileList.get(0) + "' is not-writable and the property 'failOnUnlockedNodeFiles' is set to true");
        }
        setReadOnlyAttribute(fileList, false);
      }
      else
      {
        renameFiles(fileList, suffix);
      }
      fragger.setVerbose(2);
      if (node == null)
      {
        fragger.fragall(packageName);
      }
      else
      {
        fragger.frag(packageName, NSName.create(node));
      }
    }
    catch (IOException e)
    {
      throw new BuildException(e);
    }
    finally
    {
      if (!preserveReadOnlyNodeFiles)
      {
        setReadOnlyAttribute(fileList, true);
      }
      else
      {
        restoreFiles(fileList, suffix);
      }
    }
  }

  private void renameFiles(ArrayList fileList, String suffix) throws BuildException
  {
    for (int i = 0; i < fileList.size(); i++)
    {
      File file = (File) fileList.get(i);

      if (!file.renameTo(new File(file.getAbsolutePath() + "." + suffix)))
      {
        throw new BuildException("Could not rename file \"" + file.getAbsolutePath() + "\"");
      }
    }
  }

  private void restoreFiles(ArrayList fileList, String suffix) throws BuildException
  {
    for (int i = 0; i < fileList.size(); i++)
    {
      File file = (File) fileList.get(i);
      if (!file.delete())
      {
        throw new BuildException("Could not remove file \"" + file.getAbsolutePath() + "\"");
      }
      if (new File(file.getAbsolutePath() + "." + suffix).renameTo(file))
      {
        throw new BuildException("Could not rename file \"" + file.getAbsolutePath() + "." + suffix + "\"");
      }
    }
  }

  private void setReadOnlyAttribute(ArrayList fileList, boolean readonly)
  {
    for (int i = 0; i < fileList.size(); i++)
    {
      Attrib attrib = new Attrib();
      attrib.setProject(getProject());
      attrib.setFile((File) fileList.get(i));
      attrib.setReadonly(readonly);
      attrib.execute();
    }
  }

  private void collectReadOnlyNodeFiles(ArrayList list, File folder)
  {
    if (folder == null || !folder.exists())
    {
      return;
    }
    
    File[] files = folder.listFiles();
    for (int i = 0; i < files.length; i++)
    {
      if (files[i].isDirectory())
      {
        collectReadOnlyNodeFiles(list, files[i]);
      }
      else
      {
        String name = files[i].getName();
        if (("node.ndf".equalsIgnoreCase(name) || "node.idf".equalsIgnoreCase(name)) 
            && !(new File(folder, name).canWrite()) && !(new File(folder, "flow.xml").exists()))
        {
          list.add(files[i]);
        }
      }
    }
  }

  public static void main(String[] args)
  {
    WmFrag wm = new WmFrag();
    wm.setPackagesDir(new File("c:\\scm\\webMethods\\Source"));
    wm.setPackage("Util");
    wm.setNode("util.admin");
    wm.setFailOnUnlockedNodeFiles(true);
    wm.setProject(new Project());
    wm.execute();
  }
}