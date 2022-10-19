package cinema.domain

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import org.apache.commons.lang3.RandomStringUtils
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

data class MovieSession(
    val id: MovieSessionId,
    val showTime: Showtime,
    val movieId: MovieId,
    val screenId: ScreenId,
    val seatingPlan: List<Seat>,
    val bookings: List<Booking>,
    val generateLocalBookingRef: () -> BookingRef = Companion::generateLocalBookingRef,
    val aggregateVersion: Long
) {

    companion object {

        private const val BookingRefLength = 5

        fun generateLocalBookingRef(): BookingRef = BookingRef(RandomStringUtils.randomAlphanumeric(BookingRefLength))

        fun new(
            id: MovieSessionId,
            showTime: Showtime,
            movieId: MovieId,
            screenId: ScreenId,
            seatingPlan: List<Seat>,
        ) = MovieSession(
            id = id,
            showTime = showTime,
            movieId = movieId,
            screenId = screenId,
            seatingPlan = seatingPlan,
            bookings = emptyList(),
            generateLocalBookingRef = MovieSession::generateLocalBookingRef,
            aggregateVersion = 1
        )
    }

    fun book(customerId: CustomerId, requestedSeats: List<Seat>): Either<BookingError, Pair<MovieSession, BookingRef>> =
        ensureSeatsExist(requestedSeats, seatingPlan)
            .flatMap(::ensureSeatsAreFree)
            .flatMap { createBooking(it, customerId) }


    private fun ensureSeatsAreFree(requestedSeats: List<Seat>) =
        bookings.flatMap(Booking::seats)
            .filter { requestedSeats.contains(it) }
            .let { if (it.isNotEmpty()) SeatsAlreadyBooked(it).left() else requestedSeats.right() }

    private fun ensureSeatsExist(requestedSeats: List<Seat>, seatingPlan: List<Seat>) =
        if (seatingPlan.containsAll(requestedSeats)) requestedSeats.right()
        else requestedSeats.minus(seatingPlan.toSet()).let(::SeatsDoNotExist).left()

    private fun createBooking(requestedSeats: List<Seat>, customerId: CustomerId) =
        Booking.create(generateLocalBookingRef(), customerId, requestedSeats)
            .map { Pair(this.copy(bookings = bookings + it), it.bookingRef) }
}

data class Showtime(val date: LocalDate, val time: LocalTime)

@JvmInline
value class MovieSessionId(val value: UUID)

@JvmInline
value class MovieId(val value: UUID)

@JvmInline
value class ScreenId(val value: UUID)
