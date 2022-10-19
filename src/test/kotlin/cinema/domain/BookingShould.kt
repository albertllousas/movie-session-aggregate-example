package cinema.domain

import arrow.core.left
import arrow.core.right
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class BookingShould {

    @Test
    fun `create a booking`() {
        val bookingRef = BookingRef("12345")
        val customerId = CustomerId(UUID.randomUUID())
        val seats = listOf(Seat(1, 1))
        val result = Booking.create(
            bookingRef, customerId, seats
        )

        assertThat(result).isEqualTo(Booking.reconstitute(bookingRef, customerId, seats).right())
    }

    @Test
    fun `fail booking empty seats`() {
        val result = Booking.create(
            BookingRef("12345"), CustomerId(UUID.randomUUID()), emptyList()
        )

        assertThat(result).isEqualTo(SeatsCanNotBeEmpty.left())
    }

    @Test
    fun `fail booking seats when max seats allowed per booking is reached`() {
        val seats = (1..6).map { Seat(1, 1) }

        val result = Booking.create(
            BookingRef("12345"), CustomerId(UUID.randomUUID()), seats, 5
        )

        assertThat(result).isEqualTo(MaxSeatsAllowedPerBookingReached.left())
    }
}