package org.gms.controller;

import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.gms.constants.api.ApiConstant;
import org.gms.dao.entity.AutobanLogDO;
import org.gms.model.dto.AutobanLogSearchReqDTO;
import org.gms.model.dto.AutobanLogSummaryReqDTO;
import org.gms.model.dto.AutobanLogSummaryRtnDTO;
import org.gms.model.dto.ResultBody;
import org.gms.model.dto.SubmitBody;
import org.gms.service.AutobanLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 反作弊事件日志控制器。
 *
 * @author Nap
 * @since 2026-09-12
 */
@RestController
@AllArgsConstructor
@RequestMapping("/autoban")
public class AutobanLogController {
    private final AutobanLogService autobanLogService;

    @Tag(name = "/autoban/" + ApiConstant.LATEST)
    @Operation(summary = "分页查询反作弊事件日志")
    @PostMapping("/" + ApiConstant.LATEST + "/getLogList")
    public ResultBody<Page<AutobanLogDO>> getLogList(@RequestBody SubmitBody<AutobanLogSearchReqDTO> request) {
        return ResultBody.success(request, autobanLogService.getLogList(request.getData()));
    }

    @Tag(name = "/autoban/" + ApiConstant.LATEST)
    @Operation(summary = "按角色汇总反作弊事件（问题角色 TOP）")
    @PostMapping("/" + ApiConstant.LATEST + "/getLogSummary")
    public ResultBody<List<AutobanLogSummaryRtnDTO>> getLogSummary(@RequestBody SubmitBody<AutobanLogSummaryReqDTO> request) {
        return ResultBody.success(request, autobanLogService.getSummary(request.getData()));
    }

    @Tag(name = "/autoban/" + ApiConstant.LATEST)
    @Operation(summary = "获取反作弊事件类型选项")
    @GetMapping("/" + ApiConstant.LATEST + "/getLogTypeOptions")
    public ResultBody<List<Map<String, String>>> getLogTypeOptions() {
        return ResultBody.success(autobanLogService.getTypeOptions());
    }
}
