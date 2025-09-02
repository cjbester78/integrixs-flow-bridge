const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

console.log('Running aggressive cleanup of unused imports...');

// Get all TypeScript errors
const errors = execSync('npx tsc --project tsconfig.app.json --noEmit 2>&1 || true', { 
  encoding: 'utf8',
  maxBuffer: 10 * 1024 * 1024 // 10MB buffer
});

// Track all fixes
const stats = {
  filesProcessed: 0,
  importsRemoved: 0,
  variablesRemoved: 0,
  errors: []
};

// Parse all TS6133 and TS6196 errors (unused declarations and imports)
const unusedPattern = /(.+\.tsx?)\((\d+),(\d+)\): error TS6133: '(.+)' is declared but its value is never read\./g;
const allImportsUnusedPattern = /(.+\.tsx?)\((\d+),(\d+)\): error TS6192: All imports in import declaration are unused\./g;
const unusedImportPattern = /(.+\.tsx?)\((\d+),(\d+)\): error TS6196: '(.+)' is declared but never used\./g;

// Collect all fixes by file
const fileFixes = new Map();

console.log('Checking for unused patterns...');
console.log('Total errors length:', errors.length);
console.log('First 200 chars:', errors.substring(0, 200));

let matchCount = 0;
let match;
while ((match = unusedPattern.exec(errors)) !== null) {
  matchCount++;
  const [, file, line, col, identifier] = match;
  if (!fileFixes.has(file)) {
    fileFixes.set(file, []);
  }
  fileFixes.get(file).push({ 
    lineNum: parseInt(line), 
    identifier,
    type: 'unused'
  });
}

// Also handle TS6192 (all imports unused)
while ((match = allImportsUnusedPattern.exec(errors)) !== null) {
  const [, file, line, col] = match;
  if (!fileFixes.has(file)) {
    fileFixes.set(file, []);
  }
  fileFixes.get(file).push({ 
    lineNum: parseInt(line), 
    identifier: 'ALL_IMPORTS',
    type: 'all_unused'
  });
}

// Handle TS6196
while ((match = unusedImportPattern.exec(errors)) !== null) {
  const [, file, line, col, identifier] = match;
  if (!fileFixes.has(file)) {
    fileFixes.set(file, []);
  }
  fileFixes.get(file).push({ 
    lineNum: parseInt(line), 
    identifier,
    type: 'unused_import'
  });
}

console.log(`Found ${matchCount} unused patterns`);
console.log(`Files to process: ${fileFixes.size}`);
console.log('First file:', Array.from(fileFixes.keys())[0]);

