"""
Исследование влияния индексов на скорость выполнения запросов.

Методология:
  Для каждого объёма таблицы orders (SIZES строк):
    1. Заполняем таблицу заказами.
    2. Выполняем запрос N_REPS раз с индексом  → берём медиану времени выполнения.
    3. Удаляем индекс, выполняем запрос N_REPS раз без него → берём медиану.
    4. Восстанавливаем индекс.

Время измеряется через EXPLAIN (ANALYZE, TIMING) — это время на стороне PostgreSQL
без учёта накладных расходов Python/сети.

Результат: data/index_results.csv  +  data/index_benchmark.pdf
"""

import csv
import statistics
import sys
from pathlib import Path

import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.ticker as ticker
import numpy as np

import psycopg2

# путь к проекту
sys.path.insert(0, str(Path(__file__).parent))
from seed_data import get_connection, setup_schema, seed_base_data, seed_orders, truncate_orders

# ---------------------------------------------------------------------------
DATA_DIR  = Path(__file__).parent.parent / 'data'
DATA_DIR.mkdir(exist_ok=True)

SIZES = [10_000, 25_000, 50_000, 100_000, 200_000, 300_000, 400_000, 500_000]
N_REPS  = 30
WARMUP  = 5

# Исследуемые запросы: имя, SQL, индекс, DDL для восстановления
QUERIES = [
    {
        'label': 'idx_orders_customer_status',
        'sql':   "SELECT order_id, total_amount FROM orders WHERE customer_id = %s AND status = 'delivered'",
        'index': 'idx_orders_customer_status',
        'ddl':   'CREATE INDEX idx_orders_customer_status ON orders(customer_id, status)',
    },
]
# ---------------------------------------------------------------------------


def explain_time(cur, sql: str, params: tuple) -> float:
    """Возвращает Execution Time (мс) из EXPLAIN ANALYZE."""
    cur.execute(f"EXPLAIN (ANALYZE, TIMING, FORMAT TEXT) {sql}", params)
    for row in cur.fetchall():
        line = row[0] if isinstance(row[0], str) else row[0]
        if 'Execution Time' in line:
            return float(line.strip().split(':')[1].strip().split(' ')[0])
    raise RuntimeError('Execution Time not found in EXPLAIN output')


def benchmark_query(conn, query: dict, customer_ids, restaurant_ids, n_reps: int, warmup: int) -> float:
    """Прогревает запрос и возвращает медиану времени выполнения (мс)."""
    import random

    def params():
        if query.get('no_param'):
            return ()
        if query.get('rest_param'):
            return (random.choice(restaurant_ids),)
        return (random.choice(customer_ids),)

    times = []
    with conn.cursor() as cur:
        # прогрев
        for _ in range(warmup):
            explain_time(cur, query['sql'], params())
        # измерение
        for _ in range(n_reps):
            times.append(explain_time(cur, query['sql'], params()))

    return statistics.median(times)


def run():
    conn = get_connection()
    conn.autocommit = False

    print('=== Настройка схемы ===')
    setup_schema(conn)

    print('\n=== Заполнение базовых данных ===')
    customer_ids, restaurant_ids, courier_ids, dish_ids = seed_base_data(conn)

    results = []  # [{query, rows, with_ms, without_ms}, ...]

    for size in SIZES:
        print(f'\n=== Объём таблицы: {size:,} строк ===')

        print('  Генерация заказов…')
        truncate_orders(conn)
        seed_orders(conn, size, customer_ids, restaurant_ids, courier_ids, dish_ids)

        # VACUUM ANALYZE для актуальной статистики планировщика
        conn.commit()
        conn.autocommit = True
        with conn.cursor() as cur:
            cur.execute('VACUUM ANALYZE orders')
        conn.autocommit = False

        for q in QUERIES:
            print(f'  Запрос: {q["label"].replace(chr(10), " ")}')

            with_ms = benchmark_query(conn, q, customer_ids, restaurant_ids, N_REPS, WARMUP)
            print(f'    С индексом:    {with_ms:.3f} мс')

            # удаляем индекс
            conn.commit()
            conn.autocommit = True
            with conn.cursor() as cur:
                cur.execute(f'DROP INDEX IF EXISTS {q["index"]}')
            conn.autocommit = False

            without_ms = benchmark_query(conn, q, customer_ids, restaurant_ids, N_REPS, WARMUP)
            print(f'    Без индекса:   {without_ms:.3f} мс')

            # восстанавливаем индекс
            conn.commit()
            conn.autocommit = True
            with conn.cursor() as cur:
                cur.execute(q['ddl'])
                cur.execute('VACUUM ANALYZE orders')
            conn.autocommit = False

            results.append({
                'query':      q['label'].replace('\n', ' '),
                'rows':       size,
                'with_ms':    round(with_ms, 3),
                'without_ms': round(without_ms, 3),
            })

    conn.close()

    # ------------------------------------------------------------------ CSV
    csv_path = DATA_DIR / 'index_results.csv'
    with open(csv_path, 'w', newline='', encoding='utf-8') as f:
        w = csv.DictWriter(f, fieldnames=['query', 'rows', 'with_ms', 'without_ms'])
        w.writeheader()
        w.writerows(results)
    print(f'\nCSV сохранён: {csv_path}')

    # ------------------------------------------------------------------ PDF
    _plot(results)


def _plot(results):
    size_labels = [f'{s // 1000}к' for s in SIZES]
    x = np.arange(len(SIZES))
    bar_w = 0.35

    q = QUERIES[0]
    rows_data = sorted(results, key=lambda r: r['rows'])
    with_vals    = [r['with_ms']    for r in rows_data]
    without_vals = [r['without_ms'] for r in rows_data]

    fig, ax = plt.subplots(figsize=(9, 5))
    ax.spines[['top', 'right']].set_visible(False)

    bars_with    = ax.bar(x - bar_w / 2, with_vals,    bar_w, label='С индексом',  color='#1976D2', edgecolor='black', linewidth=0.6, zorder=3)
    bars_without = ax.bar(x + bar_w / 2, without_vals, bar_w, label='Без индекса', color='#E53935', edgecolor='black', linewidth=0.6, hatch='///', zorder=3)

    ax.set_xticks(x)
    ax.set_xticklabels(size_labels)
    ax.set_xlabel('Количество строк в таблице orders')
    ax.set_ylabel('Время выполнения, мс')
    ax.legend(fontsize=10)
    ax.yaxis.grid(True, linestyle='--', alpha=0.6, zorder=0)
    ax.yaxis.set_major_formatter(matplotlib.ticker.FuncFormatter(lambda v, _: f'{v:g}'))
    ax.set_axisbelow(True)

    for bar in (*bars_with, *bars_without):
        h = bar.get_height()
        ax.text(bar.get_x() + bar.get_width() / 2, h * 1.08,
                f'{h:.2f}', ha='center', va='bottom', fontsize=8)

    fig.tight_layout(rect=[0, 0, 1, 0.92])

    pdf_path = DATA_DIR / 'index_benchmark.pdf'
    fig.savefig(pdf_path, format='pdf', bbox_inches='tight')
    print(f'PDF сохранён: {pdf_path}')
    plt.close(fig)


if __name__ == '__main__':
    run()
