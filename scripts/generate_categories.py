import json
import os
import time
from google_play_scraper import app, search

# Configuration
KEYWORDS = [
    "social", "messenger", "communication", "finance", "banking", 
    "shopping", "ecommerce", "entertainment", "video", "music", 
    "productivity", "tools", "utility", 
    "news", "health", "fitness", "travel", "education"
]

# Vietnamese specific keywords to ensure coverage
VN_KEYWORDS = [
    "Zalo", "Vietcombank", "Techcombank", "MB Bank", "Agribank", 
    "BIDV", "VIB", "TPBank", "VPBank", "MoMo", "ZaloPay"
]

APPS_PER_KEYWORD = 50
OUTPUT_PATH = "app/src/main/res/raw/categories_map.json"

def main():
    print(f"Starting refined data generation for Notilog...")
    
    seen_packages = set()
    app_mappings = []

    # 1. Process General Keywords (US/Global)
    for keyword in KEYWORDS:
        print(f"Searching for global '{keyword}'...")
        process_keyword(keyword, 'us', 'en', seen_packages, app_mappings)

    # 2. Process Vietnamese Keywords (VN)
    for keyword in VN_KEYWORDS:
        print(f"Searching for VN specific '{keyword}'...")
        process_keyword(keyword, 'vn', 'vi', seen_packages, app_mappings)

    # Ensure directory exists
    os.makedirs(os.path.dirname(OUTPUT_PATH), exist_ok=True)

    # Save to JSON
    with open(OUTPUT_PATH, "w") as f:
        json.dump(app_mappings, f, indent=2)

    print(f"Successfully generated {len(app_mappings)} mappings at {OUTPUT_PATH}")

def process_keyword(keyword, country, lang, seen_packages, app_mappings):
    try:
        results = search(keyword, n_hits=APPS_PER_KEYWORD, country=country, lang=lang)
        for entry in results:
            package_name = entry.get('appId')
            if not package_name or package_name in seen_packages:
                continue
            
            # Fetch full details to get reliable category
            try:
                time.sleep(0.1)
                details = app(package_name, lang=lang, country=country)
                category = details.get('genreId', details.get('genre', "Uncategorized"))
                
                # 1. EXCLUDE GAMES
                if "GAME" in category.upper():
                    # print(f"  Skipping game: {package_name} ({category})")
                    continue

                app_mappings.append({
                    "package": package_name,
                    "category": category
                })
                seen_packages.add(package_name)
            except Exception as e:
                # print(f"  Error fetching details for {package_name}: {e}")
                pass
                
        print(f"  Current unique non-game apps: {len(app_mappings)}")
    except Exception as e:
        print(f"Error searching for '{keyword}': {e}")

if __name__ == "__main__":
    main()
