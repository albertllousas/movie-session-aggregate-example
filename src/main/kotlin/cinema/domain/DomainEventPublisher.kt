package cinema.domain

interface DomainEventPublisher {

        fun publish(event: DomainEvent)

        fun publishError(error: DomainError)
}
