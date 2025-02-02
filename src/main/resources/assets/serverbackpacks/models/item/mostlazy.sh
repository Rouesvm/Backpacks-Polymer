#!/bin/bash

# List of colors
colors=(
    "white" "light_gray" "black" "red" "orange" "yellow"
    "lime" "green" "cyan" "light_blue" "blue" "purple" "magenta" "pink"
)

# Directory to save the files
output_dir="."
mkdir -p "$output_dir"

# Template for JSON content
for color in "${colors[@]}"; do
    filename="${output_dir}/${color}_large.json"
    cat <<EOF > "$filename"
{
  "parent": "item/handheld",
  "textures": {
    "layer0": "serverbackpacks:item/${color}_large"
  }
}
EOF
done

echo "Generated \${#colors[@]} JSON files in '\$output_dir' directory."
