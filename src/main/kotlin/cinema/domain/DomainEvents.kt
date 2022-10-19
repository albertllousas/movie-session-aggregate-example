package cinema.domain

import java.time.LocalDate

sealed interface DomainEvent

data class SeatsBooked(val on: LocalDate, val movieSession: MovieSession, val bookingRef: BookingRef): DomainEvent
