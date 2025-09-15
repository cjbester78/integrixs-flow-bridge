package com.integrixs.data.repository;

import com.integrixs.data.model.NotificationChannel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for NotificationChannel entities
 */
@Repository
public interface NotificationChannelRepository extends JpaRepository<NotificationChannel, Long> {

    /**
     * Find notification channel by name
     */
    Optional<NotificationChannel> findByChannelName(String channelName);

    /**
     * Find all enabled channels
     */
    List<NotificationChannel> findByEnabledTrue();

    /**
     * Find all enabled channels with pagination
     */
    Page<NotificationChannel> findByEnabledTrue(Pageable pageable);

    /**
     * Find enabled channels by type
     */
    List<NotificationChannel> findByChannelTypeAndEnabledTrue(NotificationChannel.ChannelType channelType);

    /**
     * Find enabled channels by type with pagination
     */
    Page<NotificationChannel> findByChannelTypeAndEnabledTrue(NotificationChannel.ChannelType channelType, Pageable pageable);

    /**
     * Find channels by IDs
     */
    @Query("SELECT nc FROM NotificationChannel nc WHERE nc.id IN :ids AND nc.enabled = true")
    List<NotificationChannel> findEnabledChannelsByIds(@Param("ids") List<Long> ids);

    /**
     * Check if channel name exists
     */
    boolean existsByChannelName(String channelName);

    /**
     * Count enabled channels by type
     */
    long countByChannelTypeAndEnabledTrue(NotificationChannel.ChannelType channelType);

    /**
     * Find channels that need rate limit reset
     */
    @Query("SELECT nc FROM NotificationChannel nc WHERE nc.enabled = true AND " +
           "nc.rateLimitPerHour IS NOT NULL AND nc.notificationCountCurrentHour > 0 AND " +
           "nc.lastNotificationAt < :oneHourAgo")
    List<NotificationChannel> findChannelsNeedingRateLimitReset(@Param("oneHourAgo") LocalDateTime oneHourAgo);
}
