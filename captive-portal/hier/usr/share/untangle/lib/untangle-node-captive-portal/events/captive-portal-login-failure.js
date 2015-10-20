{
    "category": "Captive Portal",
    "conditions": [
        {
            "column": "event_info",
            "javaClass": "com.untangle.node.reports.SqlCondition",
            "operator": "=",
            "value": "FAILED"
        }
    ],
    "defaultColumns": ["time_stamp","client_addr","login_name","event_info","auth_type"],
    "description": "Failed logins to Captive Portal.",
    "displayOrder": 22,
    "javaClass": "com.untangle.node.reports.EventEntry",
    "table": "captive_portal_user_events",
    "title": "Login Failure User Events",
    "uniqueId": "captive-portal-YQ42F59W9G"
}