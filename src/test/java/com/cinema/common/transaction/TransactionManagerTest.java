package com.cinema.common.transaction;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for TransactionManager (Nested ACID Transaction Coordination)")
class TransactionManagerTest {

    @Mock
    private Connection mockConnection;

    @BeforeEach
    void setUp() {
        TransactionManager.setTestConnection(mockConnection);
    }

    @AfterEach
    void tearDown() {
        TransactionManager.clearTestConnection();
    }

    @Test
    @DisplayName("Should commit transaction successfully on root completion")
    void testSingleTransactionCommit() throws SQLException {
        String result = TransactionManager.executeInTransaction(conn -> {
            assertSame(mockConnection, conn);
            return "RESULT_OK";
        });

        assertEquals("RESULT_OK", result);
        verify(mockConnection, times(1)).setAutoCommit(false);
        verify(mockConnection, times(1)).commit();
        verify(mockConnection, never()).rollback();
    }

    @Test
    @DisplayName("Should support nested transactions: only root commits once")
    void testNestedTransactionExecution() throws SQLException {
        String outerResult = TransactionManager.executeInTransaction(outerConn -> {
            assertSame(mockConnection, outerConn);

            // Gọi lồng Transaction thứ hai bên trong (Nested level 2)
            String innerResult = TransactionManager.executeInTransaction(innerConn -> {
                assertSame(mockConnection, innerConn);
                return "INNER_OK";
            });

            return "OUTER_" + innerResult;
        });

        assertEquals("OUTER_INNER_OK", outerResult);
        // Chỉ commit đúng 1 lần duy nhất tại Root!
        verify(mockConnection, times(1)).commit();
        verify(mockConnection, never()).rollback();
    }

    @Test
    @DisplayName("Should rollback transaction cleanly when any error occurs")
    void testTransactionRollbackOnException() throws SQLException {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            TransactionManager.executeInTransaction(conn -> {
                throw new IllegalStateException("Cố tình ném lỗi kiểm thử Rollback!");
            });
        });

        assertTrue(ex.getMessage().contains("Cố tình ném lỗi kiểm thử Rollback!"));
        verify(mockConnection, times(1)).rollback();
        verify(mockConnection, never()).commit();
    }
}
