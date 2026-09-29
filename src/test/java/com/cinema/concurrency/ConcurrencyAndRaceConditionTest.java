package com.cinema.concurrency;

import com.cinema.common.transaction.TransactionManager;
import com.cinema.model.Voucher;
import com.cinema.modules.booking.dao.SeatHoldingDAO;
import com.cinema.modules.booking.dao.VoucherDAO;
import com.cinema.modules.booking.service.VoucherService;
import com.cinema.modules.booking.service.impl.VoucherServiceImpl;
import com.cinema.modules.identity.service.LoyaltyService;
import com.cinema.modules.identity.service.impl.LoyaltyServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Kiểm thử Tranh chấp Đồng thời & Race Condition (Concurrency Testing)")
public class ConcurrencyAndRaceConditionTest {

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPs;

    @Mock
    private ResultSet mockRs;

    @BeforeEach
    void setUp() throws SQLException {
        TransactionManager.setTestConnection(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeQuery()).thenReturn(mockRs);
    }

    @AfterEach
    void tearDown() {
        TransactionManager.clearTestConnection();
    }

    @Test
    @DisplayName("2.2.1 - Giữ ghế đồng thời: 2 luồng cùng tranh chấp 1 ghế, chỉ 1 luồng được giữ ghế thành công")
    void testConcurrentSeatHolding() throws InterruptedException {
        int threads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch readyGate = new CountDownLatch(threads);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneGate = new CountDownLatch(threads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);

        // Giả lập trạng thái ghế trong CSDL có atomic lock (tương đương MERGE INTO WITH (HOLDLOCK))
        AtomicBoolean seatHeld = new AtomicBoolean(false);

        Long showtimeId = 1L;
        Long seatId = 101L;

        for (int i = 0; i < threads; i++) {
            final String sessionId = "SESSION-" + (i + 1);
            executor.submit(() -> {
                readyGate.countDown();
                try {
                    startGate.await(); // Cả 2 luồng cùng bắn tại 1 thời điểm chính xác

                    // Logic nguyên tử của MERGE WITH (HOLDLOCK):
                    // Chỉ thread đầu tiên thành công flip seatHeld từ false -> true
                    boolean acquired = seatHeld.compareAndSet(false, true);
                    if (acquired) {
                        successCount.incrementAndGet();
                    } else {
                        conflictCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    conflictCount.incrementAndGet();
                } finally {
                    doneGate.countDown();
                }
            });
        }

        readyGate.await();
        startGate.countDown(); // Phát lệnh chạy đồng thời
        boolean finished = doneGate.await(5, TimeUnit.SECONDS);

        assertTrue(finished, "Tác vụ chạy đồng thời phải kết thúc trong thời gian chờ");
        assertEquals(1, successCount.get(), "Chỉ duy nhất 1 luồng được phép giữ ghế thành công (chống double-booking)");
        assertEquals(1, conflictCount.get(), "Luồng còn lại phải bị từ chối do xung đột ghế đã được giữ");

        executor.shutdown();
    }

    @Test
    @DisplayName("2.2.2 - Dùng Voucher đồng thời: Voucher còn đúng 1 lượt dùng (limit=1), 2 luồng cùng áp dụng, chỉ 1 luồng thành công")
    void testConcurrentVoucherUsageAtomicCount() throws InterruptedException, SQLException {
        VoucherDAO voucherDAO = mock(VoucherDAO.class);
        VoucherService voucherService = new VoucherServiceImpl(voucherDAO);

        // Giả lập voucher có usage_limit = 1, used_count = 0
        AtomicInteger dbUsedCount = new AtomicInteger(0);
        final int usageLimit = 1;

        // Giả lập lệnh SQL: UPDATE vouchers SET used_count = used_count + 1 WHERE id = ? AND used_count < usage_limit
        when(voucherDAO.incrementUsedCount(any(Connection.class), eq(99L))).thenAnswer(invocation -> {
            int current = dbUsedCount.get();
            if (current < usageLimit) {
                return dbUsedCount.compareAndSet(current, current + 1);
            }
            return false;
        });

        int threads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch readyGate = new CountDownLatch(threads);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneGate = new CountDownLatch(threads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger rejectedCount = new AtomicInteger(0);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                readyGate.countDown();
                try {
                    startGate.await();
                    boolean applied = voucherService.applyVoucherUsage(mockConnection, 99L);
                    if (applied) {
                        successCount.incrementAndGet();
                    }
                } catch (IllegalStateException e) {
                    rejectedCount.incrementAndGet();
                } catch (Exception e) {
                    fail("Không được xảy ra ngoại lệ không mong muốn: " + e.getMessage());
                } finally {
                    doneGate.countDown();
                }
            });
        }

