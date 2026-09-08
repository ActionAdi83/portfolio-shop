#!/usr/bin/env bash
#
# Adds every product found under a directory — one subfolder per product.
#
#   scripts/add-populated-products.sh scripts/populate
#
# Each subfolder is one product, named for its slug:
#
#   populate/aero-runner/about.txt   first line: display name, rest: description
#
# Idempotent, like fanvote's scripts/add-populated-profiles.sh: a rerun replaces
# each product (matched by name) instead of stacking duplicates. Price and stock
# are not part of about.txt (that only carries the copy a marketer would write),
# so they are looked up below by slug — edit PRICES/STOCK to change them, or edit
# the product afterwards from /admin.
#
# Reads the database name and any Mongo credentials from .env in the repo root,
# never hardcoded, same as fanvote's seeder.
set -euo pipefail

ENV_FILE="${ENV_FILE:-.env}"
MONGO_CONTAINER="${MONGO_CONTAINER:-portfolio-shop-mongo}"

ROOT="${1:-scripts/populate}"
[ -d "$ROOT" ] || { echo "$ROOT is not a directory" >&2; exit 1; }

if [ -f "$ENV_FILE" ]; then
    MONGO_DB=$(sed -n 's/^MONGO_DB=//p' "$ENV_FILE" | head -1)
    MONGO_USER=$(sed -n 's/^MONGO_USER=//p' "$ENV_FILE" | head -1)
    MONGO_PASSWORD=$(sed -n 's/^MONGO_PASSWORD=//p' "$ENV_FILE" | head -1)
fi
MONGO_DB="${MONGO_DB:-portfolio_shop}"

if [ -n "${MONGO_USER:-}" ]; then
    AUTH="-u $MONGO_USER -p $MONGO_PASSWORD --authenticationDatabase admin"
else
    AUTH=""
fi

# Escapes a value for use inside a double-quoted JS string literal. ${VAR@Q}
# breaks on apostrophes — bash renders those as 'it'\''s', which is not
# JavaScript — and product descriptions are exactly the kind of text that
# contains them.
js() { printf '%s' "$1" | sed 's/\\/\\\\/g; s/"/\\"/g'; }

# slug -> "price stock", since about.txt only carries name + copy.
price_and_stock() {
    case "$1" in
        aero-runner)      echo "89.99 40" ;;
        trail-blazer-x)   echo "119.99 25" ;;
        urbanflex-knit)   echo "74.50 60" ;;
        sprintpro-elite)  echo "159.00 15" ;;
        *)                echo "49.99 10" ;;
    esac
}

CATEGORY_NAME="Sport Shoes"
CATEGORY_SLUG="sport-shoes"

docker exec -i "$MONGO_CONTAINER" mongosh $AUTH --quiet "$MONGO_DB" <<EOF
db.categories.updateOne(
    { slug: "$CATEGORY_SLUG" },
    { \$set: { name: "$CATEGORY_NAME", slug: "$CATEGORY_SLUG" } },
    { upsert: true }
);
EOF

ADDED=0
for dir in "$ROOT"/*/; do
    [ -d "$dir" ] || continue
    SLUG=$(basename "$dir")
    ABOUT="$dir/about.txt"
    if [ ! -f "$ABOUT" ]; then
        echo "-- $SLUG: no about.txt, skipped" >&2
        continue
    fi
    NAME=$(head -1 "$ABOUT")
    DESCRIPTION=$(tail -n +2 "$ABOUT" | sed '/^$/d' | tr '\n' ' ' | sed 's/ *$//')
    read -r PRICE STOCK <<< "$(price_and_stock "$SLUG")"

    docker exec -i "$MONGO_CONTAINER" mongosh $AUTH --quiet "$MONGO_DB" <<EOF
const name        = "$(js "$NAME")";
const description = "$(js "$DESCRIPTION")";
const price        = $PRICE;
const stock         = $STOCK;

// Delete-then-insert per product (matched by name), so a rerun replaces
// rather than stacks duplicates — imageUrls is left empty on purpose; see
// scripts/populate/README.md.
db.products.deleteMany({ name: name });
db.products.insertOne({
    name: name,
    description: description,
    price: price,
    categorySlug: "$CATEGORY_SLUG",
    imageUrls: [],
    stock: stock,
    _class: "edu.portfolioshop.entities.Product"
});
print("-- " + name + ": price " + price + ", stock " + stock);
EOF

    ADDED=$((ADDED + 1))
done

echo ""
echo "$ADDED product(s) written to the '$CATEGORY_NAME' category."
