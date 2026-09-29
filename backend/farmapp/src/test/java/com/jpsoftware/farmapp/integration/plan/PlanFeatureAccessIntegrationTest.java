package com.jpsoftware.farmapp.integration.plan;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jpsoftware.farmapp.base.BaseIntegrationTest;
import com.jpsoftware.farmapp.farm.entity.FarmEntity;
import com.jpsoftware.farmapp.shared.email.service.EmailSender;
import com.jpsoftware.farmapp.user.entity.UserEntity;
import com.jpsoftware.farmapp.user.entity.UserPlan;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

class PlanFeatureAccessIntegrationTest extends BaseIntegrationTest {

    @MockBean
    private EmailSender emailSender;

    @Test
    void shouldBlockDashboardForFreeManager() throws Exception {
        UserEntity freeManager = createAuthenticatedUser("MANAGER", UserPlan.FREE);
        FarmEntity farm = createFarmOwnedBy(freeManager, "Fazenda Premium");

        mockMvc.perform(get("/dashboard")
                        .param("farmId", farm.getId().toString())
                        .header("Authorization", bearerToken(freeManager)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Este recurso está disponível apenas no plano Premium."));
    }

    @Test
    void shouldAllowDashboardForProManager() throws Exception {
        UserEntity proManager = createAuthenticatedUser("MANAGER", UserPlan.PRO);
        FarmEntity farm = createFarmOwnedBy(proManager, "Fazenda Premium");

        mockMvc.perform(get("/dashboard")
                        .param("farmId", farm.getId().toString())
                        .header("Authorization", bearerToken(proManager)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProduction").exists())
                .andExpect(jsonPath("$.animalCount").exists());
    }

    @Test
    void shouldBlockCsvExportForFreeUser() throws Exception {
        UserEntity freeWorker = createAuthenticatedUser("WORKER", UserPlan.FREE);
        UserEntity owner = createAuthenticatedUser("MANAGER", UserPlan.FREE);
        FarmEntity farm = createFarmOwnedBy(owner, "Fazenda Worker");
        assignUserToFarm(freeWorker, farm);

        mockMvc.perform(get("/animals/export")
                        .header("Authorization", bearerToken(freeWorker)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Este recurso está disponível apenas no plano Premium."));
    }

    @Test
    void shouldAllowCsvExportForProUser() throws Exception {
        UserEntity proWorker = createAuthenticatedUser("WORKER", UserPlan.PRO);
        UserEntity owner = createAuthenticatedUser("MANAGER", UserPlan.FREE);
        FarmEntity farm = createFarmOwnedBy(owner, "Fazenda Worker");
        assignUserToFarm(proWorker, farm);

        mockMvc.perform(get("/animals/export")
                        .header("Authorization", bearerToken(proWorker)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", Matchers.containsString("animals.csv")));
    }

    @Test
    void shouldBlockAnalyticsForFreeManager() throws Exception {
        UserEntity freeManager = createAuthenticatedUser("MANAGER", UserPlan.FREE);
        createFarmOwnedBy(freeManager, "Fazenda Analytics");

        mockMvc.perform(get("/analytics/production")
                        .header("Authorization", bearerToken(freeManager)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Este recurso está disponível apenas no plano Premium."));
    }

    @Test
    void shouldAllowAnalyticsForProManager() throws Exception {
        UserEntity proManager = createAuthenticatedUser("MANAGER", UserPlan.PRO);
        createFarmOwnedBy(proManager, "Fazenda Analytics");

        mockMvc.perform(get("/analytics/production")
                        .header("Authorization", bearerToken(proManager)))
                .andExpect(status().isOk());
    }
}
