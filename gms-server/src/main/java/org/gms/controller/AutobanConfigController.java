package org.gms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.gms.constants.api.ApiConstant;
import org.gms.model.dto.AutobanConfigDTO;
import org.gms.model.dto.ResultBody;
import org.gms.model.dto.SubmitBody;
import org.gms.service.AutobanConfigService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 自动封禁配置控制器。
 *
 * @author Nap
 * @since 2026-04-22
 */
@RestController
@AllArgsConstructor
@RequestMapping("/autoban")
public class AutobanConfigController {
    private final AutobanConfigService autobanConfigService;

    @Tag(name = "/autoban/" + ApiConstant.LATEST)
    @Operation(summary = "获取自动封禁配置列表")
    @GetMapping("/" + ApiConstant.LATEST + "/getConfigList")
    public ResultBody<List<AutobanConfigDTO>> getConfigList() {
        return ResultBody.success(autobanConfigService.getConfigList());
    }

    @Tag(name = "/autoban/" + ApiConstant.LATEST)
    @Operation(summary = "更新自动封禁配置")
    @PostMapping("/" + ApiConstant.LATEST + "/updateConfig")
    public ResultBody<Object> updateConfig(@RequestBody SubmitBody<AutobanConfigDTO> request) {
        autobanConfigService.updateConfig(request.getData());
        return ResultBody.success(request, null);
    }
}
