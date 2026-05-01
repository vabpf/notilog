# Notilog Data Generation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Generate a fresh, up-to-date `categories_map.json` by scraping the live Google Play Store for the top 1,000 most popular apps.

**Architecture:** A standalone Python script using the `google-play-scraper` library.

**Tech Stack:** Python 3, `google-play-scraper`.

---

### Task 1: Setup & Dependencies

**Files:**
- Create: `scripts/requirements.txt`

- [ ] **Step 1: Create a `scripts` directory**
- [ ] **Step 2: Define dependencies**
    *   Add `google-play-scraper` to `requirements.txt`.
- [ ] **Step 3: Install dependencies**
    *   Run: `pip install -r scripts/requirements.txt`
- [ ] **Step 4: Commit**

```bash
git add scripts/requirements.txt
git commit -m "chore: add data generation script dependencies"
```

---

### Task 2: Implementation of Scraper

**Files:**
- Create: `scripts/generate_categories.py`

- [ ] **Step 1: Write the scraper logic**
    *   The script should fetch the top 1,000 "Free" apps across various collections (TOP_FREE, GROSSING, etc.).
    *   It should iterate through each app and extract its `packageName` and `category`.
    *   It should format the results into a JSON array: `[{"package": "...", "category": "..."}, ...]`.
- [ ] **Step 2: Add error handling and rate-limiting**
    *   Ensure the script doesn't crash on network errors.
    *   Add small sleeps between requests if necessary (though the library handles most of this).
- [ ] **Step 3: Run the script to generate the data**
    *   Output path: `app/src/main/res/raw/categories_map.json` (ensure directory exists).
- [ ] **Step 4: Commit the script and the generated data**

```bash
git add scripts/generate_categories.py app/src/main/res/raw/categories_map.json
git commit -m "feat: generate fresh categories_map.json from live Play Store"
```
