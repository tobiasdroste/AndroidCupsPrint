package com.tobiasdroste.papercups.printservice

import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.print.PrintJobId
import android.printservice.PrintJob
import android.printservice.PrintService
import android.printservice.PrinterDiscoverySession
import android.widget.Toast
import com.tobiasdroste.papercups.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cups4j.CupsClient
import org.cups4j.CupsPrinter
import org.cups4j.JobStateEnum
import timber.log.Timber
import java.io.FileNotFoundException
import java.io.IOException
import java.net.MalformedURLException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import javax.inject.Inject
import javax.net.ssl.SSLException

/**
 * When a print job is active, the app will poll the printer to retrieve the job status. This is the polling interval.
 */
private const val JOB_CHECK_POLLING_INTERVAL = 5000

/**
 * CUPS print service
 */
class CupsService : PrintService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private val jobs = HashMap<PrintJobId, Int>()

    @Inject
    lateinit var cupsPrinterDiscoverySessionFactory: CupsPrinterDiscoverySessionFactory

    override fun onCreatePrinterDiscoverySession(): PrinterDiscoverySession =
        cupsPrinterDiscoverySessionFactory.create(this)

    override fun onRequestCancelPrintJob(printJob: PrintJob) {
        val jobInfo = printJob.info
        val printerId = jobInfo.printerId
        if (printerId == null) {
            Timber.d("Tried to cancel a job, but the printer ID is null")
            return
        }

        val url = printerId.localId

        val id = printJob.id
        if (id == null) {
            Timber.d("Tried to cancel a job, but the print job ID is null")
            return
        }
        val jobId = jobs[id]
        if (jobId == null) {
            Timber.d("Tried to cancel a job, but the print job ID is null")
            return
        }

        try {
            val tmpUri = URI(url)
            val schemeHostPort = tmpUri.scheme + "://" + tmpUri.host + ":" + tmpUri.port

            val clientURL = URL(schemeHostPort)

            scope.launch {
                cancelPrintJob(clientURL, jobId)
                onPrintJobCancelled(printJob)
            }

        } catch (e: MalformedURLException) {
            Timber.e(e, "Couldn't cancel print job: $printJob, jobId: $jobId")
        } catch (e: URISyntaxException) {
            Timber.e(e, "Couldn't parse URI: $url")
        }
    }

    /**
     * Called from a background thread, ask the printer to cancel a job by its printer job ID
     *
     * @param clientURL The printer client URL
     * @param jobId     The printer job ID
     */
    private suspend fun cancelPrintJob(clientURL: URL, jobId: Int) = withContext(Dispatchers.IO) {
        try {
            val client = CupsClient(this@CupsService, clientURL)
            client.cancelJob(jobId)
        } catch (e: Exception) {
            Timber.e(e, "Couldn't cancel job: $jobId")
        }
    }

    /**
     * Called on the main thread, when the print job was cancelled
     *
     * @param printJob The print job
     */
    private suspend fun onPrintJobCancelled(printJob: PrintJob) = withContext(Dispatchers.Main) {
        jobs.remove(printJob.id)
        printJob.cancel()
    }

    override fun onPrintJobQueued(printJob: PrintJob) {
        printJob.start()
        val jobInfo = printJob.info
        val printerId = jobInfo.printerId
        if (printerId == null) {
            Timber.d("Tried to queue a job, but the printer ID is null")
            return
        }

        val url = printerId.localId
        try {
            val tmpUri = URI(url)
            val schemeHostPort = tmpUri.scheme + "://" + tmpUri.host + ":" + tmpUri.port

            // Prepare job
            val printerURL = URL(url)
            val clientURL = URL(schemeHostPort)
            val data = printJob.document.data
            if (data == null) {
                Timber.d("Tried to queue a job, but the document data (file descriptor) is null")
                Toast.makeText(this, R.string.err_document_fd_null, Toast.LENGTH_LONG).show()
                return
            }
            val jobId = printJob.id

            scope.launch {
                // Send print job
                try {
                    printDocument(jobId, clientURL, printerURL, data)
                    onPrintJobSent(printJob)
                } catch (e: Exception) {
                    handleJobException(printJob, e)
                } finally {
                    // Close the file descriptor, after printing
                    try {
                        data.close()
                    } catch (e: IOException) {
                        Timber.e("Job document data (file descriptor) couldn't close.")
                    }
                }
            }
        } catch (e: MalformedURLException) {
            printJob.fail(getString(R.string.print_job_queue_fail_malformed_url, printJob))
            Timber.e("Couldn't queue print job: $printJob")
        } catch (e: URISyntaxException) {
            printJob.fail(getString(R.string.print_job_queue_fail_uri_syntax, url))
            Timber.e("Couldn't parse URI: $url")
        }
    }

    /**
     * Called from the UI thread.
     * Handle the exception (e.g. log or send it to crashlytics?), and inform the user of what happened
     *
     * @param printJob The print job
     * @param e     The exception that occurred
     */
    private suspend fun handleJobException(printJob: PrintJob, e: Exception) =
        withContext(Dispatchers.Main) {
            when (e) {
                is SocketTimeoutException -> {
                    printJob.fail(getString(R.string.err_job_socket_timeout))
                    Toast.makeText(
                        this@CupsService,
                        R.string.err_job_socket_timeout,
                        Toast.LENGTH_LONG
                    ).show()
                }

                is NullPrinterException -> {
                    printJob.fail(getString(R.string.err_printer_null_when_printing))
                    Toast.makeText(
                        this@CupsService,
                        R.string.err_printer_null_when_printing,
                        Toast.LENGTH_LONG
                    ).show()
                }

                else -> {
                    val jobId = printJob.id
                    val errorMsg =
                        getString(R.string.err_job_exception, jobId.toString(), e.localizedMessage)
                    printJob.fail(errorMsg)
                    Toast.makeText(this@CupsService, errorMsg, Toast.LENGTH_LONG).show()
                    if (e is SSLException && e.message?.contains("I/O error during system call, Broken pipe") == true) {
                        // Don't send this crash report: https://github.com/BenoitDuffez/AndroidCupsPrint/issues/70
                        Timber.e("Couldn't query job $jobId")
                    } else {
                        Timber.e(e, "Couldn't query job $jobId")
                    }
                }
            }
        }

    private fun startPolling(printJob: PrintJob) {
        Handler(Looper.getMainLooper()).postDelayed(object : Runnable {
            override fun run() {
                if (updateJobStatus(printJob)) {
                    Handler(Looper.getMainLooper()).postDelayed(
                        this,
                        JOB_CHECK_POLLING_INTERVAL.toLong()
                    )
                }
            }
        }, JOB_CHECK_POLLING_INTERVAL.toLong())
    }

    /**
     * Called in the main thread, will ask the job status and update it in the Android framework
     *
     * @param printJob The print job
     * @return true if this method should be called again, false otherwise (in case the job is still pending or it is complete)
     */
    internal fun updateJobStatus(printJob: PrintJob): Boolean {
        // Check if the job is already gone
        if (!jobs.containsKey(printJob.id)) {
            Timber.d("Tried to request a job status, but the job couldn't be found in the jobs list")
            return false
        }

        val printerId = printJob.info.printerId
        if (printerId == null) {
            Timber.d("Tried to request a job status, but the printer ID is null")
            return false
        }
        val url = printerId.localId

        // Prepare job
        val clientURL: URL
        val jobId: Int
        try {
            val tmpUri = URI(url)
            val schemeHostPort = tmpUri.scheme + "://" + tmpUri.host + ":" + tmpUri.port

            clientURL = URL(schemeHostPort)
            jobId = jobs[printJob.id]!!
        } catch (e: MalformedURLException) {
            Timber.e(e, "Couldn't get job: $printJob state")
            return false
        } catch (e: URISyntaxException) {
            Timber.e(e, "Couldn't parse URI: $url")
            return false
        }

        // Send print job
        scope.launch {
            try {
                val jobState = getJobState(jobId, clientURL)
                onJobStateUpdate(printJob, jobState)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    jobs.remove(printJob.id)
                    Timber.e("Couldn't get job: $jobId state because: $e")

                    when {
                        (e is SocketException || e is SocketTimeoutException)
                                && e.message?.contains("ECONNRESET") == true -> {
                            printJob.fail(getString(R.string.err_job_econnreset, jobId))
                            Toast.makeText(
                                this@CupsService,
                                getString(R.string.err_job_econnreset, jobId),
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        e is FileNotFoundException -> {
                            printJob.fail(getString(R.string.err_job_not_found, jobId))
                            Toast.makeText(
                                this@CupsService,
                                getString(R.string.err_job_not_found, jobId),
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        e is NullPointerException -> printJob.complete()
                        else -> {
                            printJob.fail(e.localizedMessage)
                            Timber.e(e)
                        }
                    }
                }
            }
        }

        // We want to be called again if the job is still in this map
        // Indeed, when the job is complete, the job is removed from this map.
        return jobs.containsKey(printJob.id)
    }

    /**
     * Called in a background thread, in order to check the job status
     *
     * @param jobId     The printer job ID
     * @param clientURL The printer client URL
     * @return true if the job is complete/aborted/cancelled, false if it's still processing (printing, paused, etc)
     */
    @Throws(Exception::class)
    private suspend fun getJobState(jobId: Int, clientURL: URL): JobStateEnum =
        withContext(Dispatchers.IO) {
            val client = CupsClient(this@CupsService, clientURL)
            val attr = client.getJobAttributes(jobId)
            return@withContext attr.jobState!!
        }

    /**
     * Called on the main thread, when a job status has been checked
     *
     * @param printJob The print job
     * @param state    Print job state
     */
    private suspend fun onJobStateUpdate(printJob: PrintJob, state: JobStateEnum?) {
        withContext(Dispatchers.Main) {
            // Couldn't check state -- don't do anything
            when (state) {
                null -> {
                    jobs.remove(printJob.id)
                    printJob.cancel()
                }

                JobStateEnum.CANCELED -> {
                    jobs.remove(printJob.id)
                    printJob.cancel()
                }

                JobStateEnum.COMPLETED, JobStateEnum.ABORTED -> {
                    jobs.remove(printJob.id)
                    printJob.complete()
                }

                else -> {}
            }
        }
    }

    /**
     * Called from a background thread, when the print job has to be sent to the printer.
     *
     * @param clientURL  The client URL
     * @param printerURL The printer URL
     * @param fd         The document to print, as a [ParcelFileDescriptor]
     */
    @Throws(Exception::class)
    internal suspend fun printDocument(
        jobId: PrintJobId,
        clientURL: URL,
        printerURL: URL,
        fd: ParcelFileDescriptor
    ) = withContext(Dispatchers.IO) {
        val client = CupsClient(this@CupsService, clientURL)
        val printer = client.getPrinter(printerURL)?.let { printer ->
            val cupsPrinter = CupsPrinter(printerURL, printer.name, true)
            cupsPrinter.location = printer.location
            cupsPrinter
        }

        val doc = ParcelFileDescriptor.AutoCloseInputStream(fd)
        val job = org.cups4j.PrintJob.Builder(doc).build()
        val result = printer?.print(job, this@CupsService) ?: throw NullPrinterException()
        jobs[jobId] = result.jobId
    }

    /**
     * Called on the main thread, when the job was sent to the printer
     *
     * @param printJob The print job
     */
    private suspend fun onPrintJobSent(printJob: PrintJob) = withContext(Dispatchers.Main) {
        startPolling(printJob)
    }

    private class NullPrinterException :
        Exception("Printer is null when trying to print: printer no longer available?")
}
