package app.tinygiants.getalife.domain.usecase

import java.util.UUID

class ShortUuidUseCase {

    operator fun invoke() = UUID.randomUUID().toString().substringBefore("-")
}