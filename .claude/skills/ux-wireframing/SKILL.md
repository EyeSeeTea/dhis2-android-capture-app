---
name: ux-wireframing
description: >
  UX wireframing skill using Pencil Project. Teaches how to specify
  wireframes, define screen layouts, and produce wireframe documentation
  that Pencil Project can realize. Trigger on wireframe, mockup, screen
  design, user flow, or layout requests.
---

# UX Wireframing with Pencil Project

## About Pencil Project
Pencil Project is a free, open-source GUI prototyping tool for creating
wireframes, mockups, and prototypes. It runs on Windows, macOS, and Linux.
It provides built-in stencils for web, Android, and iOS UI elements.

## Wireframe Specification Format

Since Claude cannot directly manipulate Pencil's GUI, produce detailed
wireframe specifications that a human (or the next iteration of tooling)
can implement in Pencil:

### Screen Specification Template

For each screen, create a markdown file:
Screen: [Name]
Layout

Grid: [e.g., 12-column, max-width 1200px]
Sections: [header, hero, content, sidebar, footer]

Elements (top to bottom, left to right)

Header Bar (full width, 64px height)

Logo (left, 140x40px)
Navigation links (right): Home, Features, Pricing, Contact
CTA Button (right): "Sign Up" (primary color)


Hero Section (full width, 480px height)

Headline: [text]
Subheadline: [text]
CTA Button: [text] (centered)
Background: [description]



Interactions

Header: sticky on scroll
CTA Button → navigates to /signup
Navigation: hamburger menu on mobile (<768px)

Responsive Breakpoints

Desktop: > 1024px (3-column layout)
Tablet: 768-1024px (2-column)
Mobile: < 768px (single column, stacked)


## Exporting from Pencil
After creating wireframes in Pencil Project:
1. Export each screen as PNG (File → Export → PNG)
2. Save to `docs/designs/wireframes/exports/`
3. Name format: `[feature]-[screen]-[state].png`
   Example: `auth-login-default.png`, `auth-login-error.png`
