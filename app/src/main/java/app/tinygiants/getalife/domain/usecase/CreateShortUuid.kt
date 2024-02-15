package app.tinygiants.getalife.domain.usecase

import java.util.UUID

// Intended for Firebase id's
class CreateShortUuid {

    operator fun invoke() = UUID.randomUUID().toString().substringBefore("-")
}