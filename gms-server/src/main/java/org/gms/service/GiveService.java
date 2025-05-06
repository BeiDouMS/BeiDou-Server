package org.gms.service;

import lombok.extern.slf4j.Slf4j;
import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.client.Stat;
import org.gms.client.inventory.*;
import org.gms.client.inventory.manipulator.InventoryManipulator;
import org.gms.constants.inventory.ItemConstants;
import org.gms.constants.string.ExtendType;
import org.gms.dao.entity.ExtendValueDO;

import org.gms.model.dto.GiveResourceReqDTO;
import org.gms.exception.BizException;


import org.gms.net.server.Server;
import org.gms.server.CashShop;
import org.gms.server.ItemInformationProvider;
import org.gms.util.I18nUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import static java.util.concurrent.TimeUnit.DAYS;


@Service
@Slf4j
public class GiveService {
    @Autowired
    CharacterService characterService;

    public void give(GiveResourceReqDTO submitData) {
        if (submitData.getPlayerId() == 0) {
            giveAllOnlineChr(submitData);
        } else {
            giveChr(submitData);
        }
    }

    private void giveAllOnlineChr(GiveResourceReqDTO submitData) {
        switch (submitData.getType()) {
            case 0: // nxCredit 点券
            case 1: // nxPrepaid 信用点
            case 2: // maplePoint 抵用券
                int cashType = switch (submitData.getType()) {
                    case 1 -> CashShop.NX_PREPAID;
                    case 2 -> CashShop.MAPLE_POINT;
                    default -> CashShop.NX_CREDIT;
                };
                giveNxAllOnlineChr(submitData.getQuantity(), cashType);
                break;
            case 3: // mesos
                giveMesosAllOnlineChr(submitData.getQuantity());
                break;
            case 4: // exp
                giveExpAllOnlineChr(submitData.getQuantity());
                break;
            case 5: // item
                giveItemAllOnlineChr(submitData.getId(), Short.parseShort(submitData.getQuantity().toString()));
                break;
            case 6: // equip
                giveEquipAllOnlineChr(submitData);
                break;
            // 全服没有设置倍率的操作
            // case 7: // expRate
            // case 8: // mesosRate
            // case 9: // dropRate
            // case 10: // bossRate
            //     String rateType = switch (submitData.getType()) {
            //         case 7 -> "Exp";
            //         case 8 -> "Mesos";
            //         case 9 -> "Drop";
            //         case 10 -> "Boss";
            //         default -> "None";
            //     };
            //     giveRateAllOnlineChr(rateType, submitData.getRate());
            //     break;
        }
    }

    private void giveChr(GiveResourceReqDTO submitData) {
        Integer wId = submitData.getWorldId();
        Integer cId = submitData.getPlayerId();
        if (wId == null || wId < 0 || cId == null || cId < 1) {
            throw new BizException(I18nUtil.getExceptionMessage("CHR_OR_WORLD_ID_ERROR"));
        }
        Character chr = Server.getInstance()
                .getWorlds().get(wId)
                .getPlayerStorage().getCharacterById(cId);
        if (chr == null) throw new BizException(I18nUtil.getExceptionMessage("CHR_OFFLINE"));

        switch (submitData.getType()) {
            case 0: // nxCredit 点券
            case 1: // nxPrepaid 信用点
            case 2: // maplePoint 抵用券
                int cashType = switch (submitData.getType()) {
                    case 1 -> CashShop.NX_PREPAID;
                    case 2 -> CashShop.MAPLE_POINT;
                    default -> CashShop.NX_CREDIT;
                };
                giveNxChr(chr, submitData.getQuantity(), cashType);
                break;
            case 3: // mesos
                giveMesosChr(chr, submitData.getQuantity());
                break;
            case 4: // exp
                giveExpChr(chr, submitData.getQuantity());
                break;
            case 5: // item
                giveItemChr(chr, submitData.getId(), Short.parseShort(submitData.getQuantity().toString()));
                break;
            case 6: // equip
                giveEquipChr(chr, submitData);
                break;
            case 7: // expRate
            case 8: // mesosRate
            case 9: // dropRate
            case 10: // bossRate
                String rateType = switch (submitData.getType()) {
                    case 7 -> "expRate";
                    case 8 -> "mesoRate";
                    case 9 -> "dropRate";
                    default -> "None";
                };
                giveRateChr(chr, rateType, submitData.getRate());
                break;
            case 11:
                giveGMChr(chr, submitData.getQuantity());
                break;
            case 12:
                giveFameChr(chr, submitData.getQuantity());
                break;
            case 13:
                changeMap(chr, submitData.getQuantity());
                break;
        }
    }

