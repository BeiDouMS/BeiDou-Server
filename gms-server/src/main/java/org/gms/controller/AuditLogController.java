package org.gms.controller;

import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.gms.constants.api.ApiConstant;
import org.gms.dao.entity.AuditLogDO;
import org.gms.model.dto.AuditLogSearchReqDTO;
import org.gms.model.dto.ResultBody;
import org.gms.model.dto.SubmitBody;
import org.gms.service.AuditLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/auditlog")
public class AuditLogController {
    private final AuditLogService auditLogService;

    @Tag(name = "/auditlog/" + ApiConstant.LATEST)
    @Operation(summary = "分页获取审计日志")
    @PostMapping("/" + ApiConstant.LATEST + "/list")
    public ResultBody<Page<AuditLogDO>> list(@RequestBody SubmitBody<AuditLogSearchReqDTO> request) {
        return ResultBody.success(request, auditLogService.search(request.getData()));
    }

    @Operation(summary = "获取可搜索操作项")
    @GetMapping("/" + ApiConstant.LATEST + "/operation/options")
    public ResultBody<List<String>> operationOptions() {
        return ResultBody.success(auditLogService.operationOptions());
    }
}
