package org.gms.extension.runtime;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcTransactionCoordinatorTest {

    @Test
    void commitsOnlyAfterWorkCompletes() throws Exception {
        Connection connection = connection();
        JdbcTransactionCoordinator coordinator = new JdbcTransactionCoordinator(() -> connection);

        assertEquals(Integer.valueOf(7), coordinator.execute(ignored -> 7));

        var order = inOrder(connection);
        order.verify(connection).setAutoCommit(false);
        order.verify(connection).setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
        order.verify(connection).commit();
        order.verify(connection).setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
        order.verify(connection).setAutoCommit(true);
        order.verify(connection).close();
    }

    @Test
    void metadataFailureRollsBackAndDoesNotCommit() throws Exception {
        Connection connection = connection();
        JdbcTransactionCoordinator coordinator = new JdbcTransactionCoordinator(() -> connection);

        SQLException failure = assertThrows(SQLException.class,
                () -> coordinator.execute(ignored -> {
                    throw new SQLException("metadata failed");
                }));

        assertEquals("metadata failed", failure.getMessage());
        verify(connection).rollback();
        verify(connection, org.mockito.Mockito.never()).commit();
    }

    private static Connection connection() throws SQLException {
        Connection connection = mock(Connection.class);
        when(connection.getAutoCommit()).thenReturn(true);
        when(connection.getTransactionIsolation()).thenReturn(Connection.TRANSACTION_REPEATABLE_READ);
        return connection;
    }
}