    private void giveNxAllOnlineChr(int quantity, int type) {
        Server.getInstance().getWorlds().forEach(world -> world.getPlayerStorage().getAllCharacters().forEach(chr -> {
            doGainCash(chr, type, quantity);
            chr.message(I18nUtil.getMessage("Give.Nx.All", quantity, getCashTypeName(type)));
        }));
        log.info(I18nUtil.getLogMessage("Give.Nx.All.info1", quantity, getCashTypeName(type)));
    }

    private void giveNxChr(Character chr, int quantity, int type) {
        doGainCash(chr, type, quantity);
        chr.message(I18nUtil.getMessage("Give.Nx.Chr", quantity, getCashTypeName(type)));
        log.info(I18nUtil.getLogMessage("Give.Nx.Chr.info1", chr.getId(), chr.getName(), quantity, getCashTypeName(type)));
    }

    private String getCashTypeName(int type) {
        return switch (type) {
            case 1 -> I18nUtil.getMessage("Give.Nx.Type.1");
            case 2 -> I18nUtil.getMessage("Give.Nx.Type.2");
            default -> I18nUtil.getMessage("Give.Nx.Type.default");
        };
    }

    private void giveMesosAllOnlineChr(int quantity) {
        Server.getInstance().getWorlds().forEach(world -> world.getPlayerStorage().getAllCharacters().forEach(chr -> {
            doGainMeso(chr, quantity);
            chr.message(I18nUtil.getMessage("Give.Mesos.All", quantity));
        }));
        log.info(I18nUtil.getLogMessage("Give.Mesos.All.info1", quantity));
    }

    private void giveMesosChr(Character chr, int quantity) {
        doGainMeso(chr, quantity);
        chr.message(I18nUtil.getMessage("Give.Mesos.Chr", quantity));
        log.info(I18nUtil.getLogMessage("Give.Mesos.Chr.info1", chr.getId(), chr.getName(), quantity));
    }

    private void giveExpAllOnlineChr(int quantity) {
        Server.getInstance().getWorlds().forEach(world -> world.getPlayerStorage().getAllCharacters().forEach(chr -> {
            doGainExp(chr, quantity);
            chr.message(I18nUtil.getMessage("Give.Exp.All", quantity));
        }));
        log.info(I18nUtil.getLogMessage("Give.Exp.All.info1", quantity));
    }

    private void giveExpChr(Character chr, int quantity) {
        doGainExp(chr, quantity);
        chr.message(I18nUtil.getMessage("Give.Exp.Chr", quantity));
        log.info(I18nUtil.getLogMessage("Give.Exp.Chr.info1", chr.getId(), chr.getName(), quantity));
    }

    private void giveItemAllOnlineChr(int itemId, short quantity) {
        ItemInformationProvider ii = ItemInformationProvider.getInstance();

        String itemName = ii.getName(itemId);
        if (itemName == null) {
            throw new BizException(I18nUtil.getExceptionMessage("ITEM_NOT_FOUND"));
        }
        if (ItemConstants.getInventoryType(itemId).equals(InventoryType.EQUIP)) {
            throw new BizException(I18nUtil.getExceptionMessage("ONLY_SUPPORT_GIVE_ITEM"));
        }

        boolean isPet = ItemConstants.isPet(itemId);

        long expiration;
        int petId;
        if (isPet) {
            long days = Math.max(1, quantity);
            expiration = System.currentTimeMillis() + DAYS.toMillis(days);
            petId = Pet.createPet(itemId);
        } else {
            expiration = 0;
            petId = 0;
        }

        Server.getInstance().getWorlds().forEach(world -> world.getPlayerStorage().getAllCharacters().forEach(chr -> {
            if (isPet) {
                InventoryManipulator.addById(chr.getClient(), itemId, quantity, "WAdmin", petId, expiration);
                chr.message(I18nUtil.getMessage("Give.Pet.All", quantity, itemName));
            } else {
                InventoryManipulator.addById(chr.getClient(), itemId, quantity, "WAdmin", -1, (short) 0, -1);
                chr.message(I18nUtil.getMessage("Give.Item.All", quantity, itemName));
            }
        }));

        if (isPet) {
            log.info(I18nUtil.getLogMessage("Give.Pet.All.info1", quantity, itemId, itemName));
        } else {
            log.info(I18nUtil.getLogMessage("Give.Item.All.info1", quantity, itemId, itemName));
        }

    }

