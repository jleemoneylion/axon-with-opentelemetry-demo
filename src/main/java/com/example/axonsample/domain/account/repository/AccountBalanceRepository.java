package com.example.axonsample.domain.account.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public class AccountBalanceRepository {
    private final JdbcTemplate jdbcTemplate;

    public AccountBalanceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createAccountBalance(String accountId) {
        String sql = " INSERT INTO account_balances (id, balance) VALUES (?, ?) ";

        jdbcTemplate.execute(sql, (PreparedStatementCallback<Integer>) ps -> {
            ps.setString(1, accountId);
            ps.setBigDecimal(2, BigDecimal.ZERO);
            return ps.executeUpdate();
        });
    }

    public void updateAccountBalance(String accountId, BigDecimal balanceDelta) {
        String sql = " UPDATE account_balances" +
                " SET balance = balance+?::money " +
                " WHERE id = ? ";

        jdbcTemplate.execute(sql, (PreparedStatementCallback<Integer>) ps -> {
            ps.setBigDecimal(1, balanceDelta);
            ps.setString(2, accountId);
            return ps.executeUpdate();
        });
    }

    public BigDecimal getAccountBalance(String accountId) {
        String sql = " SELECT balance from account_balances WHERE id = ? ";
        return jdbcTemplate.query(sql, (rs, i) -> rs.getBigDecimal("balance"), accountId)
                .stream()
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }
}
