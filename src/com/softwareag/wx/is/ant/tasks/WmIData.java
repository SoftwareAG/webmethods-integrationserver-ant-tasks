/*
 * Created on Jan 7, 2005
 */
package com.softwareag.wx.is.ant.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Reference;

import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;
import com.wm.ps.jxpath.IDataJXPathContext;
import com.wm.util.coder.XMLCoderWrapper;

/**
 * Implements an Ant Task for creating an IData object
 */
public class WmIData extends Task
{
  private IData idata = IDataFactory.create();
  private HashMap variableMap = new HashMap();
  private String id;
  private Reference reference;
  private File file;
  private File propertiesFile;
  
  private ArrayList<WmProperty> properties = new ArrayList<WmProperty>();
  

  /**
   * Get the reference in this object
   * 
   * @return returns the reference.
   */
  public Reference getRefid()
  {
    return reference;
  }

  /**
   * Set the reference in this object
   * 
   * @param reference
   *          the reference to set.
   */
  public void setRefid(Reference reference)
  {
    this.reference = reference;
  }

  /**
   * Get the id of this IData object
   * 
   * @return returns the id.
   */
  public String getId()
  {
    return id;
  }

  /**
   * Set the id of this IData object
   * 
   * @param id
   *          the id to set.
   */
  public void setId(String id)
  {
    this.id = id;
  }

  /**
   * Get the file of this IData object
   * 
   * @return returns the file.
   */
  public File getFile()
  {
    return file;
  }

  /**
   * Set the file of this IData object
   * 
   * @param file
   *          the file to set.
   */
  public void setFile(File file)
  {
    this.file = file;
  }
  
  public void addConfiguredProperty(WmProperty propertyTask)
  {
    propertyTask.perform();
    properties.add(propertyTask);
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
   * Get the IData object wrapped by this object
   * 
   * @return the IData wrapped in this object.
   */
  public IData getIData()
  {
    return idata;
  }

  /**
   * Add a variable to this object
   * 
   * @param variable
   *          the variable to add.
   */
  public void addConfiguredVariable(WmVariable variable)
  {
    variable.perform();
    String name = variable.getName();
    Object actualValue = variable.getActualValue();

    if (variableMap.containsKey(name))
    {
      if (variableMap.get(name) instanceof ArrayList)
      {
        ArrayList list = (ArrayList) variableMap.get(name);
        addActualValueToList(actualValue, list);
        variableMap.put(name, list);
      }
      else
      {
        ArrayList list = new ArrayList();
        list.add(variableMap.get(name));
        addActualValueToList(actualValue, list);
        variableMap.put(name, list);
      }
    }
    else if (variable.isArray())
    {
      ArrayList list = new ArrayList();
      addActualValueToList(actualValue, list);
      variableMap.put(name, list);
    }
    else
    {
      variableMap.put(variable.getName(), actualValue);
    }
  }

  private void addActualValueToList(Object actualValue, ArrayList list)
  {
    if (actualValue instanceof ArrayList)
    {
      list.addAll((ArrayList) actualValue);
    }
    else
    {
      list.add(actualValue);
    }
  }
  
  private void assignProperties()
  {
    JXPathContext xpathContext = IDataJXPathContext.newContext(idata);
    for (int i = 0; i < properties.size(); i++)
    {
      WmProperty propertyTask = properties.get(i);
      Object value = xpathContext.selectSingleNode(propertyTask.getXpath());
      if (value != null)
      {
        getProject().setProperty(propertyTask.getName(), value.toString());
      }
    }
  }

  /**
   * The implementation of the execute method from the Task super class.
   */
  public void execute() throws BuildException
  {
    if (file != null)
    {
      FileInputStream fis = null;
      try
      {
        fis = new FileInputStream(file);
        IDataUtil.merge(new XMLCoderWrapper().decode(fis), idata);
        fis.close();
      }
      catch (IOException ioe)
      {
        throw new BuildException(ioe);
      }
      finally
      {
        if (fis != null)
        {
          try
          {
            fis.close();
          }
          catch (IOException ioe)
          {
            // Ignore
          }
        }
      }
    }

    if (reference != null)
    {
      Object object = reference.getReferencedObject(getProject());
      if (object != null)
      {
        if (object instanceof IData)
        {
          IDataUtil.merge((IData) object, idata);
        }
        else if (object instanceof WmIData)
        {
          IDataUtil.merge(((WmIData) object).getIData(), idata);
        }
      }
    }

    if (propertiesFile != null)
    {
      try
      {
        Properties variableProperties = new Properties();
        variableProperties.load(new FileReader(propertiesFile));
        
        for (String name : variableProperties.stringPropertyNames())
        {
          variableMap.put(name, variableProperties.get(name));
        }
      }
      catch (IOException ioe)
      {
        throw new BuildException(ioe);
      }
    }

    if (variableMap.size() > 0)
    {
      IDataCursor cursor = idata.getCursor();
      Set keys = variableMap.keySet();
      for (Iterator iter = keys.iterator(); iter.hasNext();)
      {
        String key = (String) iter.next();
        Object value = variableMap.get(key);
        if (value instanceof ArrayList)
        {
          Object arr = null;
          ArrayList list = (ArrayList) value;
          arr = Array.newInstance(list.get(0).getClass(), list.size());
          for (int i = 0; i < list.size(); i++)
          {
            Array.set(arr, i, list.get(i));
          }
          IDataUtil.put(cursor, key, arr);
        }
        else
        {
          IDataUtil.put(cursor, key, value);
        }
      }
      cursor.destroy();
    }
    assignProperties();
  }
}