package app.tinygiants.getalife.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ListenableWorker.Result
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import app.tinygiants.getalife.domain.usecase.transaction.ProcessRecurringPaymentsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * Worker der einmal täglich alle fälligen wiederkehrenden Zahlungen erzeugt.
 */
class ProcessRecurringPaymentsWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val processRecurringPaymentsUseCase: ProcessRecurringPaymentsUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = try {
        processRecurringPaymentsUseCase()
        Result.success()
    } catch (e: Exception) {
        Result.retry()
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "recurring_payments_worker"

        fun enqueue(context: Context) {
            val request = PeriodicWorkRequestBuilder<ProcessRecurringPaymentsWorker>(1, TimeUnit.DAYS)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(context: Context, params: WorkerParameters): ProcessRecurringPaymentsWorker
    }
}
