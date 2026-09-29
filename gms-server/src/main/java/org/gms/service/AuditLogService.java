package org.gms.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.gms.dao.entity.AuditLogDO;
import org.gms.dao.mapper.AuditLogMapper;
import org.gms.model.dto.AuditLogSearchReqDTO;
import org.gms.util.I18nUtil;
import org.gms.util.RequireUtil;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.mybatisflex.core.query.QueryMethods.distinct;
import static org.gms.dao.entity.table.AuditLogDOTableDef.AUDIT_LOG_D_O;

@Slf4j
@Service
public class AuditLogService {
    private final AuditLogMapper auditLogMapper;
    private final ExecutorService auditExecutor;

    public AuditLogService(AuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
        this.auditExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "audit-log-writer");
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * 异步落库，审计写入失败仅记录日志，不影响业务请求
     */
    public void asyncSave(AuditLogDO auditLog) {
        auditExecutor.submit(() -> {
            try {
                if (auditLog.getCreatedAt() == null) {
                    auditLog.setCreatedAt(new Timestamp(System.currentTimeMillis()));
                }
                auditLogMapper.insert(auditLog);
            } catch (Exception e) {
                log.error(I18nUtil.getLogMessage("AuditLogService.save.error"), e);
            }
        });
    }

    public Page<AuditLogDO> search(AuditLogSearchReqDTO condition) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .from(AUDIT_LOG_D_O)
                .orderBy(AUDIT_LOG_D_O.CREATED_AT.desc());
        if (!RequireUtil.isEmpty(condition.getOperatorName()))
            queryWrapper.and(AUDIT_LOG_D_O.OPERATOR_NAME.like(condition.getOperatorName()));
        if (!RequireUtil.isEmpty(condition.getUri()))
            queryWrapper.and(AUDIT_LOG_D_O.URI.like(condition.getUri()));
        if (!RequireUtil.isEmpty(condition.getOperation()))
            queryWrapper.and(AUDIT_LOG_D_O.OPERATION.eq(condition.getOperation()));
        if (!RequireUtil.isEmpty(condition.getResult()))
            queryWrapper.and(AUDIT_LOG_D_O.RESULT.eq(condition.getResult()));
        if (!RequireUtil.isEmpty(condition.getStartTime()))
            queryWrapper.and(AUDIT_LOG_D_O.CREATED_AT.ge(condition.getStartTime()));
        if (!RequireUtil.isEmpty(condition.getEndTime()))
            queryWrapper.and(AUDIT_LOG_D_O.CREATED_AT.le(condition.getEndTime()));

        return auditLogMapper.paginate(condition.getPageNo(), condition.getPageSize(), queryWrapper);
    }

    public List<String> operationOptions() {
        List<AuditLogDO> operationList = auditLogMapper.selectListByQuery(
                QueryWrapper.create()
                        .select(distinct(AUDIT_LOG_D_O.OPERATION))
                        .from(AUDIT_LOG_D_O)
                        .orderBy(AUDIT_LOG_D_O.OPERATION.asc())
        );
        return operationList.stream()
                .map(AuditLogDO::getOperation)
                .filter(operation -> !RequireUtil.isEmpty(operation))
                .toList();
    }

    @PreDestroy
    public void destroy() {
        auditExecutor.shutdown();
    }
}
