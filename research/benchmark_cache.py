"""
Исследование влияния внешнего кэширования (Redis) на время выполнения запросов.

Методология:
  Для каждого объёма N (количество возвращаемых объектов):
    1. Заполняем таблицу ровно N записями.
    2. Измеряем время получения данных напрямую из PostgreSQL (N_REPS запросов, медиана).
    3. Сериализуем результат в JSON, сохраняем в Redis.
    4. Измеряем время получения из Redis + десериализация (N_REPS, медиана).

  Тестируются два типа запросов:
    • Список активных ресторанов  — SELECT * FROM restaurants WHERE is_active = true
    • Список блюд ресторана       — SELECT * FROM dishes WHERE restaurant_id = ? AND is_available = true

Результат: data/cache_results.csv  +  data/cache_benchmark.pdf
"""

import csv
import json
import statistics
import sys
import time
import uuid
from datetime import datetime, date, time as time_type
from decimal import Decimal
from pathlib import Path

import matplotlib
import matplotlib.ticker
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import numpy as np
import redis as redis_lib

sys.path.insert(0, str(Path(__file__).parent))
from seed_data import (get_connection, setup_schema, seed_base_data,
                       seed_restaurants_for_cache)

# ---------------------------------------------------------------------------
DATA_DIR = Path(__file__).parent.parent / 'data'
DATA_DIR.mkdir(exist_ok=True)

REDIS_CONFIG = dict(host='localhost', port=6380, db=0)

SIZES = [500, 1000, 1500, 2000, 2500, 3000, 3500, 4000, 4500, 5000]
N_REPS = 50
WARMUP = 5
# ---------------------------------------------------------------------------


class _Encoder(json.JSONEncoder):
    def default(self, obj):
        if isinstance(obj, (uuid.UUID,)):
            return str(obj)
        if isinstance(obj, Decimal):
            return float(obj)
        if isinstance(obj, (datetime, date, time_type)):
            return obj.isoformat()
        return super().default(obj)


def _serialize(rows) -> bytes:
    return json.dumps(rows, cls=_Encoder).encode()


def _deserialize(data: bytes):
    return json.loads(data)


def _pg_time(conn, sql: str, params: tuple) -> float:
    """Медианное время (мс) выполнения SQL + fetchall (без десериализации)."""
    times = []
    with conn.cursor() as cur:
        for _ in range(WARMUP):
            cur.execute(sql, params)
            cur.fetchall()
        for _ in range(N_REPS):
            t0 = time.perf_counter()
            cur.execute(sql, params)
            cur.fetchall()
            times.append((time.perf_counter() - t0) * 1000)
    return statistics.median(times)


def _redis_time(r: redis_lib.Redis, key: str, payload: bytes) -> float:
    """Медианное время (мс) чистого GET из Redis (без десериализации).

    Десериализация JSON в обоих путях является константными накладными расходами
    на стороне приложения и не характеризует скорость самого хранилища.
    """
    r.set(key, payload)
    times = []
    for _ in range(WARMUP):
        r.get(key)
    for _ in range(N_REPS):
        t0 = time.perf_counter()
        r.get(key)
        times.append((time.perf_counter() - t0) * 1000)
    return statistics.median(times)


def _fetch_rows(conn, sql: str, params: tuple) -> list:
    with conn.cursor() as cur:
        cur.execute(sql, params)
        cols = [d[0] for d in cur.description]
        return [dict(zip(cols, row)) for row in cur.fetchall()]


