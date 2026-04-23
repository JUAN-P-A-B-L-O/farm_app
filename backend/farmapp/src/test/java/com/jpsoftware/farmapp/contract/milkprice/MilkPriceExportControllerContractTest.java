package com.jpsoftware.farmapp.contract.milkprice;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jpsoftware.farmapp.milkprice.controller.MilkPriceController;
import com.jpsoftware.farmapp.milkprice.service.MilkPriceService;
import com.jpsoftware.farmapp.shared.exception.GlobalExceptionHandler;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class MilkPriceExportControllerContractTest {

    private MockMvc mockMvc;
    private MilkPriceService milkPriceService;

    @BeforeEach
    void setUp() {
        milkPriceService = org.mockito.Mockito.mock(MilkPriceService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new MilkPriceController(milkPriceService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldExportMilkPriceHistoryAsCsv() throws Exception {
        when(milkPriceService.exportHistory("farm-1"))
                .thenReturn("id,price\nprice-1,2.35\n");

        mockMvc.perform(get("/milk-prices/export").param("farmId", "farm-1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv;charset=UTF-8"))
                .andExpect(header().string("Content-Disposition", Matchers.containsString("milk-prices.csv")))
                .andExpect(content().string("id,price\nprice-1,2.35\n"));

        verify(milkPriceService).exportHistory("farm-1");
    }
}
