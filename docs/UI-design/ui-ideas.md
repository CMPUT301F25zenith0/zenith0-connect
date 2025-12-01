# 🎨 Design Decisions

This document explains the **key visual and interaction design choices** made for the Zenith-Connect application.  
It captures the rationale behind colors, typography, buttons, and layout to ensure consistency and scalability in future design updates.

---

## 🎯 Design Goals

- Maintain a modern and welcoming look  
- Keep layouts consistent and easy to navigate  
- Support future visual updates without breaking design flow

---

## 🎨 Color Palette

### **Color Palette Overview**

The app uses a calm and modern color system centered around soft pinks, deep blues, clean neutrals, and bright accent colors. This palette supports readability, visual hierarchy, and a consistent user experience across both light and dark UI elements.

---

### **Core UI Colors**

| Element | Hex | Usage |
|--------|------|--------|
| **Light Background** | `#F9F9F9` | Main background for light mode |
| **Dark Background** | `#F8FAFC` | Subtle tinted background for depth |
| **Card Background** | `#FFFFFF` | Cards, containers, pop-ups |
| **Primary Text** | `#0F172A` | Titles, main readable text |
| **Primary Text (Light)** | `#000000` | Strong text on light backgrounds |
| **Secondary Text** | `#757575` / `#64748B` | Subtext, captions, hints |
| **Charcoal Text** | `#1A1A1A` | Bold dark text |

---

### **Accent & Action Colors**

| Element | Hex | Usage |
|---------|------|--------|
| **Mint Green Button** | `#93B185` | Primary action buttons (user pages) |
| **Accent Orange** | `#FF5722` | Key highlights & important actions |
| **Accent Orange Light** | `#FFCCBC` | Soft background emphasis |
| **Accent Green** | `#00C853` | Success indicators |
| **Accent Red** | `#D32F2F` | Errors, warnings, destructive actions |
| **Primary Gold** | `#B45309` | Lottery indicators & special UI states |
| **Interests Blue** | `#0C3B5E` | Category & interest tags |

---

### **Pink Tones**

| Element | Hex | Usage |
|---------|------|--------|
| **Mist Pink** | `#D8C2C2` | Soft highlight / passive backgrounds |
| **Warm Pink Light** | `#F5E6E8` | Decorative & calm UI sections |

---

### **Filter & Navigation Colors**

| Element | Hex | Usage |
|---------|------|--------|
| **Dark Blue** | `#1E3A5F` | Filter buttons, primary navigation |
| **Dark Blue Light** | `#2E4A7F` | Pressed/hover states |

---

### **Vision UI (Dark/Neon Theme)**  
*Used for experimental or high-contrast screens.*

| Element | Hex |
|---------|------|
| Vision Background | `#0F1535` |
| Vision Card Background | `#1F284E` |
| Vision Primary | `#2D3748` |
| Vision Accent Purple | `#582CFF` |
| Vision Accent Blue | `#0075FF` |
| Vision Accent Green | `#00E396` |
| Vision Text Primary | `#FFFFFF` |
| Vision Text Secondary | `#A0AEC0` |
| Vision Text Hint | `#718096` |
| Vision Error | `#FF5252` |

---

### **Gray Palette (Admin & System UI)**

| Shade | Hex | Usage |
|--------|------|--------|
| Gray 200 | `#E5E7EB` | Soft dividers, backgrounds |
| Gray 300 | `#E0E0E0` | Borders, cards |
| Gray 400 | `#BDBDBD` | Icons, disabled states |
| Gray 500 | `#6B7280` | Secondary admin text |
| Gray 600 | `#4B5563` | High-contrast components |
| Gray 700 | `#374151` | Strong admin UI elements |

---

### **QR Scanner Colors**

| Element | Hex | Usage |
|---------|------|--------|
| Laser Red | `#FF0000` | Scanner laser |
| Possible Points | `#C0FFBD` | Detected scan points |
| Mask Overlay | `#60000000` | Transparent overlay |
| Scanner Result | `#B0000000` | Result background |

---

### **Additional Text Color**

| Element | Hex |
|--------|------|
| **Search Text** | `#49454F` |

---

## 🔤 Typography

| Element | Font Family | Size | Weight | Usage |
|---------|-------------|------|---------|--------|
| **Headings (H1–H3)** | Roboto | 20–24sp | Bold | Event titles, section headers (from event details layout) |
| **Body Text** | Roboto | 14–16sp | Regular | Event descriptions, list item content, info fields |
| **Buttons / Labels** | Roboto | 14sp | Medium | Action buttons (Back, Interest, Create Event), detail labels |
| **Navigation Labels** | Roboto | 10sp | Medium | Bottom navigation (Home, Events, Scan, Profile, Alerts) |
| **Captions** | Roboto | 12sp | Light | Secondary labels, subtle info text |

---

## 🔘 Button Design

| Type | Style | Usage |
|------|--------|--------|
| **Primary Button** | Solid fill using `@color/primary_gold`, white text, Material TextButton style | High-importance actions (e.g., “Join Lottery”, “Create Event”) |
| **Secondary Button** | Transparent background, tinted icon & text (`@color/text_secondary`) | Low-priority actions (e.g., navigation bar buttons, view modes) |
| **Outline Button** | Border with primary color, transparent fill | Optional actions, info buttons on event detail pages |
| **Disabled Button** | Muted gray background, low-contrast text | Actions unavailable due to state conditions (capacity full, loading) |

---
_Last updated: [2025-12-1]_