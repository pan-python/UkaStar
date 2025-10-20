#!/usr/bin/env python3
import os
import sys
import mysql.connector

SCHEMA_PATH = os.path.join(os.path.dirname(__file__), '..', 'database', 'schema', '001_tables.sql')
SEED_PATH = os.path.join(os.path.dirname(__file__), '..', 'database', 'seed', '001_seed_data.sql')

def read_sql(path: str) -> str:
    with open(path, 'r', encoding='utf-8') as f:
        return f.read()

def split_sql_statements(sql: str):
    sql = sql.replace('\ufeff', '')
    statements = []
    buff = []
    for line in sql.splitlines():
        stripped = line.strip()
        if not stripped or stripped.startswith('--'):
            continue
        buff.append(line)
        if stripped.endswith(';'):
            stmt = '\n'.join(buff).strip()
            if stmt.endswith(';'):
                stmt = stmt[:-1]
            if stmt:
                statements.append(stmt)
            buff = []
    tail = '\n'.join(buff).strip()
    if tail:
        statements.append(tail)
    return statements

def main():
    host = os.getenv('MYSQL_HOST', 'localhost')
    port = int(os.getenv('MYSQL_PORT', '3306'))
    user = os.getenv('MYSQL_USER', 'root')
    password = os.getenv('MYSQL_PASSWORD', '')
    database = os.getenv('MYSQL_DATABASE', 'ukastar')

    print(f"Connecting to MySQL {host}:{port} as {user}, target DB={database} ...")
    # connect to server to ensure DB exists
    server_conn = mysql.connector.connect(host=host, port=port, user=user, password=password, database='mysql')
    server_conn.autocommit = True
    cur = server_conn.cursor()
    # Detect collation support
    cur.execute("SHOW COLLATION LIKE 'utf8mb4_0900_ai_ci'")
    collation_supported = cur.fetchone() is not None
    target_collation = 'utf8mb4_0900_ai_ci' if collation_supported else 'utf8mb4_unicode_ci'
    try:
        cur.execute(f"CREATE DATABASE IF NOT EXISTS `{database}` CHARACTER SET utf8mb4 COLLATE {target_collation}")
    finally:
        cur.close()
        server_conn.close()

    # connect to target DB
    conn = mysql.connector.connect(host=host, port=port, user=user, password=password, database=database)
    try:
        for label, path in [("schema", SCHEMA_PATH), ("seed", SEED_PATH)]:
            sql = read_sql(path)
            if not collation_supported:
                # Replace unsupported collation with a compatible one
                sql = sql.replace('utf8mb4_0900_ai_ci', 'utf8mb4_unicode_ci')
            print(f"Applying {label} SQL from {path} ...")
            cur = conn.cursor()
            for stmt in split_sql_statements(sql):
                try:
                    cur.execute(stmt)
                except mysql.connector.Error as e:
                    # 忽略重复主键/唯一键错误以便重复执行 seed
                    if e.errno == 1062:
                        print(f"Skip duplicate: {stmt[:80]}...")
                        continue
                    print(f"Failed statement: {stmt[:120]}... -> {e}")
                    raise
            conn.commit()
            cur.close()
            print(f"Applied {label} successfully.")
    finally:
        conn.close()
        print("Done.")

if __name__ == '__main__':
    try:
        main()
    except Exception as e:
        print(f"ERROR: {e}")
        sys.exit(1)
