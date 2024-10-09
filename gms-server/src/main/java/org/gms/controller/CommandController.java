package org.gms.controller;


import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.gms.constants.api.ApiConstant;
import org.gms.dao.entity.CommandInfoDO;
import org.gms.model.dto.*;
import org.gms.service.CommandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/command")
public class CommandController {

    @Autowired
    private final CommandService commandService;



    @Tag(name = "/command/" + ApiConstant.LATEST)
    @Operation(summary = "查询命令库所有指令与状态")
    @PostMapping("/" + ApiConstant.LATEST + "/getCommandListFromDB")
    public ResultBody<Page<CommandInfoDO>> getCommandListFromDB(@RequestBody SubmitBody<CommandReqDTO> submitBody) {
        return ResultBody.success(commandService.getCommandListFromDB(submitBody.getData()));
    }

    @Tag(name = "/command/" + ApiConstant.LATEST)
    @Operation(summary = "更新命令库所有指令与状态")
    @PostMapping("/" + ApiConstant.LATEST + "/updateCommand")
    public ResultBody<CommandInfoDO> updateCommand(@RequestBody SubmitBody<CommandReqDTO> submitBody) {
        return ResultBody.success(commandService.updateCommand(submitBody.getData()));
    }

    @Tag(name = "/command/" + ApiConstant.LATEST)
    @Operation(summary = "新增命令库所有指令与状态")
    @PostMapping("/" + ApiConstant.LATEST + "/insertCommand")
    public ResultBody<CommandInfoDO> insertCommandInfo(@RequestBody SubmitBody<CommandReqDTO> submitBody) {
        return ResultBody.success(commandService.insertCommandInfo(submitBody.getData()));
    }

}