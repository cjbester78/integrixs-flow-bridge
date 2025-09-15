package com.integrixs.data.repository;

import com.integrixs.data.model.RouteCondition;
import com.integrixs.data.model.FlowRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RouteConditionRepository extends JpaRepository<RouteCondition, UUID> {

    List<RouteCondition> findByFlowRoute(FlowRoute flowRoute);

    List<RouteCondition> findByFlowRouteAndActiveTrue(FlowRoute flowRoute);

    @Query("SELECT rc FROM RouteCondition rc WHERE rc.flowRoute.id = :routeId ORDER BY rc.order ASC")
    List<RouteCondition> findByRouteIdOrderByOrder(@Param("routeId") UUID routeId);

    @Query("SELECT rc FROM RouteCondition rc WHERE rc.flowRoute.id = :routeId ORDER BY rc.order ASC")
    List<RouteCondition> findByRouteId(@Param("routeId") UUID routeId);

    void deleteByFlowRoute(FlowRoute flowRoute);
}
