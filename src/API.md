# Payment Processor API guide

All JSON endpoints return normal Spring HTTP status codes. Protected routes require `Authorization: Bearer <JWT>`. Any operation that changes a balance requires an `Idempotency-Key` header; reuse the same key only for an identical retry.

## `/api/auth`

### `POST /api/auth/customer/register`

Creates an unverified customer and sends an email verification code.

```json
{"firstName":"Ada","lastName":"Lovelace","email":"ada@example.com","phoneNumber":"+1 555 123 4567","password":"StrongPassword1!"}
```

### `POST /api/auth/verify-email`

Verifies a customer registration code.

```json
{"email":"ada@example.com","code":"123456"}
```

### `POST /api/auth/login`

Authenticates a verified customer and returns a JWT.

```json
{"email":"ada@example.com","password":"StrongPassword1!"}
```

### `POST /api/auth/business/register`

Requires authentication. Creates a business owned by the authenticated customer.

```json
{"name":"Analytical Engines","businessCategory":"SHIPPING"}
```

### `POST /api/auth/customer/delete`

Requires authentication. Sends a verification code before account deletion.

### `POST /api/auth/customer/delete/verify-email`

Requires authentication. Confirms deletion with the signed-in customer’s email and code.

```json
{"email":"ada@example.com","code":"123456"}
```

## `/api/customer`

All routes require authentication and act on the customer in the JWT.

### `GET /api/customer/`

Returns the authenticated customer profile.

### `GET /api/customer/wallet`

Returns the authenticated customer’s wallet.

### `PUT /api/customer/pay`

Creates a pending payment from the authenticated customer to a business. Include an idempotency key.

```json
{"businessId":"d0d161ec-74f8-47f1-9712-2a81b8ad53c0","amount":25.00}
```

The amount must be positive with no more than two decimal places. Customer and business wallets must use the same currency.

## `/api/wallet`

All routes require authentication.

### `GET /api/wallet`

Returns the authenticated customer’s wallet.

### `PATCH /api/wallet/currency`

Changes the authenticated customer wallet’s currency only when its balance is zero and it has no pending payments.

```json
{"currency":"USD"}
```

### `POST /api/wallet/transfer`

Transfers money from the authenticated customer to another customer. Include an idempotency key.

```json
{"recipientCustomerId":"d0d161ec-74f8-47f1-9712-2a81b8ad53c0","amount":25.00}
```

The service rejects self-transfers, disabled or missing wallets, insufficient funds, and currency mismatches.

### `PUT /api/wallet/deposit`

**Admin only.** Credits a customer wallet after a trusted payment-provider charge, bank settlement, or internal administrative action. It is not a self-service balance top-up. Include an idempotency key.

```json
{"recipientCustomerId":"d0d161ec-74f8-47f1-9712-2a81b8ad53c0","amount":50.00}
```

## `/api/business`

All routes require authentication and resolve the business through the authenticated customer.

### `GET /api/business/`

Returns the authenticated customer’s business profile.

### `GET /api/business/wallet`

Returns that business’s wallet.

### `POST /api/business/webhook`

Registers a replacement webhook destination for the business. The endpoint must be a publicly resolvable HTTPS URL; internal, local, and private-network addresses are rejected.

```json
{"url":"https://example.com/payment-webhooks"}
```

The response includes the webhook secret. Store it once and use it to verify `X-Payment-Signature` HMAC-SHA256 headers on future event deliveries.

### `POST /api/business/refund`

Refunds from the authenticated customer’s business wallet. Include an idempotency key and provide exactly one recipient type.

```json
{"recipientCustomerId":"d0d161ec-74f8-47f1-9712-2a81b8ad53c0","recipientBusinessId":null,"amount":25.00}
```

To refund a business, set `recipientCustomerId` to `null` and provide `recipientBusinessId`. Refunds reject self-refunds, invalid recipients, disabled or missing wallets, insufficient funds, and currency mismatches.
