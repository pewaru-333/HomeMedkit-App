import sys
import subprocess

def main():
    version_code = sys.argv[1]

    changelog_path = f"fastlane/metadata/android/ru/changelogs/{version_code}.txt"
    try:
        with open(changelog_path, 'r', encoding='utf-8') as f:
            lines = f.readlines()
    except FileNotFoundError:
        sys.exit(1)

    transformed_lines = []
    for line in lines:
        if line.strip().startswith(('-', '—')):
            transformed_lines.append(line.strip().replace("-", "*").replace("—", "*"))
        else:
            transformed_lines.append(line)

    changelog_content = "\n".join(transformed_lines).strip()

    print(changelog_content)


if __name__ == "__main__":
    main()