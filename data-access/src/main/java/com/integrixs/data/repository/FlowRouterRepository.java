package com.integrixs.data.repository;

import com.integrixs.data.model.FlowRouter;
import com.integrixs.data.model.IntegrationFlow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FlowRouterRepository extends JpaRepository<FlowRouter, UUID> {

    Optional<FlowRouter> findByRouterName(String routerName);

    List<FlowRouter> findByFlow(IntegrationFlow flow);

    List<FlowRouter> findByFlowAndActiveTrue(IntegrationFlow flow);

    @Query("SELECT fr FROM FlowRouter fr WHERE fr.flow.id = :flowId AND fr.active = true ORDER BY fr.evaluationOrder ASC")
    List<FlowRouter> findActiveByFlowIdOrderByEvaluation(@Param("flowId") Long flowId);

    @Query("SELECT fr FROM FlowRouter fr WHERE fr.routerType = :routerType AND fr.active = true")
    List<FlowRouter> findByRouterTypeAndActiveTrue(@Param("routerType") FlowRouter.RouterType routerType);
}
