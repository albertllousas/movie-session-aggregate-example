package cinema.domain

import arrow.core.left
import arrow.core.right
import fixtures.MovieSessionBuilder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID


class MovieSessionShould {

    private val seatingPlan = listOf(
        Seat(col = 1, row = 1), Seat(col = 2, row = 1),
        Seat(col = 1, row = 2), Seat(col = 2, row = 2),
        Seat(col = 1, row = 3), Seat(col = 2, row = 3)
    )

    private val staticBookRef = BookingRef("54321")

    @Test
    fun `create a new movie session from scratch`() {
        val id = MovieSessionId(UUID.randomUUID())
        val showTime = Showtime(LocalDate.now(), LocalTime.now())
        val movieId = MovieId(UUID.randomUUID())
        val screenId = ScreenId(UUID.randomUUID())

        val result = MovieSession.new(
            id = id,
            showTime = showTime,
            movieId = movieId,
            screenId = screenId,
            seatingPlan = seatingPlan
        )

        assertThat(result).isEqualTo(
            MovieSession(
                id = id,
                showTime = showTime,
                movieId = movieId,
                screenId = screenId,
                seatingPlan = seatingPlan,
                bookings = emptyList(),
                generateLocalBookingRef = MovieSession::generateLocalBookingRef,
                aggregateVersion = 1L
            )
        )
    }

    @Test
    fun `book multiple seats`() {
        val movieSession = MovieSessionBuilder.build(
            generateLocalBookingRef = { staticBookRef },
            seatingPlan = seatingPlan
        )
        val customerId = CustomerId(UUID.randomUUID())
        val seats = listOf(Seat(col = 1, row = 1), Seat(col = 2, row = 1))

        val result = movieSession.book(customerId, seats)

        assertThat(result).isEqualTo(
            Pair(
                movieSession.copy(
                    bookings = movieSession.bookings + Booking.reconstitute(
                        staticBookRef,
                        customerId,
                        seats
                    )
                ),
                staticBookRef
            ).right()
        )
    }

    @Test
    fun `fail booking seats when seats are already booked`() {
        val customerId = CustomerId(UUID.randomUUID())
        val movieSession = MovieSessionBuilder.build(
            generateLocalBookingRef = { staticBookRef },
            seatingPlan = seatingPlan,
            bookings = listOf(
                Booking.reconstitute(
                    bookingRef = staticBookRef,
                    customerId = CustomerId(UUID.randomUUID()),
                    seats = listOf(Seat(col = 1, row = 1))
                )
            )
        )
        val seats = listOf(Seat(col = 1, row = 1), Seat(col = 2, row = 1))

        val result = movieSession.book(customerId, seats)

        assertThat(result).isEqualTo(SeatsAlreadyBooked(listOf(Seat(col = 1, row = 1))).left())
    }

    @Test
    fun `fail booking seats when seats do not exist in the screen plan`() {
        val customerId = CustomerId(UUID.randomUUID())
        val movieSession = MovieSessionBuilder.build(
            generateLocalBookingRef = { staticBookRef },
            seatingPlan = seatingPlan,
            bookings = listOf(
                Booking.reconstitute(
                    bookingRef = staticBookRef,
                    customerId = CustomerId(UUID.randomUUID()),
                    seats = listOf(Seat(col = 1, row = 1))
                )
            )
        )
        val seats = listOf(Seat(col = 4, row = 15), Seat(col = 0, row = -1))

        val result = movieSession.book(customerId, seats)

        assertThat(result).isEqualTo(SeatsDoNotExist(seats).left())
    }
}
