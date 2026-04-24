package com.jpsoftware.farmapp.contract.milkprice;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jpsoftware.farmapp.milkprice.controller.MilkPriceController;
import com.jpsoftware.farmapp.milkprice.dto.MilkPriceResponse;
import com.jpsoftware.farmapp.milkprice.service.MilkPriceService;
import com.jpsoftware.farmapp.shared.dto.PaginatedResponse;
import com.jpsoftware.farmapp.shared.exception.GlobalExceptionHandler;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class MilkPriceControllerContractTest {

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
    void shouldReturnPaginatedMilkPriceHistory() throws Exception {
        when(milkPriceService.getHistoryPaginated("farm-1", 0, 10)).thenReturn(new PaginatedResponse<>(
                List.of(new MilkPriceResponse(
                        "price-1",
                        "farm-1",
                        2.35,
                        LocalDate.of(2026, 4, 14),
                        LocalDateTime.of(2026, 4, 14, 9, 30),
                        "user-1",
                        false)),
                0,
                10,
                1,
                1));

        mockMvc.perform(get("/milk-prices")
                        .param("farmId", "farm-1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("price-1"))
                .andExpect(jsonPath("$.content[0].price").value(2.35))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }
}
