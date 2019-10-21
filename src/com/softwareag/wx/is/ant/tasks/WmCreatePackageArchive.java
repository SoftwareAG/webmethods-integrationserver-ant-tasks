package com.softwareag.wx.is.ant.tasks;

import org.apache.tools.ant.BuildException;

import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;


public class WmCreatePackageArchive extends WmServiceTask
{
  private static final String packageNameParam = "package";
  private static final String buildNumberParam = "build";
  private static final String versionParam = "version";
  private static final String patchNumberParam = "patch_nums";
  private static final String targetPackageVersionParam = "target_pkg_version";
  private static final String targetServerVersionParam = "target_server_version";
  private static final String filterParam = "filter";
  private static final String jvmVersionParam = "jvm_version";

  private static final String archiveNameParam = "name";

  private static final String includePatternParam = "includepattern";
  private static final String excludePatternParam = "excludepattern";

  private static final Object filterExcludeValue = "exclude";
  private static final Object filterIncludeValue = "include";

  private static final String typeParam = "type";
  private static final String typeAllValue = "full";
  private static final String typePartialValue = "partial";

  private String jvmVersion = "1.8";
  private String targeServerVersion = "9.12";
  private String type = typeAllValue;
  private String filter;
  private String version;
  private String buildNumber;
  private String patchNumbers;
  private String targetPackageVersion;
  private String packageName;
  private String archiveName;
  private String excludePattern;
  private String includePattern;

  /**
   * @return Returns the buildNumber.
   */
  public String getBuildNumber()
  {
    return buildNumber;
  }

  /**
   * @param buildNumber
   *          The buildNumber to set.
   */
  public void setBuildNumber(String buildNumber)
  {
    this.buildNumber = buildNumber.trim();
  }

  /**
   * @return Returns the filter.
   */
  public String getFilter()
  {
    return filter;
  }

  /**
   * @param filter
   *          The filter to set.
   */
  public void setFilter(String filter)
  {
    this.filter = filter.trim();
  }

  /**
   * @return Returns the patchNumbers.
   */
  public String getPatchNumbers()
  {
    return patchNumbers;
  }

  /**
   * @param patchNumbers
   *          The patchNumbers to set.
   */
  public void setPatchNumbers(String patchNumbers)
  {
    this.patchNumbers = patchNumbers.trim();
  }

  /**
   * @return Returns the targetPackageVersion.
   */
  public String getTargetPackageVersion()
  {
    return targetPackageVersion;
  }

  /**
   * @param targetPackageVersion
   *          The targetPackageVersion to set.
   */
  public void setTargetPackageVersion(String targetPackageVersion)
  {
    this.targetPackageVersion = targetPackageVersion.trim();
  }

  /**
   * @return Returns the targetWMServerVersion.
   */
  public String getTargetServerVersion()
  {
    return targeServerVersion;
  }

  /**
   * @param targetWMServerVersion
   *          The targetWMServerVersion to set.
   */
  public void setTargetServerVersion(String targetWMServerVersion)
  {
    this.targeServerVersion = targetWMServerVersion.trim();
  }

  /**
   * @return Returns the type.
   */
  public String getType()
  {
    return type;
  }

  /**
   * @param type
   *          The type to set.
   */
  public void setType(String type)
  {
    this.type = type.trim();
  }

  /**
   * @return Returns the version.
   */
  public String getVersion()
  {
    return version;
  }

  /**
   * @param version
   *          The version to set.
   */
  public void setVersion(String version)
  {
    this.version = version.trim();
  }

  /**
   * @return Returns the jvmVersion.
   */
  public String getJvmVersion()
  {
    return jvmVersion;
  }

  /**
   * @param jvmVersion
   *          The jvmVersion to set.
   */
  public void setJvmVersion(String jvmVersion)
  {
    this.jvmVersion = jvmVersion.trim();
  }

  /**
   * @return Returns the packageName.
   */
  public String getPackage()
  {
    return packageName;
  }

  /**
   * @param packageName
   *          The packageName to set.
   */
  public void setPackage(String packageName)
  {
    this.packageName = packageName.trim();
  }

