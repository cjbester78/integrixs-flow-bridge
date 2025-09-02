#!/usr/bin/env node

const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

console.log('Removing unused variables and imports...');

// Get TypeScript errors
const errors = execSync('npx tsc --project tsconfig.app.json --noEmit 2>&1 || true', { 
  encoding: 'utf8',
  maxBuffer: 10 * 1024 * 1024
});

// Parse TS6133 errors
const unusedPattern = /(.+\.tsx?)\((\d+),(\d+)\): error TS6133: '(.+)' is declared but its value is never read\./g;
const fixes = new Map();

let match;
while ((match = unusedPattern.exec(errors)) !== null) {
  const [, file, line, col, identifier] = match;
  if (!fixes.has(file)) {
    fixes.set(file, []);
  }
  fixes.get(file).push({ line: parseInt(line), identifier });
}

// Process each file
for (const [file, issues] of fixes) {
  if (!fs.existsSync(file)) continue;
  
  let content = fs.readFileSync(file, 'utf8');
  const lines = content.split('\n');
  
  // Sort by line number in reverse order
  issues.sort((a, b) => b.line - a.line);
  
  for (const { line, identifier } of issues) {
    const lineIndex = line - 1;
    if (lineIndex >= lines.length) continue;
    
    const lineContent = lines[lineIndex];
    
    // Remove unused functions
    if (lineContent.includes(`const ${identifier} =`) && lineContent.includes('=>')) {
      // Find the end of the function
      let endLine = lineIndex;
      let braceCount = 0;
      let inFunction = false;
      
      for (let i = lineIndex; i < lines.length; i++) {
        const currentLine = lines[i];
        for (const char of currentLine) {
          if (char === '{') braceCount++;
          if (char === '}') braceCount--;
        }
        
        if (currentLine.includes('=>')) inFunction = true;
        
        if (inFunction && braceCount === 0 && i > lineIndex) {
          endLine = i;
          break;
        }
      }
      
      // Remove all lines from start to end
      lines.splice(lineIndex, endLine - lineIndex + 1);
      console.log(`Removed function ${identifier} from ${file}`);
    }
    // Remove unused imports
    else if (lineContent.includes('import') && lineContent.includes(identifier)) {
      // Handle named imports
      if (lineContent.includes('{') && lineContent.includes('}')) {
        let newLine = lineContent;
        
        // Remove the identifier and clean up commas
        newLine = newLine.replace(new RegExp(`\\b${identifier}\\s*,\\s*`, 'g'), '');
        newLine = newLine.replace(new RegExp(`,\\s*${identifier}\\b`, 'g'), '');
        newLine = newLine.replace(new RegExp(`\\b${identifier}\\b`, 'g'), '');
        newLine = newLine.replace(/,\s*,/g, ',');
        newLine = newLine.replace(/{\s*,/g, '{');
        newLine = newLine.replace(/,\s*}/g, '}');
        
        // If import is empty, remove the line
        if (newLine.match(/import\s*{\s*}\s*from/)) {
          lines.splice(lineIndex, 1);
          console.log(`Removed empty import from ${file}`);
        } else {
          lines[lineIndex] = newLine;
          console.log(`Removed ${identifier} from import in ${file}`);
        }
      }
      // Remove entire import if it's a default/namespace import
      else if (lineContent.match(new RegExp(`import\\s+${identifier}\\s+from`))) {
        lines.splice(lineIndex, 1);
        console.log(`Removed import ${identifier} from ${file}`);
      }
    }
    // Remove unused variables
    else if (lineContent.match(new RegExp(`(const|let|var)\\s+${identifier}\\s*=`))) {
      lines.splice(lineIndex, 1);
      console.log(`Removed variable ${identifier} from ${file}`);
    }
  }
  
  // Write back
  fs.writeFileSync(file, lines.join('\n'));
}

console.log('Done!');