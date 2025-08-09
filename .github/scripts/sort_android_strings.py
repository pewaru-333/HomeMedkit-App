import os
import sys
import traceback
import xml.etree.ElementTree as ET
from xml.sax.saxutils import escape
from pathlib import Path

RES_PATH = Path('./app/src/main/res')
BASE_STRINGS_FILE = RES_PATH / 'values/strings.xml'

PLURALS_QUANTITY_ORDER = {
    'zero': 0,
    'one': 1,
    'two': 2,
    'few': 3,
    'many': 5,
    'other': 11
}


def create_template_from_base_file(file_path: Path) -> list[dict]:
    template = []
    buffer = ""

    with open(file_path, 'r', encoding='utf-8') as f:
        for line in f:
            stripped_line = line.strip()

            if stripped_line.startswith(('<?xml', '<resources')):
                continue

            if stripped_line.startswith('</resources>'):
                break

            if not stripped_line:
                template.append(
                    {
                        'type': 'blank'
                    }
                )
                continue

            if stripped_line.startswith('<!--'):
                template.append(
                    {
                        'type': 'comment',
                        'content': line.rstrip()
                    }
                )
                continue

            buffer += line
            try:
                elem = ET.fromstring(buffer)
                indent = buffer[:len(buffer) - len(buffer.lstrip())]
                template.append(
                    {
                        'type': 'resource',
                        'key': elem.get('name'),
                        'indent': indent
                    }
                )
                buffer = ""
            except ET.ParseError:
                continue
    return template


def parse_translation_to_data(file_path: Path) -> dict[str, dict]:
    data_map = {}

    try:
        root = ET.parse(file_path).getroot()
        for elem in root:
            if 'name' not in elem.attrib: continue

            key, tag = elem.get('name'), elem.tag
            data_map[key] = {
                'tag': tag,
                'attribs': elem.attrib,
                'text': elem.text or '',
                'items': [{
                    'attribs': item.attrib,
                    'text': item.text or ''
                } for item in elem] if tag == 'plurals' else []
            }
    except ET.ParseError as e:
        print(f"Файл: {file_path}. Ошибка: {e}")
    return data_map


def build_resource_string(data: dict, indent: str) -> str:
    tag, attribs_str = data['tag'], ' '.join(f'{k}="{v}"' for k, v in data['attribs'].items())

    if tag == 'string':
        return f'{indent}<string {attribs_str}>{escape(data["text"])}</string>'

    if tag == 'plurals':
        lines = [f'{indent}<plurals {attribs_str}>']
        sorted_items = sorted(data['items'], key=lambda i: PLURALS_QUANTITY_ORDER.get(i['attribs'].get('quantity'), 99))
        item_indent = indent + "    "
        for item_data in sorted_items:
            item_attribs_str = ' '.join(f'{k}="{v}"' for k, v in item_data['attribs'].items())
            lines.append(f'{item_indent}<item {item_attribs_str}>{escape(item_data["text"])}</item>')
        lines.append(f'{indent}</plurals>')
        return '\n'.join(lines)
    return ""


def sort_android_strings():
    print(f"Базовый файл для сортировки: {BASE_STRINGS_FILE}")
    if not BASE_STRINGS_FILE.is_file(): sys.exit(0)

    try:
        base_template = create_template_from_base_file(BASE_STRINGS_FILE)
    except Exception as e:
        print(f"ОШИБКА при парсинге шаблона: {e}")
        sys.exit(1)

    any_changes = False

    for lang_file_path in list(RES_PATH.glob('values-*/strings.xml')):
        print(f"\n--- Обработка файла: {lang_file_path} ---")

        try:
            original_content = lang_file_path.read_text(encoding='utf-8')
            translation_data = parse_translation_to_data(lang_file_path)
            if not translation_data: continue

            new_lines = [
                '<?xml version="1.0" encoding="utf-8"?>',
                '<resources>'
            ]

            for item in base_template:
                item_type = item['type']

                if item_type == 'blank':
                    new_lines.append('')
                elif item_type == 'comment':
                    new_lines.append(item['content'])
                elif item_type == 'resource':
                    key = item['key']
                    if key in translation_data:
                        resource_data = translation_data[key]
                        new_lines.append(build_resource_string(resource_data, item['indent']))

            new_lines.append('</resources>')
            new_content = '\n'.join(new_lines) + '\n'

            if new_content.replace('\r\n', '\n') == original_content.replace('\r\n', '\n'):
                print("Структура файла идентична шаблону, пропуск.")
                continue

            lang_file_path.write_text(new_content, encoding='utf-8')
            print(f"Файл {lang_file_path} отсортирован и сохранен.")
            any_changes = True

        except Exception as e:
            print(f"ОШИБКА при обработке файла {lang_file_path}: {e}")
            traceback.print_exc()

    if 'GITHUB_OUTPUT' in os.environ:
        with open(os.environ['GITHUB_OUTPUT'], 'a') as f:
            print(f'changes_detected={str(any_changes).lower()}', file=f)


if __name__ == "__main__":
    sort_android_strings()
