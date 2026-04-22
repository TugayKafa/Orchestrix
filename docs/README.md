# Project Documentation

Документация за **Priority-Based Task Scheduler with Dependency Resolution**.

## Съдържание

| Файл | Описание |
| :--- | :--- |
| `RestApiDocumentation.md` | План за REST ресурсите — всички endpoint-и групирани по функционалност (Jobs, Dependencies, Scheduler, Executions, Dashboard, Auth) |
| `ERDiagram.md` | Структура на базата данни — Mermaid ER диаграма + описания на таблиците, enum стойности, връзки и препоръчани индекси |
| `ERDiagram.png` | Визуално изображение на ER диаграмата |

## Таблици в базата (7)

1. `users` — потребители
2. `refresh_tokens` — JWT refresh tokens (Login Page)
3. `jobs` — самите задачи
4. `job_dependencies` — зависимости между jobs (self-referencing many-to-many)
5. `scheduler_runs` — всяко стартиране на scheduler-а
6. `executions` — опити за изпълнение на jobs
7. `execution_logs` — детайлни логове за всяко изпълнение

## Функционалности на проекта

- create job, get all jobs, get job by id
- cancel job, reschedule job
- add dependency, list dependencies
- scheduler execution
- retry failed jobs
- execution history
- detect circular dependencies

## Екрани

1. Dashboard
2. Create Job
3. Job Details
4. Dependency View
5. Execution History
6. Failed Jobs (с retry бутон)
7. Circular Dependency alert
8. Login Page

## Технологичен стек

- **Backend:** Spring Boot 3.5, Java 21, Spring Data JPA, Spring Web
- **Database:** MySQL (конфигурирана в `pom.xml`)
- **Auth:** JWT с refresh tokens (таблица `refresh_tokens`)
