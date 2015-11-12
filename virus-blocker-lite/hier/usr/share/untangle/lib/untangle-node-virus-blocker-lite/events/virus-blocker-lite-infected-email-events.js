{
    "category": "Virus Blocker Lite",
    "conditions": [
        {
            "column": "addr_kind",
            "javaClass": "com.untangle.node.reports.SqlCondition",
            "operator": "=",
            "value": "B"
        },
        {
            "column": "virus_blocker_lite_clean",
            "javaClass": "com.untangle.node.reports.SqlCondition",
            "operator": "is",
            "value": "FALSE"
        }
    ],
    "defaultColumns": ["time_stamp","hostname","username","addr","sender","virus_blocker_lite_clean","virus_blocker_lite_name","s_server_addr","s_server_port"],
    "description": "Infected email sessions blocked by Virus Blocker Lite.",
    "displayOrder": 21,
    "javaClass": "com.untangle.node.reports.EventEntry",
    "table": "mail_addrs",
    "title": "Infected Email Events",
    "uniqueId": "virus-blocker-lite-44TQDYOWVL"
}