  /**
   * @return Returns the archiveName.
   */
  public String getArchive()
  {
    return archiveName;
  }

  /**
   * @param archiveName
   *          The archiveName to set.
   */
  public void setArchive(String archiveName)
  {
    this.archiveName = archiveName.trim();
  }

  /**
   * @return Returns the excludePattern.
   */
  public String getExcludePattern()
  {
    return excludePattern;
  }

  /**
   * @param excludePattern
   *          The excludePattern to set.
   */
  public void setExcludePattern(String excludePattern)
  {
    this.excludePattern = excludePattern;
  }

  /**
   * @return Returns the includePattern.
   */
  public String getIncludePattern()
  {
    return includePattern;
  }

  /**
   * @param includePattern
   *          The includePattern to set.
   */
  public void setIncludePattern(String includePattern)
  {
    this.includePattern = includePattern;
  }

  /**
   * @see com.softwareag.wx.is.ant.tasks.is.WmServiceTask#getFolder()
   */
  public String getFolder()
  {
    return "wm.server.replicator";
  }

  /**
   * @see com.softwareag.wx.is.ant.tasks.is.WmServiceTask#getService()
   */
  public String getService()
  {
    return "packageRelease";
  }

  protected void prepare() throws BuildException
  {
    super.prepare();
    checkRequiredStringAttribute("archive", getArchive());
    checkRequiredStringAttribute(packageNameParam, getPackage());
    checkRequiredStringAttribute(filterParam, getFilter(), "Illegal value for filter specified: includeall, include, exclude");
    checkRequiredStringAttribute(typeParam, getType(), "Type required: 'full' or 'partial'");
    checkRequiredStringAttribute(versionParam, getVersion());
    assignInputs();
  }

  public void execute() throws BuildException
  {
    super.execute();
    IDataCursor cursor = getOutput().getCursor();
    System.out.println(IDataUtil.getString(cursor, "message"));
    cursor.destroy();
  }

  private void assignInputs()
  {
    IData input = getInput();
    if (input == null)
    {
      input = IDataFactory.create();
      setInput(input);
    }
    IDataCursor cursor = input.getCursor();
    IDataUtil.put(cursor, "archive", "true");
    IDataUtil.put(cursor, packageNameParam, getPackage());

    if ((getFilter() != null) && (getFilter().length() > 0))
    {
      IDataUtil.put(cursor, filterParam, getFilter());
    }

    if ((getBuildNumber() != null) && (getBuildNumber().length() > 0))
    {
      IDataUtil.put(cursor, buildNumberParam, getBuildNumber());
    }

    if ((getVersion() != null) && (getVersion().length() > 0))
    {
      IDataUtil.put(cursor, versionParam, getVersion());
    }

    if ((getPatchNumbers() != null) && (getPatchNumbers().length() > 0))
    {
      IDataUtil.put(cursor, patchNumberParam, getPatchNumbers());
    }

    if ((getTargetPackageVersion() != null) && (getTargetPackageVersion().length() > 0))
    {
      IDataUtil.put(cursor, targetPackageVersionParam, getTargetPackageVersion());
    }

    if ((getTargetServerVersion() != null) && (getTargetServerVersion().length() > 0))
    {
      IDataUtil.put(cursor, targetServerVersionParam, getTargetServerVersion());
    }

    if ((getJvmVersion() != null) && (getJvmVersion().length() > 0))
    {
      IDataUtil.put(cursor, jvmVersionParam, getJvmVersion());
    }

    if ((getType() != null) && (getType().length() > 0))
    {
      IDataUtil.put(cursor, typeParam, getType());
    }

    if ((getArchive() != null) && (getArchive().length() > 0))
    {
      IDataUtil.put(cursor, archiveNameParam, getArchive());
    }

    if ((getFilter() != null) && (getFilter().length() > 0))
    {
      if (getFilter().equals(filterIncludeValue))
      {
        IDataUtil.put(cursor, includePatternParam, getIncludePattern());
      }
      else if (getFilter().equals(filterExcludeValue))
      {
        IDataUtil.put(cursor, excludePatternParam, getExcludePattern());
      }
    }

    cursor.destroy();
  }
}