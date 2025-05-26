# Документация API системы управления банковскими картами

## Общая информация

Данный документ описывает REST API системы управления банковскими картами, разработанной на Spring Boot. Документация соответствует спецификации OpenAPI 3.0 и может быть использована для генерации клиентских SDK или тестирования API через Swagger UI.

## Базовый URL

```
http://localhost:8080/api/v1
```

## Аутентификация

API использует JWT-аутентификацию. Для доступа к защищенным endpoint'ам необходимо:

1. Выполнить POST-запрос на `/api/v1/token/create` с учетными данными 
```
{
"username" : "yuzhakov",
"password" : "Qwerty123"
}
```
2. Получить JWT токен из ответа
3. Добавлять токен в заголовок всех последующих запросов:
   ```
   Authorization: Bearer <ваш_токен>
   ```

## Модели данных

### Карта (BankCard)
```yaml
BankCardDTO:
  type: object
  properties:
    maskedCardNumber:
      type: string
      example: "**** **** **** 1234"
      description: Маскированный номер карты
    encryptedCardNumber:
      type: string
      description: Зашифрованный номер карты
    expirationDate:
      type: string
      format: date
      example: "2025-12-31"
    status:
      type: string
      enum: [ACTIVE, BLOCKED, EXPIRED]
    balance:
      type: number
      format: double
      example: 1500.50
    fullName:
      type: string
      example: "Иванов Иван Иванович"
```

### Пользователь (User)
```yaml
UserDTO:
  type: object
  properties:
    id:
      type: integer
    firstName:
      type: string
    lastName:
      type: string
    middleName:
      type: string
    username:
      type: string
    roleName:
      type: string
      enum: [USER, ADMIN]
```

### Транзакция (Transaction)
```yaml
TransactionDTO:
  type: object
  properties:
    sourceCardNumber:
      type: string
      description: Зашифрованный номер карты-отправителя
    targetCardNumber:
      type: string
      description: Зашифрованный номер карты-получателя
    amount:
      type: number
      minimum: 0.01
```

## Endpoints

### Карты (Cards)

#### Получить список карт
```
GET /cards
```

Параметры:
- `page` - номер страницы (по умолчанию 0)
- `size` - размер страницы (по умолчанию 10)
- `status` - фильтр по статусу (ACTIVE, BLOCKED, EXPIRED)
- `search` - поиск по владельцу (необязательно)

**Доступ:** ADMIN (все карты), USER (только свои карты)

#### Создать карту
```
POST /cards/create/{username}
```

**Доступ:** ADMIN

#### Удалить карту
```
POST /cards/delete
```

Тело запроса:
```json
{
  "encryptedCardNumber": "string"
}
```

**Доступ:** ADMIN

#### Блокировка/активация карты
```
POST /cards/block
POST /cards/activate
```

Тело запроса:
```json
{
  "encryptedCardNumber": "string"
}
```

**Доступ:** ADMIN (полный доступ), USER (только запрос на блокировку своих карт)

#### Перевод между картами
```
POST /cards/transaction
```

Тело запроса (TransactionDTO):
```json
{
  "sourceCardNumber": "string",
  "targetCardNumber": "string",
  "amount": 100.50
}
```

**Доступ:** USER (только между своими картами)

### Пользователи (Users)

#### Получить список пользователей
```
GET /users
```

**Доступ:** ADMIN

#### Получить пользователя по username
```
GET /users/{username}
```

**Доступ:** ADMIN

#### Создать пользователя
```
POST /users/create
```

Тело запроса (UserDTO без id):
```json
{
  "firstName": "string",
  "lastName": "string",
  "middleName": "string",
  "username": "string",
  "password": "string",
  "roleName": "USER"
}
```

**Доступ:** ADMIN

#### Обновить пользователя
```
POST /users/update
```

Тело запроса (UserDTO с id):
```json
{
  "id": 1,
  "firstName": "string",
  "lastName": "string",
  "middleName": "string",
  "username": "string"
}
```

**Доступ:** ADMIN

#### Удалить пользователя
```
GET /users/delete/{username}
```

**Доступ:** ADMIN

## Примеры запросов

### Получение списка карт (ADMIN)
```http
GET /api/v1/cards?page=0&size=5&status=ACTIVE
Authorization: Bearer <jwt_token>
```

### Перевод между картами (USER)
```http
POST /api/v1/cards/transaction
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "sourceCardNumber": "encrypted_card1",
  "targetCardNumber": "encrypted_card2",
  "amount": 500
}
```

## Ошибки

Стандартные HTTP статусы:
- 200 OK - успешный запрос
- 400 Bad Request - неверные параметры запроса
- 401 Unauthorized - требуется аутентификация
- 403 Forbidden - недостаточно прав
- 404 Not Found - ресурс не найден
- 500 Internal Server Error - серверная ошибка

## Запуск и тестирование

1. Собрать и запустить приложение:
```bash
docker-compose up -d
```

2. Документация будет доступна по адресу:
```
http://localhost:8080/swagger-ui.html
```

3. Для генерации openapi.yaml:
```bash
curl http://localhost:8080/v3/api-docs.yaml > docs/openapi.yaml
```
