package cinema.domain

import arrow.core.Either

interface MovieSessionRepository {
    fun find(movieSessionId: MovieSessionId): Either<MovieSessionNotFound, MovieSession>
    fun save(movieSession: MovieSession)
}