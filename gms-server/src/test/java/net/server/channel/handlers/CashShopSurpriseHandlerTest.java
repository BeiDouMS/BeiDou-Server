package net.server.channel.handlers;

import client.inventory.Item;
import constants.id.ItemId;
import net.packet.InPacket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import server.CashShop;
import testutil.HandlerTest;
import testutil.Items;
import testutil.Packets;
import tools.PacketCreator;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CashShopSurpriseHandlerTest extends HandlerTest {
    private final CashShopSurpriseHandler handler = new CashShopSurpriseHandler();

    @Mock
    private CashShop cashShop;

    @BeforeEach
    void prepareCashShop() {
        when(chr.getCashShop()).thenReturn(cashShop);
    }

    private InPacket useCashShopSurprisePacket(long cashId) {
        return Packets.buildInPacket(out -> out.writeLong(cashId));
    }

    @Test
    void shouldDoNothingWhenCsIsNotOpened() {
        when(cashShop.isOpened()).thenReturn(false);

        handler.handlePacket(useCashShopSurprisePacket(123), client);

        verify(cashShop, never()).openCashShopSurprise(anyLong());
    }

    @Test
    void shouldSendFailurePacketWhenFailToOpen() {
        when(cashShop.isOpened()).thenReturn(true);
        when(cashShop.openCashShopSurprise(anyLong())).thenReturn(Optional.empty());

        handler.handlePacket(useCashShopSurprisePacket(456), client);

        verify(client).sendPacket(PacketCreator.onCashItemGachaponOpenFailed());
    }

    @Test
    void shouldSendSuccessPacketWhenSuccessfullyOpen() {
        when(cashShop.isOpened()).thenReturn(true);
        Item cashShopSurprise = Items.itemWithQuantity(ItemId.CASH_SHOP_SURPRISE, 3);
        Item reward = Items.itemWithQuantity(5000012, 1);
        when(cashShop.openCashShopSurprise(789)).thenReturn(Optional.of(new CashShop.CashShopSurpriseResult(
                cashShopSurprise, reward)));

        handler.handlePacket(useCashShopSurprisePacket(789), client);

        verify(client).sendPacket(PacketCreator.onCashGachaponOpenSuccess(ACCOUNT_ID, cashShopSurprise.getCashId(), 3,
                reward, 5000012, 1, true));
    }
}
