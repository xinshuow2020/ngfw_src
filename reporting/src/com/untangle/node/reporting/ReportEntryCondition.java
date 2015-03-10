/**
 * $Id: ReportEntryCondition.java,v 1.00 2015/02/27 19:23:29 dmorris Exp $
 */
package com.untangle.node.reporting;

import java.io.Serializable;

import org.json.JSONObject;
import org.json.JSONString;
import org.apache.log4j.Logger;

/**
 * A SQL condition (clause) for limiting results of a ReportEntry
 */
@SuppressWarnings("serial")
public class ReportEntryCondition implements Serializable, JSONString
{
    private String column;
    private String value;
    private String operator;
    
    public String getColumn() { return this.column; }
    public void setColumn( String newValue ) { this.column = newValue; }

    public String getValue() { return this.value; }
    public void setValue( String newValue ) { this.value = newValue; }

    public String getOperator() { return this.operator; }
    public void setOperator( String newValue )
    {
        switch ( newValue ) {
        case "=":
        case "!=":
        case "<>":
        case ">":
        case "<":
        case ">=":
        case "<=":
        case "BETWEEN":
        case "between":
        case "LIKE":
        case "like":
        case "IN":
        case "in":
            break;
        default:
            throw new RuntimeException("Unknown SQL condition operator: " + newValue);
        }
        this.operator = newValue;
    }
    
    public String toJSONString()
    {
        JSONObject jO = new JSONObject(this);
        return jO.toString();
    }
}