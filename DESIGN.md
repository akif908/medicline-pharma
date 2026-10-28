---
name: Medicline Pharma
colors:
  surface: '#0e1513'
  surface-dim: '#0e1513'
  surface-bright: '#343b39'
  surface-container-lowest: '#090f0e'
  surface-container-low: '#161d1b'
  surface-container: '#1a211f'
  surface-container-high: '#252b2a'
  surface-container-highest: '#2f3634'
  on-surface: '#dde4e1'
  on-surface-variant: '#bbcac6'
  inverse-surface: '#dde4e1'
  inverse-on-surface: '#2b3230'
  outline: '#859490'
  outline-variant: '#3c4947'
  surface-tint: '#4fdbc8'
  primary: '#4fdbc8'
  on-primary: '#003731'
  primary-container: '#14b8a6'
  on-primary-container: '#00423b'
  inverse-primary: '#006b5f'
  secondary: '#c0c1ff'
  on-secondary: '#1000a9'
  secondary-container: '#3131c0'
  on-secondary-container: '#b0b2ff'
  tertiary: '#4edea3'
  on-tertiary: '#003824'
  tertiary-container: '#16bb83'
  on-tertiary-container: '#00442c'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#71f8e4'
  primary-fixed-dim: '#4fdbc8'
  on-primary-fixed: '#00201c'
  on-primary-fixed-variant: '#005048'
  secondary-fixed: '#e1e0ff'
  secondary-fixed-dim: '#c0c1ff'
  on-secondary-fixed: '#07006c'
  on-secondary-fixed-variant: '#2f2ebe'
  tertiary-fixed: '#6ffbbe'
  tertiary-fixed-dim: '#4edea3'
  on-tertiary-fixed: '#002113'
  on-tertiary-fixed-variant: '#005236'
  background: '#0e1513'
  on-background: '#dde4e1'
  surface-variant: '#2f3634'
typography:
  headline-xl:
    fontFamily: Inter
    fontSize: 40px
    fontWeight: '700'
    lineHeight: '1.2'
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '600'
    lineHeight: '1.25'
    letterSpacing: -0.01em
  headline-md:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: '1.3'
  body-lg:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '400'
    lineHeight: '1.6'
  body-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: '1.6'
  label-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '500'
    lineHeight: '1.4'
    letterSpacing: 0.01em
  label-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '600'
    lineHeight: '1.4'
    letterSpacing: 0.05em
rounded:
  sm: 0.5rem
  DEFAULT: 1rem
  md: 1.5rem
  lg: 2rem
  xl: 3rem
  full: 9999px
spacing:
  unit: 8px
  gutter: 24px
  margin-mobile: 16px
  margin-desktop: 40px
  container-max: 1280px
---

## Brand & Style
The brand identity centers on "Precision Care, Redefined," targeting a sophisticated demographic that values the intersection of high-end medical expertise and cutting-edge technology. The visual style is a refined **Glassmorphic-Cybernetic** hybrid. It utilizes deep charcoal surfaces, vibrant teal accents, and layered translucency to evoke a sense of clinical sterility mixed with digital innovation. The emotional response should be one of calm confidence, high-tech reliability, and premium accessibility. Key motifs include luminous glows, subtle pulse animations for "live" status, and ultra-sharp iconography.

## Colors
The palette is dominated by a "Deep Space" neutral base, allowing the **Teal 500 (Primary)** to act as a high-frequency signal for actions and branding. A secondary **Indigo 500** is used sparingly for specialized medical tiers (e.g., Neurology). 

- **Primary (#14B8A6):** Used for primary buttons, search focus, and active navigation states.
- **Surface Strategy:** Backgrounds utilize a near-black green (#0E1513), while elevated panels use a slightly lighter slate (#1C1F26).
- **Glass Effects:** UI elements often use semi-transparent white overlays (5-10% opacity) to create depth without introducing new heavy colors.
- **Semantic Accents:** Emerald is reserved for "In Stock" and "Online" statuses, while Amber indicates urgency or low availability.

## Typography
The system relies exclusively on **Inter** to maintain a systematic, utilitarian aesthetic that emphasizes legibility in a medical context. 
- **Scale:** High contrast between Headline XL (for hero statements) and Body MD (for general information).
- **Hierarchy:** Use uppercase with increased letter spacing for "overlines" or micro-labels (e.g., "FUTURE OF HEALTHCARE").
- **Coloration:** Primary headings use the surface's "on-background" color (#DDE4E1), while secondary descriptions use a muted Slate 400 to reduce visual noise.

## Layout & Spacing
The layout follows a **Fixed-Width Centered Grid** for the primary content (max-width 1280px) to ensure a controlled, editorial reading experience. 
- **Grid:** A 12-column structure is implied, with major sections using an 8+4 split for "Main Action" vs "Side Stats" or 2-column/4-column grids for card galleries.
- **Rhythm:** A strict 8px base unit (8, 16, 24, 40, 64, 80) governs padding and margins. Large section gaps (80px - 96px) are used to separate major content blocks like the Hero, Store, and Consultation sections.

## Elevation & Depth
Depth is created through **Glassmorphism** and **Luminous Tonal Layering** rather than traditional drop shadows.
- **Glass Panels:** Backgrounds use `rgba(28, 31, 38, 0.7)` with a high backdrop blur (20px) and a thin 1px border (`white/8%`) to simulate physical glass.
- **Glows:** Primary elements (like the Hero section or active buttons) use a `glow-teal` effect: a soft, diffused outer shadow with the primary color at 15% opacity (`0 0 30px rgba(20, 184, 166, 0.15)`).
- **Z-Axis:** The navigation bar is the highest level (Z-50), utilizing heavy blur and a bottom border to remain distinct during scroll.

## Shapes
The shape language is "Hyper-Rounded," leaning into an approachable and organic feel despite the tech-heavy theme. 
- **Large Containers:** Hero sections and major callouts use massive `2.5rem` (40px) or `2rem` (32px) radii.
- **Standard Cards:** Medicine and doctor cards use `1.5rem` (24px) or `rounded-3xl`.
- **Interactive Elements:** Buttons and input fields use `0.75rem` (12px) to `1rem` (16px), while "pills" (status indicators and search bars) are fully rounded (9999px).

## Components
- **Buttons:**
    - *Primary:* Solid Teal background, white text, 12px radius, with a subtle teal glow.
    - *Ghost/Secondary:* Bordered with `white/10%`, semi-transparent white background (`white/5%`), high blur.
- **Cards:**
    - Medical Item cards feature high-aspect-ratio images with an "inner-glow" on hover and a bottom-aligned metadata section. 
    - Consultation cards feature a 4px left-accent border matching the doctor's specialty color (Teal or Indigo).
- **Inputs:**
    - Search bars are fully rounded (pill), using a darker surface (`surface-container-low`) with no border, becoming active via a teal focus ring.
- **Status Indicators:**
    - "Online" indicators use a "Pulse" animation—a green dot with an expanding, fading ring to denote real-time activity.
- **Navigation:**
    - Desktop nav features "Active" states indicated by a 2px bottom border in the primary color, while mobile nav uses a docked glass bar with vertical icon-label stacks.