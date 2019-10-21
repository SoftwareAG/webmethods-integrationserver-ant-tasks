package com.softwareag.wx.is.ant.tasks;

import java.util.ArrayList;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

import com.softwareag.wx.is.ant.helpers.ContextManager;
import com.softwareag.wx.is.ant.tasks.WmAbstractTask;
import com.wm.app.b2b.client.Context;
import com.wm.data.IData;
import com.wm.ps.jxpath.IDataJXPathContext;

/**
 * Implements an Ant Task for invoking a webMethods Integration Server service.
 */

public class WmServiceTask extends WmAbstractTask
{
  public static final String defaultOutputId = "service.output.data";
  
  protected static final String serviceFolderAttributeName = "folder";
  protected static final String serviceNameAttributeName = "service";
  protected static final String passwordAttributeName = "password";
  protected static final String userAttributeName = "user";
  protected static final String portAttributeName = "port";
  protected static final String serverAttributeName = "server";

  private String server;
  private int port = -1;
  private String folder;
  private String service;
  private IData input;
  private IData output;
  private String user;
  private String password;
  private String outputId;
  private ArrayList<WmProperty> properties = new ArrayList<WmProperty>();

  /**
   * Get the port number of the server to invoke the service on.
   * 
   * @return the port number
   */
  public int getPort()
  {
    if (port < 0)
    {
      port = Integer.parseInt(getProperty(integrationServerPortPropertyName, "5555"));
    }
    return port;
  }

  /**
   * Set the port number of the server
   * 
   * @param port
   *          the port number of the server.
   */
  public void setPort(int port)
  {
    this.port = port;
  }

  /**
   * Get the server's name
   * 
   * @return the name of the server
   */
  public String getServer()
  {
    if (server == null)
    {
      server = getProperty(integrationServerHostPropertyName, "localhost");
    }
    return server;
  }

  /**
   * Set the name of the server
   * 
   * @param server
   *          the server's name
   */
  public void setServer(String server)
  {
    this.server = server;
  }

  /**
   * Get the password to connect to the server
   * 
   * @return the password to use to connect to the server.
   */
  public String getPassword()
  {
    if (password == null)
    {
      password = getProperty(integrationServerPasswordPropertyName, "manage");
    }
    return password;
  }

  /**
   * Set the password to use to connect to the server
   * 
   * @param password
   *          the password to use to connect to the server.
   */
  public void setPassword(String password)
  {
    this.password = password;
  }

  /**
   * Get the username to connect to the server
   * 
   * @return the username to use to connect to the server.
   */
  public String getUser()
  {
    if (user == null)
    {
      user = getProperty(integrationServerUserIdPropertyName, "Administrator");
    }
    return user;
  }

  /**
   * Set the username to use to connect to the server
   * 
   * @param user
   *          the username to use to connect to the server.
   */
  public void setUser(String user)
  {
    this.user = user;
  }

  /**
   * Get the folder in which the service belongs
   * 
   * @return the folder name.
   */
  public String getFolder()
  {
    return folder;
  }

  /**
   * Set the folder in which the service belongs
   * 
   * @param folder
   *          the name of the folder
   */
  public void setFolder(String folder)
  {
    this.folder = folder;
  }

  /**
   * Get the output from the service invocation.
   * 
   * @return the IData object which is returned by the service.
   */
  public IData getOutput()
  {
    return output;
  }

  /**
   * Set the IData object as input for the service invocation
   * 
   * @param input
   *          the IData input object.
   */
  public void setInput(IData input)
  {
    this.input = input;
  }

  /**
   * Get the IData object as input for the service invocation
   * 
   * @return the IData input object.
   */
  public IData getInput()
  {
    return this.input;
  }

  /**
   * Get the name of the service to invoke
   * 
   * @return the name of the service.
   */
  public String getService()
  {
    return service;
  }

  /**
   * Set the name of the service
   * 
   * @param service
   *          the name of the service
   */
  public void setService(String service)
  {
    this.service = service;
  }
  
  /**
   * @return the outputId
   */
  public String getOutputId()
  {
    return outputId == null ? defaultOutputId : outputId;
  }

  /**
   * Set the id to save the output of the service as a reference
   * @param outputId the outputId to set
   */
  public void setOutputId(String outputId)
  {
    this.outputId = outputId;
  }

  /**
   * Add an IData object to use as an input to the service
   * 
   * @param idataTask
   *          the WmIDataTask that contains the service input.
   */
  public void addConfiguredIData(WmIData idataTask)
  {
    idataTask.perform();
    input = idataTask.getIData();
  }

  /**
   * Add an Output Property object for extracting the output results from the service call.
   * 
   * @param idataTask
   *          the WmIDataTask that contains the service input.
   */
  public void addConfiguredProperty(WmProperty propertyTask)
  {
    propertyTask.perform();
    properties.add(propertyTask);
  }

  protected void prepare() throws BuildException
  {
    checkRequiredStringAttribute(serverAttributeName, getServer());
    checkRequiredNumericalAttribute(portAttributeName, getPort());
    checkRequiredStringAttribute(userAttributeName, getUser());
    checkRequiredStringAttribute(passwordAttributeName, getPassword());
    checkRequiredStringAttribute(serviceNameAttributeName, getService());
    checkRequiredStringAttribute(serviceFolderAttributeName, getFolder());
  }

  protected Context getContext() throws BuildException
  {
    String remoteServer = getServer();
    if (remoteServer.indexOf(":") < 0)
    {
      remoteServer = remoteServer + ":" + getPort();
    }
    try
    {
      return ContextManager.getContext(getProject(), remoteServer, getUser(), getPassword());
    }
    catch (Exception e)
    {
      throw new BuildException(e);
    }   
  }
  
  protected IData invoke() throws BuildException
  {
    try
    {
      return invoke(getFolder(), getService(), input);
    }
    catch (Exception e)
    {
      throw new BuildException(e);
    }
  }

  protected IData invoke(String folder, String service, IData data) throws BuildException
  {
    try
    {
      return getContext().invoke(folder, service, data);      
    }
    catch (Exception e)
    {
      throw new BuildException(e);
    }
  }

  /**
   * The implementation of the execute method from the Task super class.
   */
  public void execute() throws BuildException
  {
    prepare();
    output = invoke();
    Project project = getProject();
    project.addReference(getOutputId(), output);
    assignProperties();
  }

  private void assignProperties()
  {
    JXPathContext xpathContext = IDataJXPathContext.newContext(output);
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
   * Get a property for the project
   * 
   * @param property
   *          the name of the property
   * @return the value of the project property
   */
  private String getProperty(String property, String defaultValue)
  {
    Project project = getProject();
    String value = defaultValue;
    if (project != null && project.getProperties().containsKey(property))
    {
      value = project.getProperty(property);
    }

    return value;
  }
}