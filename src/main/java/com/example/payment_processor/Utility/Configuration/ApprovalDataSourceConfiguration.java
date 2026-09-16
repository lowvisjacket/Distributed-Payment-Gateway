package com.example.payment_processor.Utility.Configuration;

import com.example.payment_processor.Service.ManualApprovalDepositStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
@ConditionalOnProperty(prefix = "approval.datasource", name = "url")
public class ApprovalDataSourceConfiguration {
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    DataSourceProperties mainDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    DataSource dataSource(
            @Qualifier("mainDataSourceProperties") DataSourceProperties mainDataSourceProperties
    ) {
        return mainDataSourceProperties.initializeDataSourceBuilder().build();
    }

    @Bean
    @ConfigurationProperties("approval.datasource")
    DataSourceProperties approvalDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    DataSource approvalDataSource(
            @Qualifier("approvalDataSourceProperties") DataSourceProperties approvalDataSourceProperties
    ) {
        return approvalDataSourceProperties.initializeDataSourceBuilder().build();
    }

    @Bean
    ManualApprovalDepositStore manualApprovalDepositStore(
            @Qualifier("approvalDataSource") DataSource approvalDataSource
    ) {
        return new ManualApprovalDepositStore(new JdbcTemplate(approvalDataSource));
    }
}
