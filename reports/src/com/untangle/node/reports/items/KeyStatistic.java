/**
 * $Id$
 */
package com.untangle.node.reports.items;

import java.io.Serializable;

@SuppressWarnings("serial")
public class KeyStatistic implements Serializable
{
    private final String label;
    private final Object value;
    private final String unit;
    private final String linkType;

    public KeyStatistic(String label, Object value, String unit, String linkType)
    {
        this.label = label;
        this.value = value;
        this.unit = unit;
        this.linkType = linkType;
    }

    public String getLabel()
    {
        return label;
    }

    public Object getValue()
    {
        return value;
    }

    public String getUnit()
    {
        return unit;
    }

    public String getLinkType()
    {
        return linkType;
    }

    public String toString()
    {
        return "[KeyStatistic label: " + label + " value: " + value
            + " unit: " + unit + "]";
    }
}