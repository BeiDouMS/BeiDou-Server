package org.gms.controller;

import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.gms.constants.api.ApiConstant;
import org.gms.model.dto.*;
import org.gms.service.DropService;
import org.gms.util.I18nUtil;
import org.gms.util.RequireUtil;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/drop")
public class DropController {
    private final DropService dropService;

    @Tag(name = "/drop/" + ApiConstant.LATEST)
    @Operation(summary = "分页获取掉落列表")
    @PostMapping("/" + ApiConstant.LATEST + "/getDropList")
    public ResultBody<Page<DropSearchRtnDTO>> getDropList(@RequestBody SubmitBody<DropSearchReqDTO> request) {
        return ResultBody.success(request, dropService.getDropList(request.getData(), false));
    }

    @Tag(name = "/drop/" + ApiConstant.LATEST)
    @Operation(summary = "分页获取全局掉落列表")
    @PostMapping("/" + ApiConstant.LATEST + "/getGlobalDropList")
    public ResultBody<Page<DropSearchRtnDTO>> getGlobalDropList(@RequestBody SubmitBody<DropSearchReqDTO> request) {
        return ResultBody.success(request, dropService.getDropList(request.getData(), true));
    }

    @Tag(name = "/drop/" + ApiConstant.LATEST)
    @Operation(summary = "新增掉落，返回新增id")
    @PutMapping("/" + ApiConstant.LATEST + "/addDropData")
    public ResultBody<Long> addDropData(@RequestBody SubmitBody<DropSearchRtnDTO> request) {
        request.getData().setId(null);
        return ResultBody.success(request, dropService.modifyDropData(request.getData(), false, false));
    }

    @Tag(name = "/drop/" + ApiConstant.LATEST)
    @Operation(summary = "新增全局掉落，返回新增id")
    @PutMapping("/" + ApiConstant.LATEST + "/addGlobalDropData")
    public ResultBody<Long> addGlobalDropData(@RequestBody SubmitBody<DropSearchRtnDTO> request) {
        request.getData().setId(null);
        return ResultBody.success(request, dropService.modifyDropData(request.getData(), true, false));
    }

    @Tag(name = "/drop/" + ApiConstant.LATEST)
    @Operation(summary = "根据id更新掉落信息")
    @PostMapping("/" + ApiConstant.LATEST + "/updateDropData")
    public ResultBody<Object> updateDropData(@RequestBody SubmitBody<DropSearchRtnDTO> request) {
        RequireUtil.requireNotNull(request.getData().getId(), I18nUtil.getExceptionMessage("PARAMETER_SHOULD_NOT_NULL", "id"));
        dropService.modifyDropData(request.getData(), false, false);
        return ResultBody.success(request, null);
    }

    @Tag(name = "/drop/" + ApiConstant.LATEST)
    @Operation(summary = "根据id更新全局掉落信息")
    @PostMapping("/" + ApiConstant.LATEST + "/updateGlobalDropData")
    public ResultBody<Object> updateGlobalDropData(@RequestBody SubmitBody<DropSearchRtnDTO> request) {
        RequireUtil.requireNotNull(request.getData().getId(), I18nUtil.getExceptionMessage("PARAMETER_SHOULD_NOT_NULL", "id"));
        dropService.modifyDropData(request.getData(), true, false);
        return ResultBody.success(request, null);
    }

    @Tag(name = "/drop/" + ApiConstant.LATEST)
    @Operation(summary = "根据id删除掉落信息")
    @DeleteMapping("/" + ApiConstant.LATEST + "/deleteDropData/{id}")
    public ResultBody<Object> deleteDropData(@PathVariable("id") Long id) {
        dropService.modifyDropData(DropSearchRtnDTO.builder().id(id).build(), false, true);
        return ResultBody.success(null);
    }

    @Tag(name = "/drop/" + ApiConstant.LATEST)
    @Operation(summary = "根据id删除全局掉落信息")
    @DeleteMapping("/" + ApiConstant.LATEST + "/deleteGlobalDropData/{id}")
    public ResultBody<Object> deleteGlobalDropData(@PathVariable("id") Long id) {
        dropService.modifyDropData(DropSearchRtnDTO.builder().id(id).build(), true, true);
        return ResultBody.success(null);
    }
}
