#!/bin/bash

# Fix duplicate methods in FacebookMessengerInboundAdapter
echo "Fixing FacebookMessengerInboundAdapter duplicate methods..."

# Remove duplicate getTimestamp methods (keep only the first one)
file="/Users/cjbester/git/Integrixs-Flow-Bridge/adapters/src/main/java/com/integrixs/adapters/social/facebook/FacebookMessengerInboundAdapter.java"

# Create a temporary file with line numbers of duplicate methods to remove
grep -n "public String getTimestamp()" "$file" | tail -n +2 | cut -d: -f1 > /tmp/timestamp_lines.txt
grep -n "public void setTimestamp(String" "$file" | tail -n +2 | cut -d: -f1 >> /tmp/timestamp_lines.txt
grep -n "public String getSenderId()" "$file" | tail -n +2 | cut -d: -f1 >> /tmp/senderid_lines.txt
grep -n "public void setSenderId(String" "$file" | tail -n +2 | cut -d: -f1 >> /tmp/senderid_lines.txt
grep -n "public String getRecipientId()" "$file" | tail -n +2 | cut -d: -f1 >> /tmp/recipientid_lines.txt
grep -n "public void setRecipientId(String" "$file" | tail -n +2 | cut -d: -f1 >> /tmp/recipientid_lines.txt
grep -n "public String getPayload()" "$file" | tail -n +2 | cut -d: -f1 >> /tmp/payload_lines.txt

# Sort and merge all line numbers
cat /tmp/*_lines.txt | sort -n | uniq > /tmp/all_lines.txt

# Remove duplicate methods by commenting them out
while read -r line_num; do
    if [ ! -z "$line_num" ]; then
        # Comment out the method (3 lines: method signature, body, closing brace)
        sed -i '' "${line_num}s/^/\/\/ DUPLICATE: /" "$file"
        next_line=$((line_num + 1))
        sed -i '' "${next_line}s/^/\/\/ /" "$file"
        close_line=$((line_num + 2))
        sed -i '' "${close_line}s/^/\/\/ /" "$file"
    fi
done < /tmp/all_lines.txt

# Clean up
rm -f /tmp/*_lines.txt

echo "Fixed FacebookMessengerInboundAdapter"

# Fix duplicate methods in RedditInboundAdapter
echo "Fixing RedditInboundAdapter duplicate methods..."
file="/Users/cjbester/git/Integrixs-Flow-Bridge/adapters/src/main/java/com/integrixs/adapters/social/reddit/RedditInboundAdapter.java"

# Remove second getConfig method
sed -i '' '674s/public RedditApiConfig getConfig()/\/\/ DUPLICATE: public RedditApiConfig getConfig()/' "$file"
sed -i '' '675,677s/^/\/\/ /' "$file"

# Remove duplicate getId, setId, getAuthor, setAuthor, etc methods
for method in "getId" "setId" "getAuthor" "setAuthor" "getCreated" "setCreated" "getSubreddit" "setSubreddit" "getBody" "setBody"; do
    grep -n "public.*$method(" "$file" | tail -n +2 | cut -d: -f1 | while read -r line_num; do
        if [ ! -z "$line_num" ]; then
            sed -i '' "${line_num}s/^/\/\/ DUPLICATE: /" "$file"
            next_line=$((line_num + 1))
            sed -i '' "${next_line}s/^/\/\/ /" "$file"
            close_line=$((line_num + 2))
            sed -i '' "${close_line}s/^/\/\/ /" "$file"
        fi
    done
done

echo "Fixed RedditInboundAdapter"

# Fix duplicate getConfig in PinterestInboundAdapter
echo "Fixing PinterestInboundAdapter..."
file="/Users/cjbester/git/Integrixs-Flow-Bridge/adapters/src/main/java/com/integrixs/adapters/social/pinterest/PinterestInboundAdapter.java"
sed -i '' '558s/public PinterestApiConfig getConfig()/\/\/ DUPLICATE: public PinterestApiConfig getConfig()/' "$file"
sed -i '' '559,561s/^/\/\/ /' "$file"

echo "Done fixing duplicate methods"