#!/bin/bash

# Script to help replace custom queries with JPA methods
# This script provides sed commands that can be used to replace custom queries

echo "=== Custom Query Replacement Script ==="
echo "This script shows the commands to replace custom queries with JPA methods"
echo "Review each command before running!"
echo ""

# MessageRepository replacements
echo "# MessageRepository replacements:"
echo "sed -i '' 's/@Query(\"SELECT m FROM Message m WHERE m.correlationId = :correlationId ORDER BY m.receivedAt\")//' data-access/src/main/java/com/integrixs/data/repository/MessageRepository.java"
echo "sed -i '' 's/List<Message> findByCorrelationId(@Param(\"correlationId\") String correlationId);/List<Message> findByCorrelationIdOrderByReceivedAt(String correlationId);/' data-access/src/main/java/com/integrixs/data/repository/MessageRepository.java"
echo ""

echo "sed -i '' 's/@Query(\"SELECT m FROM Message m WHERE m.receivedAt BETWEEN :startDate AND :endDate\")//' data-access/src/main/java/com/integrixs/data/repository/MessageRepository.java"
echo "sed -i '' 's/Page<Message> findByDateRange(@Param(\"startDate\") LocalDateTime startDate, @Param(\"endDate\") LocalDateTime endDate, Pageable pageable);/Page<Message> findByReceivedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);/' data-access/src/main/java/com/integrixs/data/repository/MessageRepository.java"
echo ""

echo "sed -i '' 's/@Query(\"SELECT COUNT(m) FROM Message m WHERE m.flow.id = :flowId AND m.status = :status\")//' data-access/src/main/java/com/integrixs/data/repository/MessageRepository.java"
echo "sed -i '' 's/Long countByFlowIdAndStatus(@Param(\"flowId\") UUID flowId, @Param(\"status\") Message.MessageStatus status);/Long countByFlowIdAndStatus(UUID flowId, Message.MessageStatus status);/' data-access/src/main/java/com/integrixs/data/repository/MessageRepository.java"
echo ""

# Add more replacements as needed...

echo "=== Manual Steps Required ==="
echo "1. Remove unused @Param imports"
echo "2. Test each repository method after replacement"
echo "3. Check that JPA generates the expected queries"
echo "4. Monitor performance impact"
echo ""
echo "Would you like to see the full list of replacements? (y/n)"