#!/bin/bash

# Fix MessageDTO builder pattern usage in Discord adapter
echo "Fixing Discord adapter MessageDTO usage..."

# First, let's find all the builder pattern occurrences
cat > /tmp/discord_fix.sed << 'EOF'
# Match the MessageDTO builder pattern and convert to setter calls
/MessageDTO message = new MessageDTO()$/{
    N
    :loop
    N
    /\.build();$/!b loop
    # Now we have the full builder pattern, convert it
    s/MessageDTO message = new MessageDTO()\n[[:space:]]*\.type("\([^"]*\)")\n[[:space:]]*\.category("\([^"]*\)")\n[[:space:]]*\.source("\([^"]*\)")\n[[:space:]]*\.destination(\([^)]*\))\n[[:space:]]*\.content(\([^)]*\))\n[[:space:]]*\.timestamp(\([^)]*\))\n[[:space:]]*\.build();/MessageDTO message = new MessageDTO();\n                message.setType("\1");\n                message.setSource("\3");\n                message.setTarget(\4);\n                message.setPayload(\5);\n                message.setTimestamp(LocalDateTime.ofInstant(Instant.ofEpochMilli(\6), ZoneOffset.UTC));/
}

# Also handle MessageDTO msg variant
/MessageDTO msg = new MessageDTO()$/{
    N
    :loop2
    N
    /\.build();$/!b loop2
    # Now we have the full builder pattern, convert it
    s/MessageDTO msg = new MessageDTO()\n[[:space:]]*\.type("\([^"]*\)")\n[[:space:]]*\.category("\([^"]*\)")\n[[:space:]]*\.source("\([^"]*\)")\n[[:space:]]*\.destination(\([^)]*\))\n[[:space:]]*\.content(\([^)]*\))\n[[:space:]]*\.timestamp(\([^)]*\))\n[[:space:]]*\.build();/MessageDTO msg = new MessageDTO();\n                msg.setType("\1");\n                msg.setSource("\3");\n                msg.setTarget(\4);\n                msg.setPayload(\5);\n                msg.setTimestamp(LocalDateTime.ofInstant(Instant.ofEpochMilli(\6), ZoneOffset.UTC));/
}
EOF

# Apply fix to Discord adapter
cp adapters/src/main/java/com/integrixs/adapters/social/discord/DiscordInboundAdapter.java adapters/src/main/java/com/integrixs/adapters/social/discord/DiscordInboundAdapter.java.bak
sed -i -f /tmp/discord_fix.sed adapters/src/main/java/com/integrixs/adapters/social/discord/DiscordInboundAdapter.java

echo "Manual fix needed for Discord adapter - builder pattern too complex for sed"