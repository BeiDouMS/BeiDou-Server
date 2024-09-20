package org.gms.controller;

import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.gms.constants.api.ApiConstant;
import org.gms.dao.entity.GachaponRewardDO;
import org.gms.dao.entity.GachaponRewardPoolDO;
import org.gms.model.dto.*;
import org.gms.service.GachaponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/gachapon")
public class GachaponController {
    @Autowired
    private GachaponService gachaponService;

    @Tag(name = "/gachapon/" + ApiConstant.LATEST)
    @Operation(summary = "获取奖池列表")
    @PostMapping("/" + ApiConstant.LATEST + "/getPools")
    public ResultBody<Page<GachaponPoolSearchRtnDTO>> getPools(@RequestBody SubmitBody<GachaponPoolSearchReqDTO> request) {
        return ResultBody.success(request, gachaponService.getPools(request.getData()));
    }

    @Tag(name = "/gachapon/" + ApiConstant.LATEST)
    @Operation(summary = "创建或更新奖池")
    @PostMapping("/" + ApiConstant.LATEST + "/updatePool")
    public ResultBody<Object> updatePool(@RequestBody SubmitBody<GachaponRewardPoolDO> request) {
        gachaponService.updatePool(request.getData());
        return ResultBody.success();
    }

    @Tag(name = "/gachapon/" + ApiConstant.LATEST)
    @Operation(summary = "删除奖池")
    @PostMapping("/" + ApiConstant.LATEST + "/deletePool")
    public ResultBody<Object> deletePool(@RequestBody SubmitBody<GachaponRewardPoolDO> request) {
        gachaponService.deletePool(request.getData().getId());
        return ResultBody.success();
    }

    @Tag(name = "/gachapon/" + ApiConstant.LATEST)
    @Operation(summary = "获取奖品列表")
    @PostMapping("/" + ApiConstant.LATEST + "/getRewards")
    public ResultBody<List<GachaponRewardDO>> getRewards(@RequestBody SubmitBody<GachaponRewardPoolDO> request) {
        return ResultBody.success(gachaponService.getRewards(request.getData().getId()));
    }

    @Tag(name = "/gachapon/" + ApiConstant.LATEST)
    @Operation(summary = "创建或更新奖品")
    @PostMapping("/" + ApiConstant.LATEST + "/updateReward")
    public ResultBody<Object> updateReward(@RequestBody SubmitBody<GachaponRewardDO> request) {
        gachaponService.updateReward(request.getData());
        return ResultBody.success();
    }

    @Tag(name = "/gachapon/" + ApiConstant.LATEST)
    @Operation(summary = "删除奖品")
    @PostMapping("/" + ApiConstant.LATEST + "/deleteReward")
    public ResultBody<Object> deleteReward(@RequestBody SubmitBody<GachaponRewardDO> request) {
        gachaponService.deleteReward(request.getData().getId());
        return ResultBody.success();
    }
}
