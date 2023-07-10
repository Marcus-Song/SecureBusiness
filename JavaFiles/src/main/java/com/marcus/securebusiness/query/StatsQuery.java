package com.marcus.securebusiness.query;

public class StatsQuery {
    public static final String STATS_QUERY =
            "SELECT (SELECT COUNT(*) FROM customer) AS total_customers, (SELECT COUNT(*) FROM invoice) AS total_invoices, (SELECT ROUND(SUM(total)) FROM invoice) AS total_billed;";
}
