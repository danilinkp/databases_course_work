DB_URL := postgres://root:postgres@localhost:5433/food_delivery?sslmode=disable

.PHONY: db-up db-down db-create migration-up migration_up-d1 migration-down seed

db-up:
	docker compose up -d

db-down:
	docker compose down

seed:
	python3 scripts/data_generator.py

migration-up:
	migrate -path migrations -database "$(DB_URL)" up

migration-up-1:
	migrate -path migrations -database "$(DB_URL)" up 1

migration-down:
	migrate -path migrations -database "$(DB_URL)" down

db-create:
	migrate create -ext sql -dir migrations -seq $(name)