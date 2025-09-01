# Integrix Flow Bridge - Frontend Styling Guide

This document provides a comprehensive overview of the styling system used in the Integrix Flow Bridge application. Use this as a reference for maintaining consistency or adapting the design system to other applications.

## Design System Overview

- **CSS Framework**: TailwindCSS
- **Component Library**: shadcn/ui (built on Radix UI primitives)
- **Animation Library**: tailwindcss-animate
- **Styling Approach**: Utility-first CSS with component variants using class-variance-authority (CVA)
- **Theme**: Dark mode by default with professional blue accent colors

## Color System

All colors are defined in HSL format as CSS variables for easy theming.

### Core Theme Colors

```css
/* Dark Theme (Default) */
--background: 215 25% 8%;              /* Very dark blue background */
--foreground: 210 40% 98%;             /* Almost white text */

--card: 215 25% 10%;                   /* Slightly lighter than background */
--card-foreground: 210 40% 98%;        /* Light text on cards */

--popover: 215 25% 10%;                /* Popover background */
--popover-foreground: 210 40% 98%;     /* Popover text */

--primary: 217 91% 60%;                /* Bright blue - main brand color */
--primary-foreground: 215 25% 8%;      /* Dark text on primary */

--secondary: 215 20% 15%;              /* Dark blue-gray */
--secondary-foreground: 210 40% 98%;   /* Light text on secondary */

--muted: 215 20% 15%;                  /* Muted backgrounds */
--muted-foreground: 215 20% 70%;       /* Muted text color */

--accent: 217 78% 45%;                 /* Darker blue for accents */
--accent-foreground: 210 40% 98%;      /* Light text on accent */

--destructive: 0 84% 60%;              /* Red for errors/destructive actions */
--destructive-foreground: 210 40% 98%; /* Light text on destructive */

--border: 215 20% 20%;                 /* Border color */
--input: 215 20% 20%;                  /* Input border color */
--ring: 217 91% 60%;                   /* Focus ring color */
```

### Status Colors

```css
--success: 142 76% 36%;                /* Green for success states */
--success-foreground: 210 40% 98%;     /* Light text on success */

--warning: 38 92% 50%;                 /* Orange for warnings */
--warning-foreground: 215 25% 8%;      /* Dark text on warning */

--info: 199 89% 48%;                   /* Light blue for information */
--info-foreground: 210 40% 98%;        /* Light text on info */
```

### Gradient Definitions

```css
--gradient-primary: linear-gradient(135deg, hsl(217 91% 60%), hsl(217 78% 45%));
--gradient-secondary: linear-gradient(135deg, hsl(215 25% 10%), hsl(215 20% 15%));
--gradient-accent: linear-gradient(135deg, hsl(217 78% 45%), hsl(249 78% 55%));
```

## Typography

### Font System
- **Font Family**: System font stack (no custom fonts)
- **Base Font Size**: 16px (1rem)
- **Line Height**: Tailwind defaults

### Text Size Scale
```css
text-xs: 0.75rem      /* 12px - metadata, badges */
text-sm: 0.875rem     /* 14px - secondary text, labels */
text-base: 1rem       /* 16px - body text */
text-lg: 1.125rem     /* 18px - card titles */
text-xl: 1.25rem      /* 20px - section headers */
text-2xl: 1.5rem      /* 24px - smaller page titles */
text-3xl: 1.875rem    /* 30px - page titles */
text-4xl: 2.25rem     /* 36px - large headers */
```

### Font Weights
```css
font-normal: 400      /* Regular text */
font-medium: 500      /* Slight emphasis */
font-semibold: 600    /* Headings, important labels */
font-bold: 700        /* Strong emphasis */
```

### Common Text Styles
```css
/* Page Headers */
className="text-3xl font-bold text-foreground"

/* Card Titles */
className="text-lg font-semibold leading-none tracking-tight"

/* Labels */
className="text-sm font-medium"

/* Muted/Secondary Text */
className="text-sm text-muted-foreground"

/* Small Text/Metadata */
className="text-xs text-muted-foreground"
```

## Component Styles

### Buttons

Button component uses class-variance-authority (CVA) for variants:

```tsx
/* Variants */
default: "bg-primary text-primary-foreground hover:bg-primary/90"
destructive: "bg-destructive text-destructive-foreground hover:bg-destructive/90"
outline: "border border-input bg-background hover:bg-accent hover:text-accent-foreground"
secondary: "bg-secondary text-secondary-foreground hover:bg-secondary/80"
ghost: "hover:bg-accent hover:text-accent-foreground"
link: "text-primary underline-offset-4 hover:underline"

/* Sizes */
default: "h-10 px-4 py-2"
sm: "h-9 rounded-md px-3"
lg: "h-11 rounded-md px-8"
icon: "h-10 w-10"

/* Base button classes */
"inline-flex items-center justify-center gap-2 whitespace-nowrap rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50"
```

### Cards

```css
/* Card Container */
className="rounded-lg border bg-card text-card-foreground shadow-sm"

/* Card Header */
className="flex flex-col space-y-1.5 p-6"

/* Card Title */
className="text-lg font-semibold leading-none tracking-tight"

/* Card Description */
className="text-sm text-muted-foreground"

/* Card Content */
className="p-6 pt-0"

/* Card Footer */
className="flex items-center p-6 pt-0"
```

### Form Inputs

```css
/* Text Input */
className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-base ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium file:text-foreground placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 md:text-sm"

/* Select Dropdown */
className="flex h-10 w-full items-center justify-between rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
```

### Tables

