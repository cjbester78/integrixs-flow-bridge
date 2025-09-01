# TypeScript Errors Summary

## Total Errors: 505

### Error Breakdown by Type:

1. **TS6133** (225 errors) - Unused variables/imports
   - Variables declared but never read
   - Unused imports

2. **TS2339** (46 errors) - Property does not exist
   - API responses with incorrect type assumptions
   - Missing properties on objects

3. **TS2304** (26 errors) - Cannot find name
   - Missing imports (e.g., `toast`, `setChannels`)
   - Undefined variables

4. **TS2322** (25 errors) - Type assignment errors
   - Type mismatches in assignments

5. **TS2345** (22 errors) - Argument type mismatch
   - Function arguments with wrong types

6. **TS18048** (13 errors) - Possible undefined values
   - Missing null/undefined checks

7. **TS18046** (9 errors) - Implicit any type
   - Variables without explicit types

8. **TS7031** (6 errors) - Binding element implicitly has 'any' type
   - Destructured parameters without types

9. **TS6192** (6 errors) - Unused imports

10. **TS2551** (6 errors) - Property misspellings
    - Typos in property names

### Priority Areas to Fix:

1. **Remove unused imports/variables** (231 errors) - Quick wins
2. **Add missing imports** (26 errors) - Quick fixes
3. **Fix API response types** (46 errors) - Need proper type definitions
4. **Add null checks** (13 errors) - Safety improvements
5. **Fix type mismatches** (47 errors) - Requires understanding component contracts