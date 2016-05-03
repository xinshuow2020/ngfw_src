{
    "uniqueId": "bandwidth-control-6b2i2RBPiy",
    "category": "Bandwidth Control",
    "description": "The sum of the data sent grouped by Application Control application.",
    "displayOrder": 602,
    "enabled": true,
    "javaClass": "com.untangle.node.reports.ReportEntry",
    "orderByColumn": "value",
    "orderDesc": true,
    "units": "MB",
    "pieGroupColumn": "application_control_application",
    "pieSumColumn": "round(coalesce(sum(s2p_bytes), 0) / (1024*1024),1)",
    "readOnly": true,
    "table": "sessions",
    "title": "Top Application (by received bytes)",
    "pieStyle": "PIE",
    "type": "PIE_GRAPH"
}
