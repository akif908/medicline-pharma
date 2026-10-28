---
name: medicline-pharma
description: Creates clinical, high-fidelity UI designs for the Medicline Pharma platform using a teal-accented dark mode aesthetic. Optimizes for professional healthcare software standards.
---

# Medicline Pharma Platform Skills

## Context & Goals
Implement a consistent, professional medical interface across three distinct user roles (Patient, Doctor, Admin). The goal is to maximize clarity of health information while maintaining a modern, premium tech aesthetic.

## Design Tokens
- **Backgrounds:** Primary: `#0F1115`; Secondary/Cards: `#1C1F26`.
- **Accents:** Primary: `#14B8A6` (Teal); Success: `#10B981`; Urgent: `#EF4444`.
- **Typography:** `font.family=Inter`.
- **Borders:** `1px solid rgba(255, 255, 255, 0.1)`.

## Component Guidelines
- **TopAppBar:** Fixed height (64px), backdrop-blur (12px), contains logo (left), nav links (center/left), and profile (right).
- **SideNavBar:** 256px width, icons + labels, active state uses teal glow or border.
- **Data Cards:** 16px border-radius, subtle hover lift (scale 1.02), clear categorization labels at top-left.
- **Chat Interface:** Message bubbles with distinct colors (Patient: Slate; Doctor: Teal), clear timestamping, attachment support.
- **Forms:** Large labels, clear validation states, primary action button bottom-right.

## Accessibility Requirements
- All text must meet AA contrast ratios.
- interactive elements must have a minimum size of 44x44px.
- Use `aria-labels` for icon-only actions (e.g., notifications, search).

## Content & Tone
- **Voice:** Professional, calm, trustworthy.
- **Terminology:** Accurate medical terminology (Prescriptions, Consultations, Vital Signs).
- **Feedback:** Provide instant visual confirmation for status changes (e.g., "Prescription Issued").

## Anti-Patterns
- **No Neon Clutter:** Avoid over-using teal; keep it for actions.
- **No Cramped Data:** Do not squeeze medical records; use white space to separate unrelated data points.
- **No Ambiguous Status:** Never use color alone to convey status (e.g., include text labels like "Approved" or "Pending").

## QA Checklist
- [ ] Contrast ratios checked for all text.
- [ ] Responsive behavior verified for desktop and mobile.
- [ ] Consistency in card padding and border-radius.
- [ ] Hover/Active states implemented for all buttons.
- [ ] "Empty states" handled for lists and histories.
