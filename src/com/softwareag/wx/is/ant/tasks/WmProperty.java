package com.softwareag.wx.is.ant.tasks;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Implements an Ant Task for adding an output property to an Service task
 */
public class WmProperty extends Task
{
    private String name;
    private String xpath;
    
    /*
     * Implementation of the execute method that does the actual work.
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
            throw new BuildException("You must specify name for the property");
        }
        if (xpath == null)
        {
            throw new BuildException("You must specify xpath for the property");
        }
    }
    

    /**
     * Get the name of the output property
     * @return Returns the name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Set the name of the output property
     * @param name the name to set.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Get the value of the xpath
     * @return returns the value.
     */
    public String getXpath()
    {
        return xpath;
    }

    /**
     * Set the value of the xpath
     * @param value the value to set.
     */
    public void setXpath(String xpath)
    {
        this.xpath = xpath;
    }
}