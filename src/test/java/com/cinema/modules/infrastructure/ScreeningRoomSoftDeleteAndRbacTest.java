package com.cinema.modules.infrastructure;

import com.cinema.common.constant.RoleConstant;
import com.cinema.model.Role;
import com.cinema.model.ScreeningRoom;
import com.cinema.model.User;
import com.cinema.modules.infrastructure.dao.CinemaDAO;
import com.cinema.modules.infrastructure.dao.ScreeningRoomDAO;
import com.cinema.modules.infrastructure.service.ScreeningRoomService;
import com.cinema.modules.infrastructure.service.impl.ScreeningRoomServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Unit Tests for Soft Delete Cinema, Room & RBAC (DungBD Infrastructure)")
class ScreeningRoomSoftDeleteAndRbacTest {

    private ScreeningRoomService service;
    private User adminUser;
    private User managerCinema1;
    private User managerCinema2;

    @BeforeEach
    void setUp() {
        service = new ScreeningRoomServiceImpl();

        Role adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setRoleName(RoleConstant.ADMIN);

        Role managerRole = new Role();
        managerRole.setId(2L);
        managerRole.setRoleName(RoleConstant.MANAGER);

        adminUser = User.builder()
                .email("admin@cinema.com")
                .roleId(1L)
                .role(adminRole)
                .build();
        adminUser.setId(1L);

        managerCinema1 = User.builder()
                .email("manager.hn@cinema.com")
                .roleId(2L)
                .cinemaId(1L)
                .role(managerRole)
                .build();
        managerCinema1.setId(2L);

        managerCinema2 = User.builder()
                .email("manager.sg@cinema.com")
                .roleId(2L)
                .cinemaId(2L)
                .role(managerRole)
                .build();
        managerCinema2.setId(3L);
    }

    @Test
    @DisplayName("deleteCinema: Fails with UNAUTHORIZED when actor is not ADMIN")
    void testDeleteCinema_NonAdmin_Fails() {
        assertEquals("UNAUTHORIZED", service.deleteCinema(null, 1L));
        assertEquals("UNAUTHORIZED", service.deleteCinema(managerCinema1, 1L));
    }

    @Test
    @DisplayName("deleteCinema: Fails with NOT_FOUND when cinemaId is null")
    void testDeleteCinema_NullId_Fails() {
        assertEquals("NOT_FOUND", service.deleteCinema(adminUser, null));
    }

    @Test
    @DisplayName("deleteRoom: Fails with UNAUTHORIZED when actor is null")
    void testDeleteRoom_NullActor_Fails() {
        assertEquals("UNAUTHORIZED", service.deleteRoom(null, 1L));
    }

    @Test
    @DisplayName("deleteRoom: Fails with NOT_FOUND when roomId is null or room does not exist")
    void testDeleteRoom_NotFound_Fails() {
        assertEquals("NOT_FOUND", service.deleteRoom(adminUser, null));
        assertEquals("NOT_FOUND", service.deleteRoom(adminUser, 99999999L));
    }

    @Test
    @DisplayName("deleteRoom: Fails with FORBIDDEN_BRANCH when Manager deletes room of another cinema")
    void testDeleteRoom_ManagerDifferentBranch_Forbidden() throws Exception {
        // Mock ScreeningRoomDAO to return a room belonging to Cinema 2
        ScreeningRoomDAO mockDao = new ScreeningRoomDAO() {
            @Override
            public ScreeningRoom findById(Long id) {
                ScreeningRoom r = new ScreeningRoom();
                r.setId(id);
                r.setCinemaId(2L); // Cinema 2
                return r;
            }
        };

        ScreeningRoomServiceImpl testService = new ScreeningRoomServiceImpl();
        Field field = ScreeningRoomServiceImpl.class.getDeclaredField("screeningRoomDAO");
        field.setAccessible(true);
        field.set(testService, mockDao);

        // Manager of Cinema 1 tries to delete room in Cinema 2 -> FORBIDDEN_BRANCH
        String result = testService.deleteRoom(managerCinema1, 10L);
        assertEquals("FORBIDDEN_BRANCH", result);
    }

    @Test
    @DisplayName("deleteCinema: Fails with ACTIVE_SHOWTIMES when cinema has future showtimes")
    void testDeleteCinema_ActiveShowtimes_Fails() throws Exception {
        CinemaDAO mockCinemaDao = new CinemaDAO() {
            @Override
            public boolean hasFutureShowtimes(Long cinemaId) {
                return true; // Simulate cinema having upcoming showtimes
            }
        };

        ScreeningRoomServiceImpl testService = new ScreeningRoomServiceImpl();
        Field field = ScreeningRoomServiceImpl.class.getDeclaredField("cinemaDAO");
        field.setAccessible(true);
        field.set(testService, mockCinemaDao);

        String result = testService.deleteCinema(adminUser, 1L);
        assertEquals("ACTIVE_SHOWTIMES", result);
    }

    @Test
    @DisplayName("deleteRoom: Fails with ACTIVE_SHOWTIMES when room has future showtimes")
    void testDeleteRoom_ActiveShowtimes_Fails() throws Exception {
        ScreeningRoomDAO mockDao = new ScreeningRoomDAO() {
            @Override
            public ScreeningRoom findById(Long id) {
                ScreeningRoom r = new ScreeningRoom();
                r.setId(id);
                r.setCinemaId(1L);
                return r;
            }

            @Override
            public boolean hasFutureShowtimes(Long roomId) {
                return true; // Has future showtimes
            }
        };

        ScreeningRoomServiceImpl testService = new ScreeningRoomServiceImpl();
        Field field = ScreeningRoomServiceImpl.class.getDeclaredField("screeningRoomDAO");
        field.setAccessible(true);
        field.set(testService, mockDao);

        String result = testService.deleteRoom(adminUser, 10L);
        assertEquals("ACTIVE_SHOWTIMES", result);

        // Same for Manager on their own branch with active showtimes
        String mgrResult = testService.deleteRoom(managerCinema1, 10L);
        assertEquals("ACTIVE_SHOWTIMES", mgrResult);
    }
}
