package ru.practicum.shareit.bookings;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.NoAccessException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoShort;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoShort;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class)
@AutoConfigureMockMvc
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingControllerTest {

    @MockBean
    BookingService bookingService;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    MockMvc mockMvc;

    UserDto tipUser;
    UserDto owner;
    BookingResponseDto bookingResponseDto;
    ItemDto item;
    ItemDtoShort itemShort;
    UserDtoShort userDtoShort;
    List<BookingResponseDto> bookings = new ArrayList<>();

    @BeforeEach
    void setUp() {
        owner = UserDto.builder()
                .id(12L)
                .name("хозяин")
                .email("1234@gmail.ru")
                .build();

        tipUser = UserDto.builder()
                .id(21L)
                .name("user")
                .email("user@gmail.ru")
                .build();

        item = ItemDto.builder()
                .id(10L)
                .name("дрель")
                .description("дрель обычная")
                .available(true)
                .build();

        itemShort = ItemDtoShort.builder()
                .id(item.getId())
                .name(item.getName())
                .build();

        userDtoShort = UserDtoShort.builder()
                .id(tipUser.getId())
                .build();

        bookingResponseDto = BookingResponseDto.builder()
                .id(1L)
                .start(LocalDateTime.of(2027, 11, 30, 7, 22, 11))
                .end(LocalDateTime.of(2029, 11, 3, 11, 30, 10))
                .status(BookingStatus.WAITING)
                .item(itemShort)
                .booker(userDtoShort)
                .build();

        bookings = List.of(bookingResponseDto);
    }

    @DisplayName("Create booking")
    @Test
    void createBookingTest() throws Exception {

        BookingRequestDto newBooking = BookingRequestDto.builder()
                .start(LocalDateTime.of(2027, 11, 30, 7, 22, 11))
                .end(LocalDateTime.of(2029, 11, 3, 11, 30, 10))
                .itemId(10L)
                .build();

        when(bookingService.addBooking(12L, newBooking)).thenReturn(bookingResponseDto);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 12L)
                        .content(mapper.writeValueAsString(newBooking))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(bookingResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(bookingResponseDto.getStart().toString())))
                .andExpect(jsonPath("$.end", is(bookingResponseDto.getEnd().toString())))
                .andExpect(jsonPath("$.status", is(bookingResponseDto.getStatus().toString())));
    }

    @DisplayName("update booking")
    @Test
    void updateStatusBookingTest() throws Exception {
        BookingResponseDto updatedBooking = BookingResponseDto.builder()
                .id(1L)
                .start(LocalDateTime.of(2025, 10, 3, 11, 30, 10))
                .end(LocalDateTime.of(2025, 11, 3, 11, 30, 10))
                .status(BookingStatus.APPROVED)
                .item(itemShort)
                .booker(userDtoShort)
                .build();

        when(bookingService.approveBooking(any(Long.class), any(Long.class), any(Boolean.class)))
                .thenReturn(updatedBooking);

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 2L)
                        .param("approved", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(updatedBooking.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(updatedBooking.getStart().toString())))
                .andExpect(jsonPath("$.end", is(updatedBooking.getEnd().toString())))
                .andExpect(jsonPath("$.status", is(updatedBooking.getStatus().toString())));
    }

    @DisplayName("get booking by id")
    @Test
    void getBookingByIdTest() throws Exception {
        when(bookingService.getBooking(2L, 1L)).thenReturn(bookingResponseDto);

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 2L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(bookingResponseDto.getStart().toString())))
                .andExpect(jsonPath("$.end", is(bookingResponseDto.getEnd().toString())))
                .andExpect(jsonPath("$.status", is(bookingResponseDto.getStatus().toString())));
    }

    @DisplayName("Receiving a non-existent booking")
    @Test
    void findNonExistentBooking() throws Exception {
        long userId = 111L;
        long bookingId = 777L;

        when(bookingService.getBooking(userId, bookingId))
                .thenThrow(new NotFoundException("Booking not found"));

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Booking not found")));
    }

    @DisplayName("get booking for non-existent user")
    @Test
    void findBookingsForNonExistentUser() throws Exception {
        long userId = 888L;

        when(bookingService.getUserBookings(userId, "ALL"))
                .thenThrow(new NotFoundException("Booking not found"));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Booking not found")));
    }

    @DisplayName("Status update without access rights")
    @Test
    void updateStatusWithoutAccessTest() throws Exception {
        long userId = 222L;
        long bookingId = 333L;

        when(bookingService.approveBooking(userId, bookingId, true))
                .thenThrow(new NoAccessException("Access denied"));

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .param("approved", "true")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("Access denied")));
    }

}

