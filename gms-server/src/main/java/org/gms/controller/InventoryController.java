package org.gms.controller;

import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.gms.constants.api.ApiConstant;
import org.gms.model.dto.*;
import org.gms.service.InventoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/inventory")
public class InventoryController {
    private final InventoryService inventoryService;

    @Tag(name = "/inventory/" + ApiConstant.LATEST)
    @Operation(summary = "获取所有背包分类")
    @GetMapping("/" + ApiConstant.LATEST + "/getInventoryTypeList")
    public ResultBody<List<InventoryTypeRtnDTO>> getInventoryTypeList() {
        return ResultBody.success(inventoryService.getInventoryTypeList());
    }

    @Tag(name = "/inventory/" + ApiConstant.LATEST)
    @Operation(summary = "根据条件获取背包玩家列表")
    @PostMapping("/" + ApiConstant.LATEST + "/getCharacterList")
    public ResultBody<Page<InventorySearchReqDTO>> getCharacterList(@RequestBody SubmitBody<InventorySearchReqDTO> request) {
        return ResultBody.success(inventoryService.getCharacterList(request.getData()));
    }

    @Tag(name = "/inventory/" + ApiConstant.LATEST)
    @Operation(summary = "获取指定玩家背包分类下的所有物品")
    @PostMapping("/" + ApiConstant.LATEST + "/getInventoryList")
    public ResultBody<List<InventorySearchRtnDTO>> getInventoryList(@RequestBody SubmitBody<InventorySearchReqDTO> request) {
        return ResultBody.success(inventoryService.getInventoryList(request.getData()));
    }

    @Tag(name = "/inventory/" + ApiConstant.LATEST)
    @Operation(summary = "根据条件修改玩家背包")
    @PostMapping("/" + ApiConstant.LATEST + "/updateInventory")
    public ResultBody<Object> updateInventory(@RequestBody SubmitBody<InventorySearchRtnDTO> request) {
        inventoryService.updateInventory(request.getData());
        return ResultBody.success();
    }
    @Tag(name = "/inventory/" + ApiConstant.LATEST)
    @Operation(summary = "根据条件删除玩家背包")
    @PostMapping("/" + ApiConstant.LATEST + "/deleteInventory")
    public ResultBody<Object> deleteInventory(@RequestBody SubmitBody<InventorySearchRtnDTO> request) {
        inventoryService.deleteInventory(request.getData());
        return ResultBody.success();
    }
}
