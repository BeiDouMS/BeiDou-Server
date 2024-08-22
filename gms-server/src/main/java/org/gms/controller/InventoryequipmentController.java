package org.gms.controller;

import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.gms.constants.api.ApiConstant;
import org.gms.dao.entity.InventoryequipmentDO;
import org.gms.model.dto.*;
import org.gms.service.InventoryequipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/invert")
public class InventoryequipmentController {

    @Autowired
    private InventoryequipmentService inventoryequipmentService;


    @Tag(name = "/inverts/" + ApiConstant.LATEST)
    @Operation(summary = "背包分页展示")
    @PostMapping("/" + ApiConstant.LATEST + "/getInvertList")
    public ResultBody<List<InventoryequipmentDTO>>getInvertList(@RequestBody SubmitBody<InventoryequipmentDTO> request) {

        return ResultBody.success(inventoryequipmentService.getDropList(request.getData()));
    }



}
