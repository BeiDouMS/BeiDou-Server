package org.gms.controller;

import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.gms.constants.api.ApiConstant;
import org.gms.dao.entity.GameConfigDO;
import org.gms.model.dto.ConfigTypeDTO;
import org.gms.model.dto.GameConfigReqDTO;
import org.gms.model.dto.ResultBody;
import org.gms.model.dto.SubmitBody;
import org.gms.service.ConfigService;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/config")
public class ConfigController {
    private final ConfigService configService;

    @Tag(name = "/config/" + ApiConstant.LATEST)
    @Operation(summary = "获取参数大类和参数类型")
    @GetMapping("/" + ApiConstant.LATEST + "/getConfigTypeList")
    public ResultBody<ConfigTypeDTO> getConfigTypeList() {
        return ResultBody.success(configService.getConfigTypeList());
    }

    @Tag(name = "/config/" + ApiConstant.LATEST)
    @Operation(summary = "分页获取参数列表")
    @PostMapping("/" + ApiConstant.LATEST + "/getConfigList")
    public ResultBody<Page<GameConfigDO>> getConfigList(@RequestBody SubmitBody<GameConfigReqDTO> request) {
        return ResultBody.success(request, configService.getConfigList(request.getData()));
    }

    @Tag(name = "/config/" + ApiConstant.LATEST)
    @Operation(summary = "新增参数")
    @PostMapping("/" + ApiConstant.LATEST + "/addConfig")
    public ResultBody<Object> addConfig(@RequestBody SubmitBody<GameConfigDO> request) {
        configService.addConfig(request.getData());
        return ResultBody.success(request, null);
    }

    @Tag(name = "/config/" + ApiConstant.LATEST)
    @Operation(summary = "修改参数")
    @PostMapping("/" + ApiConstant.LATEST + "/updateConfig")
    public ResultBody<Object> updateConfig(@RequestBody SubmitBody<GameConfigDO> request) {
        configService.updateConfig(request.getData());
        return ResultBody.success(request, null);
    }

    @Tag(name = "/config/" + ApiConstant.LATEST)
    @Operation(summary = "删除参数")
    @DeleteMapping("/" + ApiConstant.LATEST + "/deleteConfig/{id}")
    public ResultBody<Object> deleteConfig(@PathVariable("id") Long id) {
        configService.deleteConfig(id);
        return ResultBody.success(null);
    }

    @Tag(name = "/config/" + ApiConstant.LATEST)
    @Operation(summary = "批量删除参数")
    @PostMapping("/" + ApiConstant.LATEST + "/deleteConfigList")
    public ResultBody<Object> deleteConfigList(@RequestBody SubmitBody<List<Long>> request) {
        configService.deleteConfigList(request.getData());
        return ResultBody.success(null);
    }

    @Tag(name = "/config/" + ApiConstant.LATEST)
    @Operation(summary = "从yml导入参数")
    @PostMapping(value = "/" + ApiConstant.LATEST + "/importYml", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResultBody<Object> importYml(@RequestParam("file") MultipartFile file) {
        return ResultBody.success(configService.importYml(file));
    }

    @Tag(name = "/config/" + ApiConstant.LATEST)
    @Operation(summary = "从yml导入参数")
    @GetMapping("/" + ApiConstant.LATEST + "/exportYml")
    public ResponseEntity<Resource> exportYml() {
        return configService.exportYml();
    }
}