```css
/* Table Container */
className="relative w-full overflow-auto"

/* Table */
className="w-full caption-bottom text-sm"

/* Table Header */
className="[&_tr]:border-b"

/* Table Row */
className="border-b transition-colors hover:bg-muted/50 data-[state=selected]:bg-muted"

/* Table Head Cell */
className="h-12 px-4 text-left align-middle font-medium text-muted-foreground"

/* Table Cell */
className="p-4 align-middle"
```

### Badges

```tsx
/* Badge Variants */
default: "border-transparent bg-primary text-primary-foreground hover:bg-primary/80"
secondary: "border-transparent bg-secondary text-secondary-foreground hover:bg-secondary/80"
destructive: "border-transparent bg-destructive text-destructive-foreground hover:bg-destructive/80"
outline: "text-foreground"
success: "border-transparent bg-green-100 text-green-800 hover:bg-green-100/80 dark:bg-green-900/30 dark:text-green-400"
warning: "border-transparent bg-yellow-100 text-yellow-800 hover:bg-yellow-100/80 dark:bg-yellow-900/30 dark:text-yellow-400"
info: "border-transparent bg-blue-100 text-blue-800 hover:bg-blue-100/80 dark:bg-blue-900/30 dark:text-blue-400"

/* Base badge classes */
"inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold transition-colors focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2"
```

## Layout & Spacing

### Spacing Scale
Uses Tailwind's default spacing scale (0, 1, 2, 3, 4, 5, 6, 8, 10, 12, 16, 20, 24, 32, 40, 48, 56, 64, 72, 80, 96)

### Common Spacing Patterns
```css
/* Page container */
className="min-h-full w-full px-6 md:px-8 py-6 space-y-6 overflow-auto animate-fade-in"

/* Card spacing */
padding: p-6
gap: gap-4, gap-6
space-y: space-y-4, space-y-6

/* Form spacing */
space-y-4 for form fields
gap-2 for inline elements
```

### Border Radius
```css
--radius: 0.75rem;                    /* Base radius value */
rounded-lg: var(--radius)             /* 0.75rem */
rounded-md: calc(var(--radius) - 2px) /* ~0.625rem */
rounded-sm: calc(var(--radius) - 4px) /* ~0.5rem */
rounded-full: 9999px                  /* Fully rounded */
```

## Effects & Animations

### Shadows
```css
--shadow-elegant: 0 10px 30px -10px hsl(217 91% 60% / 0.3);
--shadow-soft: 0 4px 12px -2px hsl(215 25% 8% / 0.4);

/* Usage */
shadow-sm: Default small shadow
shadow-elegant: Colored shadow for emphasis
shadow-soft: Subtle soft shadow
```

### Animations
```css
/* Available animations */
animate-fade-in: Fade in with slight upward movement
animate-scale-in: Scale from 0.95 to 1 with fade
animate-slide-up: Slide up from bottom
animate-glow: Pulsing glow effect
animate-float: Gentle floating motion
animate-spin: Continuous rotation (for loaders)

/* Hover effects */
hover-scale: Scale 1.05 on hover
hover:opacity-90: Slight transparency on hover
transition-all duration-300: Smooth transitions
```

### Animation Keyframes
```css
@keyframes fade-in {
  0% { opacity: 0; transform: translateY(20px); }
  100% { opacity: 1; transform: translateY(0); }
}

@keyframes scale-in {
  0% { transform: scale(0.95); opacity: 0; }
  100% { transform: scale(1); opacity: 1; }
}

@keyframes glow {
  0%, 100% { box-shadow: 0 0 20px hsl(var(--primary) / 0.5); }
  50% { box-shadow: 0 0 40px hsl(var(--primary) / 0.8); }
}
```

## Focus & Accessibility

### Focus States
```css
/* Default focus ring */
focus-visible:outline-none 
focus-visible:ring-2 
focus-visible:ring-ring 
focus-visible:ring-offset-2 
focus-visible:ring-offset-background

/* Links */
focus-visible:rounded-sm
```

### Disabled States
```css
disabled:pointer-events-none 
disabled:opacity-50
disabled:cursor-not-allowed
```

## Utility Classes

### Background Helpers
```css
.app-background: bg-background
.app-background-gradient: background: var(--gradient-secondary)
.app-card: bg-card border-border
```

### Gradient Classes
```css
.bg-gradient-primary: background: var(--gradient-primary)
.bg-gradient-secondary: background: var(--gradient-secondary)
.bg-gradient-accent: background: var(--gradient-accent)
.app-name-gradient: Gradient text effect
```

### Layout Helpers
```css
.page-container: Standard page layout with padding and animations
.content-spacing: space-y-6 for consistent vertical spacing
```

## Responsive Design

### Breakpoints
Uses Tailwind's default breakpoints:
- sm: 640px
- md: 768px
- lg: 1024px
- xl: 1280px
- 2xl: 1536px

### Container
```css
container: {
  center: true,
  padding: '2rem',
  screens: {
    '2xl': '1400px'
  }
}
```

### Mobile Considerations
- Mobile-first approach
- Touch-friendly tap targets (min 44px)
- Responsive text sizes (md:text-sm)
- Responsive padding (px-6 md:px-8)

## Best Practices

1. **Use semantic color variables** - Don't use raw color values
2. **Maintain consistent spacing** - Use the spacing scale
3. **Apply hover states** - All interactive elements should have hover feedback
4. **Include focus states** - Ensure keyboard navigation is visible
5. **Use animation sparingly** - Enhance UX without being distracting
6. **Follow component patterns** - Use existing component styles for consistency