import os

file_path = os.path.join(os.path.dirname(__file__), "randomRealMaplestoryIGNs.txt")

with open(file_path, "r", encoding="utf-8") as f:
    lines = f.readlines()

seen = set()
unique = []
duplicates = 0

for line in lines:
    name = line.rstrip("\n")
    if name in seen:
        duplicates += 1
    else:
        seen.add(name)
        unique.append(line)

with open(file_path, "w", encoding="utf-8") as f:
    f.writelines(unique)

print(f"Done. Removed {duplicates} duplicate(s). {len(unique)} unique names remain.")
