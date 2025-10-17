package com.tobiasdroste.papercups.app

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.tobiasdroste.papercups.app.printers.PrinterRepository
import com.tobiasdroste.papercups.app.printers.models.Printer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ManageManualPrintersViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: ManageManualPrintersViewModel
    private val printerRepository: PrinterRepository = mock()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        whenever(printerRepository.observePrinters()).thenReturn(
            MutableLiveData<List<Printer>>(
                emptyList()
            )
        )
        viewModel = ManageManualPrintersViewModel(printerRepository)
    }

    @Test
    fun `initial state is correct`() {
        // initial state is observing printers
        verify(printerRepository).observePrinters()
    }

    @Test
    fun `removePrinter calls repository`() = runTest {
        val printerId = 1
        viewModel.removePrinter(printerId)
        verify(printerRepository).deletePrinter(printerId)
    }
}