def run():
    conn = get_connection()
    r    = redis_lib.Redis(**REDIS_CONFIG)

    print('=== Настройка схемы ===')
    setup_schema(conn)

    # Нужны категории для seeder'а блюд
    seed_base_data(conn, n_customers=1, n_couriers=1, n_restaurants=0)

    results = []

    print('\n=== Тест: список активных ресторанов ===')
    SQL_REST = 'SELECT * FROM restaurants WHERE is_active = true'

    for n in SIZES:
        print(f'  N = {n} ресторанов…', end=' ', flush=True)
        seed_restaurants_for_cache(conn, n)

        pg_ms   = _pg_time(conn, SQL_REST, ())
        payload = _serialize(_fetch_rows(conn, SQL_REST, ()))
        rd_ms   = _redis_time(r, 'restaurants:active', payload)

        print(f'PG={pg_ms:.3f} мс  Redis={rd_ms:.3f} мс')
        results.append({'n_items': n,
                        'pg_ms': round(pg_ms, 3), 'redis_ms': round(rd_ms, 3)})

    conn.close()
    r.close()

    # ------------------------------------------------------------------ CSV
    csv_path = DATA_DIR / 'cache_results.csv'
    with open(csv_path, 'w', newline='', encoding='utf-8') as f:
        w = csv.DictWriter(f, fieldnames=['n_items', 'pg_ms', 'redis_ms'])
        w.writeheader()
        w.writerows(results)
    print(f'\nCSV сохранён: {csv_path}')

    # ------------------------------------------------------------------ PDF
    _plot(results)

    from scipy import stats

    x_vals = np.array([r['n_items'] for r in results], dtype=float)
    pg_vals = np.array([r['pg_ms'] for r in results])
    redis_vals = np.array([r['redis_ms'] for r in results])

    # Линейная регрессия
    slope_pg, intercept_pg, r_pg, _, _ = stats.linregress(x_vals, pg_vals)
    slope_rd, intercept_rd, r_rd, _, _ = stats.linregress(x_vals, redis_vals)

    print(f'\nРегрессионный анализ:')
    print(f'  PostgreSQL: y = {slope_pg:.6f}x + {intercept_pg:.3f},  R² = {r_pg**2:.4f}')
    print(f'  Redis:      y = {slope_rd:.6f}x + {intercept_rd:.3f},  R² = {r_rd**2:.4f}')

    # Степенная регрессия (y = a * x^b)
    log_x = np.log(x_vals)
    slope_pg_pow, intercept_pg_pow, r_pg_pow, _, _ = stats.linregress(log_x, np.log(pg_vals))
    slope_rd_pow, intercept_rd_pow, r_rd_pow, _, _ = stats.linregress(log_x, np.log(redis_vals))

    print(f'\n  PostgreSQL (степенная): y = {np.exp(intercept_pg_pow):.4f} * x^{slope_pg_pow:.3f},  R² = {r_pg_pow**2:.4f}')
    print(f'  Redis      (степенная): y = {np.exp(intercept_rd_pow):.4f} * x^{slope_rd_pow:.3f},  R² = {r_rd_pow**2:.4f}')


def _plot(results):
    results = sorted(results, key=lambda r: r['n_items'])
    x      = np.arange(len(SIZES))
    bar_w  = 0.35
    labels = [str(s) for s in SIZES]

    pg_vals    = [r['pg_ms']    for r in results]
    redis_vals = [r['redis_ms'] for r in results]

    fig, ax = plt.subplots(figsize=(9, 5))
    ax.spines[['top', 'right']].set_visible(False)

    b_pg = ax.bar(
        x - bar_w / 2, pg_vals, bar_w,
        label='PostgreSQL',
        color='#1565C0', edgecolor='black', linewidth=0.6,
        zorder=3,
    )
    b_redis = ax.bar(
        x + bar_w / 2, redis_vals, bar_w,
        label='Redis',
        color='#2E7D32', edgecolor='black', linewidth=0.6,
        hatch='///', zorder=3,
    )

    ax.set_xticks(x)
    ax.set_xticklabels(labels)
    ax.set_xlabel('Количество активных ресторанов')
    ax.set_ylabel('Время выполнения, мс')
    ax.legend(fontsize=10)
    ax.yaxis.grid(True, linestyle='--', alpha=0.5, zorder=0)
    ax.yaxis.set_major_formatter(matplotlib.ticker.FuncFormatter(
        lambda v, _: f'{v:g}'
    ))
    ax.set_axisbelow(True)

    for bar in (*b_pg, *b_redis):
        h = bar.get_height()
        ax.text(bar.get_x() + bar.get_width() / 2, h * 1.08,
                f'{h:.2f}', ha='center', va='bottom', fontsize=8)

    fig.tight_layout(rect=[0, 0, 1, 0.92])

    pdf_path = DATA_DIR / 'cache_benchmark.pdf'
    fig.savefig(pdf_path, format='pdf', bbox_inches='tight')
    print(f'PDF сохранён: {pdf_path}')
    plt.close(fig)

if __name__ == '__main__':
    run()
