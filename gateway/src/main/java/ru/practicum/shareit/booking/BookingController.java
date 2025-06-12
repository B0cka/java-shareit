package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {

	private final BookingClient bookingClient;

	@PatchMapping("/{bookingId}")
	public ResponseEntity<Object> updateStatus(@RequestHeader("X-Sharer-User-Id") long userId,
											   @PathVariable("bookingId") long bookingId,
											   @RequestParam(name = "approved") boolean approved) {
		log.info("Updating booking status, bookingId={}, userId={}, approved={}", bookingId, userId, approved);
		return bookingClient.updateStatus(userId, bookingId, approved);
	}

	@GetMapping
	public ResponseEntity<Object> getBookings(@RequestHeader("X-Sharer-User-Id") long userId,
											  @RequestParam(name = "state", defaultValue = "ALL") String stateParam,
											  @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
											  @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
		BookingState state = BookingState.from(stateParam)
				.orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
		log.info("Get bookings, state={}, userId={}, from={}, size={}", stateParam, userId, from, size);
		return bookingClient.getBookings(userId, state, from, size);
	}

	@PostMapping
	public ResponseEntity<Object> bookItem(@RequestHeader("X-Sharer-User-Id") long userId,
										   @RequestBody BookItemRequestDto requestDto) {
		log.info("Create booking, userId={}, dto={}", userId, requestDto);
		return bookingClient.bookItem(userId, requestDto);
	}

	@GetMapping("/{bookingId}")
	public ResponseEntity<Object> getBooking(@RequestHeader("X-Sharer-User-Id") long userId,
											 @PathVariable Long bookingId) {
		log.info("Get booking, bookingId={}, userId={}", bookingId, userId);
		return bookingClient.getBooking(userId, bookingId);
	}

	@GetMapping("/owner")
	public ResponseEntity<Object> getOwnerBookings(@RequestHeader("X-Sharer-User-Id") long userId,
												   @RequestParam(name = "state", defaultValue = "ALL") String stateParam,
												   @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
												   @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
		BookingState state = BookingState.from(stateParam)
				.orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
		log.info("Get owner bookings, state={}, ownerId={}, from={}, size={}", stateParam, userId, from, size);
		return bookingClient.getOwnerBookings(userId, state, from, size);
	}
}