    private void giveItemChr(Character chr, int itemId, short quantity) {
        ItemInformationProvider ii = ItemInformationProvider.getInstance();

        String itemName = ii.getName(itemId);
        if (itemName == null) {
            throw new BizException(I18nUtil.getExceptionMessage("ITEM_NOT_FOUND"));
        }
        if (ItemConstants.getInventoryType(itemId).equals(InventoryType.EQUIP)) {
            throw new BizException(I18nUtil.getExceptionMessage("ONLY_SUPPORT_GIVE_ITEM"));
        }

        boolean isPet = ItemConstants.isPet(itemId);

        long expiration = 0;
        int petId = 0;
        if (isPet) {
            long days = Math.max(1, quantity);
            expiration = System.currentTimeMillis() + DAYS.toMillis(days);
            petId = Pet.createPet(itemId);
        }

        if (isPet) {
            InventoryManipulator.addById(chr.getClient(), itemId, quantity, "WAdmin", petId, expiration);
            chr.message(I18nUtil.getMessage("Give.Pet.Chr", quantity, itemName));
        } else {
            InventoryManipulator.addById(chr.getClient(), itemId, quantity, "WAdmin", -1, (short) 0, -1);
            chr.message(I18nUtil.getMessage("Give.Item.Chr", quantity, itemName));
        }

        if (isPet) {
            log.info(I18nUtil.getLogMessage("Give.Pet.Chr.info1", chr.getId(), chr.getName(), quantity, itemId, itemName));
        } else {
            log.info(I18nUtil.getLogMessage("Give.Item.Chr.info1", chr.getId(), chr.getName(), quantity, itemId, itemName));
        }
    }

    private void giveEquipAllOnlineChr(GiveResourceReqDTO submitData) {
        ItemInformationProvider ii = ItemInformationProvider.getInstance();

        String itemName = ii.getName(submitData.getId());
        if (ii.getEquipById(submitData.getId()) == null || itemName == null) {
            throw new BizException(I18nUtil.getExceptionMessage("EQUIP_NOT_FOUND"));
        }
        if (!ItemConstants.getInventoryType(submitData.getId()).equals(InventoryType.EQUIP)) {
            throw new BizException(I18nUtil.getExceptionMessage("ONLY_SUPPORT_GIVE_EQUIP"));
        }
        Server.getInstance().getWorlds().forEach(world -> world.getPlayerStorage().getAllCharacters().forEach(chr -> {
            chr.gainEquip(
                    submitData.getId(),
                    submitData.getStr(),
                    submitData.getDex(),
                    submitData.get_int(),
                    submitData.getLuk(),
                    submitData.getHp(),
                    submitData.getMp(),
                    submitData.getPAtk(),
                    submitData.getMAtk(),
                    submitData.getPDef(),
                    submitData.getMDef(),
                    submitData.getAcc(),
                    submitData.getAvoid(),
                    submitData.getHands(),
                    submitData.getSpeed(),
                    submitData.getJump(),
                    submitData.getUpgradeSlot(),
                    submitData.getExpire()
            );
            chr.message(I18nUtil.getMessage("Give.Equip.All", submitData.getId().toString(), itemName));
        }));
        log.info(I18nUtil.getLogMessage("Give.Equip.All.info1",
                submitData.getId(),
                itemName,
                submitData.getStr(),
                submitData.getDex(),
                submitData.get_int(),
                submitData.getLuk(),
                submitData.getHp(),
                submitData.getMp(),
                submitData.getPAtk(),
                submitData.getMAtk(),
                submitData.getPDef(),
                submitData.getMDef(),
                submitData.getAcc(),
                submitData.getAvoid(),
                submitData.getHands(),
                submitData.getSpeed(),
                submitData.getJump(),
                submitData.getUpgradeSlot(),
                submitData.getExpire()
        ));
    }

