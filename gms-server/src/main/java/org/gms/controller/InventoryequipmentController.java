package org.gms.controller;

import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.gms.constants.api.ApiConstant;

import org.gms.model.dto.*;
import org.gms.service.InventoryequipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/inventory")
@Log4j2
public class InventoryequipmentController {

    @Autowired
    private InventoryequipmentService inventoryequipmentService;


    @Tag(name = "/inventory/" + ApiConstant.LATEST)
    @Operation(summary = "背包分页展示")
    @PostMapping("/" + ApiConstant.LATEST + "/getInventoryList")
    public ResultBody<Page<InventoryequipmentRtnDTO>> getInvertList(@RequestBody SubmitBody<InventoryequipmentReqDTO> request) {

        // 参数校验
        if (request == null || request.getData() == null) {
            return ResultBody.error("Invalid request data.");
        }

        try {
            // 调用服务层
            Page<InventoryequipmentRtnDTO> result = inventoryequipmentService.getDropList(request.getData());
            return ResultBody.success(result);
        } catch (Exception e) {
            // 异常处理
            log.error("Error occurred while getting inventory list: ", e);
            return ResultBody.error("Failed to fetch inventory list.");
        }


    }
}




