#!/usr/bin/env node

const fs = require('fs');
const path = require('path');
const glob = require('glob');

// Find all TypeScript/TSX files  
const files = glob.sync('src/**/*.{ts,tsx}', {
  cwd: __dirname,
  absolute: true,
  ignore: ['**/node_modules/**', '**/*.d.ts']
});

let totalFixed = 0;

files.forEach(filePath => {
  try {
    let content = fs.readFileSync(filePath, 'utf8');
    let originalContent = content;
    
    // Fix JSX return statements with semicolon before opening parenthesis
    content = content.replace(/return\s*\(;/g, 'return (');
    
    // Fix arrow functions with semicolon before JSX
    content = content.replace(/=>\s*\(;/g, '=> (');
    
    // Fix JSX elements with trailing semicolons
    content = content.replace(/(<\/\w+>)\s*;(\s*<)/g, '$1$2');
    
    // Fix self-closing JSX elements with semicolons
    content = content.replace(/(\/>)\s*;(\s*<)/g, '$1$2');
    
    // Fix unterminated string literals in replace calls
    content = content.replace(/\.replace\('([^']*)',\s*'\)/g, ".replace('$1', '')");
    
    // Fix empty string assignments
    content = content.replace(/\|\|\s*'/g, "|| '");
    content = content.replace(/=\s*'/g, "= '");
    
    // Fix ternary operators with semicolons
    content = content.replace(/(\w+)\s+instanceof\s+(\w+);/g, '$1 instanceof $2');
    
    // Fix missing semicolons after method calls
    content = content.replace(/^(\s*)([\w.]+\([^)]*\))\s*$/gm, (match, indent, call) => {
      // Skip if it's followed by a dot (chained call) or already has semicolon
      const nextLineMatch = content.substring(content.indexOf(match) + match.length).match(/^\s*[.;]/);
      if (nextLineMatch) return match;
      return `${indent}${call};`;
    });
    
    // Fix missing semicolons after assignments
    content = content.replace(/^(\s*)(const|let|var)\s+(\w+)\s*=\s*([^;{}\n]+)\s*$/gm, '$1$2 $3 = $4;');
    
    // Only write if changed
    if (content !== originalContent) {
      fs.writeFileSync(filePath, content, 'utf8');
      totalFixed++;
    }
  } catch (error) {
    console.error(`Error processing ${filePath}:`, error.message);
  }
});

console.log(`Fixed JSX return statements in ${totalFixed} files`);