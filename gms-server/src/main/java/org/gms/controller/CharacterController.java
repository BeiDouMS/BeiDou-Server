package org.gms.controller;

import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.gms.constants.api.ApiConstant;
import org.gms.dao.entity.ExtendValueDO;
import org.gms.model.dto.ChrOnlineListReqDTO;
import org.gms.model.dto.ChrOnlineListRtnDTO;
import org.gms.model.dto.ResultBody;
import org.gms.model.dto.SubmitBody;
import org.gms.service.CharacterService;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/character")
public class CharacterController {
    private final CharacterService characterService;

    @Tag(name = "/character/" + ApiConstant.LATEST)
    @Operation(summary = "调整玩家个人倍率，extendName为：expRate | mesoRate | dropRate")
    @PostMapping("/" + ApiConstant.LATEST + "/updateRate")
    public ResultBody<Object> updateRate(@RequestBody SubmitBody<ExtendValueDO> submitBody) {
        characterService.updateRate(submitBody.getData());
        return ResultBody.success();
    }


    @Tag(name = "/character/" + ApiConstant.LATEST)
    @Operation(summary = "重置玩家个人倍率，extendName为：expRate | mesoRate | dropRate")
    @PostMapping("/" + ApiConstant.LATEST + "/resetRate")
    public ResultBody<Object> resetRate(@RequestBody SubmitBody<ExtendValueDO> submitBody) {
        characterService.resetRate(submitBody.getData());
        return ResultBody.success();
    }

    @Tag(name = "/character/" + ApiConstant.LATEST)
    @Operation(summary = "重置玩家个人所有倍率")
    @GetMapping("/" + ApiConstant.LATEST + "/resetRates")
    public ResultBody<Object> resetRates(@RequestBody SubmitBody<ExtendValueDO> submitBody) {
        characterService.resetRates(submitBody.getData());
        return ResultBody.success();
    }

    @Tag(name = "/character/" + ApiConstant.LATEST)
    @Operation(summary = "查询在线玩家列表")
    @PostMapping("/" + ApiConstant.LATEST + "/online/list")
    public ResultBody<Page<ChrOnlineListRtnDTO>> onlineList(@RequestBody SubmitBody<ChrOnlineListReqDTO> submitBody) {
        return ResultBody.success(characterService.getChrOnlineList(submitBody.getData()));
    }
}
