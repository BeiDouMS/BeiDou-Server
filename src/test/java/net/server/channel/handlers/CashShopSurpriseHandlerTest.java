package net.server.channel.handlers;

import client.inventory.Item;
import constants.id.ItemId;
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

    @Test
    void shouldDoNothingWhenCsIsNotOpened() {
        when(cashShop.isOpened()).thenReturn(false);

        handler.handlePacket(Packets.emptyInPacket(), client);

        verify(cashShop, never()).openCashShopSurprise();
    }

    @Test
    void shouldSendFailurePacketWhenFailToOpen() {
        when(cashShop.isOpened()).thenReturn(true);
        when(cashShop.openCashShopSurprise()).thenReturn(Optional.empty());

        handler.handlePacket(Packets.emptyInPacket(), client);

        verify(client).sendPacket(PacketCreator.onCashItemGachaponOpenFailed());
    }

    @Test
    void shouldSendSuccessPacketWhenSuccessfullyOpen() {
        when(cashShop.isOpened()).thenReturn(true);
        Item cashShopSurprise = Items.itemWithQuantity(ItemId.CASH_SHOP_SURPRISE, 3);
        Item reward = Items.itemWithQuantity(5000012, 1);
        when(cashShop.openCashShopSurprise()).thenReturn(Optional.of(new CashShop.CashShopSurpriseResult(
                cashShopSurprise, reward)));

        handler.handlePacket(Packets.emptyInPacket(), client);

        verify(client).sendPacket(PacketCreator.onCashGachaponOpenSuccess(ACCOUNT_ID, cashShopSurprise.getCashId(), 3,
                reward, 5000012, 1, true));
    }
}
