package org.gms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.gms.constants.api.ApiConstant;
import org.gms.exception.BizException;
import org.gms.model.dto.H5CharacterInfoRtnDTO;
import org.gms.model.dto.H5CharacterRtnDTO;
import org.gms.model.dto.H5HangupClaimRtnDTO;
import org.gms.model.dto.H5HangupReqDTO;
import org.gms.model.dto.H5HangupRtnDTO;
import org.gms.model.dto.H5MapDetailRtnDTO;
import org.gms.model.dto.H5MapRtnDTO;
import org.gms.model.dto.H5WhitelistRtnDTO;
import org.gms.model.dto.ResultBody;
import org.gms.model.dto.SubmitBody;
import org.gms.service.H5CharacterInfoService;
import org.gms.service.H5HangupService;
import org.gms.util.I18nUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * H5 挂机收益接口，供独立 H5 后端调用。
 * 使用共享密钥鉴权（请求头 X-H5-Secret），密钥在 application.yml 的 h5.secret 配置。
 *
 * @since 2026-11-01
 */
@RestController
@RequestMapping("/h5")
public class H5Controller {
    private static final String SECRET_HEADER = "X-H5-Secret";

    private final H5HangupService h5HangupService;
    private final H5CharacterInfoService h5CharacterInfoService;
    private final String secret;

    public H5Controller(H5HangupService h5HangupService, H5CharacterInfoService h5CharacterInfoService,
                        @Value("${h5.secret:}") String secret) {
        this.h5HangupService = h5HangupService;
        this.h5CharacterInfoService = h5CharacterInfoService;
        this.secret = secret;
    }

    @Tag(name = "/h5/" + ApiConstant.LATEST)
    @Operation(summary = "账号下角色列表")
    @PostMapping("/" + ApiConstant.LATEST + "/characters")
    public ResultBody<List<H5CharacterRtnDTO>> characters(@RequestHeader(value = SECRET_HEADER, required = false) String secretKey,
                                                          @RequestBody SubmitBody<H5HangupReqDTO> submitBody) {
        checkSecret(secretKey);
        H5HangupReqDTO req = requireBody(submitBody);
        if (req.getAccountId() == null || req.getAccountId() <= 0) {
            throw new BizException(I18nUtil.getExceptionMessage("H5_HANGUP_PARAM_REQUIRED"));
        }
        return ResultBody.success(submitBody, h5HangupService.listCharacters(req.getAccountId()));
    }

    @Tag(name = "/h5/" + ApiConstant.LATEST)
    @Operation(summary = "角色信息：属性点、技能、装备栏、道具栏")
    @PostMapping("/" + ApiConstant.LATEST + "/character/info")
    public ResultBody<H5CharacterInfoRtnDTO> characterInfo(@RequestHeader(value = SECRET_HEADER, required = false) String secretKey,
                                                           @RequestBody SubmitBody<H5HangupReqDTO> submitBody) {
        checkSecret(secretKey);
        H5HangupReqDTO req = requireBody(submitBody);
        return ResultBody.success(submitBody, h5CharacterInfoService.info(requireCharacterId(req)));
    }

    @Tag(name = "/h5/" + ApiConstant.LATEST)
    @Operation(summary = "按地区分类的全部野外地图，附带当前角色收益估算")
    @PostMapping("/" + ApiConstant.LATEST + "/maps")
    public ResultBody<H5MapRtnDTO> maps(@RequestHeader(value = SECRET_HEADER, required = false) String secretKey,
                                        @RequestBody SubmitBody<H5HangupReqDTO> submitBody) {
        checkSecret(secretKey);
        H5HangupReqDTO req = requireBody(submitBody);
        return ResultBody.success(submitBody, h5HangupService.maps(requireCharacterId(req), null));
    }

