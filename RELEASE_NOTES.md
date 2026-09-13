# Savr v1.3.8

## What's new

- **Sub-collections.** Organize collections inside collections — create a sub-collection right from any collection and drill down as deep as you like.
- **Full-text search inside collections.** Search is no longer limited to Home — open any collection and search its bookmarks by title, link, or description, and results include everything inside its sub-collections too.
- **Smarter in-collection search.** Searching inside a collection now checks the same fields as Home search (title, URL, description), so you actually find what you're looking for.
- **Remembered sort order.** Pick "Date added (oldest first)", "Title (A-Z)", etc. and Savr remembers it — no more resetting to newest-first every time you reopen the app.
- **Confirmation before deleting.** Deleting bookmarks, deleting collections, or removing bookmarks from a collection now asks "are you sure?" first, with a clear Cancel/Delete dialog — no more accidental wipes.
- **Metadata fetched on JSON import.** Importing a backup now automatically pulls in any missing titles, descriptions, and preview images.
- **Top bar shows where you are.** The title correctly updates to the collection you're viewing when you navigate back through sub-collections.

## Bugs fixed

- **Collection ordering.** Collections and their bookmarks keep the order you chose — no more jumping around after reopening or navigating.
- **In-collection search fields.** Search inside a collection was looking at the wrong fields and missing results — now fixed.
- **Collection grid layout.** The collection grid renders cleanly with correct card sizing and spacing.
- **Backup import restores collection links.** Importing a backup now correctly re-attaches bookmarks to their collections, including the new sub-collections.
- **Nothing is lost on upgrade.** All existing bookmarks, collections, pins, and links migrate safely to this version.

## Files

- `app-release.apk` — signed installable APK (GitHub direct downloads)
- `app-release.aab` — signed App Bundle (for the Play Store)