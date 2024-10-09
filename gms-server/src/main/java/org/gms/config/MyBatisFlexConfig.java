package org.gms.config;

import com.mybatisflex.core.audit.AuditManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyBatisFlexConfig{

    private static final Logger logger = LoggerFactory
            .getLogger("mybatis-flex-sql");


    public MyBatisFlexConfig() {
        //开启审计功能,常态为注释状态,需要时,解除注释状态即可
        //AuditManager.setAuditEnable(true);

        //设置 SQL 审计收集器
        AuditManager.setMessageCollector(auditMessage ->
                logger.info("{},{}ms", auditMessage.getFullSql()
                        , auditMessage.getElapsedTime())
        );
    }
}