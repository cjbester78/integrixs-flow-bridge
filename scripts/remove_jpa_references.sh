#!/bin/bash

# Script to remove all JPA references from the codebase
echo "🧹 Starting systematic JPA removal..."

# Get all Java files with JPA annotations or imports
FILES=$(find /Users/cjbester/git/Integrixs-Flow-Bridge -name "*.java" | xargs grep -l -E "(@Entity|@Table|@Column|@Id|@OneToMany|@ManyToOne|@JoinColumn|javax\.persistence|jakarta\.persistence|@Transactional)" | grep -v target)

echo "Found $(echo "$FILES" | wc -l) files with JPA references"

for file in $FILES; do
    echo "🔧 Processing: $file"
    
    # Remove JPA imports
    sed -i '' '/import javax\.persistence\./d' "$file"
    sed -i '' '/import jakarta\.persistence\./d' "$file"
    sed -i '' '/import org\.springframework\.transaction\.annotation\.Transactional/d' "$file"
    
    # Remove JPA annotations (keeping the class/field/method declaration)
    sed -i '' '/@Entity/d' "$file"
    sed -i '' '/@Table.*$/d' "$file"
    sed -i '' '/@Column.*$/d' "$file"
    sed -i '' '/@Id$/d' "$file"
    sed -i '' '/@OneToMany.*$/d' "$file"
    sed -i '' '/@ManyToOne.*$/d' "$file"
    sed -i '' '/@JoinColumn.*$/d' "$file"
    sed -i '' '/@Transactional/d' "$file"
    
    # Remove JPA-specific annotations that span multiple lines
    sed -i '' '/^[[:space:]]*@Entity/,/^[[:space:]]*)/d' "$file"
    sed -i '' '/^[[:space:]]*@Table/,/^[[:space:]]*)/d' "$file"
    sed -i '' '/^[[:space:]]*@OneToMany/,/^[[:space:]]*)/d' "$file"
    sed -i '' '/^[[:space:]]*@ManyToOne/,/^[[:space:]]*)/d' "$file"
    sed -i '' '/^[[:space:]]*@JoinColumn/,/^[[:space:]]*)/d' "$file"
    
    echo "   ✅ Processed $file"
done

# Now check pom.xml files for JPA dependencies
echo "🔧 Checking pom.xml files for JPA dependencies..."
POM_FILES=$(find /Users/cjbester/git/Integrixs-Flow-Bridge -name "pom.xml" | grep -v target)

for pom in $POM_FILES; do
    if grep -q "spring-boot-starter-data-jpa" "$pom"; then
        echo "⚠️  Found JPA dependency in: $pom"
        # We already removed it from data-access/pom.xml, so just report others
    fi
done

echo "✅ JPA removal completed!"
echo "📊 Summary:"
echo "   - Processed $(echo "$FILES" | wc -l) Java files"
echo "   - Removed @Entity, @Table, @Column, @Id, @OneToMany, @ManyToOne, @JoinColumn, @Transactional annotations"
echo "   - Removed javax.persistence and jakarta.persistence imports"
echo "   - Checked $(echo "$POM_FILES" | wc -l) pom.xml files"