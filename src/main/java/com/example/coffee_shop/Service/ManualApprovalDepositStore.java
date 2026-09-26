package com.example.payment_processor.Service;

import com.example.payment_processor.Utility.Enum.DepositStatus;
import com.example.payment_processor.Utility.Record.CustomerRecord;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ManualApprovalDepositStore {
    private final JdbcTemplate jdbcTemplate;

    public ManualApprovalDepositStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        jdbcTemplate.execute("""
                create table if not exists manual_deposit_approval (
                    id varchar(36) primary key,
                    idempotency_key varchar(255) not null unique,
                    customer_id varchar(36) not null,
                    amount decimal(19,2) not null,
                    status varchar(40) not null,
                    created_at timestamp not null,
                    processed_at timestamp null
                )
                """);
    }

    public Optional<CustomerRecord.ManualApprovalDeposit> findByIdempotencyKey(String idempotencyKey) {
        List<CustomerRecord.ManualApprovalDeposit> results = jdbcTemplate.query("""
                        select id, idempotency_key, customer_id, amount, status, created_at
                        from manual_deposit_approval where idempotency_key = ?
                        """,
                (rs, rowNum) -> new CustomerRecord.ManualApprovalDeposit(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("idempotency_key"),
                        UUID.fromString(rs.getString("customer_id")),
                        rs.getBigDecimal("amount"),
                        rs.getTimestamp("created_at").toInstant(),
                        status(rs.getString("status"))
                ),
                idempotencyKey);
        return results.stream().findFirst();
    }

    public void queue(CustomerRecord.ManualApprovalDeposit deposit) {
        jdbcTemplate.update("""
                        insert into manual_deposit_approval
                        (id, idempotency_key, customer_id, amount, status, created_at)
                        values (?, ?, ?, ?, 'PENDING_MANUAL_REVIEW', ?)
                        """,
                deposit.id().toString(),
                deposit.idempotencyKey(),
                deposit.customerId().toString(),
                deposit.amount(),
                Timestamp.from(deposit.createdAt()));
    }

    public List<CustomerRecord.ManualApprovalDeposit> findApprovedUnprocessed() {
        return jdbcTemplate.query("""
                        select id, idempotency_key, customer_id, amount, status, created_at
                        from manual_deposit_approval
                        where status = 'APPROVED' and processed_at is null
                        order by created_at asc
                        """,
                (rs, rowNum) -> new CustomerRecord.ManualApprovalDeposit(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("idempotency_key"),
                        UUID.fromString(rs.getString("customer_id")),
                        rs.getBigDecimal("amount"),
                        rs.getTimestamp("created_at").toInstant(),
                        status(rs.getString("status"))
                ));
    }

    public void markProcessed(UUID id) {
        jdbcTemplate.update("""
                        update manual_deposit_approval
                        set status = 'SETTLED', processed_at = ?
                        where id = ? and status = 'APPROVED' and processed_at is null
                        """, Timestamp.from(Instant.now()), id.toString());
    }

    private DepositStatus status(String value) {
        return switch (value) {
            case "PENDING_MANUAL_REVIEW" -> DepositStatus.PENDING_MANUAL_REVIEW;
            case "APPROVED" -> DepositStatus.APPROVED;
            case "SETTLED" -> DepositStatus.SETTLED;
            case "REJECTED" -> DepositStatus.REJECTED;
            default -> throw new IllegalStateException("Unknown manual deposit status: " + value);
        };
    }
}
