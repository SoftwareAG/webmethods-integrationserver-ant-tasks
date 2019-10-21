package com.softwareag.wx.is.ant.tasks;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public abstract class WmAbstractTask extends Task
{
  public static final String integrationServerPortPropertyName = "webMethods.integrationServer.port";
  public static final String integrationServerUserIdPropertyName = "webMethods.integrationServer.userid";
  public static final String integrationServerPasswordPropertyName = "webMethods.integrationServer.password";
  public static final String integrationServerHostPropertyName = "webMethods.integrationServer.name";
  
  protected void checkRequiredNumericalAttribute(String attributeName, int attributeValue) throws BuildException
  {
    if (attributeValue < 0)
    {
      throwBuildException(attributeName);
    }
  }

  protected void checkRequiredStringAttribute(String attributeName, String attributeValue) throws BuildException
  {
    checkRequiredStringAttribute(attributeName, attributeValue, null);
  }

  protected void checkRequiredAttribute(String attributeName, Object attributeValue) throws BuildException
  {
    if (attributeValue  == null)
    {
      throwBuildException(attributeName);
    }
  }
  
  protected void checkRequiredStringAttribute(String attributeName, String attributeValue, String message) throws BuildException
  {
    if (isEmptyOrNullString(attributeValue))
    {
      String taskName = getTaskName();
      if (taskName == null)
      {
        throw new BuildException(message != null ? message : "Attribute '" + attributeName + "' missing");
      }
      throw new BuildException(taskName + ":" + (message != null ? message : "Attribute '" + attributeName + "' missing"));
    }
  }

  protected boolean isEmptyOrNullString(String str)
  {
    return (str == null || str.trim().length() == 0);
  }


  protected void throwBuildException(String attributeName)
  {
    String taskName = getTaskName();
    if (taskName == null)
    {
      throw new BuildException("Attribute '" + attributeName + "' missing");
    }
    throw new BuildException(taskName + ":Attribute '" + attributeName + "' missing");
  }
}