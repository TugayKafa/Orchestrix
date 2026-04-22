# Priority-Based Task Scheduler API Plan

**Base URL:** /api
**Format:** application/json

---

## 1. Job Management
*Covers: Create Job, Get All Jobs, Get Job by ID, Cancel Job, Reschedule Job*

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| POST | /jobs | Създаване на нов job (с приоритет, scheduledAt, payload) |
| GET | /jobs | Списък с всички jobs (филтри: status, priority, q) |
| GET | /jobs/{id} | Детайли за конкретен job |
| PATCH | /jobs/{id}/cancel | Отказване/спиране на job |
| PATCH | /jobs/{id}/reschedule | Препланиране на job (ново scheduledAt) |
| DELETE | /jobs/{id} | Изтриване на job (само ако не е стартирал) |

## 2. Dependency Management
*Covers: Add Dependency, List Dependencies, Detect Circular Dependencies*

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| POST | /jobs/{id}/dependencies | Добавяне на зависимост (този job зависи от друг) |
| GET | /jobs/{id}/dependencies | Списък със зависимости на даден job |
| DELETE | /jobs/{id}/dependencies/{depId} | Премахване на зависимост |
| GET | /jobs/{id}/dependencies/graph | Граф на зависимостите (за Dependency View) |
| GET | /dependencies/circular-check | Проверка за циклични зависимости в системата |

## 3. Scheduler Execution
*Covers: Scheduler Execution, Retry Failed Jobs*

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| POST | /scheduler/run | Ръчно стартиране на scheduler-а (изпълнява готовите jobs) |
| POST | /scheduler/retry-failed | Retry на всички FAILED jobs (batch операция) |
| PATCH | /jobs/{id}/retry | Retry на конкретен FAILED job |
| GET | /scheduler/status | Текущо състояние на scheduler-а (running, last run) |

## 4. Execution History
*Covers: Execution History screen*

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| GET | /executions | Цялата история на изпълнения (филтри: jobId, status, from, to) |
| GET | /executions/{id} | Детайли за конкретно изпълнение (логове, грешка) |
| GET | /jobs/{id}/executions | История на изпълнения за даден job |

## 5. Dashboard
*Covers: Dashboard screen*

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| GET | /dashboard/stats | Обобщена статистика (брой jobs по статус, успеваемост) |
| GET | /dashboard/recent | Последно изпълнени jobs (за Dashboard widget) |
| GET | /dashboard/upcoming | Предстоящи scheduled jobs |

## 6. Auth
*Required for Login Page and personalized features*

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| POST | /auth/register | Регистрация на нов потребител |
| POST | /auth/login | Вход (връща JWT token) |
| POST | /auth/logout | Изход |
| GET | /auth/me | Данни за текущо логнатия потребител |
