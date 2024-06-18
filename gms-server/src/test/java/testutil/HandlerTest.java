package testutil;

import client.Character;
import client.Client;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;

import static org.mockito.Mockito.lenient;

public abstract class HandlerTest {
    protected static final int ACCOUNT_ID = 1702;

    @Mock
    protected Client client;

    @Mock
    protected Character chr;

    @BeforeEach
    void prepareClient() {
        lenient().when(client.getAccID()).thenReturn(ACCOUNT_ID);
        lenient().when(client.getPlayer()).thenReturn(chr);
    }
}
