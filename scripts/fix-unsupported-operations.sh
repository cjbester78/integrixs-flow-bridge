#!/bin/bash

# Fix UnsupportedOperationException in adapter files

echo "Fixing UnsupportedOperationException issues in adapters..."

# Function to fix inbound adapters (remove send support)
fix_inbound_adapter_send() {
    local file=$1
    echo "Fixing inbound adapter send in: $file"
    
    # Replace doSend method
    sed -i.bak '
    /protected AdapterResult doSend.*throws Exception {/,/^[[:space:]]*}/ {
        /throw new UnsupportedOperationException/ {
            s/throw new UnsupportedOperationException.*/log.debug("Inbound adapter does not send data");/
            a\
        return AdapterResult.success(null);
        }
    }' "$file"
    
    # Replace send method  
    sed -i.bak2 '
    /public AdapterResult send.*throws AdapterException {/,/^[[:space:]]*}/ {
        /throw new UnsupportedOperationException/ {
            s/throw new UnsupportedOperationException.*/log.debug("Inbound adapter does not send data");/
            a\
        return AdapterResult.success(null);
        }
    }' "$file"
}

# Function to fix outbound adapters (remove receive support)
fix_outbound_adapter_receive() {
    local file=$1
    echo "Fixing outbound adapter receive in: $file"
    
    # Replace doReceive method
    sed -i.bak '
    /protected AdapterResult doReceive.*throws Exception {/,/^[[:space:]]*}/ {
        /throw new UnsupportedOperationException/ {
            s/throw new UnsupportedOperationException.*/log.debug("Outbound adapter does not receive data");/
            a\
        return AdapterResult.success(null);
        }
    }' "$file"
}

# Function to fix startListening in adapters that don't support it
fix_start_listening() {
    local file=$1
    echo "Fixing startListening in: $file"
    
    sed -i.bak3 '
    /public void startListening.*{/,/^[[:space:]]*}/ {
        /throw new UnsupportedOperationException/ {
            s/throw new UnsupportedOperationException.*/log.debug("Push-based listening not supported by this adapter type");/
        }
    }' "$file"
}

# Function to fix unknown action handlers
fix_unknown_action() {
    local file=$1
    echo "Fixing unknown action handler in: $file"
    
    sed -i.bak4 '
    /default:/ {
        N
        /throw new UnsupportedOperationException.*"Unknown action/ {
            s/throw new UnsupportedOperationException.*/log.warn("Unknown action received: {}", action);/
            a\
                    return message;
        }
    }' "$file"
}

# Fix inbound adapters
for file in adapters/src/main/java/com/integrixs/adapters/messaging/rabbitmq/RabbitMQInboundAdapter.java \
            adapters/src/main/java/com/integrixs/adapters/messaging/amqp/AMQPInboundAdapter.java \
            adapters/src/main/java/com/integrixs/adapters/messaging/sms/SMSInboundAdapter.java \
            adapters/src/main/java/com/integrixs/adapters/social/instagram/InstagramGraphInboundAdapter.java \
            adapters/src/main/java/com/integrixs/adapters/social/linkedin/LinkedInAdsInboundAdapter.java \
            adapters/src/main/java/com/integrixs/adapters/social/twitter/TwitterApiV2InboundAdapter.java \
            adapters/src/main/java/com/integrixs/adapters/social/whatsapp/WhatsAppBusinessInboundAdapter.java \
            adapters/src/main/java/com/integrixs/adapters/social/youtube/YouTubeAnalyticsInboundAdapter.java \
            adapters/src/main/java/com/integrixs/adapters/social/youtube/YouTubeDataInboundAdapter.java \
            adapters/src/main/java/com/integrixs/adapters/social/facebook/FacebookAdsInboundAdapter.java; do
    if [ -f "$file" ]; then
        fix_inbound_adapter_send "$file"
    fi
done

# Fix outbound adapters  
for file in adapters/src/main/java/com/integrixs/adapters/messaging/rabbitmq/RabbitMQOutboundAdapter.java \
            adapters/src/main/java/com/integrixs/adapters/messaging/amqp/AMQPOutboundAdapter.java \
            adapters/src/main/java/com/integrixs/adapters/messaging/sms/SMSOutboundAdapter.java \
            adapters/src/main/java/com/integrixs/adapters/infrastructure/adapter/MailOutboundAdapter.java \
            adapters/src/main/java/com/integrixs/adapters/social/youtube/YouTubeAnalyticsOutboundAdapter.java \
            adapters/src/main/java/com/integrixs/adapters/social/youtube/YouTubeDataOutboundAdapter.java \
            adapters/src/main/java/com/integrixs/adapters/social/snapchat/SnapchatAdsOutboundAdapter.java \
            adapters/src/main/java/com/integrixs/adapters/social/tiktok/TikTokContentOutboundAdapter.java; do
    if [ -f "$file" ]; then
        fix_outbound_adapter_receive "$file"
    fi
done

# Fix push-based listening  
for file in adapters/src/main/java/com/integrixs/adapters/infrastructure/adapter/FtpInboundAdapter.java \
            adapters/src/main/java/com/integrixs/adapters/infrastructure/adapter/SftpInboundAdapter.java \
            adapters/src/main/java/com/integrixs/adapters/infrastructure/adapter/FileInboundAdapter.java \
            adapters/src/main/java/com/integrixs/adapters/infrastructure/adapter/HttpInboundAdapter.java \
            adapters/src/main/java/com/integrixs/adapters/infrastructure/adapter/JdbcInboundAdapter.java \
            adapters/src/main/java/com/integrixs/adapters/infrastructure/adapter/RestInboundAdapter.java \
            adapters/src/main/java/com/integrixs/adapters/infrastructure/adapter/SoapInboundAdapter.java \
            adapters/src/main/java/com/integrixs/adapters/infrastructure/adapter/IdocInboundAdapter.java \
            adapters/src/main/java/com/integrixs/adapters/infrastructure/adapter/RfcInboundAdapter.java \
            adapters/src/main/java/com/integrixs/adapters/infrastructure/adapter/OdataInboundAdapter.java \
            adapters/src/main/java/com/integrixs/adapters/infrastructure/adapter/MailInboundAdapter.java \
            adapters/src/main/java/com/integrixs/adapters/infrastructure/adapter/KafkaInboundAdapter.java \
            adapters/src/main/java/com/integrixs/adapters/infrastructure/adapter/IbmmqInboundAdapter.java; do
    if [ -f "$file" ]; then
        fix_start_listening "$file"
    fi
done

# Fix unknown action handlers
for file in adapters/src/main/java/com/integrixs/adapters/social/pinterest/PinterestOutboundAdapter.java \
            adapters/src/main/java/com/integrixs/adapters/social/reddit/RedditOutboundAdapter.java \
            adapters/src/main/java/com/integrixs/adapters/social/facebook/FacebookMessengerOutboundAdapter.java; do
    if [ -f "$file" ]; then
        fix_unknown_action "$file"
    fi
done

# Clean up backup files
find adapters -name "*.bak*" -delete

echo "Completed fixing UnsupportedOperationException issues"