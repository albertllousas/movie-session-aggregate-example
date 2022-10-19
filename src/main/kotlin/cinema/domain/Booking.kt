package cinema.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import java.util.UUID

data class Booking private constructor(
    val bookingRef: BookingRef,
    val customerId: CustomerId,
    val seats: List<Seat>
) {

    companion object {

        fun create(
            bookingRef: BookingRef,
            customerId: CustomerId,
            seats: List<Seat>,
            maxSeatsAllowedPerBooking: Int = 10
        ): Either<BookingError, Booking> = when {
            seats.size > maxSeatsAllowedPerBooking -> MaxSeatsAllowedPerBookingReached.left()
            seats.isEmpty() -> SeatsCanNotBeEmpty.left()
            else -> Booking(bookingRef, customerId, seats).right()
        }

        fun reconstitute(bookingRef: BookingRef, customerId: CustomerId, seats: List<Seat>) =
            Booking(bookingRef, customerId, seats)
    }
}

@JvmInline
value class BookingRef(val value: String)

@JvmInline
value class CustomerId(val value: UUID)

data class Seat(val col: Int, val row: Int)

