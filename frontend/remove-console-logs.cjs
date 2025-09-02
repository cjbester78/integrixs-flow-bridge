#!/usr/bin/env node

const fs = require('fs');
const path = require('path');
const glob = require('glob');

// Patterns to remove
const patterns = [
  /console\.(log|error|warn|info|debug)\([^)]*\);?\s*\n?/g,
  /console\.(log|error|warn|info|debug)\([^)]*\),?\s*$/gm,
  /console\.(log|error|warn|info|debug)\s*\([^)]*\)[,;]?\s*\n/g,
  // Multi-line console statements
  /console\.(log|error|warn|info|debug)\s*\([^)]*\n[^)]*\);?\s*\n?/g,
];

// Files to process
const files = glob.sync('src/**/*.{js,jsx,ts,tsx}', {
  ignore: ['**/node_modules/**', '**/dist/**', '**/build/**']
});

let totalRemoved = 0;
let filesModified = 0;

files.forEach(file => {
  const content = fs.readFileSync(file, 'utf8');
  let modified = content;
  let removedInFile = 0;

  patterns.forEach(pattern => {
    const matches = modified.match(pattern) || [];
    removedInFile += matches.length;
    modified = modified.replace(pattern, '');
  });

  // Clean up extra blank lines
  modified = modified.replace(/\n\s*\n\s*\n/g, '\n\n');

  if (modified !== content) {
    fs.writeFileSync(file, modified);
    filesModified++;
    totalRemoved += removedInFile;
    console.log(`✓ ${file} - removed ${removedInFile} console statements`);
  }
});

console.log(`\n✅ Complete! Removed ${totalRemoved} console statements from ${filesModified} files.`);