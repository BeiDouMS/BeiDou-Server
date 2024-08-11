package org.gms.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.gms.constants.api.ApiConstant;
import org.gms.model.dto.EquipmentInfoReqDTO;
import org.gms.model.dto.GiveResourceReqDTO;
import org.gms.model.dto.ResultBody;
import org.gms.model.dto.SubmitBody;
import org.gms.service.GiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/search")
public class EquipController {
    @Autowired
    private final GiveService giveService;


    @Tag(name = "/search/" + ApiConstant.LATEST)
    @Operation(summary = "查询装备基础属性信息")
    @PostMapping("/" + ApiConstant.LATEST + "/getEquInitialInfo")
    public ResultBody<Object> getEquInfoById(@RequestBody SubmitBody<EquipmentInfoReqDTO> submitBody) {
        return giveService.getEquipmentInfoById(submitBody.getData());
    }
}