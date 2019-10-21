package com.softwareag.wx.is.ant.tasks;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Reference;

import com.wm.data.IData;

/**
 * Implements an Ant Task for adding a variable to an IData task
 */
public class WmVariable extends Task
{
  private String name;
  private String value;
  private boolean array;
  private boolean nullValue;
  private Object actualValue;
  private Reference reference;
  private File propertiesFile;
  private ArrayList children = new ArrayList();

  /**
   * Add an IData object to use as an input to the service
   * 
   * @param idataTask
   *          the WmIDataTask that contains the service input.
   */
  public void addConfiguredIData(WmIData idataTask)
  {
    idataTask.perform();
    children.add(idataTask.getIData());
  }

  /*
   * Implementation of the execute method that does the actual work.
   * 
   * @see org.apache.tools.ant.Task#execute()
   */
  public void execute() throws BuildException
  {
    if (getProject() == null)
    {
      throw new IllegalStateException("project has not been set");
    }
    if (name == null)
    {
      throw new BuildException("You must specify name for the variable");
    }

    int childrenSize = children.size();
    if (childrenSize == 1)
    {
      actualValue = (IData) children.get(0);
      return;
    }
    else if (childrenSize > 1)
    {
      actualValue = children;
      return;
    }

    if (value == null && propertiesFile != null)
    {
      try
      {
        Properties props = new Properties();
        props.load(new FileReader(propertiesFile));
        value = props.getProperty(name);
      }
      catch (Exception e)
      {
        throw new BuildException(e);
      }
    }

    if (value == null && !nullValue && reference == null)
    {
      throw new BuildException("You must specify name, refid or propertiesFile for the variable or set the nullValue attribute to true");
    }
    
    if (nullValue)
    {
      if (value != null)
      {
        throw new BuildException("You cannot set the value attribute when nullValue is set to true.");
      }
    }
    else if (value == null && reference != null)
    {
      Object object = reference.getReferencedObject(getProject());
      if (object != null)
      {
        if (object instanceof IData)
        {
          actualValue = object;
        }
        else if (object instanceof WmIData)
        {
          actualValue = ((WmIData) object).getIData();
        }
      }
    }
    else
    {
      actualValue = value;
    }
  }

  public File getPropertiesFile()
  {
    return propertiesFile;
  }

  public void setPropertiesFile(File propertiesFile)
  {
    this.propertiesFile = propertiesFile;
  }

  /**
   * Get the actual value of this variable
   * 
   * @return returns the actualValue.
   */
  public Object getActualValue()
  {
    return actualValue;
  }

  /**
   * Get the name of the variable
   * 
   * @return Returns the name.
   */
  public String getName()
  {
    return name;
  }

  /**
   * Set the name of the variable
   * 
   * @param name
   *          the name to set.
   */
  public void setName(String name)
  {
    this.name = name;
  }

  /**
   * Get the value of the variable
   * 
   * @return returns the value.
   */
  public String getValue()
  {
    return value;
  }

 public boolean isNullValue()
 {
   return nullValue;
 }

 public void setNullValue(boolean nullValue)
 {
   this.nullValue = nullValue;
 }
 
  /**
   * Get the array value
   * 
   * @return true/false
   */
  public boolean isArray()
  {
    return array;
  }

  /**
   * Set the array value
   * 
   * @param array
   *          true/false
   */
  public void setArray(boolean array)
  {
    this.array = array;
  }

  /**
   * Set the value of the variable
   * 
   * @param value
   *          the value to set.
   */
  public void setValue(String value)
  {
    this.value = value;
  }

  /**
   * Get the reference id.
   * 
   * @return returns the reference.
   */
  public Reference getRefid()
  {
    return reference;
  }

  /**
   * Set the variable as a reference.
   * 
   * @param reference
   *          The reference to set.
   */
  public void setRefid(Reference reference)
  {
    this.reference = reference;
  }
}