    @Tag(name = "/h5/" + ApiConstant.LATEST)
    @Operation(summary = "地图详情：刷怪、掉落、收益估算")
    @PostMapping("/" + ApiConstant.LATEST + "/map/detail")
    public ResultBody<H5MapDetailRtnDTO> mapDetail(@RequestHeader(value = SECRET_HEADER, required = false) String secretKey,
                                                   @RequestBody SubmitBody<H5HangupReqDTO> submitBody) {
        checkSecret(secretKey);
        H5HangupReqDTO req = requireBody(submitBody);
        return ResultBody.success(submitBody, h5HangupService.mapDetail(requireCharacterId(req), requireMapId(req)));
    }

    @Tag(name = "/h5/" + ApiConstant.LATEST)
    @Operation(summary = "选择挂机地图")
    @PostMapping("/" + ApiConstant.LATEST + "/map/select")
    public ResultBody<H5CharacterRtnDTO> selectMap(@RequestHeader(value = SECRET_HEADER, required = false) String secretKey,
                                                   @RequestBody SubmitBody<H5HangupReqDTO> submitBody) {
        checkSecret(secretKey);
        H5HangupReqDTO req = requireBody(submitBody);
        return ResultBody.success(submitBody, h5HangupService.selectMapRtn(requireCharacterId(req), requireMapId(req)));
    }

    @Tag(name = "/h5/" + ApiConstant.LATEST)
    @Operation(summary = "地图可掉落物品与白名单状态，不传 mapId 时只返回已启用的物品")
    @PostMapping("/" + ApiConstant.LATEST + "/drops")
    public ResultBody<H5WhitelistRtnDTO> drops(@RequestHeader(value = SECRET_HEADER, required = false) String secretKey,
                                               @RequestBody SubmitBody<H5HangupReqDTO> submitBody) {
        checkSecret(secretKey);
        H5HangupReqDTO req = requireBody(submitBody);
        return ResultBody.success(submitBody,
                h5HangupService.whitelist(requireCharacterId(req), req.getMapId() == null ? 0 : req.getMapId()));
    }

    @Tag(name = "/h5/" + ApiConstant.LATEST)
    @Operation(summary = "保存掉落白名单，勾选数量不设上限")
    @PostMapping("/" + ApiConstant.LATEST + "/whitelist/save")
    public ResultBody<H5WhitelistRtnDTO> saveWhitelist(@RequestHeader(value = SECRET_HEADER, required = false) String secretKey,
                                                       @RequestBody SubmitBody<H5HangupReqDTO> submitBody) {
        checkSecret(secretKey);
        H5HangupReqDTO req = requireBody(submitBody);
        return ResultBody.success(submitBody,
                h5HangupService.saveWhitelist(requireCharacterId(req), req.getItemIds(), req.getMapId() == null ? 0 : req.getMapId()));
    }

    @Tag(name = "/h5/" + ApiConstant.LATEST)
    @Operation(summary = "查询角色挂机状态")
    @PostMapping("/" + ApiConstant.LATEST + "/state")
    public ResultBody<H5HangupRtnDTO.H5HangupCharRtnDTO> state(@RequestHeader(value = SECRET_HEADER, required = false) String secretKey,
                                                               @RequestBody SubmitBody<H5HangupReqDTO> submitBody) {
        checkSecret(secretKey);
        H5HangupReqDTO req = requireBody(submitBody);
        return ResultBody.success(submitBody, h5HangupService.stateSummary(requireCharacterId(req)));
    }

    @Tag(name = "/h5/" + ApiConstant.LATEST)
    @Operation(summary = "开始挂机，锁定地图并开启会话")
    @PostMapping("/" + ApiConstant.LATEST + "/start")
    public ResultBody<H5HangupRtnDTO.H5HangupCharRtnDTO> start(@RequestHeader(value = SECRET_HEADER, required = false) String secretKey,
                                                               @RequestBody SubmitBody<H5HangupReqDTO> submitBody) {
        checkSecret(secretKey);
        H5HangupReqDTO req = requireBody(submitBody);
        return ResultBody.success(submitBody,
                h5HangupService.start(requireCharacterId(req), req.getMapId()));
    }

