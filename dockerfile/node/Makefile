# Makefile
CURRENT_USER := $(shell whoami)
CURRENT_UID := $(shell id -u)
CURRENT_GID := $(shell id -g)

.PHONY: build
build:
	docker compose build \
		--build-arg USERNAME=$(CURRENT_USER) \
		--build-arg USER_UID=$(CURRENT_UID) \
		--build-arg USER_GID=$(CURRENT_GID)

.PHONY: up
up:
	docker compose up

.PHONY: down
down:
	docker compose down