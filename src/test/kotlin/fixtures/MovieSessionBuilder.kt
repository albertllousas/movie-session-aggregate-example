package fixtures

import cinema.domain.Booking
import cinema.domain.BookingRef
import cinema.domain.MovieId
import cinema.domain.MovieSession
import cinema.domain.MovieSessionId
import cinema.domain.ScreenId
import cinema.domain.Seat
import cinema.domain.Showtime
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

object MovieSessionBuilder {

    fun build(
        id: MovieSessionId = MovieSessionId(UUID.randomUUID()),
        showTime: Showtime = Showtime(LocalDate.now(), LocalTime.now()),
        movieId: MovieId = MovieId(UUID.randomUUID()),
        screenId: ScreenId = ScreenId(UUID.randomUUID()),
        bookings: List<Booking> = emptyList(),
        seatingPlan: List<Seat> = emptyList(),
        aggregateVersion: Long = 0,
        generateLocalBookingRef: () -> BookingRef = MovieSession.Companion::generateLocalBookingRef
    ) = MovieSession(id, showTime, movieId, screenId, seatingPlan, bookings, generateLocalBookingRef, aggregateVersion)
}