    @Tag(name = "/h5/" + ApiConstant.LATEST)
    @Operation(summary = "挂机中刷新收益，不停止挂机")
    @PostMapping("/" + ApiConstant.LATEST + "/progress")
    public ResultBody<H5HangupRtnDTO.H5HangupCharRtnDTO> progress(@RequestHeader(value = SECRET_HEADER, required = false) String secretKey,
                                                                  @RequestBody SubmitBody<H5HangupReqDTO> submitBody) {
        checkSecret(secretKey);
        H5HangupReqDTO req = requireBody(submitBody);
        return ResultBody.success(submitBody, h5HangupService.progress(requireCharacterId(req)));
    }

    @Tag(name = "/h5/" + ApiConstant.LATEST)
    @Operation(summary = "结算挂机收益并停止本次挂机，accountId 与 characterId 二选一")
    @PostMapping("/" + ApiConstant.LATEST + "/settle")
    public ResultBody<H5HangupRtnDTO> settle(@RequestHeader(value = SECRET_HEADER, required = false) String secretKey,
                                             @RequestBody SubmitBody<H5HangupReqDTO> submitBody) {
        checkSecret(secretKey);
        H5HangupReqDTO req = requireBody(submitBody);
        return ResultBody.success(submitBody, h5HangupService.settle(req.getAccountId(), req.getCharacterId(), req.getMapId()));
    }

    @Tag(name = "/h5/" + ApiConstant.LATEST)
    @Operation(summary = "领取挂机收益，离线角色的收益会在下次登录端游时自动发放")
    @PostMapping("/" + ApiConstant.LATEST + "/claim")
    public ResultBody<H5HangupClaimRtnDTO> claim(@RequestHeader(value = SECRET_HEADER, required = false) String secretKey,
                                                 @RequestBody SubmitBody<H5HangupReqDTO> submitBody) {
        checkSecret(secretKey);
        H5HangupReqDTO req = requireBody(submitBody);
        return ResultBody.success(submitBody, h5HangupService.claimByCharacterId(requireCharacterId(req)));
    }

    @Tag(name = "/h5/" + ApiConstant.LATEST)
    @Operation(summary = "丢弃待领取战利品中的指定物品")
    @PostMapping("/" + ApiConstant.LATEST + "/reward/discard")
    public ResultBody<H5HangupRtnDTO.H5HangupCharRtnDTO> discardRewardItems(@RequestHeader(value = SECRET_HEADER, required = false) String secretKey,
                                                                            @RequestBody SubmitBody<H5HangupReqDTO> submitBody) {
        checkSecret(secretKey);
        H5HangupReqDTO req = requireBody(submitBody);
        return ResultBody.success(submitBody, h5HangupService.discardItems(requireCharacterId(req), req.getItemIds()));
    }

    private H5HangupReqDTO requireBody(SubmitBody<H5HangupReqDTO> submitBody) {
        H5HangupReqDTO req = submitBody.getData();
        if (req == null) {
            throw new BizException(I18nUtil.getExceptionMessage("H5_HANGUP_PARAM_REQUIRED"));
        }
        return req;
    }

    private int requireCharacterId(H5HangupReqDTO req) {
        if (req.getCharacterId() == null || req.getCharacterId() <= 0) {
            throw new BizException(I18nUtil.getExceptionMessage("H5_HANGUP_CHARACTER_ID_REQUIRED"));
        }
        return req.getCharacterId();
    }

    private int requireMapId(H5HangupReqDTO req) {
        if (req.getMapId() == null || req.getMapId() <= 0) {
            throw new BizException(I18nUtil.getExceptionMessage("H5_HANGUP_PARAM_REQUIRED"));
        }
        return req.getMapId();
    }

    private void checkSecret(String secretKey) {
        if (secret == null || secret.isEmpty()) {
            throw new BizException(I18nUtil.getExceptionMessage("H5_HANGUP_SECRET_NOT_CONFIGURED"));
        }
        if (secretKey == null || !secret.equals(secretKey)) {
            throw new BizException(I18nUtil.getExceptionMessage("H5_HANGUP_SECRET_INVALID"));
        }
    }
}
