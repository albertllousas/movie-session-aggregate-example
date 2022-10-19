package cinema.domain

sealed interface DomainError

sealed interface BookingError : DomainError

data class SeatsAlreadyBooked(val seats: List<Seat>): BookingError

data class SeatsDoNotExist(val seats: List<Seat>): BookingError

object MovieSessionNotFound : BookingError

object SeatsCanNotBeEmpty: BookingError

object MaxSeatsAllowedPerBookingReached: BookingError
