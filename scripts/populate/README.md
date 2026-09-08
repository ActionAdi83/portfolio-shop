# Demo catalogue

Four demo sport shoes that make the shop look like a real catalogue. Each
folder is one product; `about.txt` holds the display name (first line) and
the marketing description (the rest).

No product photos are included here on purpose — sourcing real shoe photos
is out of scope for this seeder, and fabricating placeholder binaries isn't
worth committing. `imageUrls` is written empty for every seeded product;
the frontend's product cards show a "No image yet" placeholder instead of a
broken image, so the site still looks intentional. To add real photos later,
either edit a product from `/admin` and paste in hosted image URLs, or extend
the seeder to upload local files the way `fanvote`'s
`scripts/add-populated-profiles.sh` does for its profile pictures.

## Importing

From the repo root, with the stack's Mongo container running
(`docker compose up -d mongo`):

    scripts/add-populated-products.sh scripts/populate

Reads the database name and any Mongo credentials from `.env` in the repo
root (never hardcoded), and writes into the `portfolio-shop-mongo` container
by default — override with `MONGO_CONTAINER` if you renamed it.

One category is created as a side effect: `sport-shoes` / "Sport Shoes". All
four demo products are filed under it.

Idempotent: rerunning replaces each product (matched by name) rather than
adding duplicates, so it's safe to run again after editing `about.txt`.