// Process each file
for (const [file, fixes] of fileFixes) {
  try {
    if (!fs.existsSync(file)) {
      console.log(`File does not exist: ${file}`);
      continue;
    }
    
    let content = fs.readFileSync(file, 'utf8');
    const lines = content.split('\\n');
    console.log(`Processing ${file} with ${fixes.length} fixes...`);
    
    // Sort fixes by line number in reverse order
    fixes.sort((a, b) => b.lineNum - a.lineNum);
    
    if (file.includes('AdapterCard')) {
      console.log('AdapterCard fixes:', JSON.stringify(fixes, null, 2));
    }
    
    // Track which lines to remove
    const linesToRemove = new Set();
    let importsRemoved = 0;
    let varsRemoved = 0;
    
    for (const { lineNum, identifier, type } of fixes) {
      const lineIndex = lineNum - 1;
      if (lineIndex >= lines.length || lineIndex < 0) continue;
      
      const line = lines[lineIndex];
      console.log(`  Fix: line ${lineNum}, identifier: "${identifier}", type: ${type}`);
      console.log(`  Line content: "${line.trim()}"`)
      
      if (type === 'all_unused' || type === 'unused_import' || (line.includes('import') && line.includes(identifier))) {
        // Handle import statements
        if (identifier === 'ALL_IMPORTS' || line.match(new RegExp(`import\\\\s+${identifier}\\\\s+from`)) || line.match(new RegExp(`import\\\\s+\\\\*\\\\s+as\\\\s+${identifier}\\\\s+from`))) {
          // Remove entire import
          linesToRemove.add(lineIndex);
          importsRemoved++;
        } else if (line.includes('{') && line.includes('}')) {
          // Named import - try to remove just the identifier
          let newLine = line;
          
          // Remove the identifier with various comma positions
          const identifierRegex = new RegExp(`\\\\b${identifier.replace(/[.*+?^${}()|[\\]\\\\]/g, '\\\\$&')}\\\\b`);
          newLine = newLine.replace(new RegExp(`${identifierRegex.source}\\\\s*,\\\\s*`, 'g'), '');
          newLine = newLine.replace(new RegExp(`,\\\\s*${identifierRegex.source}`, 'g'), '');
          newLine = newLine.replace(identifierRegex, '');
          
          // Clean up any remaining commas
          newLine = newLine.replace(/,\\s*,/g, ',');
          newLine = newLine.replace(/{\\s*,/g, '{');
          newLine = newLine.replace(/,\\s*}/g, '}');
          newLine = newLine.replace(/{\\s*}/g, '{}');
          
          // Check if import is now empty
          if (newLine.match(/import\\s*{\\s*}\\s*from/) || newLine.match(/import\\s*{}\\s*from/)) {
            linesToRemove.add(lineIndex);
            importsRemoved++;
          } else if (newLine !== line) {
            lines[lineIndex] = newLine;
            importsRemoved++;
          }
        }
      } else if (line.includes(`const ${identifier}`) || 
                 line.includes(`let ${identifier}`) || 
                 line.includes(`var ${identifier}`)) {
        // Handle variable/function declarations
        // Check if it's a function declaration
        if (line.includes('= (') || line.includes('= function')) {
          // It's a function - need to find the closing brace
          let braceCount = 0;
          let endLine = lineIndex;
          let inFunction = false;
          
          for (let i = lineIndex; i < lines.length; i++) {
            const currentLine = lines[i];
            if (currentLine.includes('= (') || currentLine.includes('= function')) {
              inFunction = true;
            }
            
            if (inFunction) {
              for (const char of currentLine) {
                if (char === '{') braceCount++;
                if (char === '}') braceCount--;
              }
              
              if (braceCount === 0 && inFunction && currentLine.includes('}')) {
                endLine = i;
                break;
              }
            }
          }
          
          // Mark all lines from start to end for removal
          for (let i = lineIndex; i <= endLine; i++) {
            linesToRemove.add(i);
          }
          varsRemoved++;
        } else if (line.trim().endsWith(';') || line.trim().endsWith(',')) {
          // Simple variable declaration
          linesToRemove.add(lineIndex);
          varsRemoved++;
        }
      } else if (line.match(new RegExp(`^\\\\s*${identifier.replace(/[.*+?^${}()|[\\]\\\\]/g, '\\\\$&')}\\\\s*:\\\\s*`))) {
        // Handle function parameters or object properties that are unused
        // This is more complex, skip for now
      }
    }
    
    // Remove marked lines
    console.log(`  Lines to remove: ${linesToRemove.size}, imports: ${importsRemoved}, vars: ${varsRemoved}`);
    if (linesToRemove.size > 0 || importsRemoved > 0) {
      const newLines = lines.filter((_, index) => !linesToRemove.has(index));
      content = newLines.join('\\n');
      fs.writeFileSync(file, content);
      
      stats.filesProcessed++;
      stats.importsRemoved += importsRemoved;
      stats.variablesRemoved += varsRemoved;
      
      console.log(`✓ ${path.relative(process.cwd(), file)}: removed ${importsRemoved} imports, ${varsRemoved} variables`);
    }
  } catch (error) {
    stats.errors.push({ file, error: error.message });
    console.error(`✗ Error processing ${file}:`, error.message);
  }
}

// Print summary
console.log('\\n=== Cleanup Summary ===');
console.log(`Files processed: ${stats.filesProcessed}`);
console.log(`Imports removed: ${stats.importsRemoved}`);
console.log(`Variables removed: ${stats.variablesRemoved}`);
console.log(`Errors: ${stats.errors.length}`);

if (stats.errors.length > 0) {
  console.log('\\nErrors encountered:');
  stats.errors.forEach(({ file, error }) => {
    console.log(`  - ${file}: ${error}`);
  });
}