    private void giveEquipChr(Character chr, GiveResourceReqDTO submitData) {
        ItemInformationProvider ii = ItemInformationProvider.getInstance();

        String itemName = ii.getName(submitData.getId());
        if (itemName == null) {
            throw new BizException(I18nUtil.getExceptionMessage("EQUIP_NOT_FOUND"));
        }

        if (!ItemConstants.getInventoryType(submitData.getId()).equals(InventoryType.EQUIP)) {
            throw new BizException(I18nUtil.getExceptionMessage("ONLY_SUPPORT_GIVE_EQUIP"));
        }
        chr.gainEquip(
                submitData.getId(),
                submitData.getStr(),
                submitData.getDex(),
                submitData.get_int(),
                submitData.getLuk(),
                submitData.getHp(),
                submitData.getMp(),
                submitData.getPAtk(),
                submitData.getMAtk(),
                submitData.getPDef(),
                submitData.getMDef(),
                submitData.getAcc(),
                submitData.getAvoid(),
                submitData.getHands(),
                submitData.getSpeed(),
                submitData.getJump(),
                submitData.getUpgradeSlot(),
                submitData.getExpire()
        );
        chr.message(I18nUtil.getMessage("Give.Equip.Chr", submitData.getId().toString(), itemName));
        log.info(I18nUtil.getLogMessage("Give.Equip.Chr.info1",
                submitData.getId(),
                itemName,
                submitData.getStr(),
                submitData.getDex(),
                submitData.get_int(),
                submitData.getLuk(),
                submitData.getHp(),
                submitData.getMp(),
                submitData.getPAtk(),
                submitData.getMAtk(),
                submitData.getPDef(),
                submitData.getMDef(),
                submitData.getAcc(),
                submitData.getAvoid(),
                submitData.getHands(),
                submitData.getSpeed(),
                submitData.getJump(),
                submitData.getUpgradeSlot(),
                submitData.getExpire(),
                chr.getId(),
                chr.getName()
        ));
    }

    private void giveRateChr(Character chr, String type, float rate) {
        if (rate <= 0) {
            throw new BizException(I18nUtil.getExceptionMessage("PARAMETER_SHOULD_NOT_ZERO", "rate"));
        }
        ExtendValueDO data = ExtendValueDO.builder()
                .extendId(String.valueOf(chr.getId()))
                .extendType(ExtendType.CHARACTER_EXTEND.getType())
                .extendName(type)
                .extendValue(String.valueOf(rate))
                .build();
        characterService.updateRate(data);

        chr.message(I18nUtil.getMessage("Give.Rate.Chr", type, rate));
        log.info(I18nUtil.getLogMessage("Give.Rate.Chr.info1", chr.getId(), chr.getName(), type, rate));
    }

    private void giveGMChr(Character chr, Integer level) {
        if (level < 0  || level > 127) {
            throw new BizException(I18nUtil.getExceptionMessage("ILLEGAL_PARAMETERS",level));
        }
        // 按照以下顺序hide，否则因为没有GM权限而无法hide或unhide
        if (level < 3) {
            chr.hide(false);
            chr.setGMLevel(level);
        } else {
            chr.setGMLevel(level);
            chr.hide(true);
        }
        chr.message(I18nUtil.getMessage("Give.GM.Chr", level));
        log.info(I18nUtil.getLogMessage("Give.GM.Chr.info1", chr.getId(), chr.getName(), level));
    }

    private void giveFameChr(Character chr, Integer fame) {
        chr.setFame(fame);
        chr.updateSingleStat(Stat.FAME, fame);
        chr.message(I18nUtil.getMessage("Give.Fame.Chr", fame));
        log.info(I18nUtil.getLogMessage("Give.Fame.Chr.info1", chr.getId(), chr.getName(), fame));
    }

    private void changeMap(Character chr, Integer mapId) {
        if (910000000 == mapId) {
            chr.saveLocation("FREE_MARKET");
            chr.changeMap(mapId, "out00");
        } else {
            chr.changeMap(mapId);
        }
        chr.message(I18nUtil.getMessage("Give.Map.Chr", mapId));
        log.info(I18nUtil.getLogMessage("Give.Map.Chr.info1", chr.getId(), chr.getName(), mapId));
    }

    private void doGainCash(Character chr, int type, int quantity) {
        int cash = chr.getCashShop().getCash(type);
        long sum = (long) cash + (long) quantity;
        // 禁止点券小于0导致商城错误
        if (sum < 0) {
            quantity = -cash;
        }
        // 禁止点券大于最大值
        if (sum > Integer.MAX_VALUE) {
            quantity = Integer.MAX_VALUE - cash;
        }
        chr.getCashShop().gainCash(type, quantity);
    }

    private void doGainExp(Character chr, int quantity) {
        int exp = chr.getExp();
        long sum = (long) exp + (long) quantity;
        // 最低只能把经验清0
        if (sum < 0) {
            quantity = -exp;
        }
        if (sum > Integer.MAX_VALUE) {
            quantity = Integer.MAX_VALUE - exp;
        }
        chr.gainExp(quantity);
    }

    private void doGainMeso(Character chr, int quantity) {
        int meso = chr.getMeso();
        long sum = (long) meso + (long) quantity;
        if (sum < 0) {
            quantity = -meso;
        }
        if (sum > Integer.MAX_VALUE) {
            quantity = Integer.MAX_VALUE - meso;
        }
        chr.gainMeso(quantity);
    }
}
