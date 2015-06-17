{
    "uniqueId": "firewall-AsifWhYFae",
    "category": "Firewall",
    "description": "The number of flagged session grouped by client.",
    "displayOrder": 502,
    "enabled": true,
    "javaClass": "com.untangle.node.reporting.ReportEntry",
    "orderByColumn": "value",
    "orderDesc": true,
    "units": "hits",
    "pieGroupColumn": "c_client_addr",
    "pieSumColumn": "count(*)",
    "conditions": [
        {
            "javaClass": "com.untangle.uvm.node.SqlCondition",
            "column": "firewall_blocked",
            "operator": "=",
            "value": "true"
        }
    ],
    "readOnly": true,
    "table": "sessions",
    "title": "Top Blocked Clients",
    "type": "PIE_GRAPH"
}