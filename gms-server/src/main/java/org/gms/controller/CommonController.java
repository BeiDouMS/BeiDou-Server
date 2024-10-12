package org.gms.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.gms.constants.api.ApiConstant;
import org.gms.model.dto.*;
import org.gms.model.pojo.InformationSearch;
import org.gms.model.pojo.InformationResult;
import org.gms.service.CommonService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/common")
public class CommonController {
    private final CommonService commonService;


    @Tag(name = "/common/" + ApiConstant.LATEST)
    @Operation(summary = "查询装备基础属性信息")
    @PostMapping("/" + ApiConstant.LATEST + "/getEquipmentInfoByItemId")
    public ResultBody<Object> getEquipmentInfoByItemId(@RequestBody SubmitBody<EquipmentInfoReqDTO> submitBody) {
        return ResultBody.success(commonService.getEquipmentInfoByItemId(submitBody.getData()));
    }

    @Tag(name = "/common/" + ApiConstant.LATEST)
    @Operation(summary = "查询所有世界中当前在线玩家数量")
    @PostMapping("/" + ApiConstant.LATEST + "/getAllWorldsOnlinePlayersCount")
    public ResultBody<Integer> getAllWorldsOnlinePlayersCount(@RequestBody SubmitBody<ServerInfoReqDto> submitBody) {
        return ResultBody.success(commonService.getAllWorldsOnlinePlayersCount(submitBody.getData().getWorldIdList()));
    }

    @Tag(name = "/common/" + ApiConstant.LATEST)
    @Operation(summary = "资料查询，根据id或者name查询对应信息")
    @PostMapping("/" + ApiConstant.LATEST + "/informationSearch")
    public ResultBody<List<InformationResult>> informationSearch(@RequestBody SubmitBody<InformationSearch> submitBody) {
        return ResultBody.success(commonService.getInformation(submitBody.getData()));
    }
}