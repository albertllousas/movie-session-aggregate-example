package cinema.application.service

import arrow.core.left
import arrow.core.right
import cinema.domain.Booking
import cinema.domain.BookingRef
import cinema.domain.CustomerId
import cinema.domain.DomainEventPublisher
import cinema.domain.MovieSessionId
import cinema.domain.MovieSessionNotFound
import cinema.domain.MovieSessionRepository
import cinema.domain.Seat
import cinema.domain.SeatsAlreadyBooked
import fixtures.MovieSessionBuilder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.UUID


class BookSeatsForAMovieSessionServiceShould {

    private val movieSessionRepository = mockk<MovieSessionRepository>(relaxed = true)

    private val domainEventPublisher = mockk<DomainEventPublisher>(relaxed = true)

    private val clock = Clock.fixed(Instant.parse("2007-12-03T10:15:30.00Z"), ZoneId.of("UTC"))

    private val bookSeatsServiceForAMovieSession = BookSeatsForAMovieSessionService(
        movieSessionRepository, domainEventPublisher, clock
    )

    @Test
    fun `book seats for a movie session`() {
        val movieSessionId = UUID.randomUUID()
        val customerId = UUID.randomUUID()
        val requestedSeats = listOf(RequestedSeat(1, 1))
        val movieSession = MovieSessionBuilder.build(
            id = MovieSessionId(movieSessionId),
            seatingPlan = listOf(
                Seat(col = 1, row = 1), Seat(col = 2, row = 1), Seat(col = 1, row = 2), Seat(col = 2, row = 2)
            )
        )
        every { movieSessionRepository.find(MovieSessionId(movieSessionId)) } returns movieSession.right()

        val result = bookSeatsServiceForAMovieSession(BookSeatsRequest(movieSessionId, customerId, requestedSeats))

        assertThat(result).isEqualTo(Unit.right())
        verify {
            movieSessionRepository.save(any())
            domainEventPublisher.publish(any())
        }
    }

    @Test
    fun `fail booking seats for a movie session when movie session is not found`() {
        val movieSessionId = UUID.randomUUID()
        val customerId = UUID.randomUUID()
        val requestedSeats = listOf(RequestedSeat(1, 1))
        every { movieSessionRepository.find(MovieSessionId(movieSessionId)) } returns MovieSessionNotFound.left()

        val result = bookSeatsServiceForAMovieSession(BookSeatsRequest(movieSessionId, customerId, requestedSeats))

        assertThat(result).isEqualTo(MovieSessionNotFound.left())
        verify { domainEventPublisher.publishError(any()) }
    }

    @Test
    fun `fail booking seats for a movie session when any booking invariant is not meet`() {
        val movieSessionId = UUID.randomUUID()
        val customerId = UUID.randomUUID()
        val requestedSeats = listOf(RequestedSeat(1, 1))
        val movieSession = MovieSessionBuilder.build(
            seatingPlan = listOf(
                Seat(col = 1, row = 1), Seat(col = 2, row = 1), Seat(col = 1, row = 2), Seat(col = 2, row = 2)
            ),
            bookings = listOf(
                Booking.reconstitute(BookingRef("12345"), CustomerId(UUID.randomUUID()), listOf(Seat(1, 1)))
            )
        )
        every { movieSessionRepository.find(MovieSessionId(movieSessionId)) } returns movieSession.right()

        val result = bookSeatsServiceForAMovieSession(BookSeatsRequest(movieSessionId, customerId, requestedSeats))

        assertThat(result).isEqualTo(SeatsAlreadyBooked(listOf(Seat(1, 1))).left())
        verify { domainEventPublisher.publishError(any()) }
    }
}
