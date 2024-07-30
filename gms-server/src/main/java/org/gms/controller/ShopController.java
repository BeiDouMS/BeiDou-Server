package org.gms.controller;

import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.gms.constants.api.ApiConstant;
import org.gms.model.dto.*;
import org.gms.service.ShopService;
import org.gms.util.I18nUtil;
import org.gms.util.RequireUtil;
import org.springframework.web.bind.annotation.*;

/**
 * 后面如有商城相关，叫cashShop
 */
@RestController
@AllArgsConstructor
@RequestMapping("/shop")
public class ShopController {
    private final ShopService shopService;

    @Tag(name = "/shop/" + ApiConstant.LATEST)
    @Operation(summary = "分页获取商店列表")
    @PostMapping("/" + ApiConstant.LATEST + "/getShopList")
    public ResultBody<Page<ShopSearchRtnDTO>> getShopList(@RequestBody SubmitBody<ShopSearchReqDTO> request) {
        return ResultBody.success(request, shopService.getShopList(request.getData()));
    }

    @Tag(name = "/shop/" + ApiConstant.LATEST)
    @Operation(summary = "根据商店id分页获取商品列表")
    @PostMapping("/" + ApiConstant.LATEST + "/getShopItemList")
    public ResultBody<Page<ShopItemSearchRtnDTO>> getShopItemList(@RequestBody SubmitBody<ShopSearchReqDTO> request) {
        return ResultBody.success(request, shopService.getShopItemList(request.getData()));
    }

    @Tag(name = "/shop/" + ApiConstant.LATEST)
    @Operation(summary = "根据id查询商品信息")
    @GetMapping("/" + ApiConstant.LATEST + "/getShopItem/{id}")
    public ResultBody<ShopItemSearchRtnDTO> getShopItem(@PathVariable("id") Long id) {
        return ResultBody.success(shopService.getShopItem(id));
    }

    @Tag(name = "/shop/" + ApiConstant.LATEST)
    @Operation(summary = "新增商品信息，返回新增的商品id")
    @PutMapping("/" + ApiConstant.LATEST + "/addShopItem")
    public ResultBody<Long> addShopItem(@RequestBody SubmitBody<ShopItemSearchRtnDTO> request) {
        request.getData().setId(null);
        return ResultBody.success(request, shopService.modifyShopItem(request.getData(), false));
    }

    @Tag(name = "/shop/" + ApiConstant.LATEST)
    @Operation(summary = "根据id更新商品信息")
    @PostMapping("/" + ApiConstant.LATEST + "/updateShopItem")
    public ResultBody<Object> updateShopItem(@RequestBody SubmitBody<ShopItemSearchRtnDTO> request) {
        RequireUtil.requireNotNull(request.getData().getId(), I18nUtil.getExceptionMessage("PARAMETER_SHOULD_NOT_NULL", "id"));
        shopService.modifyShopItem(request.getData(), false);
        return ResultBody.success(request, null);
    }

    @Tag(name = "/shop/" + ApiConstant.LATEST)
    @Operation(summary = "根据id删除商品信息")
    @DeleteMapping("/" + ApiConstant.LATEST + "/deleteShopItem/{id}")
    public ResultBody<Object> deleteShopItem(@PathVariable("id") Long id) {
        shopService.modifyShopItem(ShopItemSearchRtnDTO.builder().id(id).build(), true);
        return ResultBody.success(null);
    }
}