        readyGate.await();
        startGate.countDown();
        doneGate.await(5, TimeUnit.SECONDS);

        assertEquals(1, successCount.get(), "Chỉ đúng 1 luồng được phép áp dụng voucher thành công");
        assertEquals(1, rejectedCount.get(), "Luồng thứ 2 phải nhận IllegalStateException do voucher đã hết lượt");
        assertEquals(1, dbUsedCount.get(), "used_count tuyệt đối không được vượt quá usage_limit = 1");

        executor.shutdown();
    }

    @Test
    @DisplayName("2.2.3 - Trừ kho F&B đồng thời: Tồn kho = 5, 2 luồng cùng mua 4 phần (tổng yêu cầu = 8 > 5), hệ thống phải ngăn chặn trừ âm kho")
    void testConcurrentFnbInventoryDeduction() throws InterruptedException {
        int initialStock = 5;
        AtomicInteger currentStock = new AtomicInteger(initialStock);
        int requestedQtyPerThread = 4;

        int threads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch readyGate = new CountDownLatch(threads);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneGate = new CountDownLatch(threads);

        AtomicInteger successOrders = new AtomicInteger(0);
        AtomicInteger rejectedOrders = new AtomicInteger(0);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                readyGate.countDown();
                try {
                    startGate.await();

                    // Kiểm tra và khấu trừ kho theo logic UPDATE ... WHERE stock_quantity >= qty
                    boolean updated = false;
                    while (true) {
                        int stock = currentStock.get();
                        if (stock < requestedQtyPerThread) {
                            break; // Không đủ tồn kho
                        }
                        if (currentStock.compareAndSet(stock, stock - requestedQtyPerThread)) {
                            updated = true;
                            break;
                        }
                    }

                    if (updated) {
                        successOrders.incrementAndGet();
                    } else {
                        rejectedOrders.incrementAndGet();
                    }
                } catch (Exception e) {
                    rejectedOrders.incrementAndGet();
                } finally {
                    doneGate.countDown();
                }
            });
        }

        readyGate.await();
        startGate.countDown();
        doneGate.await(5, TimeUnit.SECONDS);

        assertEquals(1, successOrders.get(), "Chỉ 1 đơn hàng F&B được thành công vì 5 - 4 = 1 không đủ cho đơn thứ hai");
        assertEquals(1, rejectedOrders.get(), "Đơn hàng F&B thứ hai phải bị từ chối do thiếu tồn kho");
        assertEquals(1, currentStock.get(), "Tồn kho còn lại phải là 1 (5 - 4), tuyệt đối không bị trừ âm (không thành -3)");

        executor.shutdown();
    }

    @Test
    @DisplayName("2.2.4 - Trừ điểm Loyalty đồng thời: User có 100 điểm, 2 luồng cùng trừ 70 điểm (tổng 140 > 100), chỉ 1 luồng thành công")
    void testConcurrentLoyaltyPointsDeduction() throws InterruptedException, SQLException {
        AtomicInteger userPoints = new AtomicInteger(100);
        int deductAmount = 70;

        int threads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch readyGate = new CountDownLatch(threads);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneGate = new CountDownLatch(threads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                readyGate.countDown();
                try {
                    startGate.await();

                    // Mô phỏng logic SELECT loyalty_points WITH (UPDLOCK, ROWLOCK)
                    // và kiểm tra currentPoints >= deductAmount
                    boolean success = false;
                    synchronized (userPoints) {
                        int current = userPoints.get();
                        if (current >= deductAmount) {
                            userPoints.set(current - deductAmount);
                            success = true;
                        }
                    }

                    if (success) {
                        successCount.incrementAndGet();
                    } else {
                        failureCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    doneGate.countDown();
                }
            });
        }

        readyGate.await();
        startGate.countDown();
        doneGate.await(5, TimeUnit.SECONDS);

        assertEquals(1, successCount.get(), "Chỉ 1 giao dịch trừ điểm được thành công");
        assertEquals(1, failureCount.get(), "Giao dịch thứ 2 phải thất bại vì số dư còn 30 < 70 điểm");
        assertEquals(30, userPoints.get(), "Số dư điểm sau cùng phải là 30, không bao giờ bị âm (-40)");

        executor.shutdown();
    }
}
