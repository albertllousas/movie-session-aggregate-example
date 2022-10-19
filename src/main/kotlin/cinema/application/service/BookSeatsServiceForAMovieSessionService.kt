package cinema.application.service

import arrow.core.Either
import arrow.core.flatMap
import cinema.domain.BookingError
import cinema.domain.BookingRef
import cinema.domain.CustomerId
import cinema.domain.DomainEventPublisher
import cinema.domain.MovieSession
import cinema.domain.MovieSessionId
import cinema.domain.MovieSessionRepository
import cinema.domain.Seat
import cinema.domain.SeatsBooked
import java.time.Clock
import java.time.LocalDate.now
import java.util.UUID

class BookSeatsServiceForAMovieSessionService(
    private val movieSessionRepository: MovieSessionRepository,
    private val publisher: DomainEventPublisher,
    private val clock: Clock
) {
    operator fun invoke(request: BookSeatsRequest): Either<BookingError, Unit> =
        movieSessionRepository.find(MovieSessionId(request.movieSessionId))
            .flatMap { movieSession -> movieSession.book(CustomerId(request.customerId), request.requestedSeats.asSeats()) }
            .tap { movieSessionRepository.save(it.first) }
            .map(::asSeatsBookedEvent)
            .map(publisher::publish)
            .tapLeft(publisher::publishError)

    private fun asSeatsBookedEvent(pair: Pair<MovieSession, BookingRef>) = SeatsBooked(now(clock), pair.first, pair.second)

    private fun List<RequestedSeat>.asSeats() = this.map { Seat(it.col, it.row) }
}

data class BookSeatsRequest(val movieSessionId: UUID, val customerId: UUID, val requestedSeats: List<RequestedSeat>)

data class RequestedSeat(val col: Int, val row: Int)
