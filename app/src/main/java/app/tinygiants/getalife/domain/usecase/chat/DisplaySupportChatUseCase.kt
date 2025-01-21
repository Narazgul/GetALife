package app.tinygiants.getalife.domain.usecase.chat

import app.tinygiants.getalife.domain.repository.SupportChatRepository
import javax.inject.Inject

class DisplaySupportChatUseCase @Inject constructor(private val repository: SupportChatRepository) {

    operator fun invoke() = repository.isSupportChatEnabled()
}