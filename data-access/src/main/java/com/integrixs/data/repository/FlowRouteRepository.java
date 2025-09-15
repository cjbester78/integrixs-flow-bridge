package com.integrixs.data.repository;

import com.integrixs.data.model.FlowRoute;
import com.integrixs.data.model.IntegrationFlow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FlowRouteRepository extends JpaRepository<FlowRoute, UUID> {

    List<FlowRoute> findByFlow(IntegrationFlow flow);

    List<FlowRoute> findByFlowAndActiveTrue(IntegrationFlow flow);

    @Query("SELECT fr FROM FlowRoute fr WHERE fr.flow.id = :flowId AND fr.active = true ORDER BY fr.priority ASC")
    List<FlowRoute> findActiveRoutesByFlowId(@Param("flowId") UUID flowId);

    @Query("SELECT fr FROM FlowRoute fr WHERE fr.flow.id = :flowId AND fr.sourceStep = :sourceStep AND fr.active = true ORDER BY fr.priority")
    List<FlowRoute> findByFlowIdAndSourceStep(@Param("flowId") UUID flowId, @Param("sourceStep") String sourceStep);

    @Query("SELECT fr FROM FlowRoute fr WHERE fr.flow.id = :flowId AND fr.sourceStep = :stepId AND fr.active = true ORDER BY fr.priority")
    List<FlowRoute> findByFlowIdAndStepId(@Param("flowId") UUID flowId, @Param("stepId") String stepId);

    Optional<FlowRoute> findByFlowAndRouteName(IntegrationFlow flow, String routeName);
}
