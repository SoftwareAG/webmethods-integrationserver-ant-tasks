package com.softwareag.wx.is.ant.tasks;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;

public class WmUpdateExtendedSettings extends WmServiceTask
{
  private static final String defaultDelimitor = ";";
  private static final String lineSeparator = System.getProperty("line.separator");

  private String removeSettingsList = null;
  private String addUpdateSettingsList = null;
  private String delimitor = defaultDelimitor;

  public String getFolder()
  {
    return "wm.server.admin";
  }

  public String getService()
  {
    return "setExtendedSettings";
  }

  public void execute() throws BuildException
  {
    IData pipeline = invoke("wm.server.query", "getExtendedSettings", null);

    IDataCursor pipelineCursor = pipeline.getCursor();
    String settings = IDataUtil.getString(pipelineCursor, "settings");
    pipelineCursor.destroy();

    String[][] cmd = { { "settings", newSettings(settings) } };
    IData idata = IDataFactory.create(cmd);
    IData input = getInput();
    if (input == null)
    {
      setInput(idata);
    }
    super.execute();
  }

  private String newSettings(String settings)
  {
    String[] oldExtSettings = settings.split("\n");
    HashMap<String, String> newExtSettings = new HashMap<String, String>();

    if (removeSettingsList != null)
    {
      List<String> removeSettings = Arrays.asList(removeSettingsList.split("\\s*" + delimitor +"\\s*"));
      for (String setting : oldExtSettings)
      {
        String key = getKey(setting);
        if (!removeSettings.contains(key))
        {
          newExtSettings.put(key, getValue(setting));
        }
      }
    }

    if (addUpdateSettingsList != null)
    {
      HashMap<String, String> updateSettings = getHashmap(addUpdateSettingsList, delimitor);
      for (String key : updateSettings.keySet())
      {
        newExtSettings.put(key, updateSettings.get(key));
      }
    }

    StringBuffer sb = new StringBuffer();
    for (String key : newExtSettings.keySet())
    {
      sb.append(key);
      sb.append("=");
      sb.append(newExtSettings.get(key));
      sb.append(lineSeparator);
    }
    return sb.toString();
  }

  private String getKey(String setting)
  {
    if (setting.indexOf("=") > 0)
    {
      return setting.substring(0, setting.indexOf("=")).trim();
    }
    else
    {
      return setting.trim();
    }
  }

  private String getValue(String setting)
  {
    return setting.substring(setting.indexOf("=") + 1, setting.length()).trim();
  }

  private HashMap<String, String> getHashmap(String setting, String delimiter)
  {
    HashMap<String, String> map = new HashMap<String, String>();
    String[] stringParts = setting.split(delimiter);
    for (int i = 0; i < stringParts.length; i++)
    {
      map.put(getKey(stringParts[i]), getValue(stringParts[i]));
    }
    return map;
  }

  public void setRemoveSettingsList(String removeSettingsList)
  {
    this.removeSettingsList = removeSettingsList;
  }

  public void setAddUpdateSettingsList(String addUpdateSettingsList)
  {
    this.addUpdateSettingsList = addUpdateSettingsList;
  }

  public void setDelimitor(String delimitor)
  {
    this.delimitor = delimitor;
  }

  public static void main(String[] args)
  {
    WmUpdateExtendedSettings wmsa = new WmUpdateExtendedSettings();
    wmsa.setProject(new Project());
    wmsa.setDelimitor(",");
    wmsa.setRemoveSettingsList("watt.abc.prop1, watt.abc.prop2");
    wmsa.setAddUpdateSettingsList("watt.def.prop1=val1, watt.def.prop2=val2");
    wmsa.execute();
  }
}