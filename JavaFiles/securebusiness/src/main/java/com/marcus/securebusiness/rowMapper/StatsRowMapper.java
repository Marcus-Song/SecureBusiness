package com.marcus.securebusiness.rowMapper;

import com.marcus.securebusiness.model.Role;
import com.marcus.securebusiness.model.Stats;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StatsRowMapper implements RowMapper<Stats> {
    @Override
    public Stats mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        // using SuperBuilder to construct Role, just more elegant
        return Stats.builder()
                .totalCustomers(resultSet.getInt("total_customers"))
                .totalInvoices(resultSet.getInt("total_invoices"))
                .totalBilled(resultSet.getLong("total_billed"))
                .build();
    }
}
