{
    "uniqueId": "spam-blocker-lite-9uBCmCxM",
    "category": "Spam Blocker Lite",
    "description": "The number of IP addresses sending spam.",
    "displayOrder": 202,
    "enabled": true,
    "javaClass": "com.untangle.node.reporting.ReportEntry",
    "orderByColumn": "value",
    "orderDesc": true,
    "units": "msgs",
    "pieGroupColumn": "c_client_addr",
    "pieSumColumn": "count(*)",
    "conditions": [
        {
            "column": "spam_blocker_lite_is_spam",
            "javaClass": "com.untangle.node.reporting.SqlCondition",
            "operator": "=",
            "value": "true"
        },
        {
            "column": "addr_kind",
            "javaClass": "com.untangle.node.reporting.SqlCondition",
            "operator": "=",
            "value": "B"
        }
    ],
    "readOnly": true,
    "table": "mail_addrs",
    "title": "Top Spam Sender Addresses",
    "type": "PIE_GRAPH"
}