const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

console.log('Running final cleanup of unused imports...');

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

// Parse all TS6133 errors (unused declarations)
const unusedPattern = /(.+\.tsx?)\((\d+),(\d+)\): error TS6133: '(.+)' is declared but its value is never read\./g;
const allImportsUnusedPattern = /(.+\.tsx?)\((\d+),(\d+)\): error TS6192: All imports in import declaration are unused\./g;

// Collect all fixes by file
const fileFixes = new Map();

let match;
while ((match = unusedPattern.exec(errors)) !== null) {
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
errors.replace(allImportsUnusedPattern, (match, file, line, col) => {
  if (!fileFixes.has(file)) {
    fileFixes.set(file, []);
  }
  fileFixes.get(file).push({ 
    lineNum: parseInt(line), 
    identifier: 'ALL_IMPORTS',
    type: 'all_unused'
  });
  return match;
});

// Process each file
for (const [file, fixes] of fileFixes) {
  try {
    if (!fs.existsSync(file)) continue;
    
    let content = fs.readFileSync(file, 'utf8');
    const lines = content.split('\n');
    
    // Sort fixes by line number in reverse order
    fixes.sort((a, b) => b.lineNum - a.lineNum);
    
    // Track which lines to remove
    const linesToRemove = new Set();
    let importsRemoved = 0;
    let varsRemoved = 0;
    
    for (const { lineNum, identifier, type } of fixes) {
      const lineIndex = lineNum - 1;
      if (lineIndex >= lines.length || lineIndex < 0) continue;
      
      const line = lines[lineIndex];
      
      if (type === 'all_unused' || (line.includes('import') && line.includes(identifier))) {
        // Handle import statements
        if (identifier === 'ALL_IMPORTS' || line.match(new RegExp(`import\\s+${identifier}\\s+from`))) {
          // Remove entire import
          linesToRemove.add(lineIndex);
          importsRemoved++;
        } else if (line.includes('{') && line.includes('}')) {
          // Named import - try to remove just the identifier
          let newLine = line;
          
          // Remove the identifier with various comma positions
          newLine = newLine.replace(new RegExp(`\\b${identifier}\\s*,\\s*`, 'g'), '');
          newLine = newLine.replace(new RegExp(`,\\s*${identifier}\\b`, 'g'), '');
          newLine = newLine.replace(new RegExp(`\\b${identifier}\\b`, 'g'), '');
          
          // Clean up any remaining commas
          newLine = newLine.replace(/,\s*,/g, ',');
          newLine = newLine.replace(/{\s*,/g, '{');
          newLine = newLine.replace(/,\s*}/g, '}');
          
          // Check if import is now empty
          if (newLine.match(/import\s*{\s*}\s*from/) || newLine.match(/import\s*{}\s*from/)) {
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
        // Handle variable declarations
        // Only remove if it's a simple declaration
        if (line.trim().endsWith(';') || line.trim().endsWith(',')) {
          linesToRemove.add(lineIndex);
          varsRemoved++;
        }
      }
    }
    
    // Remove marked lines
    if (linesToRemove.size > 0) {
      const newLines = lines.filter((_, index) => !linesToRemove.has(index));
      content = newLines.join('\n');
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
console.log('\n=== Cleanup Summary ===');
console.log(`Files processed: ${stats.filesProcessed}`);
console.log(`Imports removed: ${stats.importsRemoved}`);
console.log(`Variables removed: ${stats.variablesRemoved}`);
console.log(`Errors: ${stats.errors.length}`);

if (stats.errors.length > 0) {
  console.log('\nErrors encountered:');
  stats.errors.forEach(({ file, error }) => {
    console.log(`  - ${file}: ${error}`);
  });
}