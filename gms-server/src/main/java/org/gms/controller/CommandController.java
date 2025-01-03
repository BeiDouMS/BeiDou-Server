package org.gms.controller;


import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.gms.constants.api.ApiConstant;
import org.gms.dao.entity.CommandInfoDO;
import org.gms.model.dto.*;
import org.gms.service.CommandService;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/command")
public class CommandController {
    private final CommandService commandService;

    @Tag(name = "/command/" + ApiConstant.LATEST)
    @Operation(summary = "查询命令库所有指令与状态")
    @PostMapping("/" + ApiConstant.LATEST + "/getCommandListFromDB")
    public ResultBody<Page<CommandReqDTO>> getCommandListFromDB(@RequestBody SubmitBody<CommandReqDTO> submitBody) {
        return ResultBody.success(commandService.getCommandListFromDB(submitBody.getData()));
    }

    @Tag(name = "/command/" + ApiConstant.LATEST)
    @Operation(summary = "更新命令库所有指令与状态")
    @PostMapping("/" + ApiConstant.LATEST + "/updateCommand")
    public ResultBody<CommandInfoDO> updateCommand(@RequestBody SubmitBody<CommandReqDTO> submitBody) {
        return ResultBody.success(commandService.updateCommand(submitBody.getData()));
    }

    //重载事件
    @Tag(name = "/command/" + ApiConstant.LATEST)
    @Operation(summary = "复用GM命令代码进行重载事件")
    @GetMapping("/" + ApiConstant.LATEST + "/reloadEventsByGMCommand")
    public ResultBody reloadEventsByGMCommand() {
        commandService.reloadEventsByGMCommand();
        return ResultBody.success();
    }
    //重装传送点
    @Tag(name = "/command/" + ApiConstant.LATEST)
    @Operation(summary = "复用GM命令代码进行重装传送点")
    @GetMapping("/" + ApiConstant.LATEST + "/reloadPortalsByGMCommand")
    public ResultBody reloadPortalsByGMCommand() {
        commandService.reloadPortalsByGMCommand();
        return ResultBody.success();
    }

    //重装地图
    @Tag(name = "/command/" + ApiConstant.LATEST)
    @Operation(summary = "复用GM命令代码进行重装地图")
    @GetMapping("/" + ApiConstant.LATEST + "/reloadMapsByGMCommand")
    public ResultBody reloadMapsByGMCommand() {
        commandService.reloadMapsByGMCommand();
        return ResultBody.success();
    }


}